package com.mkobit.libraryexample;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class NotifyBuildTest {

  @Test
  void echosRenderedMessageWithStatus(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "my-service");
    job.setDefinition(new CpsFlowDefinition("notifyBuild('FAILURE')", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Build FAILURE", run);
    rule.assertLogContains("my-service", run);
  }
}
