package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

/**
 * Demonstrates a {@code src/} class that calls pipeline steps via an injected script context.
 * Implements {@link Serializable} so Jenkins can checkpoint the pipeline across node boundaries.
 */
class ExampleSrc implements Serializable {

    // transient: the pipeline context is not serializable; callers must recreate
    // ExampleSrc instances if the pipeline is suspended and resumed.
    private transient Object script

    ExampleSrc(final Object script) {
        this.script = Objects.requireNonNull(script)
    }

    void sayHelloTo(String name) {
        script.echo("Hello there $name")
    }

    /** @NonCPS required: closures inside this method are incompatible with CPS transformation. */
    @NonCPS
    List<Integer> nonCpsDouble(List<Integer> integers) {
        integers.collect { Integer i -> i * 2 }
    }
}
