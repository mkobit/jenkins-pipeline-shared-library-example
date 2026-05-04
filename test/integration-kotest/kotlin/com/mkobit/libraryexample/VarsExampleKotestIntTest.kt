package com.mkobit.libraryexample

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

class VarsExampleKotestIntTest :
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
  })
