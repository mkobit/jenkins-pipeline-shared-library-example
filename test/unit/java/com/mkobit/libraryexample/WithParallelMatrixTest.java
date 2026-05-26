package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import groovy.lang.Closure;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WithParallelMatrixTest extends PipelineTest {

  @BeforeEach
  void registerSteps() {
    base.getHelper()
        .registerAllowedMethod(
            "parallel",
            List.of(Map.class),
            closure(
                args -> {
                  ((Map<?, ?>) args[0]).values().forEach(v -> ((Closure<?>) v).call());
                  return null;
                }));
  }

  @Test
  void invokesBodyForEachCartesianCombination() {
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
}
