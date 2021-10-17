package com.aliyun.sls.android.producer.example.example.trace.ui.core.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private VM viewModel;
    protected BaseListContainerLayoutBinding listContainerLayoutBinding;
    private SwipeRefreshLayout refreshLayout;

    protected abstract BaseRecyclerAdapter.IViewUpdater<BIND, ITEM> onCreateViewUpdater();

    protected void onRefresh() {

    }

    protected boolean init = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //noinspection unchecked
        viewModel = new ViewModelProvider(this).get((Class<VM>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2]);

        listContainerLayoutBinding = BaseListContainerLayoutBinding.inflate(inflater, container, false);
        refreshLayout = listContainerLayoutBinding.swiperefresh;
        refreshLayout.setOnRefreshListener(() -> {
            BaseListFragment.this.onRefresh();
            refreshLayout.setRefreshing(false);
        });

        final BaseRecyclerAdapter.IViewUpdater<BIND, ITEM> viewUpdater = onCreateViewUpdater();
        if (null == viewUpdater) {
            throw new IllegalArgumentException("Must implement onCreateViewUpdater method");
        }

        final RecyclerView recyclerView = listContainerLayoutBinding.baseRecyclerview;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        final BaseRecyclerAdapter<BIND, ITEM> adapter = new BaseRecyclerAdapter<>(viewUpdater);
        recyclerView.setAdapter(adapter);

        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.updateDatum(items);
            if (refreshLayout.isRefreshing()) {
                refreshLayout.setRefreshing(false);
            }
        });

        return listContainerLayoutBinding.getRoot();
    }

    @Override
    protected void onVisibilityChanged(boolean visible) {
        super.onVisibilityChanged(visible);
        if (visible && !init) {
            init = true;
            refreshLayout.setRefreshing(true);
            viewModel.requestItemsFromServer();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
