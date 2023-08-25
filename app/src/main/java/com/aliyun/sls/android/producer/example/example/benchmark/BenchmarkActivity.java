package com.aliyun.sls.android.producer.example.example.benchmark;

import android.os.Bundle;
import android.util.Pair;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.aliyun.sls.android.producer.Log;
import com.aliyun.sls.android.producer.LogProducerClient;
import com.aliyun.sls.android.producer.LogProducerConfig;
import com.aliyun.sls.android.producer.LogProducerException;
import com.aliyun.sls.android.producer.R;
import com.aliyun.sls.android.producer.example.example.producer.LogUtils;
import com.aliyun.sls.android.producer.example.utils.PreferenceUtils;

/**
 * @author yulong.gyl
 * @date 2023/8/23
 */
@SuppressWarnings("ConstantConditions")
public class BenchmarkActivity extends AppCompatActivity {
    private static final int BENCHMARK_DURATION = 1 * 60;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_benchmark);
        findViewById(R.id.benchmark_mem_write_1).setOnClickListener(v -> benchmarkMem1s());
        findViewById(R.id.benchmark_mem_write_10).setOnClickListener(v -> benchmarkMem10s());
        findViewById(R.id.benchmark_mem_write_50).setOnClickListener(v -> benchmarkMem50s());
        findViewById(R.id.benchmark_mem_write_100).setOnClickListener(v -> benchmarkMem100s());
        findViewById(R.id.benchmark_mem_write_200).setOnClickListener(v -> benchmarkMem200s());
        findViewById(R.id.benchmark_file_write_1).setOnClickListener(v -> benchmarkFile1s());
        findViewById(R.id.benchmark_file_write_10).setOnClickListener(v -> benchmarkFile10s());
        findViewById(R.id.benchmark_file_write_50).setOnClickListener(v -> benchmarkFile50s());
        findViewById(R.id.benchmark_file_write_100).setOnClickListener(v -> benchmarkFile100s());
        findViewById(R.id.benchmark_file_write_200).setOnClickListener(v -> benchmarkFile200s());
    }

    private static void sleep(long start, long end, int wantSleep) {
        long diff = end - start;
        if (diff / 1000000 < wantSleep) {
            try {
                android.util.Log.d("sleep", wantSleep - diff / 1000000 + ", " + diff % 1000000);
                Thread.sleep(wantSleep - diff / 1000000, (int)(diff % 1000000));
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private static Log oneLog() {
        return LogUtils.createLog();
    }

    private Pair<LogProducerConfig, LogProducerClient> initLogProducer() {
        LogProducerConfig config;
        LogProducerClient client;
        try {
            // 使用默认参数初始化
            config = new LogProducerConfig(
                PreferenceUtils.getEndpoint(this),
                PreferenceUtils.getLogProject(this),
                PreferenceUtils.getLogStore(this),
                PreferenceUtils.getAccessKeyId(this),
                PreferenceUtils.getAccessKeyToken(this)
            );
            client = new LogProducerClient(config);
        } catch (LogProducerException e) {
            e.printStackTrace();
            return null;
        }

        return Pair.create(config, client);
    }

    private void benchmarkMem1s() {
        Pair<LogProducerConfig, LogProducerClient> pair = initLogProducer();

        // 测试持续一分钟的性能信息
        for (int i = 0; i < BENCHMARK_DURATION; i++) {
            long start = System.nanoTime();
            boolean succ = pair.second.addLog(oneLog()).isLogProducerResultOk();
            android.util.Log.d("benchmarkMem1s", "add log: " + succ);
            long end = System.nanoTime();
            if (!succ) {
                throw new RuntimeException("add log to producer error.");
            }

            sleep(start, end, 1000);
        }

        pair.second.destroyLogProducer();
    }

    private void benchmarkMem10s() {
        Pair<LogProducerConfig, LogProducerClient> pair = initLogProducer();

        pair.second.destroyLogProducer();
    }

    private void benchmarkMem50s() {
        Pair<LogProducerConfig, LogProducerClient> pair = initLogProducer();

        pair.second.destroyLogProducer();
    }

    private void benchmarkMem100s() {
        Pair<LogProducerConfig, LogProducerClient> pair = initLogProducer();

        pair.second.destroyLogProducer();
    }

    private void benchmarkMem200s() {
        Pair<LogProducerConfig, LogProducerClient> pair = initLogProducer();

        pair.second.destroyLogProducer();
    }

    private void benchmarkFile1s() {
        Pair<LogProducerConfig, LogProducerClient> pair = initLogProducer();

        pair.second.destroyLogProducer();
    }

    private void benchmarkFile10s() {
        Pair<LogProducerConfig, LogProducerClient> pair = initLogProducer();

        pair.second.destroyLogProducer();
    }

    private void benchmarkFile50s() {
        Pair<LogProducerConfig, LogProducerClient> pair = initLogProducer();

        pair.second.destroyLogProducer();
    }

    private void benchmarkFile100s() {
        Pair<LogProducerConfig, LogProducerClient> pair = initLogProducer();

        pair.second.destroyLogProducer();
    }

    private void benchmarkFile200s() {
        Pair<LogProducerConfig, LogProducerClient> pair = initLogProducer();

        pair.second.destroyLogProducer();
    }
}
