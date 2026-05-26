package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

class SemVer implements Serializable, Comparable<SemVer> {

    final int major
    final int minor
    final int patch

    SemVer(int major, int minor, int patch) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("SemVer components must be non-negative: ${major}.${minor}.${patch}")
        }
        this.major = major
        this.minor = minor
        this.patch = patch
    }

    @NonCPS
    static SemVer parse(String version) {
        def m = (version =~ /^v?(\d+)\.(\d+)\.(\d+)$/)
        if (!m) {
            throw new IllegalArgumentException("Not a semver: ${version}")
        }
        new SemVer(m[0][1] as int, m[0][2] as int, m[0][3] as int)
    }

    @NonCPS
    SemVer bumpMajor() {
        new SemVer(major + 1, 0, 0)
    }

    @NonCPS
    SemVer bumpMinor() {
        new SemVer(major, minor + 1, 0)
    }

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
