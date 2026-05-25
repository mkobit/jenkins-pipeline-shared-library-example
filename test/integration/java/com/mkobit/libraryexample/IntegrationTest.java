package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import hudson.model.BooleanParameterDefinition;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.model.StringParameterDefinition;
import hudson.util.Secret;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
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
    var job = rule.createProject(WorkflowJob.class, "doStuff");
    job.setDefinition(new CpsFlowDefinition("doStuff()", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("hello stuff", run);
  }

  @Test
  void evenOrOddStepIdentifiesOddBuildNumbers(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "evenOdd");
    job.setDefinition(new CpsFlowDefinition("evenOrOdd(1)", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("The build number is odd", run);
  }

  @Test
  void libraryResourceLoadsContent(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "resource");
    job.setDefinition(new CpsFlowDefinition("loadGreeting()", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Hello from libraryResource!", run);
  }

  @Test
  void sayHelloToPrintsGreetingFromSrc(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "sayHello");
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
    var job = rule.createProject(WorkflowJob.class, "nonCpsDouble");
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
    var job = rule.createProject(WorkflowJob.class, "lockStep");
    job.setDefinition(
        new CpsFlowDefinition(
            "lock('myLock') {\n" + "  echo 'Hello world during lock!'\n" + "}", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Hello world during lock!", run);
  }

  @Test
  void parameterizedProjectUsesDefaultAndOverriddenValues(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "parameterized");
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

  @Test
  void captureBuildInfoLogsBuildContext(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "captureBuildInfo");
    job.setDefinition(new CpsFlowDefinition("captureBuildInfo()", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("BuildContext(", run);
    rule.assertLogContains("captureBuildInfo", run);
  }

  @Test
  void captureBuildInfoInFolderIncludesFolderPath(JenkinsRule rule) throws Exception {
    var folder = rule.createProject(Folder.class, "my-folder");
    var job = folder.createProject(WorkflowJob.class, "my-job");
    job.setDefinition(new CpsFlowDefinition("captureBuildInfo()", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("my-folder/my-job", run);
  }

  @Test
  void tagBuildSetsDisplayNameAndDescription(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "tagBuild");
    job.setDefinition(new CpsFlowDefinition("tagBuild('v1.2.3')", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    assertEquals("#1 v1.2.3", run.getDisplayName());
    assertEquals("v1.2.3", run.getDescription());
  }

  @Test
  void notifyBuildEchosRenderedPayloadWithStatus(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "my-service");
    job.setDefinition(new CpsFlowDefinition("notifyBuild('FAILURE')", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Build FAILURE", run);
    rule.assertLogContains("my-service", run);
  }

  @Test
  void eventuallySucceedsWhenConditionIsMetImmediately(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "eventuallySuccess");
    job.setDefinition(
        new CpsFlowDefinition("eventually(maxAttempts: 3, initialWaitSeconds: 0) { true }", true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("Condition met on attempt 1", run);
  }

  @Test
  void eventuallyFailsWhenConditionNeverMet(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "eventuallyFails");
    job.setDefinition(
        new CpsFlowDefinition("eventually(maxAttempts: 2, initialWaitSeconds: 0) { false }", true));
    WorkflowRun run = job.scheduleBuild2(0).get();
    rule.assertBuildStatus(Result.FAILURE, run);
    rule.assertLogContains("Condition not met after 2 attempts", run);
  }

  @Test
  void withSecretTextInjectsCredentialIntoBody(JenkinsRule rule) throws Exception {
    SystemCredentialsProvider.getInstance()
        .getCredentials()
        .add(
            new StringCredentialsImpl(
                CredentialsScope.GLOBAL,
                "my-api-key",
                "API key",
                Secret.fromString("supersecret")));
    SystemCredentialsProvider.getInstance().save();

    var job = rule.createProject(WorkflowJob.class, "withSecretText");
    job.setDefinition(
        new CpsFlowDefinition(
            "withSecretText('my-api-key', 'MY_SECRET') {\n"
                + "  echo 'credential retrieved successfully'\n"
                + "}",
            true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("credential retrieved successfully", run);
  }

  @Test
  void withParallelMatrixRunsAllCombinations(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "parallelMatrix");
    job.setDefinition(
        new CpsFlowDefinition(
            "withParallelMatrix([os: ['linux', 'windows'], jdk: ['11', '21']]) { axes ->\n"
                + "  echo \"combo: ${axes.os}-${axes.jdk}\"\n"
                + "}",
            true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("combo: linux-11", run);
    rule.assertLogContains("combo: linux-21", run);
    rule.assertLogContains("combo: windows-11", run);
    rule.assertLogContains("combo: windows-21", run);
  }

  @Test
  void branchPolicyEnvironmentReflectsBranchName(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "branchPolicy");
    job.setDefinition(
        new CpsFlowDefinition(
            "import com.mkobit.libraryexample.BranchPolicy\n"
                + "def main = new BranchPolicy('main')\n"
                + "def release = new BranchPolicy('release/2.0')\n"
                + "def pr = new BranchPolicy('PR-99')\n"
                + "echo main.environment\n"
                + "echo release.environment\n"
                + "echo pr.environment",
            true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("production", run);
    rule.assertLogContains("staging", run);
    rule.assertLogContains("development", run);
  }

  @Test
  void milestonesPassSuccessfully(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "milestones");
    job.setDefinition(
        new CpsFlowDefinition(
            "milestone(1)\n"
                + "echo 'passed milestone 1'\n"
                + "milestone(2)\n"
                + "echo 'passed milestone 2'",
            true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("passed milestone 1", run);
    rule.assertLogContains("passed milestone 2", run);
  }
}
