package com.aliyun.sls.android.producer;

/**
 * @author yulong.gyl
 * @date 2023/12/13
 */
public class LogHttpResponse {
    private int statusCode;
    private String requestId = "";
    private String errorMessage = "";

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
