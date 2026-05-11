package com.mkobit.libraryexample;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class IntegrationTest {

  @Rule public JenkinsRule rule = new JenkinsRule();

  @Test
  public void doStuffStepLogsExpectedOutput() throws Exception {
    var job = rule.createProject(WorkflowJob.class, "doStuff");
    job.setDefinition(new CpsFlowDefinition("doStuff()", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("hello stuff", run);
  }

  @Test
  public void evenOrOddStepIdentifiesOddBuildNumbers() throws Exception {
    var job = rule.createProject(WorkflowJob.class, "evenOdd");
    job.setDefinition(new CpsFlowDefinition("evenOrOdd(1)", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("The build number is odd", run);
  }

  @Test
  public void libraryResourceLoadsContent() throws Exception {
    var job = rule.createProject(WorkflowJob.class, "resource");
    job.setDefinition(new CpsFlowDefinition("loadGreeting()", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Hello from libraryResource!", run);
  }

  @Test
  public void sayHelloToPrintsGreetingFromSrc() throws Exception {
    var job = rule.createProject(WorkflowJob.class, "sayHello");
    job.setDefinition(
        new CpsFlowDefinition(
            "import com.mkobit.libraryexample.ExampleSrc\n"
                + "new ExampleSrc(this).sayHelloTo('Alice')",
            true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Hello there Alice", run);
  }
}
