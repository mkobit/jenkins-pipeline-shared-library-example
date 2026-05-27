package com.mkobit.libraryexample;

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest;
import groovy.lang.Closure;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;

abstract class PipelineTest {

  DeclarativePipelineTest base;

  @FunctionalInterface
  interface ClosureFn {
    Object call(Object[] args);
  }

  static Closure<Object> closure(ClosureFn fn) {
    return new Closure<Object>(null) {
      @Override
      public Object call(Object... args) {
        return fn.call(args);
      }
    };
  }

  @BeforeEach
  void initPipeline() throws Exception {
    base = new DeclarativePipelineTest() {};
    base.setUp();
    base.getHelper()
        .registerAllowedMethod(
            "error",
            List.of(String.class),
            closure(
                args -> {
                  throw new IllegalStateException((String) args[0]);
                }));
  }
}
