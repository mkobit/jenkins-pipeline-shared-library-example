package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

// Reads env eagerly so no script reference is kept after construction.
class BuildContext implements Serializable {

    final String jobName
    final int buildNumber
    final String branch
    final String commitSha

    BuildContext(final Object script) {
        this.jobName     = script.env.JOB_NAME      ?: 'unknown'
        this.buildNumber = (script.env.BUILD_NUMBER  ?: '0').toInteger()
        this.branch      = script.env.BRANCH_NAME    ?: 'unknown'
        this.commitSha   = script.env.GIT_COMMIT     ?: 'unknown'
    }

    // Map literal construction is CPS-unsafe.
    @NonCPS
    Map<String, Object> toMap() {
        [job: jobName, build: buildNumber, branch: branch, commit: commitSha]
    }

    @NonCPS
    String toString() {
        "BuildContext(job=${jobName}, build=${buildNumber}, branch=${branch}, commit=${commitSha})"
    }
}
