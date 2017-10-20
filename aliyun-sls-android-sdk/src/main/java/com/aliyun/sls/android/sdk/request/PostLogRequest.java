package com.aliyun.sls.android.sdk.request;

import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.LogGroup;
import com.aliyun.sls.android.sdk.OSSRequest;
import com.aliyun.sls.android.sdk.common.utils.OSSUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Deflater;

/**
 * Created by wangzheng on 2017/10/11.
 */

public class PostLogRequest extends OSSRequest{

    //保存 log 的 project
    public String mProject;
    //保存 log 的 logstore
    public String mLogStoreName;
    //log 内容
    public LogGroup mLogGroup;

    public PostLogRequest(String project, String logStoreName, LogGroup logGroup){
        mProject = project;
        mLogStoreName = logStoreName;
        mLogGroup = logGroup;
    }

}
