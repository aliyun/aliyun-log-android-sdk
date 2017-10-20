package com.aliyun.sls.android.sdk.request;

import com.aliyun.sls.android.sdk.model.LogGroup;
import com.aliyun.sls.android.sdk.core.Request;

/**
 * Created by wangzheng on 2017/10/11.
 */

public class PostLogRequest extends Request {

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
