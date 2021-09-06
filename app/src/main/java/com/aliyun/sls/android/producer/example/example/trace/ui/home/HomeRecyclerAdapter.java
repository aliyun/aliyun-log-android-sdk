package com.aliyun.sls.android.producer.example.example.trace.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.sls.android.producer.example.databinding.TraceItemLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerAdapter.ViewHolder> {

    private List<ItemModel> datum;

    @NonNull
    @Override
    public HomeRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(TraceItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeRecyclerAdapter.ViewHolder holder, int position) {
        ItemModel model = getItemModel(position);
        if (null == model) {
            return;
        }

        Glide.with(holder.binding.itemImage)
                .load(model.imageUrl.get(0))
                .optionalCenterCrop()
                .into(holder.binding.itemImage);
        holder.binding.itemTitle.setText(model.name);
        holder.binding.itemDesc.setText(model.description);
        holder.binding.itemPrice.setText(model.price + "");
    }

    @Override
    public int getItemCount() {
        return null == datum ? 0 : datum.size();
    }

    private ItemModel getItemModel(int pos) {
        return pos >= getItemCount() ? null : datum.get(pos);
    }

    public void updateData(List<ItemModel> datum) {
        this.datum = datum;
        notifyDataSetChanged();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        TraceItemLayoutBinding binding;

        public ViewHolder(@NonNull TraceItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}
