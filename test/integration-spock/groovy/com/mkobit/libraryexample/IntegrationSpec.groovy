package com.mkobit.libraryexample

import hudson.model.BooleanParameterDefinition
import hudson.model.ChoiceParameterDefinition
import hudson.model.ParametersAction
import hudson.model.ParametersDefinitionProperty
import hudson.model.StringParameterDefinition
import jenkins.model.ParameterizedJobMixIn
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import testsupport.spock.JenkinsSpec

class IntegrationSpec extends JenkinsSpec {

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

    def 'library resource loads content'() {
        when:
        jenkins { j ->
            def job = j.createProject(WorkflowJob, 'spock-resource')
            job.definition = new CpsFlowDefinition('loadGreeting()', false)
            j.assertLogContains('Hello from libraryResource!', j.buildAndAssertSuccess(job))
        }

        then:
        noExceptionThrown()
    }

    def 'sayHelloTo prints greeting from src/'() {
        when:
        jenkins { j ->
            def job = j.createProject(WorkflowJob, 'spock-say-hello')
            job.definition = new CpsFlowDefinition("""
                import com.mkobit.libraryexample.ExampleSrc
                final exampleSrc = new ExampleSrc(this)
                exampleSrc.sayHelloTo('Bob')
            """.stripIndent(), false)
            j.assertLogContains('Hello there Bob', j.buildAndAssertSuccess(job))
        }

        then:
        noExceptionThrown()
    }

    def 'nonCpsDouble doubles each integer from src/'() {
        when:
        jenkins { j ->
            def job = j.createProject(WorkflowJob, 'spock-non-cps')
            job.definition = new CpsFlowDefinition("""
                import com.mkobit.libraryexample.ExampleSrc
                final exampleSrc = new ExampleSrc(this)
                echo 'Numbers: ' + exampleSrc.nonCpsDouble([1, 2])
            """.stripIndent(), false)
            j.assertLogContains('Numbers: [2, 4]', j.buildAndAssertSuccess(job))
        }

        then:
        noExceptionThrown()
    }

    def 'lock step from plugin runs successfully'() {
        when:
        jenkins { j ->
            def job = j.createProject(WorkflowJob, 'spock-lock-step')
            job.definition = new CpsFlowDefinition("""
                lock('myLock') {
                    echo 'Hello world during lock!'
                }
            """.stripIndent(), false)
            j.assertLogContains('Hello world during lock!', j.buildAndAssertSuccess(job))
        }

        then:
        noExceptionThrown()
    }

    def 'parameterized project uses default and overridden values'() {
        when:
        jenkins { j ->
            def job = j.createProject(WorkflowJob, 'spock-parameterized')
            def stringParam = new StringParameterDefinition('myString', 'myDefault')
            def boolParam = new BooleanParameterDefinition('myBoolean', false, 'boolean parameter description')
            def choiceParam = new ChoiceParameterDefinition('myChoice', ['choice1', 'choice2'] as String[], 'choice parameter description')
            job.addProperty(new ParametersDefinitionProperty(stringParam, boolParam, choiceParam))
            job.definition = new CpsFlowDefinition("""
                echo 'String param: ' + params.myString
                echo 'Boolean param: ' + params.myBoolean
                echo 'Choice param: ' + params.myChoice
            """.stripIndent(), false)

            def defaults = j.buildAndAssertSuccess(job)
            j.assertLogContains('String param: myDefault', defaults)
            j.assertLogContains('Boolean param: false', defaults)
            j.assertLogContains('Choice param: choice1', defaults)

            def withParams = j.assertBuildStatusSuccess(
                    ParameterizedJobMixIn.scheduleBuild2(
                    job,
                    0,
                    new ParametersAction(
                    stringParam.createValue('mySpecified'),
                    boolParam.createValue('true'),
                    choiceParam.createValue('choice2')
                    )
                    ).future.get()
                    )
            j.assertLogContains('String param: mySpecified', withParams)
            j.assertLogContains('Boolean param: true', withParams)
            j.assertLogContains('Choice param: choice2', withParams)
        }

        then:
        noExceptionThrown()
    }
}
