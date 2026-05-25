import com.mkobit.libraryexample.ConventionalCommit

def call() {
    if (!env.CHANGE_ID) {
        return
    }
    def title = env.CHANGE_TITLE ?: ''
    if (!ConventionalCommit.parse(title)) {
        error("PR #${env.CHANGE_ID} title does not follow Conventional Commits: '${title}'")
    }
}
