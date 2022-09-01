package com.aliyun.sls.android.producer.example.example.trace.ui.core;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.aliyun.sls.android.plugin.trace.SLSTracePlugin;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

/**
 * @author gordon
 */
public class VisibilityFragment extends BaseFragment implements View.OnAttachStateChangeListener, OnFragmentVisibilityChangedListener {
    private static final String TAG = "VisibilityFragment";
    /**
     * ParentActivity是否可见
     */
    private boolean parentActivityVisible = false;

    /**
     * 是否可见（Activity处于前台、Tab被选中、Fragment被添加、Fragment没有隐藏、Fragment.View已经Attach）
     */
    private boolean visible = false;

    private VisibilityFragment localParentFragment;
    private final List<OnFragmentVisibilityChangedListener> listeners = new ArrayList<>();
    protected Tracer tracer = SLSTracePlugin.getInstance().getSLSTelemetry().getTracer(this.getClass().getSimpleName());
    private Span pageSpan;
    private Scope scope;

    public void addOnVisibilityChangedListener(OnFragmentVisibilityChangedListener listener) {
        listeners.add(listener);
    }

    public void removeOnVisibilityChangedListener(OnFragmentVisibilityChangedListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        info("onAttach");
        super.onAttach(context);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof VisibilityFragment) {
            this.localParentFragment = (VisibilityFragment) parentFragment;
            localParentFragment.addOnVisibilityChangedListener(this);
        }
        checkVisibility(true);
    }

    @Override
    public void onDetach() {
        info("onDetach");
        if (null != localParentFragment) {
            localParentFragment.removeOnVisibilityChangedListener(this);
        }
        super.onDetach();
        checkVisibility(false);
        localParentFragment = null;
    }

    @Override
    public void onResume() {
        info("onResume");
        super.onResume();
        onActivityVisibilityChanged(true);
    }

    @Override
    public void onPause() {
        info("onPause");
        super.onPause();
        onActivityVisibilityChanged(false);
    }

    /**
     * ParentActivity可见性改变
     */
    protected void onActivityVisibilityChanged(boolean visible) {
        parentActivityVisible = visible;
        checkVisibility(visible);
    }

    /**
     * ParentFragment可见性改变
     */
    @Override
    public void onFragmentVisibilityChanged(boolean visible) {
        checkVisibility(visible);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        info("onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 处理直接 replace 的 case
        view.addOnAttachStateChangeListener(this);
    }

    /**
     * 调用 fragment add hide 的时候回调用这个方法
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        checkVisibility(hidden);
    }

    /**
     * Tab切换时会回调此方法。对于没有Tab的页面，[Fragment.getUserVisibleHint]默认为true。
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        info("setUserVisibleHint = " + isVisibleToUser);
        super.setUserVisibleHint(isVisibleToUser);
        checkVisibility(isVisibleToUser);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        info("onViewAttachedToWindow");
        checkVisibility(true);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        info("onViewDetachedFromWindow");
        v.removeOnAttachStateChangeListener(this);
        checkVisibility(false);
    }

    /**
     * 检查可见性是否变化
     *
     * @param expected 可见性期望的值。只有当前值和expected不同，才需要做判断
     */
    private void checkVisibility(Boolean expected) {
        if (expected == visible) {
            return;
        }

        boolean parentVisible = null == localParentFragment ? parentActivityVisible : localParentFragment.isFragmentVisible();
//        if (localParentFragment == null) parentActivityVisible
//        else localParentFragment?.isFragmentVisible() ?: false

        final boolean superVisible = super.isVisible();
        final boolean hintVisible = getUserVisibleHint();
        final boolean visible = parentVisible && superVisible && hintVisible;
        info(
                String.format(
                        "==> checkVisibility = %s  ( parent = %s, super = %s, hint = %s )",
                        visible, parentVisible, superVisible, hintVisible
                )
        );

        if (visible != this.visible) {
            this.visible = visible;
            onVisibilityChanged(this.visible);
        }
    }

    private String getSpanName(String name) {
        return name + ": " + this.getSpanName();
    }

    private String getSpanName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 可见性改变
     */
    protected void onVisibilityChanged(boolean visible) {
        info("==> onVisibilityChanged = " + visible);
        if (visible) {
            pageSpan = tracer.spanBuilder(getSpanName("Page In"))
                    .startSpan()
                    .setAttribute("component", "Android.Page")
                    .setAttribute("page.name", getSpanName())
                    .setAttribute("page.visibility", "visible");
            scope = pageSpan.makeCurrent();
        } else {
            tracer.spanBuilder(getSpanName("Page Out"))
                    .startSpan()
                    .setAttribute("component", "Android.Page")
                    .setAttribute("page.name", getSpanName())
                    .setAttribute("page.visibility", "in_visibile")
                    .end();

            pageSpan.end();
            scope.close();
        }

        for (OnFragmentVisibilityChangedListener listener : listeners) {
            listener.onFragmentVisibilityChanged(visible);
        }
    }

    /**
     * 是否可见（Activity处于前台、Tab被选中、Fragment被添加、Fragment没有隐藏、Fragment.View已经Attach）
     */
    boolean isFragmentVisible() {
        return visible;
    }

    private void info(String s) {
        Log.i(TAG, String.format("fragment: %s, %s", this.getClass().getSimpleName(), s));
    }

}
