package com.mkobit.libraryexample

import com.mkobit.jenkins.pipelines.testing.LocalLibraryRetriever
import groovy.transform.CompileDynamic
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.junit.runner.Description
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Shared
import spock.lang.Specification

// sandbox=false is required when using Spock 2.x on Jenkins 2.479.x LTS.
// Spock brings groovy:3.x onto the runtime classpath; the Groovy sandbox transformer
// was compiled for Groovy 2.4 AST API and fails with a Groovy 3.x runtime context
// when sandbox=true. Using sandbox=false runs the CPS-transformed pipeline as trusted
// code, which is correct for shared library source owned by your organisation.
//
// Spock 2.x runs on JUnit Platform which does not honour JUnit 4 @Rule / @ClassRule
// lifecycle wiring — JenkinsRule.before() is never called by the rule mechanism.
// We manage the Jenkins lifecycle explicitly in setupSpec/cleanupSpec: create the
// Description JenkinsRule expects (via reflection) and call before()/after() directly.
@CompileDynamic
class VarsExampleSpockTest extends Specification {

    @Shared
    JenkinsRule rule

    def setupSpec() {
        rule = new JenkinsRule()
        // JenkinsRule.before() calls recipe() which reads testDescription for annotations.
        // Without @Rule wiring, testDescription is null; set it directly.
        def descField = JenkinsRule.getDeclaredField('testDescription')
        descField.accessible = true
        descField.set(rule, Description.createSuiteDescription(VarsExampleSpockTest))
        rule.before()
        rule.timeout = 30
        GlobalLibraries.get().libraries = [
            LocalLibraryRetriever.implicitLibrary('testLibrary')
        ]
    }

    def cleanupSpec() {
        rule?.after()
    }

    def 'doStuff step logs expected output'() {
        given:
        def job = rule.createProject(WorkflowJob, 'spock-doStuff')
        job.definition = new CpsFlowDefinition('doStuff()', false)

        when:
        def run = rule.buildAndAssertSuccess(job)

        then:
        rule.assertLogContains('hello stuff', run)
    }

    def 'evenOrOdd step identifies odd build numbers'() {
        given:
        def job = rule.createProject(WorkflowJob, 'spock-evenOdd')
        job.definition = new CpsFlowDefinition('evenOrOdd(1)', false)

        when:
        def run = rule.buildAndAssertSuccess(job)

        then:
        rule.assertLogContains('The build number is odd', run)
    }
}
