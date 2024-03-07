package com.aliyunsls.examples.network_diagnosis;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.aliyun.sls.android.core.SLSAndroid;
import com.aliyun.sls.android.core.configuration.Credentials;
import com.aliyun.sls.android.core.configuration.Credentials.NetworkDiagnosisCredentials;
import com.aliyun.sls.android.core.configuration.UserInfo;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.DnsRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.HttpRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.MtrRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.MtrRequest.Protocol;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.PingRequest;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.TcpPingRequest;
import com.aliyun.sls.android.network_diagnosis.NetworkDiagnosis;
import com.aliyun.sls.android.producer.LogProducerResult;

/**
 * @author yulong.gyl
 * @date 2024/3/7
 */
public class MainActivity extends Activity {
    private String accessKeyId = "";
    private String accessKeySecret = "";
    private String secretKey = "=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_init).setOnClickListener(v -> initSDK());
        findViewById(R.id.main_update).setOnClickListener(v -> updateSDK());
        findViewById(R.id.main_preinit).setOnClickListener(v -> preInitSDK());

        findViewById(R.id.main_ping).setOnClickListener(v -> ping());
        findViewById(R.id.main_tcpping).setOnClickListener(v -> tcpping());
        findViewById(R.id.main_dns).setOnClickListener(v -> dns());
        findViewById(R.id.main_mtr).setOnClickListener(v -> mtr());
        findViewById(R.id.main_http).setOnClickListener(v -> http());
    }

    private void initSDK() {
        Credentials credentials = new Credentials();
        // （必填）endpoint和project不支持动态更新，请在初始化SDK时指定
        credentials.endpoint = "https://cn-beijing.log.aliyuncs.com";
        credentials.project = "mobile-demo-beijing-b";

        // AccessKey建议通过STS方式获取，参考下面的文档：
        // https://help.aliyun.com/zh/sls/user-guide/build-a-service-to-upload-logs-from-mobile-apps-to-log-service
        // AccessKey在初始化时可以先不填，后续通过SLSAndroid.setCredentials(credentials);方法可以更新，参考后面的updateSDK方法实现
        credentials.accessKeyId = accessKeyId;
        credentials.accessKeySecret = accessKeySecret;
        //credentials.securityToken = "<your accessKey securityToekn>"; // 仅当AccessKey是通过STS服务获取时需要

        NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.getNetworkDiagnosisCredentials();
        // （必填）secretKey不支持动态更新，请在初始化时设置。
        networkDiagnosisCredentials.secretKey = secretKey;
        // （可选）设置业务扩展字段。仅对新产生的探测数据生效。
        // 不支持动态更新，请在SDK初始化时设置。
        networkDiagnosisCredentials.extension.put("custom_key", "custom_value");

        SLSAndroid.initialize(
            getApplicationContext(),
            credentials,
            configuration -> configuration.enableNetworkDiagnosis = true
        );

        // （建议）注册探测数据上报回调
        SLSAndroid.registerCredentialsCallback((feature, result) -> {
            if (LogProducerResult.LOG_PRODUCER_SEND_UNAUTHORIZED == result ||
                LogProducerResult.LOG_PRODUCER_PARAMETERS_INVALID == result) {
                // 处理AccessKey过期、失效等鉴权问题
                updateSDK();
            }
        });

        // （可选）设置设备ID，可在任何时机调用，仅对新产生的探测数据生效。
        SLSAndroid.setUtdid(getApplicationContext(), "<your device id");

        // （可选）配置用户信息，可在任何时机调用。仅对新产生的探测数据生效。
        UserInfo userInfo = new UserInfo();
        userInfo.uid = "<your user id>";
        userInfo.channel = "<your user channel>";
        userInfo.addExt("ext_key", "ext_value");
        SLSAndroid.setUserInfo(userInfo);
    }

    private void updateSDK() {
        Credentials credentials = new Credentials();
        credentials.accessKeyId = "";
        credentials.accessKeySecret = "";
        credentials.securityToken = "";

        SLSAndroid.setCredentials(credentials);
    }

    private void preInitSDK() {
        Credentials credentials = new Credentials();
        // （必填）endpoint和project不支持动态更新，请在初始化SDK时指定
        credentials.endpoint = "https://cn-beijing.log.aliyuncs.com";
        credentials.project = "mobile-demo-beijing-b";

        // AccessKey建议通过STS方式获取，参考下面的文档：
        // https://help.aliyun.com/zh/sls/user-guide/build-a-service-to-upload-logs-from-mobile-apps-to-log-service
        // AccessKey在初始化时可以先不填，后续通过SLSAndroid.setCredentials(credentials);方法可以更新，参考后面的updateSDK方法实现
        credentials.accessKeyId = accessKeyId;
        credentials.accessKeySecret = accessKeySecret;
        //credentials.securityToken = "<your accessKey securityToekn>"; // 仅当AccessKey是通过STS服务获取时需要

        NetworkDiagnosisCredentials networkDiagnosisCredentials = credentials.getNetworkDiagnosisCredentials();
        // （必填）secretKey不支持动态更新，请在初始化时设置。
        networkDiagnosisCredentials.secretKey = secretKey;
        // （可选）设置业务扩展字段。仅对新产生的探测数据生效。
        // 不支持动态更新，请在SDK初始化时设置。
        networkDiagnosisCredentials.extension.put("custom_key", "custom_value");

        // 用户接受隐私协议之前，先调用preInit完成SDK初始化。
        SLSAndroid.preInit(
            getApplicationContext(),
            credentials,
            configuration -> configuration.enableNetworkDiagnosis = true
        );

        // 用户接受隐私协议之后，再调用initialize完成SDK的完整初始化。
        SLSAndroid.initialize(
            getApplicationContext(),
            credentials,
            configuration -> configuration.enableNetworkDiagnosis = true
        );

        // （建议）注册探测数据上报回调
        SLSAndroid.registerCredentialsCallback((feature, result) -> {
            if (LogProducerResult.LOG_PRODUCER_SEND_UNAUTHORIZED == result ||
                LogProducerResult.LOG_PRODUCER_PARAMETERS_INVALID == result) {
                // 处理AccessKey过期、失效等鉴权问题
                updateSDK();
            }
        });

        // （可选）设置设备ID，可在任何时机调用，仅对新产生的探测数据生效。
        SLSAndroid.setUtdid(getApplicationContext(), "<your device id");

        // （可选）配置用户信息，可在任何时机调用。仅对新产生的探测数据生效。
        UserInfo userInfo = new UserInfo();
        userInfo.uid = "<your user id>";
        userInfo.channel = "<your user channel>";
        userInfo.addExt("ext_key", "ext_value");
        SLSAndroid.setUserInfo(userInfo);
    }

    private void ping() {
        PingRequest request = new PingRequest();
        // （必填）域名
        request.domain = "www.aliyun.com";

        // （可选）context id，在探测结果回调中会返回该值
        request.context = "context id";
        // （可选）探测超时时间, 默认2000ms
        request.timeout = 3000;
        // （可选）最大探测次数，默认10次
        request.maxTimes = 10;
        // （可选）探测包大小，默认64
        request.size = 64;
        // （可选）探测扩展参数
        request.extension = new HashMap<>();
        request.extension.put("ext_key", "ext_value");

        NetworkDiagnosis.getInstance().ping(request, response -> {
            Log.d("network_diagnosis", "ping result type: " + response.type);
            Log.d("network_diagnosis", "ping result content: " + response.content);
            Log.d("network_diagnosis", "ping result content: " + response.content);
            Log.d("network_diagnosis", "ping result error: " + response.error);
        });
    }

    private void tcpping() {
        TcpPingRequest request = new TcpPingRequest();
        // （必填）域名
        request.domain = "www.aliyun.com";
        request.port = 80;

        // （可选）context id，在探测结果回调中会返回该值
        request.context = "context id";
        // （可选）探测超时时间, 默认2000ms
        request.timeout = 3000;
        // （可选）最大探测次数，默认10次
        request.maxTimes = 10;
        // （可选）探测包大小，默认64
        request.size = 64;
        // （可选）探测扩展参数
        request.extension = new HashMap<>();
        request.extension.put("ext_key", "ext_value");

        NetworkDiagnosis.getInstance().tcpPing(request, response -> {
            Log.d("network_diagnosis", "tcp ping result type: " + response.type);
            Log.d("network_diagnosis", "tcp ping result content: " + response.content);
            Log.d("network_diagnosis", "tcp ping result content: " + response.content);
            Log.d("network_diagnosis", "tcp ping result error: " + response.error);
        });
    }

    private void dns() {
        DnsRequest request = new DnsRequest();
        // （必填）域名
        request.domain = "www.aliyun.com";

        // （可选）context id，在探测结果回调中会返回该值
        request.context = "context id";
        // （可选）
        // IP类型，取值为：
        // IPv4：A
        // IPv6：AAAA
        // 默认为A，即IPv4
        request.type = "A";
        // （可选）域名解析服务，默认为null
        request.nameServer = "114.114.114.114";
        // （可选）探测超时时间, 默认2000ms
        request.timeout = 3000;
        // （可选）最大探测次数，默认10次
        request.maxTimes = 10;
        // （可选）探测包大小，默认64
        request.size = 64;
        // （可选）探测扩展参数
        request.extension = new HashMap<>();
        request.extension.put("ext_key", "ext_value");

        NetworkDiagnosis.getInstance().dns(request, response -> {
            Log.d("network_diagnosis", "dns result type: " + response.type);
            Log.d("network_diagnosis", "dns result content: " + response.content);
            Log.d("network_diagnosis", "dns result content: " + response.content);
            Log.d("network_diagnosis", "dns result error: " + response.error);
        });
    }

    private void mtr() {
        MtrRequest request = new MtrRequest();
        // （必填）域名
        request.domain = "www.aliyun.com";

        // （可选）context id，在探测结果回调中会返回该值
        request.context = "context id";
        // （可选）最大ttl，默认为30
        request.maxTTL = 30;
        // （可选）最大path，默认为1
        request.maxPaths = 1;
        // （可选）
        // 探测协议，取值为：
        // 全部：Protocol.ALL
        // ICMP：Protocol.ICMP
        // UDP：Protocol.UDP
        // 默认为 Protocol.ALL
        request.protocol = Protocol.ICMP;
        // （可选）探测超时时间, 默认2000ms
        request.timeout = 3000;
        // （可选）最大探测次数，默认10次
        request.maxTimes = 10;
        // （可选）探测包大小，默认64
        request.size = 64;
        // （可选）探测扩展参数
        request.extension = new HashMap<>();
        request.extension.put("ext_key", "ext_value");

        NetworkDiagnosis.getInstance().mtr(request, response -> {
            Log.d("network_diagnosis", "mtr result type: " + response.type);
            Log.d("network_diagnosis", "mtr result content: " + response.content);
            Log.d("network_diagnosis", "mtr result content: " + response.content);
            Log.d("network_diagnosis", "mtr result error: " + response.error);
        });
    }

    private void http() {
        HttpRequest request = new HttpRequest();
        // （必填）域名
        request.domain = "https://www.aliyun.com";

        // （可选）context id，在探测结果回调中会返回该值
        request.context = "context id";
        // （可选）探测超时时间, 默认2000ms
        request.timeout = 3000;
        // （可选）下载内容大小限制，默认为不限制
        request.downloadBytesLimit = 1024;
        // （可选）是否只请求header，默认为YES
        request.headerOnly = true;
        // （可选）探测扩展参数
        request.extension = new HashMap<>();
        request.extension.put("ext_key", "ext_value");

        NetworkDiagnosis.getInstance().http(request, response -> {
            Log.d("network_diagnosis", "http result type: " + response.type);
            Log.d("network_diagnosis", "http result content: " + response.content);
            Log.d("network_diagnosis", "http result content: " + response.content);
            Log.d("network_diagnosis", "http result error: " + response.error);
        });
    }
}
