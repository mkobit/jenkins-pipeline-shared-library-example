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
class PipelineMechanicsTest {

  @Test
  void parameterizedProjectUsesDefaultAndOverriddenValues(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "parameterized");
    var stringParam = new StringParameterDefinition("myString", "myDefault");
    var boolParam = new BooleanParameterDefinition("myBoolean", false, "boolean parameter");
    var choiceParam =
        new ChoiceParameterDefinition(
            "myChoice", new String[] {"choice1", "choice2"}, "choice parameter");
    job.addProperty(new ParametersDefinitionProperty(stringParam, boolParam, choiceParam));
    job.setDefinition(
        new CpsFlowDefinition(
            """
            echo 'String param: ' + params.myString
            echo 'Boolean param: ' + params.myBoolean
            echo 'Choice param: ' + params.myChoice
            """,
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
  void milestonesPassSuccessfully(JenkinsRule rule) throws Exception {
    var job = rule.createProject(WorkflowJob.class, "milestones");
    job.setDefinition(
        new CpsFlowDefinition(
            """
            milestone(1)
            echo 'passed milestone 1'
            milestone(2)
            echo 'passed milestone 2'
            """,
            true));
    WorkflowRun run = rule.buildAndAssertSuccess(job);
    rule.assertLogContains("passed milestone 1", run);
    rule.assertLogContains("passed milestone 2", run);
  }
}
