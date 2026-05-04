package com.mkobit.libraryexample

import groovy.transform.CompileDynamic
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.fixtures.JenkinsSessionFixture
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.SpecInfo

@CompileDynamic
class WithJenkinsExtension extends AbstractAnnotationDrivenExtension<WithJenkins> {

    @Override
    void visitSpecAnnotation(WithJenkins annotation, SpecInfo spec) {
        // The annotation lives on an abstract base class. visitSpecAnnotation is called
        // with that base class's SpecInfo, so spec.allFeatures returns empty. Walk down
        // to the concrete (bottom) spec to install feature interceptors on actual tests.
        def bottomSpec = spec
        while (bottomSpec.subSpec != null) {
            bottomSpec = bottomSpec.subSpec
        }

        // Guard against double-installation.
        if (bottomSpec.metadata != null) {
            return
        }
        bottomSpec.metadata = Boolean.TRUE

        def fixture = new JenkinsSessionFixture()

        spec.addSetupSpecInterceptor { inv ->
            fixture.setUp(bottomSpec.reflection.name, 'suite')
            inv.proceed()
        }

        spec.addCleanupSpecInterceptor { inv ->
            inv.proceed()
            fixture.tearDown()
        }

        // Feature interceptors receive inv.instance = the spec's shared instance.
        // JenkinsSpec.jenkins is @Shared, so setting it here is visible from every
        // per-feature 'this' that Spock creates for the body blocks.
        bottomSpec.allFeatures.each { feature ->
            feature.addInterceptor { inv ->
                fixture.then { JenkinsRule r ->
                    inv.instance.jenkins = r
                    try {
                        inv.proceed()
                    } finally {
                        inv.instance.jenkins = null
                    }
                }
            }
        }
    }
}
