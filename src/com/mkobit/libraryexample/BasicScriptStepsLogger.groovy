package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

class BasicScriptStepsLogger implements PipelineLogger, Serializable {

    private final String tag
    private transient Object script
    private final int logLevel

    BasicScriptStepsLogger(Object script, String tag) {
        this.script = Objects.requireNonNull(script)
        this.tag = Objects.requireNonNull(tag)
        this.logLevel = parseLevel(script.env?.PIPELINE_LOG_LEVEL)
    }

    void debug(String message) {
        if (logLevel <= PipelineLogger.DEBUG) {
            script.echo(format('DEBUG', message))
        }
    }

    void info(String message) {
        if (logLevel <= PipelineLogger.INFO) {
            script.echo(format('INFO', message))
        }
    }

    void warn(String message) {
        if (logLevel <= PipelineLogger.WARN) {
            script.echo(format('WARN', message))
        }
    }

    void error(String message) {
        if (logLevel <= PipelineLogger.ERROR) {
            script.echo(format('ERROR', message))
        }
    }

    @NonCPS
    private static int parseLevel(Object raw) {
        if (!raw) {
            return PipelineLogger.INFO
        }
        try {
            int n = raw.toString() as int
            n >= PipelineLogger.DEBUG && n <= PipelineLogger.ERROR ? n : PipelineLogger.INFO
        } catch (NumberFormatException ignored) {
            PipelineLogger.INFO
        }
    }

    // padRight is CPS-unsafe (GDK collection method).
    @NonCPS
    private String format(String level, String message) {
        "[${level.padRight(5)} ${tag}] ${message}"
    }
}
