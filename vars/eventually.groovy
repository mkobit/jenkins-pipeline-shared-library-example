import com.mkobit.libraryexample.PipelineLogger

// Polls until condition() returns true, with exponential backoff between attempts.
// Unlike the built-in retry step (which is exception-based), this polls a boolean condition.
def call(Map opts = [:], Closure condition) {
    def log = new PipelineLogger(this, 'eventually')
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
