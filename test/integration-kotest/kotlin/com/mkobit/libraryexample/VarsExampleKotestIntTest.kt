package com.mkobit.libraryexample

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

class VarsExampleKotestIntTest :
  JenkinsFunSpec({
    test("doStuff step logs expected output") {
      val job = rule.createProject(WorkflowJob::class.java, "kotest-doStuff")
      job.definition = CpsFlowDefinition("doStuff()", true)

      val run = rule.buildAndAssertSuccess(job)
      rule.assertLogContains("hello stuff", run)
    }

    test("evenOrOdd step identifies odd build numbers") {
      val job = rule.createProject(WorkflowJob::class.java, "kotest-evenOdd")
      job.definition = CpsFlowDefinition("evenOrOdd(1)", true)

      val run = rule.buildAndAssertSuccess(job)
      rule.assertLogContains("The build number is odd", run)
    }
  })
