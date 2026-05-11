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
        jenkins {
            def job = createProject(WorkflowJob, 'spock-doStuff')
            job.definition = new CpsFlowDefinition('doStuff()', false)
            assertLogContains('hello stuff', buildAndAssertSuccess(job))
        }

        then:
        noExceptionThrown()
    }

    def 'evenOrOdd step identifies odd build numbers'() {
        when:
        jenkins {
            def job = createProject(WorkflowJob, 'spock-evenOdd')
            job.definition = new CpsFlowDefinition('evenOrOdd(1)', false)
            assertLogContains('The build number is odd', buildAndAssertSuccess(job))
        }

        then:
        noExceptionThrown()
    }

    def 'library resource loads content'() {
        when:
        jenkins {
            def job = createProject(WorkflowJob, 'spock-resource')
            job.definition = new CpsFlowDefinition('loadGreeting()', false)
            assertLogContains('Hello from libraryResource!', buildAndAssertSuccess(job))
        }

        then:
        noExceptionThrown()
    }

    def 'sayHelloTo prints greeting from src/'() {
        when:
        jenkins {
            def job = createProject(WorkflowJob, 'spock-say-hello')
            job.definition = new CpsFlowDefinition("""
                import com.mkobit.libraryexample.ExampleSrc
                final exampleSrc = new ExampleSrc(this)
                exampleSrc.sayHelloTo('Bob')
            """.stripIndent(), false)
            assertLogContains('Hello there Bob', buildAndAssertSuccess(job))
        }

        then:
        noExceptionThrown()
    }

    def 'nonCpsDouble doubles each integer from src/'() {
        when:
        jenkins {
            def job = createProject(WorkflowJob, 'spock-non-cps')
            job.definition = new CpsFlowDefinition("""
                import com.mkobit.libraryexample.ExampleSrc
                final exampleSrc = new ExampleSrc(this)
                echo 'Numbers: ' + exampleSrc.nonCpsDouble([1, 2])
            """.stripIndent(), false)
            assertLogContains('Numbers: [2, 4]', buildAndAssertSuccess(job))
        }

        then:
        noExceptionThrown()
    }

    def 'lock step from plugin runs successfully'() {
        when:
        jenkins {
            def job = createProject(WorkflowJob, 'spock-lock-step')
            job.definition = new CpsFlowDefinition("""
                lock('myLock') {
                    echo 'Hello world during lock!'
                }
            """.stripIndent(), false)
            assertLogContains('Hello world during lock!', buildAndAssertSuccess(job))
        }

        then:
        noExceptionThrown()
    }

    def 'parameterized project uses default and overridden values'() {
        when:
        jenkins {
            def job = createProject(WorkflowJob, 'spock-parameterized')
            def stringParam = new StringParameterDefinition('myString', 'myDefault')
            def boolParam = new BooleanParameterDefinition('myBoolean', false, 'boolean parameter description')
            def choiceParam = new ChoiceParameterDefinition('myChoice', ['choice1', 'choice2'] as String[], 'choice parameter description')
            job.addProperty(new ParametersDefinitionProperty(stringParam, boolParam, choiceParam))
            job.definition = new CpsFlowDefinition("""
                echo 'String param: ' + params.myString
                echo 'Boolean param: ' + params.myBoolean
                echo 'Choice param: ' + params.myChoice
            """.stripIndent(), false)

            def defaults = buildAndAssertSuccess(job)
            assertLogContains('String param: myDefault', defaults)
            assertLogContains('Boolean param: false', defaults)
            assertLogContains('Choice param: choice1', defaults)

            def withParams = assertBuildStatusSuccess(
                    ParameterizedJobMixIn.scheduleBuild2(
                    job,
                    0,
                    new ParametersAction(
                    stringParam.createValue('mySpecified'),
                    boolParam.createValue('true'),
                    choiceParam.createValue('choice2')
                    )
                    ).future
                    )
            assertLogContains('String param: mySpecified', withParams)
            assertLogContains('Boolean param: true', withParams)
            assertLogContains('Choice param: choice2', withParams)
        }

        then:
        noExceptionThrown()
    }
}
