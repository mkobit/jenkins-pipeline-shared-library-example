package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

class PipelineLogger implements Serializable {

    private final String tag
    private transient Object script

    PipelineLogger(final Object script, final String tag) {
        this.script = Objects.requireNonNull(script)
        this.tag = Objects.requireNonNull(tag)
    }

    void info(String message)  {
        script.echo(format('INFO', message))
    }
    void warn(String message)  {
        script.echo(format('WARN', message))
    }
    void error(String message) {
        script.echo(format('ERROR', message))
    }

    // padRight is a GDK collection method that confuses the CPS transformer.
    @NonCPS
    String format(String level, String message) {
        "[${level.padRight(5)} ${tag}] ${message}"
    }
}
