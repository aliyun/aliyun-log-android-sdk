package com.aliyun.sls.android.producer.example.example.trace.ui.core.list;

import java.util.List;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

/**
 * @author gordon
 * @date 2021/10/17
 */
public class BaseRecyclerAdapter<BIND extends ViewBinding, ITEM> extends RecyclerView.Adapter<BaseRecyclerAdapter.ViewHolder<BIND>> {

    private List<ITEM> datum;

    private IViewContract<BIND, ITEM> viewUpdater;

    public BaseRecyclerAdapter(IViewContract<BIND, ITEM> viewUpdater) {
        this.viewUpdater = viewUpdater;
    }

    @NonNull
    @Override
    public ViewHolder<BIND> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder<>(viewUpdater.onCreateBinding(LayoutInflater.from(parent.getContext()), parent, viewType));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder<BIND> holder, int position) {
        if (null != viewUpdater) {
            ITEM item = datum.get(position);
            viewUpdater.onUpdate(holder.binding, item, position);
        }
    }

    @Override
    public int getItemCount() {
        return null == datum ? 0 : datum.size();
    }

    @Override
    public int getItemViewType(int position) {
        return viewUpdater.getItemViewType(position);
    }

    public void updateDatum(List<ITEM> datum) {
        this.datum = datum;
        notifyDataSetChanged();
    }

    protected static class ViewHolder<BIND extends ViewBinding> extends RecyclerView.ViewHolder {
        BIND binding;

        public ViewHolder(@NonNull BIND binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public  interface IViewContract<BIND, ITEM> {

        BIND onCreateBinding(LayoutInflater inflater, ViewGroup parent, int viewType);

        void onUpdate(BIND bind, ITEM item, int pos);

        default int getItemViewType(int pos) {
            return 0;
        }
    }
}
