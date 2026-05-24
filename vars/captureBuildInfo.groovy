import com.mkobit.libraryexample.BuildContext
import com.mkobit.libraryexample.PipelineLogger

def call() {
    def log = new PipelineLogger(this, 'captureBuildInfo')
    def ctx = new BuildContext(this)
    log.info(ctx.toString())
    echo "Build metadata: ${ctx.toMap()}"
}
