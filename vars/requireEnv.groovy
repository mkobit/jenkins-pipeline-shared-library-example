/** Fails the build if any of the named environment variables are absent. */
def call(String... names) {
    def missing = names.findAll { name -> !env[name] }
    if (missing) {
        error "Required environment variables not set: ${missing.join(', ')}"
    }
}
