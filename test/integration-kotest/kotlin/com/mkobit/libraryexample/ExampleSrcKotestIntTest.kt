package com.mkobit.libraryexample

import hudson.model.BooleanParameterDefinition
import hudson.model.ChoiceParameterDefinition
import hudson.model.ParametersAction
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import jenkins.model.ParameterizedJobMixIn
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun

class ExampleSrcKotestIntTest :
  JenkinsFunSpec({
    test("sayHelloTo prints greeting") {
      jenkins { r ->
        val job = r.createProject(WorkflowJob::class.java, "say-hello")
        job.definition =
          CpsFlowDefinition(
            """
            import com.mkobit.libraryexample.ExampleSrc
            final exampleSrc = new ExampleSrc(this)
            exampleSrc.sayHelloTo('Bob')
            """.trimIndent(),
            true,
          )
        r.assertLogContains("Hello there Bob", r.buildAndAssertSuccess(job))
      }
    }

    test("nonCpsDouble doubles each integer") {
      jenkins { r ->
        val job = r.createProject(WorkflowJob::class.java, "non-cps")
        job.definition =
          CpsFlowDefinition(
            """
            import com.mkobit.libraryexample.ExampleSrc
            final exampleSrc = new ExampleSrc(this)
            echo 'Numbers: ' + exampleSrc.nonCpsDouble([1, 2])
            """.trimIndent(),
            true,
          )
        r.assertLogContains("Numbers: [2, 4]", r.buildAndAssertSuccess(job))
      }
    }

    test("lock step from plugin runs successfully") {
      jenkins { r ->
        val job = r.createProject(WorkflowJob::class.java, "lock-step")
        job.definition =
          CpsFlowDefinition(
            """
            lock('myLock') {
              echo 'Hello world during lock!'
            }
            """.trimIndent(),
            true,
          )
        r.assertLogContains("Hello world during lock!", r.buildAndAssertSuccess(job))
      }
    }

    test("parameterized project uses default and overridden values") {
      jenkins { r ->
        val job = r.createProject(WorkflowJob::class.java, "parameterized")
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

        val defaults = r.buildAndAssertSuccess(job)
        r.assertLogContains("String param: myDefault", defaults)
        r.assertLogContains("Boolean param: false", defaults)
        r.assertLogContains("Choice param: choice1", defaults)

        @Suppress("UNCHECKED_CAST")
        val withParams =
          r.assertBuildStatusSuccess(
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
        r.assertLogContains("String param: mySpecified", withParams)
        r.assertLogContains("Boolean param: true", withParams)
        r.assertLogContains("Choice param: choice2", withParams)
      }
    }
  })
