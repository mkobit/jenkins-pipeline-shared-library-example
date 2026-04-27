package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS
import groovy.transform.CompileDynamic

@CompileDynamic
class ExampleSrc {

    private final Object script

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
