package testsupport.spock

import groovy.transform.CompileDynamic
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.fixtures.JenkinsSessionFixture
import spock.lang.Shared
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

    void jenkins(Closure block) {
        fixture.then { JenkinsRule j -> block(j) }
    }
}
