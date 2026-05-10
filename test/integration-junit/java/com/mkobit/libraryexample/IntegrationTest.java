package com.mkobit.libraryexample;

import hudson.model.BooleanParameterDefinition;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class IntegrationTest {

  @Test
  void doStuffStepLogsExpectedOutput(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "junit-doStuff");
    job.setDefinition(new CpsFlowDefinition("doStuff()", true));

    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("hello stuff", run);
  }

  @Test
  void evenOrOddStepIdentifiesOddBuildNumbers(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "junit-even-odd");
    job.setDefinition(new CpsFlowDefinition("evenOrOdd(1)", true));

    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("The build number is odd", run);
  }

  @Test
  void libraryResourceLoadsContent(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "junit-resource");
    job.setDefinition(new CpsFlowDefinition("loadGreeting()", true));

    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Hello from libraryResource!", run);
  }

  @Test
  void sayHelloToPrintsGreetingFromSrc(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "junit-say-hello");
    job.setDefinition(
        new CpsFlowDefinition(
            "import com.mkobit.libraryexample.ExampleSrc\n"
                + "final exampleSrc = new ExampleSrc(this)\n"
                + "exampleSrc.sayHelloTo('Bob')",
            true));

    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Hello there Bob", run);
  }

  @Test
  void nonCpsDoubleDoublesEachIntegerFromSrc(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "junit-non-cps");
    job.setDefinition(
        new CpsFlowDefinition(
            "import com.mkobit.libraryexample.ExampleSrc\n"
                + "final exampleSrc = new ExampleSrc(this)\n"
                + "echo 'Numbers: ' + exampleSrc.nonCpsDouble([1, 2])",
            true));

    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Numbers: [2, 4]", run);
  }

  @Test
  void lockStepFromPluginRunsSuccessfully(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "junit-lock-step");
    job.setDefinition(
        new CpsFlowDefinition(
            "lock('myLock') {\n" + "  echo 'Hello world during lock!'\n" + "}", true));

    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Hello world during lock!", run);
  }

  @Test
  void parameterizedProjectUsesDefaultAndOverriddenValues(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "junit-parameterized");
    var stringParam = new StringParameterDefinition("myString", "myDefault");
    var boolParam =
        new BooleanParameterDefinition("myBoolean", false, "boolean parameter description");
    var choiceParam =
        new ChoiceParameterDefinition(
            "myChoice", new String[] {"choice1", "choice2"}, "choice parameter description");
    job.addProperty(new ParametersDefinitionProperty(stringParam, boolParam, choiceParam));
    job.setDefinition(
        new CpsFlowDefinition(
            "echo 'String param: ' + params.myString\n"
                + "echo 'Boolean param: ' + params.myBoolean\n"
                + "echo 'Choice param: ' + params.myChoice",
            true));

    WorkflowRun defaults = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("String param: myDefault", defaults);
    rule.assertLogContains("Boolean param: false", defaults);
    rule.assertLogContains("Choice param: choice1", defaults);

    WorkflowRun withParams =
        rule.assertBuildStatusSuccess(
            job.scheduleBuild2(
                0,
                new ParametersAction(
                    stringParam.createValue("mySpecified"),
                    boolParam.createValue("true"),
                    choiceParam.createValue("choice2"))));
    rule.assertLogContains("String param: mySpecified", withParams);
    rule.assertLogContains("Boolean param: true", withParams);
    rule.assertLogContains("Choice param: choice2", withParams);
  }
}
