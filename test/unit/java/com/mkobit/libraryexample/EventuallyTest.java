package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventuallyTest extends PipelineTest {

  @BeforeEach
  void registerSteps() {
    base.getHelper().registerAllowedMethod("sleep", List.of(Integer.class), closure(args -> null));
  }

  @Test
  void returnsImmediatelyWhenConditionMetOnFirstAttempt() {
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
  void errorsAfterAllAttemptsWhenConditionNeverMet() {
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
}
