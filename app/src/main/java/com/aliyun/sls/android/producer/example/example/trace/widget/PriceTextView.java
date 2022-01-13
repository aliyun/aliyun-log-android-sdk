package com.aliyun.sls.android.producer.example.example.trace.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.aliyun.sls.android.producer.example.R;

import java.util.Locale;

/**
 * @author gordon
 * @date 2021/10/18
 */
public class PriceTextView extends AppCompatTextView {

    private String prefixText;
    private String suffixText;

    public PriceTextView(@NonNull Context context) {
        super(context);
    }

    public PriceTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PriceTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PriceTextView);
        prefixText = a.getString(R.styleable.PriceTextView_prefixText);
        suffixText = a.getString(R.styleable.PriceTextView_suffixText);
        a.recycle();
    }

    public void setPrice(long price) {
        final String prefix = TextUtils.isEmpty(prefixText) ? "" : prefixText;
        if (TextUtils.isEmpty(suffixText)) {
            setText(String.format(Locale.CHINA, "%s￥%.2f", prefix, (float) (price / 100)));
        } else {
            setText(String.format(Locale.CHINA, "%s￥%.2f%s", prefix, (float) (price / 100), suffixText));
        }
    }


    public String getPrefixText() {
        return prefixText;
    }

    public void setPrefixText(String prefixText) {
        this.prefixText = prefixText;
    }

    public String getSuffixText() {
        return suffixText;
    }

    public void setSuffixText(String suffixText) {
        this.suffixText = suffixText;
    }
}
