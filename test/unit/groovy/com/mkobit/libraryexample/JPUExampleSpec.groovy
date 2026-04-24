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
  void "requireEnv passes when all named variables are set"() {
    binding.setProperty('env', [DEPLOY_TARGET: 'staging', API_KEY: 'secret'])
    def script = loadScript('vars/requireEnv.groovy')
    script.call('DEPLOY_TARGET', 'API_KEY')
    assertJobStatusSuccess()
  }

  @Test
  void "requireEnv fails build listing all missing variables"() {
    binding.setProperty('env', [DEPLOY_TARGET: 'staging'])
    def script = loadScript('vars/requireEnv.groovy')
    try {
      script.call('DEPLOY_TARGET', 'API_KEY', 'REGION')
      assert false : "error step should have been called"
    } catch (Exception ignored) {
    }
    assertThat(helper.callStack.findAll { it.methodName == 'error' }.size(), 1)
  }
}
