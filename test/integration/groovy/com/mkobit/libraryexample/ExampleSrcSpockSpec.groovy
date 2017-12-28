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
    CpsFlowDefinition flow = new CpsFlowDefinition(
        '''
          import com.mkobit.libraryexample.ExampleSrc
          
          final exampleSrc = new ExampleSrc(this)
          exampleSrc.sayHelloTo('Bob')
        '''.stripIndent(),
        true
    )
    WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'project')
    workflowJob.definition = flow

    when:
    QueueTaskFuture<WorkflowRun> futureRun = workflowJob.scheduleBuild2(0)

    then:
    // JenkinsRule has different assertion capabilities
    WorkflowRun run = rule.assertBuildStatusSuccess(futureRun)
    rule.assertLogContains('Hello there Bob', run)
  }
}
