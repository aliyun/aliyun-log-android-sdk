package com.aliyun.sls.android.producer.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.sls.android.producer.example.example.crash.CrashExampleActivity;
import com.aliyun.sls.android.producer.example.example.network.NetworkExample;
import com.aliyun.sls.android.producer.example.example.producer.ProducerExample;
import com.aliyun.sls.android.producer.example.example.producer.ProducerWebTracking;
import com.aliyun.sls.android.producer.example.example.producer.ProducerWithDestroy;
import com.aliyun.sls.android.producer.example.example.producer.ProducerWithDynamicConfig;
import com.aliyun.sls.android.producer.example.example.producer.ProducerWithImmediately;
import com.aliyun.sls.android.producer.example.example.producer.ProducerWithMultiClients;
import com.aliyun.sls.android.producer.example.example.producer.ProducerWithNoPersistent;
import com.aliyun.sls.android.producer.example.utils.PreferenceUtils;

/**
 * @author gordon
 * @date 2021/08/18
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewById(R.id.main_producer_basic).setOnClickListener(this);
        findViewById(R.id.main_producer_persistent).setOnClickListener(this);
        findViewById(R.id.main_producer_dynamic_config).setOnClickListener(this);
        findViewById(R.id.main_producer_multi_clients).setOnClickListener(this);
        findViewById(R.id.main_producer_immediately).setOnClickListener(this);
        findViewById(R.id.main_producer_destroy).setOnClickListener(this);
        findViewById(R.id.main_producer_webtracking).setOnClickListener(this);
        findViewById(R.id.main_apm_crash).setOnClickListener(this);
        findViewById(R.id.main_network_diagnosis).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (R.id.main_producer_basic == v.getId()) {
            // 基本配置
            startActivity(ProducerExample.class);
        } else if (R.id.main_producer_persistent == v.getId()) {
            // 不带缓存的配置
            startActivity(ProducerWithNoPersistent.class);
        } else if (R.id.main_producer_dynamic_config == v.getId()) {
            // 动态配置
            startActivity(ProducerWithDynamicConfig.class);
        } else if (R.id.main_producer_multi_clients == v.getId()) {
            // 多 client（即：多个不同的 logstore）
            startActivity(ProducerWithMultiClients.class);
        } else if (R.id.main_producer_immediately == v.getId()) {
            // 多 client（即：多个不同的 logstore）
            startActivity(ProducerWithImmediately.class);
        } else if (R.id.main_producer_destroy == v.getId()) {
            // 销毁LogProducerClient
            startActivity(ProducerWithDestroy.class);
        } else if (R.id.main_producer_webtracking == v.getId()) {
            // 销毁LogProducerClient
            startActivity(ProducerWebTracking.class);
        } else if (R.id.main_apm_crash == v.getId()) {
            // 崩溃监控
            startActivity(CrashExampleActivity.class);
        } else if (R.id.main_network_diagnosis == v.getId()) {
            // 网络探测
            startActivity(NetworkExample.class);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.example_menus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.example_settings) {
            SettingsActivity.start(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startActivity(Class<? extends Activity> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }
}
