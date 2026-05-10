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
import testsupport.kotest.JenkinsFunSpec

class IntegrationKotest :
  JenkinsFunSpec({
    test("doStuff step logs expected output") {
      jenkins { r ->
        val job = r.createProject(WorkflowJob::class.java, "kotest-doStuff")
        job.definition = CpsFlowDefinition("doStuff()", true)
        r.assertLogContains("hello stuff", r.buildAndAssertSuccess(job))
      }
    }

    test("evenOrOdd step identifies odd build numbers") {
      jenkins { r ->
        val job = r.createProject(WorkflowJob::class.java, "kotest-evenOdd")
        job.definition = CpsFlowDefinition("evenOrOdd(1)", true)
        r.assertLogContains("The build number is odd", r.buildAndAssertSuccess(job))
      }
    }

    test("library resource loads content") {
      jenkins { r ->
        val job = r.createProject(WorkflowJob::class.java, "kotest-resource")
        job.definition = CpsFlowDefinition("loadGreeting()", true)
        r.assertLogContains("Hello from libraryResource!", r.buildAndAssertSuccess(job))
      }
    }

    test("sayHelloTo prints greeting from src/") {
      jenkins { j ->
        val job = j.createProject(WorkflowJob::class.java, "say-hello")
        job.definition =
          CpsFlowDefinition(
            """
            import com.mkobit.libraryexample.ExampleSrc
            final exampleSrc = new ExampleSrc(this)
            exampleSrc.sayHelloTo('Bob')
            """.trimIndent(),
            true,
          )
        j.assertLogContains("Hello there Bob", j.buildAndAssertSuccess(job))
      }
    }

    test("nonCpsDouble doubles each integer from src/") {
      jenkins { j ->
        val job = j.createProject(WorkflowJob::class.java, "non-cps")
        job.definition =
          CpsFlowDefinition(
            """
            import com.mkobit.libraryexample.ExampleSrc
            final exampleSrc = new ExampleSrc(this)
            echo 'Numbers: ' + exampleSrc.nonCpsDouble([1, 2])
            """.trimIndent(),
            true,
          )
        j.assertLogContains("Numbers: [2, 4]", j.buildAndAssertSuccess(job))
      }
    }

    test("lock step from plugin runs successfully") {
      jenkins { j ->
        val job = j.createProject(WorkflowJob::class.java, "lock-step")
        job.definition =
          CpsFlowDefinition(
            """
            lock('myLock') {
              echo 'Hello world during lock!'
            }
            """.trimIndent(),
            true,
          )
        j.assertLogContains("Hello world during lock!", j.buildAndAssertSuccess(job))
      }
    }

    test("parameterized project uses default and overridden values") {
      jenkins { j ->
        val job = j.createProject(WorkflowJob::class.java, "parameterized")
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

        val defaults = j.buildAndAssertSuccess(job)
        j.assertLogContains("String param: myDefault", defaults)
        j.assertLogContains("Boolean param: false", defaults)
        j.assertLogContains("Choice param: choice1", defaults)

        @Suppress("UNCHECKED_CAST")
        val withParams =
          j.assertBuildStatusSuccess(
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
        j.assertLogContains("String param: mySpecified", withParams)
        j.assertLogContains("Boolean param: true", withParams)
        j.assertLogContains("Choice param: choice2", withParams)
      }
    }
  })
