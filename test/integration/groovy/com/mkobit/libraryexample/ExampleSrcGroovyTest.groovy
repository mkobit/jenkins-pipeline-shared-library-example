package com.mkobit.libraryexample

import groovy.transform.CompileDynamic
import hudson.model.BooleanParameterDefinition
import hudson.model.ChoiceParameterDefinition
import hudson.model.ParametersAction
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import hudson.model.queue.QueueTaskFuture
import jenkins.model.ParameterizedJobMixIn
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.junit.Rule
import org.junit.Test
import org.jvnet.hudson.test.JenkinsRule

@CompileDynamic
class ExampleSrcGroovyTest {

    @Rule
    public JenkinsRule rule = new JenkinsRule()

    @Test
    void sayHelloToName() {
        WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'say-hello')
        workflowJob.definition = new CpsFlowDefinition('''
        import com.mkobit.libraryexample.ExampleSrc
        final exampleSrc = new ExampleSrc(this)
        exampleSrc.sayHelloTo('Bob')
        '''.stripIndent(), true)

        WorkflowRun run = rule.assertBuildStatusSuccess(workflowJob.scheduleBuild2(0))
        rule.assertLogContains('Hello there Bob', run)
    }

    @Test
    void callingNonCpsMethod() {
        WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'non-cps')
        workflowJob.definition = new CpsFlowDefinition('''
        import com.mkobit.libraryexample.ExampleSrc
        final exampleSrc = new ExampleSrc(this)
        echo 'Numbers: ' + exampleSrc.nonCpsDouble([1, 2])
        '''.stripIndent(), true)

        WorkflowRun run = rule.assertBuildStatusSuccess(workflowJob.scheduleBuild2(0))
        rule.assertLogContains('Numbers: [2, 4]', run)
    }

    @Test
    void lockStepFromPlugin() {
        WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'lock-step')
        workflowJob.definition = new CpsFlowDefinition('''
        lock('myLock') {
          echo 'Hello world during lock!'
        }
        '''.stripIndent(), true)

        WorkflowRun run = rule.assertBuildStatusSuccess(workflowJob.scheduleBuild2(0))
        rule.assertLogContains('Hello world during lock!', run)
    }

    @Test
    void parameterizedProject() {
        WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'parameterized')
        StringParameterDefinition string = new StringParameterDefinition('myString', 'myDefault')
        BooleanParameterDefinition bool = new BooleanParameterDefinition('myBoolean', false, 'boolean parameter description')
        ChoiceParameterDefinition choice = new ChoiceParameterDefinition('myChoice', ['choice1', 'choice2'] as String[], 'choice parameter description')
        workflowJob.addProperty(new ParametersDefinitionProperty(string, bool, choice))
        workflowJob.definition = new CpsFlowDefinition('''
        echo 'String param: ' + params.myString
        echo 'Boolean param: ' + params.myBoolean
        echo 'Choice param: ' + params.myChoice
        '''.stripIndent(), true)

        WorkflowRun defaults = rule.assertBuildStatusSuccess(workflowJob.scheduleBuild2(0))
        rule.assertLogContains('String param: myDefault', defaults)
        rule.assertLogContains('Boolean param: false', defaults)
        rule.assertLogContains('Choice param: choice1', defaults)

        QueueTaskFuture<WorkflowRun> second = ParameterizedJobMixIn.scheduleBuild2(
                workflowJob, 0,
                new ParametersAction(
                string.createValue('mySpecified'),
                bool.createValue(true.toString()),
                choice.createValue('choice2')
                )
                ).future as QueueTaskFuture<WorkflowRun>
        WorkflowRun withParams = rule.assertBuildStatusSuccess(second)
        rule.assertLogContains('String param: mySpecified', withParams)
        rule.assertLogContains('Boolean param: true', withParams)
        rule.assertLogContains('Choice param: choice2', withParams)
    }
}
