package com.mkobit.libraryexample

// Numeric log levels for PIPELINE_LOG_LEVEL env var: 1=DEBUG, 2=INFO (default), 3=WARN, 4=ERROR.
interface PipelineLogger extends Serializable {

    int DEBUG = 1
    int INFO  = 2
    int WARN  = 3
    int ERROR = 4

    void debug(String message)

    void info(String message)

    void warn(String message)

    void error(String message)
}
