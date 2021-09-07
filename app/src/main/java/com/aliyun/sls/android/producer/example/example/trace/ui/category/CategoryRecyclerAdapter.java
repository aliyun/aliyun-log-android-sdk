package com.aliyun.sls.android.producer.example.example.trace.ui.category;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.sls.android.producer.example.databinding.TraceItemCategoryLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.example.example.trace.utils.ImageUtils;

import java.util.List;

public class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryRecyclerAdapter.ViewHolder> {

    private List<ItemModel> datum;

    @NonNull
    @Override
    public CategoryRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(TraceItemCategoryLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryRecyclerAdapter.ViewHolder holder, int position) {
        final ItemModel model = getItem(position);

        holder.binding.categoryTitleText.setText(model.name);
        holder.binding.categoryDescText.setText(model.description);
        holder.binding.categoryPriceText.setText(String.valueOf(model.price));
        holder.binding.categoryAddToCart.setOnClickListener(v -> ApiClient.addToCart(model.id, new ApiClient.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                Toast.makeText(holder.itemView.getContext(), "加入购物车成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int code, String error) {
                Toast.makeText(holder.itemView.getContext(), error, Toast.LENGTH_SHORT).show();
            }
        }));

        ImageUtils.loadImage(model.imageUrl.get(0), holder.binding.categoryImage);
    }

    @Override
    public int getItemCount() {
        return null == datum ? 0 : datum.size();
    }

    public void update(List<ItemModel> datum) {
        this.datum = datum;
        notifyDataSetChanged();
    }

    private ItemModel getItem(int pos){
        return datum.get(pos);
    }


    protected static class ViewHolder extends RecyclerView.ViewHolder {

        TraceItemCategoryLayoutBinding binding;

        public ViewHolder(@NonNull TraceItemCategoryLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
