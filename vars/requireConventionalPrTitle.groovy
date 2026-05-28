import com.mkobit.libraryexample.ConventionalCommit

/**
 * Fails the build if the PR title is not a conventional commit.
 */
def call() {
    if (!env.CHANGE_ID) {
        return
    }
    def title = env.CHANGE_TITLE ?: ''
    if (!ConventionalCommit.parse(title)) {
        error("PR #${env.CHANGE_ID} title does not follow Conventional Commits: '${title}'")
    }
}
