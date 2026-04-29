package com.mkobit.libraryexample

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

class VarsExampleKotlinTest {

    @Rule @JvmField val rule = JenkinsRule()

    @Test
    fun jenkinsRuleStartsSuccessfully() {
        assert(rule.jenkins != null)
    }

    @Test
    fun canCreateAndRunPipelineJob() {
        val flow = CpsFlowDefinition("echo 'hello from Kotlin test'", false)
        val job = rule.createProject(WorkflowJob::class.java, "kotlin-test")
        job.definition = flow
        val run = rule.buildAndAssertSuccess(job)
        rule.assertLogContains("hello from Kotlin test", run)
    }
}
