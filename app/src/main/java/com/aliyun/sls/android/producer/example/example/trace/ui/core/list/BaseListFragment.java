package com.aliyun.sls.android.producer.example.example.trace.ui.core.list;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewbinding.ViewBinding;

import com.aliyun.sls.android.producer.example.databinding.BaseListContainerLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.VisibilityFragment;

import java.lang.reflect.ParameterizedType;

/**
 * @author gordon
 * @date 2021/10/17
 */
public abstract class BaseListFragment<BIND extends ViewBinding, ITEM, VM extends BaseListViewModel<ITEM>> extends VisibilityFragment {

    protected VM viewModel;
    protected BaseListContainerLayoutBinding listContainerLayoutBinding;
    private SwipeRefreshLayout refreshLayout;

    protected abstract BaseRecyclerAdapter.IViewContract<BIND, ITEM> onCreateViewUpdater();

    protected void onRefresh() {
        viewModel.getTracer().spanBuilder(viewModel.generatorSpanName("pull_refresh")).startSpan().end();
        viewModel.requestItemsFromServer();
    }

    protected boolean init = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //noinspection unchecked
        viewModel = new ViewModelProvider(this).get((Class<VM>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2]);

        listContainerLayoutBinding = BaseListContainerLayoutBinding.inflate(inflater, container, false);
        refreshLayout = listContainerLayoutBinding.swiperefresh;
        refreshLayout.setOnRefreshListener(BaseListFragment.this::onRefresh);

        final View footerView = onCreateFooterView(inflater, listContainerLayoutBinding.baseContentLayout);
        if (null != footerView) {
            listContainerLayoutBinding.baseFooterContainer.setVisibility(View.VISIBLE);
            listContainerLayoutBinding.baseFooterContainer.addView(footerView, new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT));
        }

        listContainerLayoutBinding.baseErrorBtn.setOnClickListener(v -> {
            viewModel.getTracer().spanBuilder(viewModel.generatorSpanName("retry")).startSpan().end();
            BaseListFragment.this.update();
        });

        final BaseRecyclerAdapter.IViewContract<BIND, ITEM> viewUpdater = onCreateViewUpdater();
        if (null == viewUpdater) {
            throw new IllegalArgumentException("Must implement onCreateViewUpdater method");
        }

        final RecyclerView recyclerView = listContainerLayoutBinding.baseRecyclerview;
        recyclerView.setLayoutManager(onCreateLayoutManager(getActivity()));
        final BaseRecyclerAdapter<BIND, ITEM> adapter = new BaseRecyclerAdapter<>(viewUpdater);
        recyclerView.setAdapter(adapter);

        onInitRecyclerView(getActivity(), recyclerView);

        viewModel.getItems().observe(getViewLifecycleOwner(), adapter::updateDatum);

        viewModel.getStatus().observe(getViewLifecycleOwner(), status -> {
            refreshLayout.setRefreshing(false);
            onStatusChanged(status);
        });

        return listContainerLayoutBinding.getRoot();
    }

    protected void onInitRecyclerView(Context context, RecyclerView recyclerView) {

    }

    protected RecyclerView.LayoutManager onCreateLayoutManager(Context context) {
        return new LinearLayoutManager(context);
    }

    protected View onCreateFooterView(final LayoutInflater inflater, final ViewGroup parent) {
        return null;
    }

    protected void onStatusChanged(BaseListViewModel.Status status) {
        if (status.success) {
            listContainerLayoutBinding.baseContentLayout.setVisibility(View.VISIBLE);
            listContainerLayoutBinding.baseErrorLayout.setVisibility(View.GONE);
        } else {
            listContainerLayoutBinding.baseContentLayout.setVisibility(View.GONE);
            listContainerLayoutBinding.baseErrorLayout.setVisibility(View.VISIBLE);

            listContainerLayoutBinding.baseErrorText.setText(status.error);
        }
    }


    protected void update() {
        if (refreshLayout.isRefreshing()) {
            return;
        }

        listContainerLayoutBinding.swiperefresh.setVisibility(View.VISIBLE);
        listContainerLayoutBinding.baseErrorLayout.setVisibility(View.GONE);

        refreshLayout.setRefreshing(true);
        viewModel.requestItemsFromServer();
    }

    @Override
    protected void onVisibilityChanged(boolean visible) {
        super.onVisibilityChanged(visible);
        if (visible && !init) {
            init = true;
            update();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
