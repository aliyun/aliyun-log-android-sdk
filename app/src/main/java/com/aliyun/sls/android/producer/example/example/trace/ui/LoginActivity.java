package com.aliyun.sls.android.producer.example.example.trace.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.aliyun.sls.android.producer.example.R;
import com.aliyun.sls.android.producer.example.example.trace.ui.login.LoginFragment;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, LoginFragment.newInstance())
                    .commitNow();
        }
    }
}