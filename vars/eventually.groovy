import com.mkobit.libraryexample.BasicScriptStepsLogger

// Polls a boolean condition with exponential backoff; unlike retry, this does not require exceptions.
// Accepts: maxAttempts (int), initialWaitSeconds (int), backoffMultiplier (int).
def call(Map opts = [:], Closure condition) {
    def log = new BasicScriptStepsLogger(this, 'eventually')
    def maxAttempts = opts.maxAttempts != null ? opts.maxAttempts : 5
    def waitSeconds = opts.initialWaitSeconds != null ? opts.initialWaitSeconds : 2
    def multiplier = opts.backoffMultiplier != null ? opts.backoffMultiplier : 2
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
        log.info("Polling (attempt ${attempt}/${maxAttempts})")
        if (condition()) {
            log.info("Condition met on attempt ${attempt}")
            return
        }
        if (attempt < maxAttempts) {
            log.info("Waiting ${waitSeconds}s before retry")
            sleep(waitSeconds as int)
            waitSeconds = (waitSeconds * multiplier).toInteger()
        }
    }
    error("Condition not met after ${maxAttempts} attempts")
}
