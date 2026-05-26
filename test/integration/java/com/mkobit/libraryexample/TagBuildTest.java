package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class TagBuildTest {

  @Test
  void setsDisplayNameAndDescription(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "tagBuild");
    job.setDefinition(new CpsFlowDefinition("tagBuild('v1.2.3')", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    assertEquals("#1 v1.2.3", run.getDisplayName());
    assertEquals("v1.2.3", run.getDescription());
  }
}
