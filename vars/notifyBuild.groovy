import com.mkobit.libraryexample.BasicScriptStepsLogger
import com.mkobit.libraryexample.BuildContext

def call(String status) {
    def log = new BasicScriptStepsLogger(this, 'notifyBuild')
    def ctx = new BuildContext(this)
    def message = "Build ${status}: ${ctx.jobName} #${ctx.buildNumber} (${ctx.branch})"
    log.info(message)
    echo message
}
