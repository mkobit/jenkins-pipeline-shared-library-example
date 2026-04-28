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
class LibraryResourceStepGroovyTest {

    @Rule
    public JenkinsRule rule = new JenkinsRule()

    @Before
    void setUp() {
        RuleBootstrapper.setup(rule)
    }

    @Test
    void libraryResourceCanLoadResources() {
        WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'library-resource')
        workflowJob.definition = new CpsFlowDefinition('''
        final resource = libraryResource('com/mkobit/globallibraryresources/lorumipsum.txt')
        echo "Resource Text: $resource"
        '''.stripIndent(), true)

        WorkflowRun run = rule.assertBuildStatusSuccess(workflowJob.scheduleBuild2(0))
        rule.assertLogContains('Resource Text: Lorem ipsum dolor sit amet', run)
    }
}
