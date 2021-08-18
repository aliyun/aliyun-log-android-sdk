package com.aliyun.sls.android.producer.example.example.crash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.sls.android.SLSAdapter;
import com.aliyun.sls.android.SLSConfig;
import com.aliyun.sls.android.plugin.crashreporter.SLSCrashReporterPlugin;
import com.aliyun.sls.android.producer.example.R;
import com.aliyun.sls.android.producer.example.utils.PreferenceUtils;
import com.uc.crashsdk.JNIBridge;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author gordon
 * @date 2021/07/26
 */
public class CrashExampleActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CrashExample";

    public static void start(Context context) {
        Intent starter = new Intent(context, CrashExampleActivity.class);
        context.startActivity(starter);
    }

    private List<FileInputStream> mFiles = new ArrayList<FileInputStream>();
    private List<byte[]> mMems = new ArrayList<byte[]>(4096);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SLSConfig config = new SLSConfig(this);
        config.endpoint = PreferenceUtils.getEndpoint(this);
        config.pluginLogproject = PreferenceUtils.getLogProject(this);
        config.pluginAppId = "sls-7011bc6fb3af0244d8";
        config.debuggable = true;
        config.accessKeyId = PreferenceUtils.getAccessKeyId(this);
        config.accessKeySecret = PreferenceUtils.getAccessKeySecret(this);
        config.securityToken = PreferenceUtils.getAccessKeyToken(this);

        SLSAdapter adapter = SLSAdapter.getInstance();
        adapter.addPlugin(new SLSCrashReporterPlugin());
        adapter.init(config);

        setContentView(R.layout.activity_crash_example);

        // register onClick() event
        int[] btnIds = new int[] {
                R.id.java_null_ptr, R.id.java_oom, R.id.java_fd_leak,
                R.id.java_class_cast, R.id.java_number_format, R.id.java_out_of_bounds,
                R.id.native_crash, R.id.native_heap_corruption, R.id.native_fd_leak,
                R.id.native_abort, R.id.native_stack_overflow, R.id.native_oom,
                R.id.unexp_kill_process, R.id.unexp_exit, R.id.unexp_anr,
        };
        for (int btnId : btnIds) {
            findViewById(btnId).setOnClickListener(this);
        }
    }


    @Override
    public void onClick(View view) {
        doCrash(view.getId());
    }

    public void doCrash(int id) {
        switch (id) {
            case R.id.java_null_ptr:
                crashInJavaNull();
                break;

            case R.id.java_oom:
                javaOOMCrash();
                break;

            case R.id.java_fd_leak:
                useOutAllFileHandles();
                crashInJavaNull();
                break;

            case R.id.java_class_cast:
                crashInJavaClassCast();
                break;

            case R.id.java_number_format:
                crashInJavaNumberFormat();
                break;

            case R.id.java_out_of_bounds:
                crashInJavaOutOfBounds();
                break;

            case R.id.native_crash:
                nativeCrash(0);
                break;

            case R.id.native_heap_corruption:
                nativeCrash(1);
                break;

            case R.id.native_fd_leak:
                useOutAllFileHandles();
                nativeCrash(0);
                break;

            case R.id.native_abort:
                nativeCrash(2);
                break;

            case R.id.native_stack_overflow:
                nativeCrash(3);
                break;

            case R.id.native_oom:
                nativeCrash(5);
                break;

            case R.id.unexp_kill_process:
                android.os.Process.killProcess(android.os.Process.myPid());
                break;

            case R.id.unexp_exit:
                nativeCrash(4);
                break;

            case R.id.unexp_anr:
                while (true) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            default:
                break;
        }
    }

    private void useOutAllFileHandles() {
        for (int i = 0; i < 2048; ++i) {
            try {
                mFiles.add(new FileInputStream("/dev/null"));
            } catch (Exception e) {
                e.printStackTrace();
                Log.v(TAG, "!!!!! too many open files!");
                break;
            }
        }
    }

    private void nativeCrash(int type) {
        JNIBridge.nativeCrash(type, 0);
    }

    private void crashInJavaNull() {
        String nullStr = "1";
        if (nullStr.equals("1")) {
            nullStr = null;
        }
        nullStr.equals("");
    }

    private void crashInJavaClassCast() {
        View view = new View(this);
        TextView text = (TextView)view;
    }

    private void crashInJavaNumberFormat() {
        int num = Integer.parseInt("1.1f");
    }

    private void crashInJavaOutOfBounds() {
        new ArrayList<>(10).get(11);
    }

    private void javaOOMCrash() {
        final int kInitSize = 10 * 1024 * 1024;
        final int kMinSize = 1024;

        int size = kInitSize;
        int totalAllocSize = 0;
        while (size > 0) {
            try {
                byte[] mem = new byte[size];
                for (int i = 1; i < size; i += 4096) {
                    mem[i] = (byte)i;
                }
                mMems.add(mem);
                totalAllocSize += size;
            } catch (OutOfMemoryError t) {
                if (size < kMinSize) {
                    Log.w(TAG, String.format(Locale.US,
                            "=Total %d bytes", totalAllocSize));
                    throw t;
                }
                size /= 2;
            }
        }
        // Crash what ever
        byte[] mem = new byte[kInitSize];
        mMems.add(mem);
    }
}
