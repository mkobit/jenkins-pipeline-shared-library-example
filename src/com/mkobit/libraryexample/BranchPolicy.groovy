package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS
import groovy.transform.CompileStatic

/**
 * Classifies a branch name into an environment.
 */
@CompileStatic
class BranchPolicy implements Serializable {

    private final String branchName

    /**
     * Constructs a new branch policy.
     * @param branchName the name of the branch
     */
    BranchPolicy(final String branchName) {
        this.branchName = Objects.requireNonNull(branchName)
    }

    /**
     * @return whether the branch is the main branch
     */
    @NonCPS
    boolean isMain() {
        branchName == 'main' || branchName == 'master'
    }

    /**
     * @return whether the branch is a release branch
     */
    @NonCPS
    boolean isRelease() {
        branchName.startsWith('release/')
    }

    /**
     * @return whether the branch is a pull request branch
     */
    @NonCPS
    boolean isPullRequest() {
        branchName.startsWith('PR-')
    }

    /**
     * @return the environment classification for the branch
     */
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
