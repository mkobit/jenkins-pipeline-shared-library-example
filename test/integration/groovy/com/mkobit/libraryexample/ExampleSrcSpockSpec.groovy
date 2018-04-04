package com.mkobit.libraryexample

import hudson.model.queue.QueueTaskFuture
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
}
