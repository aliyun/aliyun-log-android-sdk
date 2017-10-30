package com.aliyun.sls.android.sdk;

/**
 * Created by Lenovo on 2016/9/22.
 */
public class LogException extends Exception {
    private static final long serialVersionUID = -3451945810203597732L;

    private String errorCode;

    private String requestId;

    public Boolean canceled = false;

    public int responseCode = -1111;

    public LogException(String code, String message, String requestId) {
        super(message);
        this.errorCode = code;
        this.requestId = requestId;
    }

    public LogException(String code, String message, Throwable cause,
                        String requestId) {
        super(message, cause);
        this.errorCode = code;
        this.requestId = requestId;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return super.getMessage();
    }

    /**
     * Get the request id
     *
     * @return request id, if the error is happened in the client, the request
     * id is empty
     */
    public String getRequestId() {
        return this.requestId;
    }
}
