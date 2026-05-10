package testsupport.kotest

import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.fixtures.JenkinsSessionFixture

abstract class JenkinsFunSpec(
  body: JenkinsFunSpec.() -> Unit,
) : FunSpec() {
  private val fixture = JenkinsSessionFixture()

  // fixture.then() is a synchronous blocking call (Jetty startup + shutdown).
  // withContext(Dispatchers.IO) prevents it from starving Kotest's coroutine dispatcher.
  suspend fun jenkins(block: (JenkinsRule) -> Unit) = withContext(Dispatchers.IO) { fixture.then { r -> block(r) } }

  init {
    beforeSpec { fixture.setUp(this::class.java.name, "integration") }
    afterSpec { fixture.tearDown() }
    body()
  }
}
