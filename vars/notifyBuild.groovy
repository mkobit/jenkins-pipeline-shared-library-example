import com.mkobit.libraryexample.BasicScriptStepsLogger
import com.mkobit.libraryexample.BuildContext

/**
 * Emits a build notification message with job metadata.
 * @param status the build status to notify
 */
def call(String status) {
    def log = new BasicScriptStepsLogger(this, 'notifyBuild')
    def ctx = new BuildContext(this)
    def message = "Build ${status}: ${ctx.jobName} #${ctx.buildNumber} (${ctx.branch})"
    log.info(message)
    echo message
}
