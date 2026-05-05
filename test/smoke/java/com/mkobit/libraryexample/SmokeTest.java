package com.mkobit.libraryexample;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class SmokeTest {

  @Test
  void sharedLibraryLoadsAndPipelineExecutes(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "smoke-test");
    job.setDefinition(new CpsFlowDefinition("doStuff()", true));
    rule.buildAndAssertSuccess(job);
  }
}
