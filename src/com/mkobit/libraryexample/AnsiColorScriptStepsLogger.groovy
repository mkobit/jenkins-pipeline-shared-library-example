package com.mkobit.libraryexample

import com.cloudbees.groovy.cps.NonCPS

// Emits ANSI-colored output. Requires the AnsiColor Jenkins plugin to render colors in the log.
class AnsiColorScriptStepsLogger implements PipelineLogger, Serializable {

    private final String tag
    private transient Object script
    private final int logLevel

    AnsiColorScriptStepsLogger(Object script, String tag) {
        this.script = Objects.requireNonNull(script)
        this.tag = Objects.requireNonNull(tag)
        this.logLevel = parseLevel(script.env?.PIPELINE_LOG_LEVEL)
    }

    void debug(String message) {
        if (logLevel <= PipelineLogger.DEBUG) {
            script.echo(ansi('36', 'DEBUG', message))
        }
    }

    void info(String message) {
        if (logLevel <= PipelineLogger.INFO) {
            script.echo(ansi('32', 'INFO', message))
        }
    }

    void warn(String message) {
        if (logLevel <= PipelineLogger.WARN) {
            script.echo(ansi('33', 'WARN', message))
        }
    }

    void error(String message) {
        if (logLevel <= PipelineLogger.ERROR) {
            script.echo(ansi('31', 'ERROR', message))
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
    // ESC is built at runtime to avoid raw control bytes in source.
    @NonCPS
    private String ansi(String colorCode, String level, String message) {
        def esc = new String([27] as char[])
        "${esc}[${colorCode}m[${level.padRight(5)} ${tag}] ${message}${esc}[0m"
    }
}
