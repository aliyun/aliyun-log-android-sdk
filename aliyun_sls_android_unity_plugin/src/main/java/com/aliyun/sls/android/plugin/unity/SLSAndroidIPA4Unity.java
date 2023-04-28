package com.aliyun.sls.android.plugin.unity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.util.Log;
import com.aliyun.sls.android.core.SLSAndroid;
import com.aliyun.sls.android.core.configuration.Configuration;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.configuration.Credentials.NetworkDiagnosisCredentials;
import com.aliyun.sls.android.core.configuration.UserInfo;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.DnsRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.HttpRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.MtrRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.PingRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.TcpPingRequest;
import com.aliyun.sls.android.network_diagnosis.NetworkDiagnosis;

/**
 * @author gordon
 * @date 2023/4/27
 */
@SuppressWarnings("unused")
public class SLSAndroidIPA4Unity {
    private static final AtomicBoolean hasInit = new AtomicBoolean(false);
    private static Configuration configuration;

    private static Activity getCurrentActivity() {
        try {
            Object object = Reflection.getStaticField("com.unity3d.player.UnityPlayer", "currentActivity", null);
            if (object instanceof Activity) {return (Activity)object;}
        } catch (Exception exception) {
            Log.w("SLSAndroidAgent", "Failed to get the current activity from UnityPlayer");
            exception.printStackTrace();
        }
        return null;
    }

    public static void initialize(NetworkDiagnosisCredentials networkDiagnosisCredentials) {
        if (hasInit.get()) {
            return;
        }
        final Activity activity = getCurrentActivity();
        if (null == activity) {
            return;
        }

        Credentials credentials = new Credentials();
        credentials.endpoint = networkDiagnosisCredentials.endpoint;
        credentials.project = networkDiagnosisCredentials.project;

        credentials.accessKeyId = networkDiagnosisCredentials.accessKeyId;
        credentials.accessKeySecret = networkDiagnosisCredentials.accessKeySecret;
        credentials.securityToken = networkDiagnosisCredentials.securityToken;

        credentials.networkDiagnosisCredentials = networkDiagnosisCredentials;

        SLSAndroid.initialize(activity.getApplicationContext(), credentials, configuration -> {
            SLSAndroidIPA4Unity.configuration = configuration;
            configuration.enableNetworkDiagnosis = true;
        });
        hasInit.set(true);
    }

    public static void registerCredentialsCallback(CredentialsCallback callback) {
        SLSAndroid.registerCredentialsCallback((feature, result) -> {
            if (null != callback) {
                callback.onCall(feature, result.name());
            }
        });
    }

    public static void setLogLevel(int level) {
        SLSAndroid.setLogLevel(level);
    }

    public static void setCredentials(Credentials credentials) {
        SLSAndroid.setCredentials(credentials);
    }

    public static void setUserInfo(UserInfo info) {
        SLSAndroid.setUserInfo(info);
    }

    public static void setUserInfoExt(Map<String, String> ext) {
        if (null == ext) {
            return;
        }

        if (null == configuration || null == configuration.userInfo) {
            return;
        }

        for (Entry<String, String> entry : ext.entrySet()) {
            configuration.userInfo.addExt(entry.getKey(), entry.getValue());
        }
    }

    public static void setExtra(String key, Map<String, String> values) {
        SLSAndroid.setExtra(key, values);
    }

    public static void setExtra(String key, String value) {
        SLSAndroid.setExtra(key, value);
    }

    public static void removeExtra(String key) {
        SLSAndroid.removeExtra(key);
    }

    public static void clearExtra() {
        SLSAndroid.clearExtra();
    }

    public static void http(HttpRequest request, CompleteCallback callback) {
        NetworkDiagnosis.getInstance().http(request, response -> {
            if (null != callback) {
                callback.onComplete(response.type.ordinal(), response.content,
                    null != response.context ? (String)response.context : "",
                    null != response.error ? response.error : "");
            }
        });
    }

    public static void ping(PingRequest request, CompleteCallback callback) {
        NetworkDiagnosis.getInstance().ping(request, response -> {
            if (null != callback) {
                callback.onComplete(response.type.ordinal(), response.content,
                    null != response.context ? (String)response.context : "",
                    null != response.error ? response.error : "");
            }
        });
    }

    public static void tcpping(TcpPingRequest request, CompleteCallback callback) {
        NetworkDiagnosis.getInstance().tcpPing(request, response -> {
            if (null != callback) {
                callback.onComplete(response.type.ordinal(), response.content,
                    null != response.context ? (String)response.context : "",
                    null != response.error ? response.error : "");
            }
        });
    }

    public static void dns(DnsRequest request, CompleteCallback callback) {
        NetworkDiagnosis.getInstance().dns(request, response -> {
            if (null != callback) {
                callback.onComplete(response.type.ordinal(), response.content,
                    null != response.context ? (String)response.context : "",
                    null != response.error ? response.error : "");
            }
        });
    }

    public static void mtr(MtrRequest request, CompleteCallback callback) {
        NetworkDiagnosis.getInstance().mtr(request, response -> {
            if (null != callback) {
                callback.onComplete(response.type.ordinal(), response.content,
                    null != response.context ? (String)response.context : "",
                    null != response.error ? response.error : "");
            }
        });
    }

    public static void disableExNetworkInfo() {
        NetworkDiagnosis.getInstance().disableExNetworkInfo();
    }

    public static void setMultiplePortsDetect(boolean enable) {
        NetworkDiagnosis.getInstance().setMultiplePortsDetect(enable);
    }

    public static void setPolicyDomain(String domain) {
        NetworkDiagnosis.getInstance().setPolicyDomain(domain);
    }

    public static void registerCallback(CompleteCallback callback) {
        NetworkDiagnosis.getInstance().registerCallback(response -> {
            if (null != callback) {
                callback.onComplete(response.type.ordinal(), response.content,
                    null != response.context ? (String)response.context : "",
                    null != response.error ? response.error : "");
            }
        });
    }

    public static void updateExtensions(Map<String, String> extension) {
        NetworkDiagnosis.getInstance().updateExtensions(extension);
    }

    public String helloFromAndroid(String appid) {
        return "hello from android, appid: " + appid;
    }

}
