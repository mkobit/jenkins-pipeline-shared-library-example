package com.mkobit.libraryexample

/**
 * Interface for pipeline logging.
 * Numeric log levels for {@code PIPELINE_LOG_LEVEL} env var: 1=DEBUG, 2=INFO (default), 3=WARN, 4=ERROR.
 */
interface PipelineLogger extends Serializable {

    int DEBUG = 1
    int INFO  = 2
    int WARN  = 3
    int ERROR = 4

    /**
     * Logs a debug message.
     * @param message the message to log
     */
    void debug(String message)

    /**
     * Logs an info message.
     * @param message the message to log
     */
    void info(String message)

    /**
     * Logs a warning message.
     * @param message the message to log
     */
    void warn(String message)

    /**
     * Logs an error message.
     * @param message the message to log
     */
    void error(String message)
}
