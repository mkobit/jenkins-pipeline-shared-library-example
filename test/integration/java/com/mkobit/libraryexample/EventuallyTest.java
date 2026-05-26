package com.mkobit.libraryexample;

import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class EventuallyTest {

  @Test
  void succeedsWhenConditionIsMetImmediately(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "eventuallySuccess");
    job.setDefinition(
        new CpsFlowDefinition("eventually(maxAttempts: 3, initialWaitSeconds: 0) { true }", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Condition met on attempt 1", run);
  }

  @Test
  void failsWhenConditionNeverMet(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "eventuallyFails");
    job.setDefinition(
        new CpsFlowDefinition("eventually(maxAttempts: 2, initialWaitSeconds: 0) { false }", true));
    WorkflowRun run = job.scheduleBuild2(0).get();
    rule.assertBuildStatus(Result.FAILURE, run);
    rule.assertLogContains("Condition not met after 2 attempts", run);
  }
}
