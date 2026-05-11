package com.mkobit.libraryexample

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import groovy.transform.CompileDynamic
import spock.lang.Specification

@CompileDynamic
class VarsUnitSpec extends Specification {

    // DeclarativePipelineTest registers the `pipeline {}` keyword and all declarative
    // step mocks; composition avoids JUnit 4 lifecycle annotations incompatible with Spock 2.
    DeclarativePipelineTest base = new DeclarativePipelineTest() {}

    def setup() {
        base.scriptRoots += 'vars'
        base.setUp()
        // JPU 1.29 defaults error(String) to null (no-op). Override so it actually fails the build.
        base.helper.registerAllowedMethod('error', [String]) { String s -> throw new IllegalStateException(s) }
    }

    def "doStuff runs successfully"() {
        when:
        base.loadScript('vars/doStuff.groovy').call()

        then:
        noExceptionThrown()
        base.helper.callStack.findAll { call -> call.methodName == 'error' }.empty
    }

    def "evenOrOdd executes even pipeline for even build number"() {
        when:
        base.loadScript('vars/evenOrOdd.groovy').call(2)

        then:
        noExceptionThrown()
        base.helper.callStack.findAll { call -> call.methodName == 'error' }.empty
    }

    def "evenOrOdd executes odd pipeline for odd build number"() {
        when:
        base.loadScript('vars/evenOrOdd.groovy').call(1)

        then:
        noExceptionThrown()
        base.helper.callStack.findAll { call -> call.methodName == 'error' }.empty
    }

    def "requireEnv passes when all named variables are set"() {
        given:
        base.binding.setProperty('env', [DEPLOY_TARGET: 'staging', API_KEY: 'secret'])

        when:
        base.loadScript('vars/requireEnv.groovy').call('DEPLOY_TARGET', 'API_KEY')

        then:
        noExceptionThrown()
        base.helper.callStack.findAll { call -> call.methodName == 'error' }.empty
    }

    def "requireEnv fails build listing all missing variables"() {
        given:
        base.binding.setProperty('env', [DEPLOY_TARGET: 'staging'])
        def script = base.loadScript('vars/requireEnv.groovy')

        when:
        script.call('DEPLOY_TARGET', 'API_KEY', 'REGION')

        then:
        thrown(Exception)
        base.helper.callStack.findAll { call -> call.methodName == 'error' }.size() == 1
    }
}
