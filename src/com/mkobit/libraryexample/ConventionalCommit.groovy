package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

/**
 * Parses and classifies a Conventional Commit message.
 */
class ConventionalCommit implements Serializable {

    final String type
    final boolean breaking
    final String description

    private ConventionalCommit(String type, boolean breaking, String description) {
        this.type = type
        this.breaking = breaking
        this.description = description
    }

    /**
     * Parses a commit message string.
     * @param message the message to parse
     * @return the parsed Conventional Commit, or null if it does not match
     */
    @NonCPS
    static ConventionalCommit parse(String message) {
        def m = (message =~ /^(\w+)(?:\([^)]*\))?(!)?:\s+(.+)$/)
        if (!m) {
            return null
        }
        new ConventionalCommit(m[0][1], m[0][2] == '!', m[0][3])
    }

    /**
     * Finds the highest version bump type from a list of commit subjects.
     * @param subjects the list of commit subjects
     * @return the highest bump type ('major', 'minor', or 'patch')
     */
    @NonCPS
    static String highestBump(List<String> subjects) {
        def result = null
        for (def subject : subjects) {
            def c = parse(subject)
            if (!c) {
                continue
            }
            def bump = c.bumpType
            if (bump == 'major') {
                return 'major'
            }
            if (bump == 'minor') {
                result = 'minor'
            } else if (bump == 'patch' && result == null) {
                result = 'patch'
            }
        }
        result
    }

    /**
     * @return the bump type for this commit ('major', 'minor', or 'patch')
     */
    @NonCPS
    String getBumpType() {
        if (breaking) {
            return 'major'
        }
        if (type == 'feat') {
            return 'minor'
        }
        if (type == 'fix' || type == 'perf') {
            return 'patch'
        }
        null
    }

    @NonCPS
    String toString() {
        "${type}${breaking ? '!' : ''}: ${description}"
    }
}
