/** Fails the build if any of the named environment variables are absent. */
def call(String... names) {
    def missing = []
    for (def name in names) {
        if (!env[name]) {
            missing << name
        }
    }
    if (missing) {
        error "Required environment variables not set: ${missing.join(', ')}"
    }
}
