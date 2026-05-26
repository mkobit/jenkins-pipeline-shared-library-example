package com.mkobit.libraryexample;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class BranchPolicyTest {

  @Test
  void environmentReflectsBranchName(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "branchPolicy");
    job.setDefinition(
        new CpsFlowDefinition(
            """
            import com.mkobit.libraryexample.BranchPolicy
            def main = new BranchPolicy('main')
            def release = new BranchPolicy('release/2.0')
            def pr = new BranchPolicy('PR-99')
            echo main.environment
            echo release.environment
            echo pr.environment
            """,
            true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("production", run);
    rule.assertLogContains("staging", run);
    rule.assertLogContains("development", run);
  }
}
