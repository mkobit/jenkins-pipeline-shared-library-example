package com.mkobit.libraryexample;

import com.mkobit.jenkins.pipelines.testing.LocalLibraryRetriever;
import hudson.model.Item;
import java.util.Collections;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Demonstrates using Jenkins integration tests with plain Java and JUnit 4.
 * Compiled from test/integration/java/ to show the plugin supports
 * Java-only consumers alongside Groovy-based tests.
 */
public class VarsExampleJavaTest {

    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Before
    public void configureGlobalLibraries() {
        rule.timeout = 30;
        GlobalLibraries.get().setLibraries(
                Collections.singletonList(LocalLibraryRetriever.implicitLibrary("testLibrary")));
    }

    @Test
    public void doStuffStepRunsSuccessfully() throws Exception {
        CpsFlowDefinition flow = new CpsFlowDefinition("doStuff()", true);
        WorkflowJob job = rule.createProject(WorkflowJob.class, "java-test");
        job.setDefinition(flow);

        WorkflowRun run = rule.buildAndAssertSuccess(job);
        rule.assertLogContains("hello stuff", run);
    }

    @Test
    public void jenkinsApiTypesAreAvailableOnClasspath() {
        // Verifies that Jenkins API JAR is on the compile and runtime classpath
        // for Java-only test consumers (no Groovy toolchain required).
        assert Item.class != null;
    }
}
