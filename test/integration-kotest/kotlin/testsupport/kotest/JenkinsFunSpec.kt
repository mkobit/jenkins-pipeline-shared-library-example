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

  /**
   * Runs a block of code within a fresh Jenkins process.
   * Jenkins is started before the block and shut down after.
   * State in JENKINS_HOME is persisted across multiple calls to [jenkins] within the same test.
   */
  suspend fun jenkins(block: (JenkinsRule) -> Unit) =
    withContext(Dispatchers.IO) {
      fixture.then(block)
    }

  init {
    beforeTest {
      val className = this::class.qualifiedName ?: this::class.java.name
      fixture.setUp(className, "integration")
    }
    afterTest {
      fixture.tearDown()
    }
    body()
  }
}
