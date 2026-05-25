package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

// Pure value class — no script reference. All methods are @NonCPS (string logic only).
class BranchPolicy implements Serializable {

    private final String branchName

    BranchPolicy(final String branchName) {
        this.branchName = Objects.requireNonNull(branchName)
    }

    @NonCPS
    boolean isMain() {
        branchName == 'main' || branchName == 'master'
    }

    @NonCPS
    boolean isRelease() {
        branchName.startsWith('release/')
    }

    @NonCPS
    boolean isPullRequest() {
        branchName.startsWith('PR-')
    }

    @NonCPS
    String getEnvironment() {
        if (main) {
            return 'production'
        }
        release ? 'staging' : 'development'
    }

    @NonCPS
    String toString() {
        "BranchPolicy(branch=${branchName}, environment=${environment})"
    }
}
