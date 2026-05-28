package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

/**
 * Represents a Semantic Version.
 */
class SemVer implements Serializable, Comparable<SemVer> {

    final int major
    final int minor
    final int patch

    /**
     * Constructs a Semantic Version.
     * @param major the major version component
     * @param minor the minor version component
     * @param patch the patch version component
     */
    SemVer(int major, int minor, int patch) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("SemVer components must be non-negative: ${major}.${minor}.${patch}")
        }
        this.major = major
        this.minor = minor
        this.patch = patch
    }

    /**
     * Parses a string into a Semantic Version.
     * @param version the version string to parse
     * @return the parsed Semantic Version
     */
    @NonCPS
    static SemVer parse(String version) {
        def m = (version =~ /^v?(\d+)\.(\d+)\.(\d+)$/)
        if (!m) {
            throw new IllegalArgumentException("Not a semver: ${version}")
        }
        new SemVer(m[0][1] as int, m[0][2] as int, m[0][3] as int)
    }

    /**
     * Bumps the major version.
     * @return a new Semantic Version with the bumped major version
     */
    @NonCPS
    SemVer bumpMajor() {
        new SemVer(major + 1, 0, 0)
    }

    /**
     * Bumps the minor version.
     * @return a new Semantic Version with the bumped minor version
     */
    @NonCPS
    SemVer bumpMinor() {
        new SemVer(major, minor + 1, 0)
    }

    /**
     * Bumps the patch version.
     * @return a new Semantic Version with the bumped patch version
     */
    @NonCPS
    SemVer bumpPatch() {
        new SemVer(major, minor, patch + 1)
    }

    @NonCPS
    int compareTo(SemVer other) {
        if (major != other.major) {
            return major <=> other.major
        }
        if (minor != other.minor) {
            return minor <=> other.minor
        }
        patch <=> other.patch
    }

    @NonCPS
    String toString() {
        "v${major}.${minor}.${patch}"
    }
}
