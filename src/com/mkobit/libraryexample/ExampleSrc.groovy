package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

class ExampleSrc {

  private final script

  ExampleSrc(final script) {
    this.script = Objects.requireNonNull(script)
  }

  void sayHelloTo(String name) {
    script.echo("Hello there $name")
  }

  @NonCPS
  List<Integer> nonCpsDouble(List<Integer> integers) {
    integers.collect { it * 2 }
  }
}
