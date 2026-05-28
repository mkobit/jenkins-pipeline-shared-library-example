import com.mkobit.libraryexample.ConventionalCommit
import com.mkobit.libraryexample.SemVer

/**
 * Computes next version from git tags and conventional commits.
 * @param fallback the fallback version to use if no tags exist
 * @return the next version string
 */
def call(String fallback = 'v0.0.0') {
    def tag = sh(script: 'git tag --list "v*.*.*" --sort=-v:refname | head -1', returnStdout: true).trim()
    def base = SemVer.parse(tag ?: fallback)
    def range = tag ? "${tag}..HEAD" : 'HEAD'
    def subjects = sh(script: "git log --format=%s ${range}", returnStdout: true).trim()
    if (!subjects) {
        return base.toString()
    }
    def bump = ConventionalCommit.highestBump(splitLines(subjects))
    switch (bump) {
        case 'major': return base.bumpMajor().toString()
        case 'minor': return base.bumpMinor().toString()
        case 'patch': return base.bumpPatch().toString()
        default: return base.toString()
    }
}

@com.cloudbees.groovy.cps.NonCPS
private List<String> splitLines(String text) {
    text.split('\n').toList()
}
