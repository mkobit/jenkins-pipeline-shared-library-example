import com.mkobit.libraryexample.ConventionalCommit
import com.mkobit.libraryexample.SemVer

def call(String fallback = 'v0.0.0') {
    def tag = sh(script: 'git tag --list "v*.*.*" --sort=-v:refname | head -1', returnStdout: true).trim()
    def base = SemVer.parse(tag ?: fallback)
    def range = tag ? "${tag}..HEAD" : 'HEAD'
    def subjects = sh(script: "git log --format=%s ${range}", returnStdout: true).trim()
    if (!subjects) {
        return base.toString()
    }
    def bump = ConventionalCommit.highestBump(subjects.split('\n').toList())
    switch (bump) {
        case 'major': return base.bumpMajor().toString()
        case 'minor': return base.bumpMinor().toString()
        case 'patch': return base.bumpPatch().toString()
        default: return base.toString()
    }
}
