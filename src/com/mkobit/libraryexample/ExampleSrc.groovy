package com.mkobit.libraryexample

class ExampleSrc {

  private final script

  ExampleSrc(final script) {
    this.script = Objects.requireNonNull(script)
  }

  void sayHelloTo(String name) {
    script.echo("Hello there $name")
  }
}
