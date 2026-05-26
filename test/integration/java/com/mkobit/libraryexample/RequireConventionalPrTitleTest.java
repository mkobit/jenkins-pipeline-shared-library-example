package com.mkobit.libraryexample;

import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class RequireConventionalPrTitleTest {

  @Test
  void isNoOpOnNonPrBuild(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "prTitleNonPr");
    job.setDefinition(new CpsFlowDefinition("requireConventionalPrTitle()", true));
    rule.buildAndAssertSuccess(job);
  }

  @Test
  void passesForValidConventionalTitle(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "prTitleValid");
    job.setDefinition(
        new CpsFlowDefinition(
            """
            env.CHANGE_ID = '42'
            env.CHANGE_TITLE = 'feat(auth): add OAuth login'
            requireConventionalPrTitle()
            """,
            true));
    rule.buildAndAssertSuccess(job);
  }

  @Test
  void failsBuildForNonConventionalTitle(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "prTitleInvalid");
    job.setDefinition(
        new CpsFlowDefinition(
            """
            env.CHANGE_ID = '99'
            env.CHANGE_TITLE = 'fix some stuff'
            requireConventionalPrTitle()
            """,
            true));
    WorkflowRun run = job.scheduleBuild2(0).get();
    rule.assertBuildStatus(Result.FAILURE, run);
    rule.assertLogContains("PR #99", run);
    rule.assertLogContains("fix some stuff", run);
  }
}
