package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

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

    @NonCPS
    List<Integer> nonCpsDouble(List<Integer> integers) {
        integers.collect { Integer i -> i * 2 }
    }
}
