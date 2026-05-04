package com.mkobit.libraryexample

import groovy.transform.CompileDynamic
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Shared
import spock.lang.Specification

// Spock 2.x runs on JUnit Platform which does not honour JUnit 4 @Rule / @ClassRule
// lifecycle wiring. SpockJenkinsRule overrides recipe() to skip the JUnit 4 Description
// lookup, making before()/after() safe to call directly from setupSpec/cleanupSpec.
//
// @CompileDynamic is intentional: Spock 2.x AST transformations run on Groovy 3 AST
// and can fail under @CompileStatic on a Specification subclass.
@CompileDynamic
abstract class JenkinsSpec extends Specification {

    @Shared
    JenkinsRule rule

    def setupSpec() {
        rule = new SpockJenkinsRule()
        rule.timeout = 30
        rule.before()
    }

    def cleanupSpec() {
        rule?.after()
    }

    // JenkinsRule.recipe() reads testDescription (a JUnit 4 concept) to wire test
    // annotations like @LocalData. Skipping it makes JenkinsRule framework-agnostic.
    private static final class SpockJenkinsRule extends JenkinsRule {
        @Override
        void recipe() throws Exception {
            recipeLoadCurrentPlugin()
        }
    }
}
