package com.mkobit.libraryexample

import com.mkobit.jenkins.pipelines.testing.LocalLibraryRetriever
import groovy.transform.CompileDynamic
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jvnet.hudson.test.JenkinsRule

@CompileDynamic
final class RuleBootstrapper {
    private RuleBootstrapper() {
    }

    static void setup(JenkinsRule rule) {
        rule.timeout = 30
        GlobalLibraries.get().libraries = [
            LocalLibraryRetriever.implicitLibrary('testLibrary')
        ]
    }
}
