package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.Test;

class NotifyBuildTest extends PipelineTest {

  @Test
  void echoesRenderedMessageWithStatus() {
    base.getBinding()
        .setProperty(
            "env", Map.of("JOB_NAME", "my-service", "BUILD_NUMBER", "42", "BRANCH_NAME", "main"));
    InvokerHelper.invokeMethod(base.loadScript("vars/notifyBuild.groovy"), "call", "SUCCESS");
    assertTrue(
        base.getHelper().getCallStack().stream()
            .anyMatch(
                c ->
                    c.getMethodName().equals("echo")
                        && c.toString().contains("SUCCESS")
                        && c.toString().contains("my-service")
                        && c.toString().contains("42")));
  }
}
