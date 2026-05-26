package com.mkobit.libraryexample;

import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class RequireEnvTest {

  @Test
  void passesWhenAllVariablesAreSet(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "requireEnvPass");
    job.setDefinition(
        new CpsFlowDefinition(
            """
            env.DEPLOY_TARGET = 'staging'
            env.API_KEY = 'secret'
            requireEnv('DEPLOY_TARGET', 'API_KEY')
            """,
            true));
    rule.buildAndAssertSuccess(job);
  }

  @Test
  void failsListingAllMissingVariables(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "requireEnvFail");
    job.setDefinition(
        new CpsFlowDefinition(
            """
            env.DEPLOY_TARGET = 'staging'
            requireEnv('DEPLOY_TARGET', 'API_KEY', 'REGION')
            """,
            true));
    WorkflowRun run = job.scheduleBuild2(0).get();
    rule.assertBuildStatus(Result.FAILURE, run);
    rule.assertLogContains("API_KEY", run);
    rule.assertLogContains("REGION", run);
  }
}
