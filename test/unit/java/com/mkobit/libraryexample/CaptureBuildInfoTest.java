package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.Test;

class CaptureBuildInfoTest extends PipelineTest {

  @Test
  void logsMetadataWithoutError() {
    base.getBinding()
        .setProperty(
            "env",
            Map.of(
                "JOB_NAME", "my-job",
                "BUILD_NUMBER", "5",
                "BRANCH_NAME", "main",
                "GIT_COMMIT", "abc1234"));
    InvokerHelper.invokeMethod(base.loadScript("vars/captureBuildInfo.groovy"), "call", null);
    assertTrue(
        base.getHelper().getCallStack().stream().noneMatch(c -> c.getMethodName().equals("error")));
    assertEquals(
        1,
        base.getHelper().getCallStack().stream()
            .filter(c -> c.getMethodName().equals("echo") && c.toString().contains("BuildContext("))
            .count());
  }
}
