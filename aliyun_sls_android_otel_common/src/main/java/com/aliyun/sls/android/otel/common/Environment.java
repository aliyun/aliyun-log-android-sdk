package com.aliyun.sls.android.otel.common;

/**
 * @author yulong.gyl
 * @date 2023/9/14
 */
public class Environment {
    private String env = "default";
    private String uid;
    private String utdid;
    private String channel = "default";

    private Environment() {
    }

    public static Environment environment() {
        return new Environment();
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUtdid() {
        return utdid;
    }

    public void setUtdid(String utdid) {
        this.utdid = utdid;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
