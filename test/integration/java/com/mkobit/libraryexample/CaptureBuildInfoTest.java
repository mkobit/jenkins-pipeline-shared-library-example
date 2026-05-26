package com.mkobit.libraryexample;

import com.cloudbees.hudson.plugins.folder.Folder;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class CaptureBuildInfoTest {

  @Test
  void logsBuildContext(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "captureBuildInfo");
    job.setDefinition(new CpsFlowDefinition("captureBuildInfo()", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("BuildContext(", run);
    rule.assertLogContains("captureBuildInfo", run);
  }

  @Test
  void inFolderIncludesFolderPath(JenkinsRule rule) throws Exception {
    var folder = rule.createProject(Folder.class, "my-folder");
    var job = folder.createProject(WorkflowJob.class, "my-job");
    job.setDefinition(new CpsFlowDefinition("captureBuildInfo()", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("my-folder/my-job", run);
  }
}
