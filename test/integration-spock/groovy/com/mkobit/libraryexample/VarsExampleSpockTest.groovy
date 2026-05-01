package com.mkobit.libraryexample

import com.mkobit.jenkins.pipelines.testing.LocalLibraryRetriever
import groovy.transform.CompileDynamic
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Specification

// sandbox=false is required when using Spock 2.x on Jenkins 2.479.x LTS.
// Spock brings groovy:3.x onto the runtime classpath; the Groovy sandbox transformer
// was compiled for Groovy 2.4 AST API and fails with a Groovy 3.x runtime context
// when sandbox=true. Using sandbox=false runs the CPS-transformed pipeline as trusted
// code, which is correct for shared library source owned by your organisation.
@CompileDynamic
class VarsExampleSpockTest extends Specification {

    @Rule JenkinsRule rule = new JenkinsRule()

    def setup() {
        rule.timeout = 30
        GlobalLibraries.get().libraries = [LocalLibraryRetriever.implicitLibrary('testLibrary')]
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
