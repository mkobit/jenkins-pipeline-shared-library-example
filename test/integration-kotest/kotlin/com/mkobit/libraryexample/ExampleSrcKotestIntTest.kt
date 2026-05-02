package com.mkobit.libraryexample

import com.mkobit.jenkins.pipelines.testing.LocalLibraryRetriever
import hudson.model.BooleanParameterDefinition
import hudson.model.ChoiceParameterDefinition
import hudson.model.ParametersAction
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import io.kotest.core.spec.style.FunSpec
import jenkins.model.ParameterizedJobMixIn
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jvnet.hudson.test.JenkinsRule

class ExampleSrcKotestIntTest :
  FunSpec({
    lateinit var rule: JenkinsRule

    beforeEach {
      rule = KotestJenkinsRule()
      rule.timeout = 30
      rule.before()
      GlobalLibraries.get().libraries = listOf(LocalLibraryRetriever.implicitLibrary("testLibrary"))
    }

    afterEach { rule.after() }

    test("sayHelloTo prints greeting") {
      val job = rule.createProject(WorkflowJob::class.java, "say-hello")
      job.definition =
        CpsFlowDefinition(
          """
          import com.mkobit.libraryexample.ExampleSrc
          final exampleSrc = new ExampleSrc(this)
          exampleSrc.sayHelloTo('Bob')
          """.trimIndent(),
          true,
        )

      rule.assertLogContains("Hello there Bob", rule.buildAndAssertSuccess(job))
    }

    test("nonCpsDouble doubles each integer") {
      val job = rule.createProject(WorkflowJob::class.java, "non-cps")
      job.definition =
        CpsFlowDefinition(
          """
          import com.mkobit.libraryexample.ExampleSrc
          final exampleSrc = new ExampleSrc(this)
          echo 'Numbers: ' + exampleSrc.nonCpsDouble([1, 2])
          """.trimIndent(),
          true,
        )

      rule.assertLogContains("Numbers: [2, 4]", rule.buildAndAssertSuccess(job))
    }

    test("lock step from plugin runs successfully") {
      val job = rule.createProject(WorkflowJob::class.java, "lock-step")
      job.definition =
        CpsFlowDefinition(
          """
          lock('myLock') {
            echo 'Hello world during lock!'
          }
          """.trimIndent(),
          true,
        )

      rule.assertLogContains("Hello world during lock!", rule.buildAndAssertSuccess(job))
    }

    test("parameterized project uses default and overridden values") {
      val job = rule.createProject(WorkflowJob::class.java, "parameterized")
      val string = StringParameterDefinition("myString", "myDefault")
      val bool = BooleanParameterDefinition("myBoolean", false, "boolean parameter description")
      val choice =
        ChoiceParameterDefinition(
          "myChoice",
          arrayOf("choice1", "choice2"),
          "choice parameter description",
        )
      job.addProperty(ParametersDefinitionProperty(string, bool, choice))
      job.definition =
        CpsFlowDefinition(
          """
          echo 'String param: ' + params.myString
          echo 'Boolean param: ' + params.myBoolean
          echo 'Choice param: ' + params.myChoice
          """.trimIndent(),
          true,
        )

      val defaults = rule.buildAndAssertSuccess(job)
      rule.assertLogContains("String param: myDefault", defaults)
      rule.assertLogContains("Boolean param: false", defaults)
      rule.assertLogContains("Choice param: choice1", defaults)

      @Suppress("UNCHECKED_CAST")
      val withParams =
        rule.assertBuildStatusSuccess(
          checkNotNull(
            ParameterizedJobMixIn.scheduleBuild2(
              job,
              0,
              ParametersAction(
                string.createValue("mySpecified"),
                bool.createValue("true"),
                choice.createValue("choice2"),
              ),
            ),
          ).future as java.util.concurrent.Future<WorkflowRun>,
        )
      rule.assertLogContains("String param: mySpecified", withParams)
      rule.assertLogContains("Boolean param: true", withParams)
      rule.assertLogContains("Choice param: choice2", withParams)
    }
  })
