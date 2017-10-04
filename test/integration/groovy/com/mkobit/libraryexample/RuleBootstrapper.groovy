package com.mkobit.libraryexample

import jenkins.plugins.git.GitSCMSource
import org.jenkinsci.plugins.workflow.libs.GlobalLibraries
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration
import org.jenkinsci.plugins.workflow.libs.SCMSourceRetriever
import org.jvnet.hudson.test.JenkinsRule

final class RuleBootstrapper {
  private RuleBootstrapper() {
  }

  static void setup(JenkinsRule rule) {
    rule.timeout = 30
    final libraryPath = System.getProperty('user.dir')
    println("Using Git library path at $libraryPath")
    final SCMSourceRetriever retriever = new SCMSourceRetriever(
        new GitSCMSource(
            null,
            libraryPath,
            '',
            'local-source-code',
            // Fetch everything - if this is not used builds fail on Jenkins for some reason
            '*:refs/remotes/origin/*',
            '*',
            '',
            true
        )
    )
    final LibraryConfiguration localLibrary =
        new LibraryConfiguration('testLibrary', retriever)
    localLibrary.implicit = true
    localLibrary.defaultVersion = 'git rev-parse HEAD'.execute().text.trim()
    localLibrary.allowVersionOverride = false
    GlobalLibraries.get().setLibraries(Collections.singletonList(localLibrary))
  }
}
