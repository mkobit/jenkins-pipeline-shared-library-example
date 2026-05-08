package com.mkobit.libraryexample;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class VarsExampleJUnit6Test {

  @Test
  void doStuffStepRunsSuccessfully(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "junit6-test");
    job.setDefinition(new CpsFlowDefinition("doStuff()", true));

    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("hello stuff", run);
  }

  @Test
  void evenOrOddStepRunsSuccessfully(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "junit6-even-odd");
    job.setDefinition(new CpsFlowDefinition("evenOrOdd(1)", true));

    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("The build number is odd", run);
  }

  @Test
  void libraryResourceLoadsContent(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "junit6-library-resource");
    job.setDefinition(new CpsFlowDefinition("loadGreeting()", true));

    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Hello from libraryResource!", run);
  }
}
