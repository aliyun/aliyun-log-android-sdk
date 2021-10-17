package com.aliyun.sls.android.producer.example.example.trace.ui.order.detail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.aliyun.sls.android.producer.example.R;
import com.aliyun.sls.android.producer.example.example.trace.ui.detail.DetailFragment;

public class DetailActivity extends AppCompatActivity {

    public static void start(Context context, String id) {
        Intent starter = new Intent(context, DetailActivity.class);
        starter.putExtra("item_id", id);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, DetailFragment.newInstance(getIntent().getStringExtra("item_id")))
                    .commitNow();
        }
    }
}