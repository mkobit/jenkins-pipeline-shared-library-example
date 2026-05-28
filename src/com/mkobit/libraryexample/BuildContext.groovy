package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

/**
 * Context object containing build metadata.
 * Reads env eagerly so no script reference is kept after construction.
 */
class BuildContext implements Serializable {

    final String jobName
    final int buildNumber
    final String branch
    final String commitSha

    /**
     * Constructs a build context.
     * @param script the pipeline script to read env from
     */
    BuildContext(final Object script) {
        this.jobName     = script.env.JOB_NAME      ?: 'unknown'
        this.buildNumber = (script.env.BUILD_NUMBER  ?: '0').toInteger()
        this.branch      = script.env.BRANCH_NAME    ?: 'unknown'
        this.commitSha   = script.env.GIT_COMMIT     ?: 'unknown'
    }

    /**
     * Returns the context as a map.
     * Map literal construction is CPS-unsafe.
     * @return the build context map
     */
    @NonCPS
    Map<String, Object> toMap() {
        [job: jobName, build: buildNumber, branch: branch, commit: commitSha]
    }

    @NonCPS
    String toString() {
        "BuildContext(job=${jobName}, build=${buildNumber}, branch=${branch}, commit=${commitSha})"
    }
}
