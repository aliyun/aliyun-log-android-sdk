package com.aliyun.sls.android.producer.example.example.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import com.aliyun.sls.android.plugin.network_diagnosis.SLSNetDiagnosis;
import com.aliyun.sls.android.plugin.network_diagnosis.SLSNetPolicyBuilder;
import com.aliyun.sls.android.producer.example.BaseActivity;
import com.aliyun.sls.android.producer.example.R;

/**
 * @author gordon
 * @date 2021/08/26
 */
public class NetworkWithPolicy extends BaseActivity {
    private static final String TAG = "NetworkExample";
    private List<String> endpoints = new ArrayList<String>() {
        {
            add("cn-hangzhou.log.aliyuncs.com");
            add("cn-hangzhou-finance.log.aliyuncs.com");
            add("cn-shanghai.log.aliyuncs.com");
            add("cn-shanghai-finance-1.log.aliyuncs.com");
            add("cn-qingdao.log.aliyuncs.com");
            add("cn-beijing.log.aliyuncs.com");
            add("cn-north-2-gov-1.log.aliyuncs.com");
            add("cn-zhangjiakou.log.aliyuncs.com");
            add("cn-huhehaote.log.aliyuncs.com");
            add("cn-wulanchabu.log.aliyuncs.com");
            add("cn-shenzhen.log.aliyuncs.com");
            add("cn-shenzhen-finance.log.aliyuncs.com");
            add("cn-heyuan.log.aliyuncs.com");
            add("cn-guangzhou.log.aliyuncs.com");
            add("cn-chengdu.log.aliyuncs.com");
            add("cn-hongkong.log.aliyuncs.com");
            add("ap-northeast-1.log.aliyuncs.com");
            add("ap-southeast-1.log.aliyuncs.com");
            add("ap-southeast-2.log.aliyuncs.com");
            add("ap-southeast-3.log.aliyuncs.com");
            add("ap-southeast-6.log.aliyuncs.com");
            add("ap-southeast-5.log.aliyuncs.com");
            add("me-east-1.log.aliyuncs.com");
            add("us-west-1.log.aliyuncs.com");
            add("eu-central-1.log.aliyuncs.com");
            add("us-east-1.log.aliyuncs.com");
            add("ap-south-1.log.aliyuncs.com");
            add("eu-west-1.log.aliyuncs.com");
        }
    };
    private int index = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_with_policy_example);
        findViewById(R.id.example_network_policy_setup_text).setOnClickListener(v -> setup());
        SLSNetDiagnosis.getInstance().registerCallback(
            (type, result) -> printStatus(String.format("type: %s, result: %s", type.type, result))
        );
    }

    private void setup() {
        SLSNetPolicyBuilder builder = new SLSNetPolicyBuilder();
        // 默认开启
        builder.setEnable(getBooleanFromEdit(R.id.example_network_policy_enable_edit, true));
        // 默认为 ""
        builder.setType(getStringFromEdit(R.id.example_network_policy_type_edit, ""));
        // 默认为1
        builder.setVersion(getIntFromEdit(R.id.example_network_policy_version_edit, 1));
        // 默认为true，周期性策略
        builder.setPeriodicity(getBooleanFromEdit(R.id.example_network_policy_periodicity_edit, true));
        // 默认3分钟
        builder.setInterval(getIntFromEdit(R.id.example_network_policy_interval_edit, 30));
        // 默认7天
        builder.setExpiration(getLongFromEdit(R.id.example_network_policy_expiration_edit, System.currentTimeMillis() / 1000 + 300));
        // 默认1000，即全量
        builder.setRatio(getIntFromEdit(R.id.example_network_policy_ratio_edit, 1000));
        String whiteList = getStringFromEdit(R.id.example_network_policy_whitelist_edit, "");
        if (!TextUtils.isEmpty(whiteList)) {
            builder.setWhiteList(Arrays.asList(whiteList.split(",")));
        }
        String methods = getStringFromEdit(R.id.example_network_policy_method_edit, "mtr,ping,tcpping,http");
        builder.setMethods(Arrays.asList(methods.split(",")));

        String ips = getStringFromEdit(R.id.example_network_policy_ips_edit, "");
        String urls = getStringFromEdit(R.id.example_network_policy_url_edit, "");
        if (TextUtils.isEmpty(urls)) {
            StringBuilder urlsBuilder = new StringBuilder();
            for (String ept : endpoints) {
                urlsBuilder.append("https://").append(ept);
                urlsBuilder.append(",");
            }
            urls = urlsBuilder.toString();
        }
        builder.addDestination(TextUtils.isEmpty(ips) ? endpoints : Arrays.asList(ips.split(",")), Arrays.asList(urls.split(",")));

        SLSNetDiagnosis.getInstance().registerPolicy(builder);
        printStatus("setup policy");
    }

    private boolean getBooleanFromEdit(int id, boolean def) {
        EditText editText = findViewById(id);

        if (TextUtils.isEmpty(editText.getText())) {
            return def;
        }
        return Boolean.parseBoolean(editText.getText().toString());
    }

    private String getStringFromEdit(int id, String def) {
        EditText editText = findViewById(id);

        if (TextUtils.isEmpty(editText.getText())) {
            return def;
        }
        return editText.getText().toString();
    }

    private int getIntFromEdit(int id, int def) {
        EditText editText = findViewById(id);

        if (TextUtils.isEmpty(editText.getText())) {
            return def;
        }
        try {
            return Integer.parseInt(editText.getText().toString());
        } catch (Throwable e) {
            return def;
        }
    }

    private long getLongFromEdit(int id, long def) {
        EditText editText = findViewById(id);

        if (TextUtils.isEmpty(editText.getText())) {
            return def;
        }
        try {
            return Long.parseLong(editText.getText().toString());
        } catch (Throwable e) {
            return def;
        }
    }

}
