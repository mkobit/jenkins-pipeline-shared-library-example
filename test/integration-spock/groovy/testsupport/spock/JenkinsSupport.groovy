package testsupport.spock

import groovy.transform.CompileDynamic
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.fixtures.JenkinsSessionFixture

/**
 * Trait providing an embedded Jenkins environment for Spock specifications.
 */
@CompileDynamic
trait JenkinsSupport {

    private JenkinsSessionFixture fixture = new JenkinsSessionFixture()

    def setup() {
        fixture.setUp(this.class.name, 'integration')
    }

    def cleanup() {
        fixture.tearDown()
    }

    void jenkins(@DelegatesTo(value = JenkinsRule, strategy = Closure.DELEGATE_FIRST) Closure block) {
        fixture.then { JenkinsRule j ->
            block.delegate = j
            block.resolveStrategy = Closure.DELEGATE_FIRST
            block.call(j)
        }
    }
}
