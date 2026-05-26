package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.Test;

class RequireEnvTest extends PipelineTest {

  @Test
  void passesWhenAllNamedVariablesAreSet() {
    base.getBinding().setProperty("env", Map.of("DEPLOY_TARGET", "staging", "API_KEY", "secret"));
    InvokerHelper.invokeMethod(
        base.loadScript("vars/requireEnv.groovy"),
        "call",
        new String[] {"DEPLOY_TARGET", "API_KEY"});
    assertTrue(
        base.getHelper().getCallStack().stream().noneMatch(c -> c.getMethodName().equals("error")));
  }

  @Test
  void failsListingAllMissingVariables() {
    base.getBinding().setProperty("env", Map.of("DEPLOY_TARGET", "staging"));
    Exception thrown = null;
    try {
      InvokerHelper.invokeMethod(
          base.loadScript("vars/requireEnv.groovy"),
          "call",
          new String[] {"DEPLOY_TARGET", "API_KEY", "REGION"});
    } catch (Exception e) {
      thrown = e;
    }
    assertNotNull(thrown);
    assertTrue(
        base.getHelper().getCallStack().stream()
            .anyMatch(
                c ->
                    c.getMethodName().equals("error")
                        && c.toString().contains("API_KEY")
                        && c.toString().contains("REGION")
                        && !c.toString().contains("DEPLOY_TARGET")));
  }
}
