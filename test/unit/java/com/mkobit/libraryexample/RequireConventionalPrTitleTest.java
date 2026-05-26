package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.Test;

class RequireConventionalPrTitleTest extends PipelineTest {

  @Test
  void isNoOpWhenNotOnPr() {
    base.getBinding().setProperty("env", Map.of());
    InvokerHelper.invokeMethod(
        base.loadScript("vars/requireConventionalPrTitle.groovy"), "call", null);
    assertTrue(
        base.getHelper().getCallStack().stream().noneMatch(c -> c.getMethodName().equals("error")));
  }

  @Test
  void passesForValidTitle() {
    base.getBinding()
        .setProperty("env", Map.of("CHANGE_ID", "42", "CHANGE_TITLE", "feat(auth): add login"));
    InvokerHelper.invokeMethod(
        base.loadScript("vars/requireConventionalPrTitle.groovy"), "call", null);
    assertTrue(
        base.getHelper().getCallStack().stream().noneMatch(c -> c.getMethodName().equals("error")));
  }

  @Test
  void failsForNonConventionalTitle() {
    base.getBinding()
        .setProperty("env", Map.of("CHANGE_ID", "42", "CHANGE_TITLE", "fix some stuff"));
    Exception thrown = null;
    try {
      InvokerHelper.invokeMethod(
          base.loadScript("vars/requireConventionalPrTitle.groovy"), "call", null);
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
}
