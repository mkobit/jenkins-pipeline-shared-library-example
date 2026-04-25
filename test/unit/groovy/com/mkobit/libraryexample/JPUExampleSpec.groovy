package com.mkobit.libraryexample

import com.lesfurets.jenkins.unit.BasePipelineTest
import spock.lang.Specification

class JPUExampleSpec extends Specification {

    // BasePipelineTest registers pipeline step mocks (echo, sh, node, stage, etc.);
    // using composition and calling setUp() manually avoids the JUnit 4 annotations
    // that exist on the subclass while still getting the full initialization.
    BasePipelineTest base = new BasePipelineTest() {}

    def setup() {
        base.scriptRoots += 'vars'
        base.setUp()
        // Declarative Pipeline keyword; mock as no-op so the closure body is never executed.
        base.helper.registerAllowedMethod("pipeline", [Closure.class], { Closure c -> })
        // JPU 1.29 defaults error(String) to null (no-op). Override so it actually fails the build.
        base.helper.registerAllowedMethod("error", [String.class], { String s -> throw new RuntimeException(s) })
    }

    def "doStuff runs successfully"() {
        when:
        base.loadScript('vars/doStuff.groovy').call()

        then:
        noExceptionThrown()
        base.helper.callStack.findAll { it.methodName == 'error' }.isEmpty()
    }

    def "evenOrOdd executes even pipeline for even build number"() {
        when:
        base.loadScript('vars/evenOrOdd.groovy').call(2)

        then:
        noExceptionThrown()
        base.helper.callStack.findAll { it.methodName == 'error' }.isEmpty()
    }

    def "evenOrOdd executes odd pipeline for odd build number"() {
        when:
        base.loadScript('vars/evenOrOdd.groovy').call(1)

        then:
        noExceptionThrown()
        base.helper.callStack.findAll { it.methodName == 'error' }.isEmpty()
    }

    def "requireEnv passes when all named variables are set"() {
        given:
        base.binding.setProperty('env', [DEPLOY_TARGET: 'staging', API_KEY: 'secret'])

        when:
        base.loadScript('vars/requireEnv.groovy').call('DEPLOY_TARGET', 'API_KEY')

        then:
        noExceptionThrown()
        base.helper.callStack.findAll { it.methodName == 'error' }.isEmpty()
    }

    def "requireEnv fails build listing all missing variables"() {
        given:
        base.binding.setProperty('env', [DEPLOY_TARGET: 'staging'])
        def script = base.loadScript('vars/requireEnv.groovy')

        when:
        script.call('DEPLOY_TARGET', 'API_KEY', 'REGION')

        then:
        thrown(Exception)
        base.helper.callStack.findAll { it.methodName == 'error' }.size() == 1
    }
}
