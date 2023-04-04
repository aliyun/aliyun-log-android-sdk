package com.aliyun.sls.android.ot.logs;

/**
 * @author gordon
 * @date 2023/2/1
 */
public enum LogLevel {
    UNDEFINED_SEVERITY_NUMBER(0),
    TRACE(1),
    DEBUG(5),
    INFO(9),
    WARN(13),
    ERROR(17),
    FATAL(21)
    ;

    private final int severityNumber;

    LogLevel(int severityNumber) {
        this.severityNumber = severityNumber;
    }

    public String getSeverityNumber() {
        switch (severityNumber) {
            case 1: return "SEVERITY_NUMBER_TRACE";
            case 5: return "SEVERITY_NUMBER_DEBUG";
            case 9: return "SEVERITY_NUMBER_INFO";
            case 13: return "SEVERITY_NUMBER_WARN";
            case 17: return "SEVERITY_NUMBER_ERROR";
            case 21: return "SEVERITY_NUMBER_FATAL";
            default: return "UNDEFINED_SEVERITY_NUMBER";
        }
    }
}
