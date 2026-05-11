package testsupport.spock

import groovy.transform.CompileDynamic
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.fixtures.JenkinsSessionFixture
import spock.lang.Specification

// @CompileDynamic is required: Spock 2.x AST transformations can fail under
// @CompileStatic on Specification subclasses.
@CompileDynamic
abstract class JenkinsSpec extends Specification {
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
