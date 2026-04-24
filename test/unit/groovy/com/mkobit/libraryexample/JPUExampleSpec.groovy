package com.mkobit.libraryexample

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test

class JPUExampleSpec extends BasePipelineTest {

  @Override
  @Before
  void setUp() throws Exception {
    helper.scriptRoots += 'vars'
    super.setUp()
  }

  @Test
  void "doStuff runs successfully"() {
    def script = loadScript('vars/doStuff.groovy')
    script.call()
    assertJobStatusSuccess()
  }

  @Test
  void "evenOrOdd executes even pipeline for even build number"() {
    def script = loadScript('vars/evenOrOdd.groovy')
    script.call(2)
    assertJobStatusSuccess()
  }

  @Test
  void "evenOrOdd executes odd pipeline for odd build number"() {
    def script = loadScript('vars/evenOrOdd.groovy')
    script.call(1)
    assertJobStatusSuccess()
  }

  @Test
  void "withRetry succeeds on first attempt"() {
    def script = loadScript('vars/withRetry.groovy')
    boolean executed = false
    script.call(3) { executed = true }
    assert executed
    assertJobStatusSuccess()
  }

  @Test
  void "withRetry retries on failure and succeeds within limit"() {
    def script = loadScript('vars/withRetry.groovy')
    int attempts = 0
    script.call(3) {
      attempts++
      if (attempts < 3) {
        throw new RuntimeException("simulated failure on attempt ${attempts}")
      }
    }
    assert attempts == 3
    assertJobStatusSuccess()
  }

  @Test
  void "withRetry rethrows after exhausting all attempts"() {
    def script = loadScript('vars/withRetry.groovy')
    try {
      script.call(2) { throw new RuntimeException("always fails") }
      assert false : "expected exception not thrown"
    } catch (RuntimeException e) {
      assert e.message == "always fails"
    }
  }
}
