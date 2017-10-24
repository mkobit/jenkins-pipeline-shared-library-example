package com.mkobit.libraryexample

import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.LibraryRetriever
import org.jvnet.hudson.test.JenkinsRule
import testsupport.LocalLibraryRetriever

final class RuleBootstrapper {
  private RuleBootstrapper() {
  }

  static void setup(JenkinsRule rule) {
    rule.timeout = 30
    final LibraryRetriever retriever = new LocalLibraryRetriever()
    final LibraryConfiguration localLibrary =
        new LibraryConfiguration('testLibrary', retriever)
    localLibrary.implicit = true
    localLibrary.defaultVersion = 'unused'
    localLibrary.allowVersionOverride = false
    GlobalLibraries.get().setLibraries(Collections.singletonList(localLibrary))
  }
}
