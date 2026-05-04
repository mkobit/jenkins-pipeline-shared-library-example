package com.mkobit.libraryexample

import groovy.transform.CompileDynamic
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
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
// lifecycle wiring. SpockJenkinsRule overrides recipe() to skip the JUnit 4 Description
// lookup, making before()/after() safe to call directly from setupSpec/cleanupSpec.
// Migrate to RealJenkinsFixture (jenkins-test-harness fixtures package) once the harness
// version bundled in the BOM includes it.
//
// @CompileDynamic is intentional: Spock 2.x AST transformations run on Groovy 3 AST
// and can fail under @CompileStatic on a Specification subclass.
@CompileDynamic
class VarsExampleSpockTest extends Specification {

    // JenkinsRule.recipe() reads testDescription (a JUnit 4 concept) to wire test
    // annotations like @LocalData. Skipping it makes JenkinsRule framework-agnostic.
    private static class SpockJenkinsRule extends JenkinsRule {
        @Override
        void recipe() throws Exception {
            recipeLoadCurrentPlugin()
        }
    }

    @Shared
    JenkinsRule rule

    def setupSpec() {
        rule = new SpockJenkinsRule()
        rule.before()
        rule.timeout = 30
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
