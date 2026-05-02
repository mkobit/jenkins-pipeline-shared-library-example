package com.mkobit.libraryexample

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

class VarsExampleKotlinTest {
  @Rule @JvmField
  val rule = JenkinsRule()

  @Test
  fun jenkinsRuleStartsSuccessfully() {
    Assert.assertNotNull(rule.jenkins)
  }

  @Test
  fun canCreateAndRunPipelineJob() {
    val job = rule.createProject(WorkflowJob::class.java, "kotlin-test")
    job.definition = CpsFlowDefinition("echo 'hello from Kotlin test'", false)
    val run = rule.buildAndAssertSuccess(job)
    rule.assertLogContains("hello from Kotlin test", run)
  }
}
