package com.mkobit.libraryexample;

import com.mkobit.jenkins.pipelines.testing.LocalLibraryRetriever;
import java.util.List;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class VarsExampleJUnit6Test {

  private static void configureLibrary(JenkinsRule rule) {
    rule.timeout = 30;
    GlobalLibraries.get().setLibraries(List.of(LocalLibraryRetriever.implicitLibrary()));
  }

  @Test
  void doStuffStepRunsSuccessfully(JenkinsRule rule) throws Exception {
    configureLibrary(rule);
    var job = rule.createProject(WorkflowJob.class, "junit6-test");
    job.setDefinition(new CpsFlowDefinition("doStuff()", true));

    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("hello stuff", run);
  }

  @Test
  void evenOrOddStepRunsSuccessfully(JenkinsRule rule) throws Exception {
    configureLibrary(rule);
    var job = rule.createProject(WorkflowJob.class, "junit6-even-odd");
    job.setDefinition(new CpsFlowDefinition("evenOrOdd(1)", true));

    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("The build number is odd", run);
  }
}
