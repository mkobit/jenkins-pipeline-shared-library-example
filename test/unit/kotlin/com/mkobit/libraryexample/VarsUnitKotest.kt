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
  })
