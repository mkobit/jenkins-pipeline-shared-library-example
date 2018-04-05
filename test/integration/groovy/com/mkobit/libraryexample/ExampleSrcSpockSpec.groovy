package com.mkobit.libraryexample

import hudson.model.BooleanParameterDefinition
import hudson.model.BooleanParameterValue
import hudson.model.ChoiceParameterDefinition
import hudson.model.ParametersAction
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import hudson.model.StringParameterValue
import hudson.model.queue.QueueTaskFuture
import jenkins.model.ParameterizedJobMixIn
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Specification

class ExampleSrcSpockSpec extends Specification {
  @Rule
  public JenkinsRule rule = new JenkinsRule()

  void setup() {
    RuleBootstrapper.setup(rule)
  }

  def "say hello to name"() {
    given:
    WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'project')
    workflowJob.definition = new CpsFlowDefinition('''
        import com.mkobit.libraryexample.ExampleSrc
        
        final exampleSrc = new ExampleSrc(this)
        exampleSrc.sayHelloTo('Bob')
      '''.stripIndent(), true)

    when:
    QueueTaskFuture<WorkflowRun> futureRun = workflowJob.scheduleBuild2(0)

    then:
    // JenkinsRule has different assertion capabilities
    WorkflowRun run = rule.assertBuildStatusSuccess(futureRun)
    rule.assertLogContains('Hello there Bob', run)
  }

  def "calling a @NonCPS method"() {
    given:
    WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'project')
    workflowJob.definition = new CpsFlowDefinition('''
      import com.mkobit.libraryexample.ExampleSrc
      
      final exampleSrc = new ExampleSrc(this)
      echo "Numbers: ${exampleSrc.nonCpsDouble([1, 2])}"
    '''.stripIndent(), true)

    when:
    QueueTaskFuture<WorkflowRun> futureRun = workflowJob.scheduleBuild2(0)

    then:
    // JenkinsRule has different assertion capabilities
    WorkflowRun run = rule.assertBuildStatusSuccess(futureRun)
    rule.assertLogContains('Numbers: [2, 4]', run)
  }

  def "using 'lock' step from plugin"() {
    given:
    WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'project')
    workflowJob.definition = new CpsFlowDefinition('''
        lock('myLock') {
          echo 'Hello world during lock!'
        }
    '''.stripIndent(), true)

    when:
    QueueTaskFuture<WorkflowRun> futureRun = workflowJob.scheduleBuild2(0)

    then:
    // JenkinsRule has different assertion capabilities
    WorkflowRun run = rule.assertBuildStatusSuccess(futureRun)
    rule.assertLogContains('''
        [Pipeline] lock
        Trying to acquire lock on [myLock]
        Resource [myLock] did not exist. Created.
        Lock acquired on [myLock]
    '''.stripIndent(), run)
  }

  def "create and run a parameterized project"() {
    given:
    WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'project')
    final StringParameterDefinition string = new StringParameterDefinition('myString', 'myDefault')
    final BooleanParameterDefinition bool = new BooleanParameterDefinition('myBoolean', false, 'boolean parameter description')
    final ChoiceParameterDefinition choice = new ChoiceParameterDefinition('myChoice', ['choice1', 'choice2'] as String[], 'choice parameter description')
    ParametersDefinitionProperty parameterizedProperty = new ParametersDefinitionProperty(string, bool, choice)
    workflowJob.addProperty(parameterizedProperty)
    workflowJob.definition = new CpsFlowDefinition('''
        echo "String param: ${params.myString}"
        echo "Boolean param: ${params.myBoolean}"
        echo "Choice param: ${params.myChoice}"
    '''.stripIndent(), true)

    when: 'no parameters specified'
    QueueTaskFuture<WorkflowRun> defaultsRun = workflowJob.scheduleBuild2(0)

    then: 'defaults are used'
    WorkflowRun first = rule.assertBuildStatusSuccess(defaultsRun)
    rule.assertLogContains('String param: myDefault', first)
    rule.assertLogContains('Boolean param: false', first)
    rule.assertLogContains('Choice param: choice1', first)

    when: 'parameters action specified'
    ParametersAction parametersAction = new ParametersAction(
        string.createValue('mySpecified'),
        bool.createValue(true.toString()),
        choice.createValue('choice2')
    )
    QueueTaskFuture<WorkflowRun> parametersRun = ParameterizedJobMixIn.scheduleBuild2(
        workflowJob,
        0,
        parametersAction
    ).future as QueueTaskFuture<WorkflowRun>

    then: 'parameters can be used'
    WorkflowRun second = rule.assertBuildStatusSuccess(parametersRun)
    rule.assertLogContains('String param: mySpecified', second)
    rule.assertLogContains('Boolean param: true', second)
    rule.assertLogContains('Choice param: choice2', second)
  }
}
