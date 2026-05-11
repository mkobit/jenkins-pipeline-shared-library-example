package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest;
import groovy.lang.Closure;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VarsUnitJavaTest {

  private DeclarativePipelineTest base;

  @BeforeEach
  void setUp() throws Exception {
    base = new DeclarativePipelineTest() {};
    base.setScriptRoots(new String[] {".", "vars"});
    base.setUp();
    // JPU 1.29 defaults error(String) to null (no-op). Override so it actually fails the build.
    base.getHelper()
        .registerAllowedMethod(
            "error",
            List.of(String.class),
            new Closure<Void>(null) {
              public Void doCall(String s) {
                throw new IllegalStateException(s);
              }
            });
  }

  @Test
  void doStuffRunsSuccessfully() {
    InvokerHelper.invokeMethod(base.loadScript("vars/doStuff.groovy"), "call", null);

    long errorCalls =
        base.getHelper().getCallStack().stream()
            .filter(call -> call.getMethodName().equals("error"))
            .count();
    assertEquals(0, errorCalls, "Should have no error() calls");
  }

  @Test
  void evenOrOddExecutesEvenPipelineForEvenBuildNumber() {
    InvokerHelper.invokeMethod(base.loadScript("vars/evenOrOdd.groovy"), "call", 2);

    long errorCalls =
        base.getHelper().getCallStack().stream()
            .filter(call -> call.getMethodName().equals("error"))
            .count();
    assertEquals(0, errorCalls, "Should have no error() calls");
  }

  @Test
  void evenOrOddExecutesOddPipelineForOddBuildNumber() {
    InvokerHelper.invokeMethod(base.loadScript("vars/evenOrOdd.groovy"), "call", 1);

    long errorCalls =
        base.getHelper().getCallStack().stream()
            .filter(call -> call.getMethodName().equals("error"))
            .count();
    assertEquals(0, errorCalls, "Should have no error() calls");
  }

  @Test
  void requireEnvPassesWhenAllNamedVariablesAreSet() {
    base.getBinding().setProperty("env", Map.of("DEPLOY_TARGET", "staging", "API_KEY", "secret"));
    InvokerHelper.invokeMethod(
        base.loadScript("vars/requireEnv.groovy"),
        "call",
        new Object[] {"DEPLOY_TARGET", "API_KEY"});

    long errorCalls =
        base.getHelper().getCallStack().stream()
            .filter(call -> call.getMethodName().equals("error"))
            .count();
    assertEquals(0, errorCalls, "Should have no error() calls");
  }

  @Test
  void requireEnvFailsBuildListingAllMissingVariables() {
    base.getBinding().setProperty("env", Map.of("DEPLOY_TARGET", "staging"));
    Object script = base.loadScript("vars/requireEnv.groovy");

    assertThrows(
        Exception.class,
        () -> {
          InvokerHelper.invokeMethod(
              script, "call", new Object[] {"DEPLOY_TARGET", "API_KEY", "REGION"});
        });

    long errorCalls =
        base.getHelper().getCallStack().stream()
            .filter(call -> call.getMethodName().equals("error"))
            .count();
    assertEquals(1, errorCalls, "Should have exactly one error() call");
  }
}
