package com.mkobit.libraryexample

import groovy.transform.CompileDynamic
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Shared
import spock.lang.Specification

// @CompileDynamic is required: Spock 2.x AST transformations can fail under
// @CompileStatic on Specification subclasses.
@WithJenkins
@CompileDynamic
abstract class JenkinsSpec extends Specification {
    // @Shared places the field on the spec's shared instance, which is the same object
    // that WithJenkinsExtension's feature interceptors receive as inv.instance. Without
    // @Shared, the interceptor sets the field on a different object than the one each
    // feature method's 'this' reads from.
    @Shared
    JenkinsRule jenkins
}
