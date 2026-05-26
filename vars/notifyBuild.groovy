import com.mkobit.libraryexample.BuildContext
import com.mkobit.libraryexample.PipelineLogger

def call(String status) {
    def log = new PipelineLogger(this, 'notifyBuild')
    def ctx = new BuildContext(this)
    def message = "Build ${status}: ${ctx.jobName} #${ctx.buildNumber} (${ctx.branch})"
    log.info(message)
    echo message
}
