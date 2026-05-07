package com.mkobit.libraryexample

import groovy.transform.CompileDynamic
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

// sandbox=false is required when using Spock 2.x: Spock brings groovy:3.x onto the
// runtime classpath while groovyAllRuntime injects groovy-all:2.4.21. With both Groovy
// versions present, the embedded Jenkins CPS compiler sees inconsistent class versions and
// fails with "Cannot reference 'toArray' before supertype constructor" during sandbox
// transformation. sandbox=true works correctly in the JUnit 5 and Kotest suites, which
// do not introduce a second Groovy runtime. See docs/07-research.md for the full analysis.
@CompileDynamic
class VarsExampleSpockTest extends JenkinsSpec {

    def 'doStuff step logs expected output'() {
        when:
        jenkins { j ->
            def job = j.createProject(WorkflowJob, 'spock-doStuff')
            job.definition = new CpsFlowDefinition('doStuff()', false)
            j.assertLogContains('hello stuff', j.buildAndAssertSuccess(job))
        }

        then:
        noExceptionThrown()
    }

    def 'evenOrOdd step identifies odd build numbers'() {
        when:
        jenkins { j ->
            def job = j.createProject(WorkflowJob, 'spock-evenOdd')
            job.definition = new CpsFlowDefinition('evenOrOdd(1)', false)
            j.assertLogContains('The build number is odd', j.buildAndAssertSuccess(job))
        }

        then:
        noExceptionThrown()
    }
}
