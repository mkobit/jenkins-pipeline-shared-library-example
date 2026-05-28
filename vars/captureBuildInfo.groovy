import com.mkobit.libraryexample.BasicScriptStepsLogger
import com.mkobit.libraryexample.BuildContext
import com.mkobit.libraryexample.PipelineLogger

/**
 * Logs job metadata.
 * Accepts an optional logger to demonstrate dependency injection.
 * Default: BasicScriptStepsLogger reading PIPELINE_LOG_LEVEL from env.
 * @param log an optional custom logger
 */
def call(PipelineLogger log = null) {
    def effectiveLog = log ?: new BasicScriptStepsLogger(this, 'captureBuildInfo')
    def ctx = new BuildContext(this)
    effectiveLog.info(ctx.toString())
    echo "Build metadata: ${ctx.toMap()}"
}
