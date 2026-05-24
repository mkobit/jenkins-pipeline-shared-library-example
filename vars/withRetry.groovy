import com.mkobit.libraryexample.PipelineLogger

def call(int maxAttempts, Closure body) {
    def log = new PipelineLogger(this, 'withRetry')
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
            log.info("Attempt ${attempt} of ${maxAttempts}")
            body()
            return
        } catch (e) {
            log.warn("Attempt ${attempt} failed: ${e.message}")
            if (attempt == maxAttempts) {
                log.error('All attempts exhausted')
                throw e
            }
        }
    }
}
