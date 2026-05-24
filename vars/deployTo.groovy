import com.mkobit.libraryexample.BuildContext
import com.mkobit.libraryexample.PipelineLogger

def call(String environment, int retries = 2, Closure body) {
    requireEnv('DEPLOY_ENV')
    def log = new PipelineLogger(this, "deployTo/${environment}")
    def ctx = new BuildContext(this)
    log.info("Deploying ${ctx.jobName} #${ctx.buildNumber} to ${environment}")
    withRetry(retries) {
        body()
    }
    log.info("Deployment to ${environment} complete")
}
