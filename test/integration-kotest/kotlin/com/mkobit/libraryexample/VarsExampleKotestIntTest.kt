package com.mkobit.libraryexample

import com.mkobit.jenkins.pipelines.testing.LocalLibraryRetriever
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.junit.runner.Description
import org.jvnet.hudson.test.JenkinsRule

// Kotest runs on JUnit Platform (JUnit 5). JenkinsRule.before() requires testDescription
// to be set (normally done via JUnit 4 @Rule wiring). Set it directly via reflection
// before each test, since there is no @Rule lifecycle in JUnit Platform contexts.
class VarsExampleKotestIntTest :
  FunSpec({
    lateinit var rule: JenkinsRule

    beforeEach {
      rule = JenkinsRule()
      val descField = JenkinsRule::class.java.getDeclaredField("testDescription")
      descField.isAccessible = true
      descField.set(
        rule,
        Description.createSuiteDescription(VarsExampleKotestIntTest::class.java),
      )
      rule.before()
      rule.timeout = 30
      GlobalLibraries.get().libraries =
        listOf(LocalLibraryRetriever.implicitLibrary("testLibrary"))
    }

    afterEach { rule.after() }

    test("doStuff step logs expected output") {
      rule.jenkins shouldNotBe null
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
