package com.mkobit.libraryexample

import com.mkobit.jenkins.pipelines.testing.LocalLibraryRetriever
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jvnet.hudson.test.JenkinsRule

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
