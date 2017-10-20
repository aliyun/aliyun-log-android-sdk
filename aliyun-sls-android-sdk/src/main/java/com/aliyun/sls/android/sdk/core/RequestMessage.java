package com.aliyun.sls.android.sdk.core;


import com.aliyun.sls.android.sdk.core.http.HttpMethod;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by zhouzhuo on 11/22/15.
 */
public class RequestMessage {

    public Map<String, String> headers = new HashMap<String, String>();


    public HttpMethod method;

    public String url;

    //可以提供字节流，文件，内存数据块等方式直接上传
    private byte[] uploadData;
    private String uploadFilePath;
    private InputStream uploadInputStream;
    private long readStreamLength;


    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public byte[] getUploadData() {
        return uploadData;
    }

    public void setUploadData(byte[] uploadData) {
        this.uploadData = uploadData;
    }

    public String getUploadFilePath() {
        return uploadFilePath;
    }

    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public void setUploadInputStream(InputStream uploadInputStream, long inputLength) {
        if (uploadInputStream != null) {
            this.uploadInputStream = uploadInputStream;
            this.readStreamLength = inputLength;
        }
    }

    public InputStream getUploadInputStream() {
        return uploadInputStream;
    }


    public long getReadStreamLength() {
        return readStreamLength;
    }

}
