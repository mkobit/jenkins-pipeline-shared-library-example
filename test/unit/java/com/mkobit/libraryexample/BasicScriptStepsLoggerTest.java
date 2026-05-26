package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import groovy.lang.Closure;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BasicScriptStepsLoggerTest {

  private final List<String> echoed = new ArrayList<>();
  private groovy.util.Expando script;

  @BeforeEach
  void setup() {
    script = new groovy.util.Expando();
    script.setProperty(
        "echo",
        new Closure<Void>(null) {
          @Override
          public Void call(Object... args) {
            echoed.add((String) args[0]);
            return null;
          }
        });
  }

  private BasicScriptStepsLogger loggerWithLevel(String level) {
    script.setProperty("env", level != null ? Map.of("PIPELINE_LOG_LEVEL", level) : Map.of());
    return new BasicScriptStepsLogger(script, "test");
  }

  @Test
  void defaultsToInfoLevelWhenEnvNotSet() {
    var log = loggerWithLevel(null);
    log.debug("d");
    log.info("i");
    assertTrue(echoed.stream().noneMatch(m -> m.contains("DEBUG")));
    assertEquals(1, echoed.size());
    assertTrue(echoed.get(0).contains("INFO"));
  }

  @Test
  void debugLevelEmitsAllMessages() {
    var log = loggerWithLevel("1");
    log.debug("d");
    log.info("i");
    log.warn("w");
    log.error("e");
    assertEquals(4, echoed.size());
  }

  @Test
  void warnLevelSuppressesDebugAndInfo() {
    var log = loggerWithLevel("3");
    log.debug("d");
    log.info("i");
    log.warn("w");
    log.error("e");
    assertEquals(2, echoed.size());
    assertTrue(echoed.stream().anyMatch(m -> m.contains("WARN")));
    assertTrue(echoed.stream().anyMatch(m -> m.contains("ERROR")));
  }

  @Test
  void errorLevelSuppressesAllButError() {
    var log = loggerWithLevel("4");
    log.debug("d");
    log.info("i");
    log.warn("w");
    log.error("e");
    assertEquals(1, echoed.size());
    assertTrue(echoed.get(0).contains("ERROR"));
  }

  @Test
  void invalidLevelFallsBackToInfo() {
    var log = loggerWithLevel("not-a-number");
    log.debug("d");
    log.info("i");
    assertEquals(1, echoed.size());
    assertTrue(echoed.get(0).contains("INFO"));
  }

  @Test
  void outOfRangeLevelFallsBackToInfo() {
    var log = loggerWithLevel("99");
    log.debug("d");
    log.info("i");
    assertEquals(1, echoed.size());
  }

  @Test
  void tagAndLevelAppearInOutput() {
    var log = loggerWithLevel("2");
    log.warn("something odd");
    assertTrue(echoed.get(0).contains("test"));
    assertTrue(echoed.get(0).contains("WARN"));
    assertTrue(echoed.get(0).contains("something odd"));
  }
}
