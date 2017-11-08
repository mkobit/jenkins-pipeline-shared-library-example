package com.mkobit.libraryexample

import hudson.model.queue.QueueTaskFuture
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.job.WorkflowRun
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Specification

class LibraryResourceStepSpec extends Specification {
  @Rule
  public JenkinsRule rule = new JenkinsRule()

  void setup() {
    RuleBootstrapper.setup(rule)
  }

  def "say hello to name"() {
    given:
    final CpsFlowDefinition flow = new CpsFlowDefinition('''
final resource = libraryResource('com/mkobit/globallibraryresources/lorumipsum.txt')
echo "Resource Text: $resource"
    ''', true)
    final WorkflowJob workflowJob = rule.createProject(WorkflowJob, 'project')
    workflowJob.definition = flow

    when:
    final QueueTaskFuture<WorkflowRun> futureRun = workflowJob.scheduleBuild2(0)

    then:
    final WorkflowRun run = rule.assertBuildStatusSuccess(futureRun)
    rule.assertLogContains('Resource Text: Lorem ipsum dolor sit amet', run)
  }
}
