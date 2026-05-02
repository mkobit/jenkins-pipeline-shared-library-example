package com.mkobit.libraryexample

import com.mkobit.jenkins.pipelines.testing.LocalLibraryRetriever
import io.kotest.core.spec.style.FunSpec
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jvnet.hudson.test.JenkinsRule

class VarsExampleKotestIntTest :
  FunSpec({
    lateinit var rule: JenkinsRule

    beforeEach {
      rule = KotestJenkinsRule()
      rule.timeout = 30
      rule.before()
      GlobalLibraries.get().libraries = listOf(LocalLibraryRetriever.implicitLibrary("testLibrary"))
    }

    afterEach { rule.after() }

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
