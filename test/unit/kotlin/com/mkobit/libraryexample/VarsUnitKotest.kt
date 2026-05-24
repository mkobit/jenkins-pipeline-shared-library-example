package com.mkobit.libraryexample

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import groovy.lang.Closure
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forNone
import io.kotest.inspectors.forOne
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.codehaus.groovy.runtime.InvokerHelper

// JPU's registerAllowedMethod expects a groovy.lang.Closure; adapt a Kotlin lambda.
@Suppress("UNCHECKED_CAST")
private fun <T> closure(fn: (Array<out Any?>) -> T): Closure<T> =
  object : Closure<T>(null) {
    override fun call(vararg args: Any?): T = fn(args)
  }

class VarsUnitKotest :
  FunSpec({
    lateinit var base: DeclarativePipelineTest

    beforeEach {
      base =
        object : DeclarativePipelineTest() {}.also { t ->
          t.setScriptRoots(*t.scriptRoots, "vars")
          t.setUp()
          t.helper.registerAllowedMethod(
            "error",
            listOf(String::class.java),
            closure { args -> throw IllegalStateException(args[0] as String) },
          )
          t.helper.registerAllowedMethod(
            "libraryResource",
            listOf(String::class.java),
            closure { _ -> """{"text": "Build {{status}}: {{job}} #{{build}} ({{branch}})"}""" },
          )
          // Stub withRetry to execute the body — used when deployTo delegates to it.
          t.helper.registerAllowedMethod(
            "withRetry",
            listOf(Integer::class.java, Closure::class.java),
            closure { args -> (args[1] as groovy.lang.Closure<*>).call() },
          )
          t.helper.registerAllowedMethod(
            "requireEnv",
            listOf(String::class.java),
            closure { _ -> },
          )
        }
    }

    test("doStuff runs successfully") {
      InvokerHelper.invokeMethod(base.loadScript("vars/doStuff.groovy"), "call", null)

      base.helper.callStack.forNone { it.methodName shouldBe "error" }
    }

    test("evenOrOdd executes even pipeline for even build number") {
      InvokerHelper.invokeMethod(base.loadScript("vars/evenOrOdd.groovy"), "call", 2)

      base.helper.callStack.forNone { it.methodName shouldBe "error" }
    }

    test("evenOrOdd executes odd pipeline for odd build number") {
      InvokerHelper.invokeMethod(base.loadScript("vars/evenOrOdd.groovy"), "call", 1)

      base.helper.callStack.forNone { it.methodName shouldBe "error" }
    }

    test("requireEnv passes when all named variables are set") {
      base.binding.setProperty("env", mapOf("DEPLOY_TARGET" to "staging", "API_KEY" to "secret"))
      InvokerHelper.invokeMethod(
        base.loadScript("vars/requireEnv.groovy"),
        "call",
        arrayOf("DEPLOY_TARGET", "API_KEY"),
      )

      base.helper.callStack.forNone { it.methodName shouldBe "error" }
    }

    test("requireEnv fails build listing all missing variables") {
      base.binding.setProperty("env", mapOf("DEPLOY_TARGET" to "staging"))
      val script = base.loadScript("vars/requireEnv.groovy")

      val thrown =
        runCatching {
          InvokerHelper.invokeMethod(script, "call", arrayOf("DEPLOY_TARGET", "API_KEY", "REGION"))
        }
      thrown.exceptionOrNull() shouldNotBe null
      base.helper.callStack.forOne { it.methodName shouldBe "error" }
    }

    test("captureBuildInfo logs metadata without error") {
      base.binding.setProperty(
        "env",
        mapOf("JOB_NAME" to "my-job", "BUILD_NUMBER" to "5", "BRANCH_NAME" to "main", "GIT_COMMIT" to "abc1234"),
      )
      InvokerHelper.invokeMethod(base.loadScript("vars/captureBuildInfo.groovy"), "call", null)

      base.helper.callStack.forNone { it.methodName shouldBe "error" }
      // call.toString() uses JPU's Groovy formatter and includes the echo argument content.
      base.helper.callStack.forOne { call ->
        (call.methodName == "echo" && call.toString().contains("BuildContext(")) shouldBe true
      }
    }

    test("withRetry succeeds on first attempt") {
      val noOp = closure<Unit> { }
      InvokerHelper.invokeMethod(
        base.loadScript("vars/withRetry.groovy"),
        "call",
        arrayOf(3, noOp),
      )

      base.helper.callStack.forNone { it.methodName shouldBe "error" }
      base.helper.callStack.forOne { call ->
        (call.methodName == "echo" && call.toString().contains("Attempt 1")) shouldBe true
      }
    }

    test("withRetry rethrows after all attempts exhausted") {
      val alwaysThrows = closure<Unit> { throw RuntimeException("boom") }
      val thrown =
        runCatching {
          InvokerHelper.invokeMethod(
            base.loadScript("vars/withRetry.groovy"),
            "call",
            arrayOf(2, alwaysThrows),
          )
        }

      thrown.exceptionOrNull() shouldNotBe null
      base.helper.callStack.forOne { call ->
        (call.methodName == "echo" && call.toString().contains("exhausted")) shouldBe true
      }
    }

    test("deployTo calls body and logs environment") {
      base.binding.setProperty(
        "env",
        mapOf("DEPLOY_ENV" to "staging", "JOB_NAME" to "deploy-job", "BUILD_NUMBER" to "3"),
      )
      val noOp = closure<Unit> { }
      InvokerHelper.invokeMethod(
        base.loadScript("vars/deployTo.groovy"),
        "call",
        arrayOf("staging", 1, noOp),
      )

      base.helper.callStack.forNone { it.methodName shouldBe "error" }
      base.helper.callStack.forOne { call ->
        (call.methodName == "echo" && call.toString().contains("Deploying deploy-job")) shouldBe true
      }
    }

    test("notifyBuild echoes rendered payload with status") {
      base.binding.setProperty(
        "env",
        mapOf("JOB_NAME" to "my-service", "BUILD_NUMBER" to "42", "BRANCH_NAME" to "main"),
      )
      InvokerHelper.invokeMethod(base.loadScript("vars/notifyBuild.groovy"), "call", "SUCCESS")

      base.helper.callStack.any { call ->
        call.methodName == "echo" &&
          call.toString().contains("SUCCESS") &&
          call.toString().contains("my-service") &&
          call.toString().contains("42")
      } shouldBe true
    }
  })
