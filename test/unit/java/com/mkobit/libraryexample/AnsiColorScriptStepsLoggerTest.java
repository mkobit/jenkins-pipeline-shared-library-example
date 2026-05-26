package com.mkobit.libraryexample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import groovy.lang.Closure;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnsiColorScriptStepsLoggerTest {

  private static final String ESC = "";

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
    script.setProperty("env", Map.of("PIPELINE_LOG_LEVEL", "1"));
  }

  private AnsiColorScriptStepsLogger logger(String tag) {
    return new AnsiColorScriptStepsLogger(script, tag);
  }

  @Test
  void infoOutputIsGreenWithReset() {
    logger("svc").info("deployed");
    var line = echoed.get(0);
    assertTrue(line.startsWith(ESC + "[32m"), "should start with green ANSI code");
    assertTrue(line.endsWith(ESC + "[0m"), "should end with reset ANSI code");
    assertTrue(line.contains("INFO"));
    assertTrue(line.contains("deployed"));
  }

  @Test
  void warnOutputIsYellow() {
    logger("svc").warn("slow");
    assertTrue(echoed.get(0).startsWith(ESC + "[33m"));
  }

  @Test
  void errorOutputIsRed() {
    logger("svc").error("boom");
    assertTrue(echoed.get(0).startsWith(ESC + "[31m"));
  }

  @Test
  void debugOutputIsCyan() {
    logger("svc").debug("verbose");
    assertTrue(echoed.get(0).startsWith(ESC + "[36m"));
  }

  @Test
  void levelFilteringMatchesBasicLogger() {
    script.setProperty("env", Map.of("PIPELINE_LOG_LEVEL", "3"));
    var log = logger("svc");
    log.debug("d");
    log.info("i");
    log.warn("w");
    log.error("e");
    assertEquals(2, echoed.size());
  }
}
