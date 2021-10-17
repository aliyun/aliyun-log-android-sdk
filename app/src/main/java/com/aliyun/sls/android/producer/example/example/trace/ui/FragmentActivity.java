package com.aliyun.sls.android.producer.example.example.trace.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aliyun.sls.android.producer.example.R;
import com.aliyun.sls.android.producer.example.example.trace.ui.order.OrderListFragment;

/**
 * @author gordon
 * @date 2021/10/17
 */
public class FragmentActivity extends AppCompatActivity {

    public static void start(Context context, String fragmentClassName, Bundle extras) {
        Intent starter = new Intent(context, FragmentActivity.class);
        starter.putExtra("fragment_class_name", fragmentClassName);
        if (null != extras) {
            starter.putExtras(extras);
        }
        context.startActivity(starter);
    }

    public static void startOrderListPage(Context context) {
        FragmentActivity.start(context, OrderListFragment.class.getName(), null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        if (savedInstanceState == null && null != getIntent()) {
            final Intent intent = getIntent();
            final FragmentManager fm = getSupportFragmentManager();
            final Fragment target = fm.getFragmentFactory().instantiate(getClassLoader(), intent.getStringExtra("fragment_class_name"));
            target.setArguments(intent.getExtras());

            fm.beginTransaction()
                    .replace(R.id.container, target)
                    .commitNow();
        }
    }
}
