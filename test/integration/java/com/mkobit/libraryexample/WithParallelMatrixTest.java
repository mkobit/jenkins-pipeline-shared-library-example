package com.mkobit.libraryexample;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class WithParallelMatrixTest {

  @Test
  void runsAllCombinations(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "parallelMatrix");
    job.setDefinition(
        new CpsFlowDefinition(
            """
            withParallelMatrix([os: ['linux', 'windows'], jdk: ['11', '21']]) { axes ->
              echo "combo: ${axes.os}-${axes.jdk}"
            }
            """,
            true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("combo: linux-11", run);
    rule.assertLogContains("combo: linux-21", run);
    rule.assertLogContains("combo: windows-11", run);
    rule.assertLogContains("combo: windows-21", run);
  }
}
