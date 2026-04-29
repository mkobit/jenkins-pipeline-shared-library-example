package com.mkobit.libraryexample

import com.mkobit.jenkins.pipelines.testing.LocalLibraryRetriever
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

class VarsExampleKotlinTest {

    @Rule @JvmField val rule = JenkinsRule()

    @Before
    fun configureGlobalLibraries() {
        rule.timeout = 30
        GlobalLibraries.get().libraries = listOf(LocalLibraryRetriever.implicitLibrary("testLibrary"))
    }

    @Test
    fun doStuffStepRunsSuccessfully() {
        val flow = CpsFlowDefinition("doStuff()", true)
        val job = rule.createProject(WorkflowJob::class.java, "kotlin-test")
        job.definition = flow

        val run = rule.buildAndAssertSuccess(job)
        rule.assertLogContains("hello stuff", run)
    }

    @Test
    fun jenkinsApiTypesAreAvailableOnClasspath() {
        assert(WorkflowJob::class.java != null)
    }
}
