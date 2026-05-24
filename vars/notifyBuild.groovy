import com.mkobit.libraryexample.BuildContext
import com.mkobit.libraryexample.NotificationPayload
import com.mkobit.libraryexample.PipelineLogger

def call(String status) {
    def log      = new PipelineLogger(this, 'notifyBuild')
    def ctx      = new BuildContext(this)
    def template = libraryResource('com/mkobit/libraryexample/notification-payload.json')
    def payload  = new NotificationPayload(template).render(ctx, status)
    log.info("Notification payload: ${payload}")
    echo payload
}
