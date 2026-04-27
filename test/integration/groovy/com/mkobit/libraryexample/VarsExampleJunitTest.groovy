package com.mkobit.libraryexample

import groovy.transform.CompileDynamic
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

@CompileDynamic
class VarsExampleJunitTest {

    @Rule
    public JenkinsRule rule = new JenkinsRule()

    @Before
    void configureGlobalGitLibraries() {
        RuleBootstrapper.setup(rule)
    }

    @Test
    void "testing library that uses declarative pipeline libraries"() {
        final CpsFlowDefinition flow = new CpsFlowDefinition('''
        evenOrOdd(env.BUILD_NUMBER as int)
    '''.stripIndent(), false)
        final WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'project')
        workflowJob.definition = flow

        final WorkflowRun firstResult = rule.buildAndAssertSuccess(workflowJob)
        rule.assertLogContains('The build number is odd', firstResult)

        final WorkflowRun secondResult = rule.buildAndAssertSuccess(workflowJob)
        rule.assertLogContains('The build number is even', secondResult)
    }

    @Test
    void "testing library function"() {
        final CpsFlowDefinition flow = new CpsFlowDefinition('''
        doStuff()
    '''.stripIndent(), false)
        final WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'project')
        workflowJob.definition = flow

        rule.assertLogContains('hello stuff', rule.buildAndAssertSuccess(workflowJob))
    }
}
