package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest;
import groovy.lang.Closure;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VarsUnitTest {

  private DeclarativePipelineTest base;

  // Adapt a lambda into a Groovy Closure for JPU's registerAllowedMethod.
  @FunctionalInterface
  interface ClosureFn {
    Object call(Object[] args);
  }

  private static Closure<Object> closure(ClosureFn fn) {
    return new Closure<Object>(null) {
      @Override
      public Object call(Object... args) {
        return fn.call(args);
      }
    };
  }

  @BeforeEach
  void init() throws Exception {
    base = new DeclarativePipelineTest() {};
    base.setScriptRoots(new String[] {".", "vars"});
    base.setUp();
    var h = base.getHelper();
    h.registerAllowedMethod(
        "error",
        List.of(String.class),
        closure(
            args -> {
              throw new IllegalStateException((String) args[0]);
            }));
    h.registerAllowedMethod(
        "libraryResource",
        List.of(String.class),
        closure(args -> "{\"text\": \"Build {{status}}: {{job}} #{{build}} ({{branch}})\"}"));
    h.registerAllowedMethod("sleep", List.of(Integer.class), closure(args -> null));
    h.registerAllowedMethod(
        "parallel",
        List.of(Map.class),
        closure(
            args -> {
              ((Map<?, ?>) args[0]).values().forEach(v -> ((Closure<?>) v).call());
              return null;
            }));
    h.registerAllowedMethod(
        "withCredentials",
        List.of(List.class, Closure.class),
        closure(
            args -> {
              ((Closure<?>) args[1]).call();
              return null;
            }));
    h.registerAllowedMethod("string", List.of(Map.class), closure(args -> args[0]));
  }

  // BranchPolicy — pure @NonCPS logic; instantiate directly, no JPU needed.

  @Test
  void branchPolicyIdentifiesMainBranchAsProductionEnvironment() {
    var policy = new BranchPolicy("main");
    assertTrue(policy.isMain());
    assertFalse(policy.isRelease());
    assertFalse(policy.isPullRequest());
    assertEquals("production", policy.getEnvironment());
  }

  @Test
  void branchPolicyIdentifiesMasterBranchAsProductionEnvironment() {
    var policy = new BranchPolicy("master");
    assertTrue(policy.isMain());
    assertEquals("production", policy.getEnvironment());
  }

  @Test
  void branchPolicyIdentifiesReleaseBranchAsStagingEnvironment() {
    var policy = new BranchPolicy("release/1.2.3");
    assertTrue(policy.isRelease());
    assertFalse(policy.isMain());
    assertEquals("staging", policy.getEnvironment());
  }

  @Test
  void branchPolicyIdentifiesPrBranchAsDevelopmentEnvironment() {
    var policy = new BranchPolicy("PR-42");
    assertTrue(policy.isPullRequest());
    assertFalse(policy.isMain());
    assertFalse(policy.isRelease());
    assertEquals("development", policy.getEnvironment());
  }

  @Test
  void branchPolicyTreatsFeatureBranchAsDevelopmentEnvironment() {
    var policy = new BranchPolicy("feature/new-thing");
    assertFalse(policy.isMain());
    assertFalse(policy.isRelease());
    assertFalse(policy.isPullRequest());
    assertEquals("development", policy.getEnvironment());
  }

  // doStuff / evenOrOdd / requireEnv

  @Test
  void doStuffRunsSuccessfully() {
    InvokerHelper.invokeMethod(base.loadScript("vars/doStuff.groovy"), "call", null);
    assertTrue(
        base.getHelper().getCallStack().stream().noneMatch(c -> c.getMethodName().equals("error")));
  }

  @Test
  void evenOrOddExecutesEvenPipelineForEvenBuildNumber() {
    InvokerHelper.invokeMethod(base.loadScript("vars/evenOrOdd.groovy"), "call", 2);
    assertTrue(
        base.getHelper().getCallStack().stream().noneMatch(c -> c.getMethodName().equals("error")));
  }

  @Test
  void evenOrOddExecutesOddPipelineForOddBuildNumber() {
    InvokerHelper.invokeMethod(base.loadScript("vars/evenOrOdd.groovy"), "call", 1);
    assertTrue(
        base.getHelper().getCallStack().stream().noneMatch(c -> c.getMethodName().equals("error")));
  }

  @Test
  void requireEnvPassesWhenAllNamedVariablesAreSet() {
    base.getBinding().setProperty("env", Map.of("DEPLOY_TARGET", "staging", "API_KEY", "secret"));
    InvokerHelper.invokeMethod(
        base.loadScript("vars/requireEnv.groovy"),
        "call",
        new String[] {"DEPLOY_TARGET", "API_KEY"});
    assertTrue(
        base.getHelper().getCallStack().stream().noneMatch(c -> c.getMethodName().equals("error")));
  }

  @Test
  void requireEnvFailsBuildListingAllMissingVariables() {
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
    assertEquals(
        1,
        base.getHelper().getCallStack().stream()
            .filter(c -> c.getMethodName().equals("error"))
            .count());
  }

  // captureBuildInfo

  @Test
  void captureBuildInfoLogsMetadataWithoutError() {
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

  // tagBuild

  @Test
  void tagBuildSetsDisplayNameAndDescriptionOnCurrentBuild() {
    var mockBuild = new groovy.util.Expando();
    mockBuild.setProperty("number", 1);
    mockBuild.setProperty("displayName", "");
    mockBuild.setProperty("description", "");
    base.getBinding().setProperty("currentBuild", mockBuild);
    InvokerHelper.invokeMethod(base.loadScript("vars/tagBuild.groovy"), "call", "v2.0.0");
    assertEquals("#1 v2.0.0", mockBuild.getProperty("displayName").toString());
    assertEquals("v2.0.0", mockBuild.getProperty("description").toString());
  }

  // eventually

  @Test
  void eventuallyReturnsImmediatelyWhenConditionIsMetOnFirstAttempt() {
    InvokerHelper.invokeMethod(
        base.loadScript("vars/eventually.groovy"),
        "call",
        new Object[] {Map.of("maxAttempts", 2, "initialWaitSeconds", 1), closure(args -> true)});
    assertTrue(
        base.getHelper().getCallStack().stream().noneMatch(c -> c.getMethodName().equals("sleep")));
    assertTrue(
        base.getHelper().getCallStack().stream().noneMatch(c -> c.getMethodName().equals("error")));
  }

  @Test
  void eventuallyErrorsAfterAllAttemptsWhenConditionIsNeverMet() {
    Exception thrown = null;
    try {
      InvokerHelper.invokeMethod(
          base.loadScript("vars/eventually.groovy"),
          "call",
          new Object[] {Map.of("maxAttempts", 2, "initialWaitSeconds", 1), closure(args -> false)});
    } catch (Exception e) {
      thrown = e;
    }
    assertNotNull(thrown);
    assertEquals(
        1,
        base.getHelper().getCallStack().stream()
            .filter(c -> c.getMethodName().equals("error"))
            .count());
    assertTrue(
        base.getHelper().getCallStack().stream()
            .anyMatch(c -> c.getMethodName().equals("echo") && c.toString().contains("Waiting")));
  }

  // withParallelMatrix

  @Test
  void withParallelMatrixInvokesBodyForEachCartesianCombination() {
    var receivedCombos = new ArrayList<Map<?, ?>>();
    InvokerHelper.invokeMethod(
        base.loadScript("vars/withParallelMatrix.groovy"),
        "call",
        new Object[] {
          Map.of("os", List.of("linux", "windows"), "jdk", List.of("11", "21")),
          closure(
              args -> {
                receivedCombos.add((Map<?, ?>) args[0]);
                return null;
              })
        });
    assertEquals(4, receivedCombos.size());
    assertTrue(
        receivedCombos.stream()
            .anyMatch(c -> "linux".equals(c.get("os")) && "11".equals(c.get("jdk"))));
    assertTrue(
        receivedCombos.stream()
            .anyMatch(c -> "linux".equals(c.get("os")) && "21".equals(c.get("jdk"))));
    assertTrue(
        receivedCombos.stream()
            .anyMatch(c -> "windows".equals(c.get("os")) && "11".equals(c.get("jdk"))));
    assertTrue(
        receivedCombos.stream()
            .anyMatch(c -> "windows".equals(c.get("os")) && "21".equals(c.get("jdk"))));
  }

  // withSecretText

  @Test
  void withSecretTextExecutesBodyWithCredentialsBound() {
    var calls = new ArrayList<>();
    InvokerHelper.invokeMethod(
        base.loadScript("vars/withSecretText.groovy"),
        "call",
        new Object[] {
          "my-cred",
          "MY_VAR",
          closure(
              args -> {
                calls.add(true);
                return null;
              })
        });
    assertEquals(1, calls.size());
    assertTrue(
        base.getHelper().getCallStack().stream().noneMatch(c -> c.getMethodName().equals("error")));
  }

  // notifyBuild

  @Test
  void notifyBuildEchoesRenderedPayloadWithStatus() {
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
