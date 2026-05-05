package com.mkobit.libraryexample

import groovy.transform.CompileDynamic
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

// sandbox=false is required when using Spock 2.x on Jenkins 2.479.x LTS.
// Spock brings groovy:3.x onto the runtime classpath; the Groovy sandbox transformer
// was compiled for Groovy 2.4 AST API and fails with a Groovy 3.x runtime context
// when sandbox=true. Using sandbox=false runs the CPS-transformed pipeline as trusted
// code, which is correct for shared library source owned by your organisation.
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
