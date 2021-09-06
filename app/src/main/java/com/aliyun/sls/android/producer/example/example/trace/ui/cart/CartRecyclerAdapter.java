package com.aliyun.sls.android.producer.example.example.trace.ui.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.sls.android.producer.example.databinding.TraceItemCartLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.CartItemModel;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.DetailActivity;
import com.aliyun.sls.android.producer.example.example.trace.utils.ImageUtils;

import java.util.List;

/**
 * @author gordon
 * @date 2021/09/06
 */
public class CartRecyclerAdapter extends RecyclerView.Adapter<CartRecyclerAdapter.ViewHolder> {
    private List<CartItemModel> datum;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(TraceItemCartLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItemModel cartModel = getItem(position);

        holder.binding.cartPriceText.setText(String.valueOf(cartModel.unitPrice));
        ApiClient.getDetail(cartModel.itemId, new ApiClient.ApiCallback<ItemModel>() {
            @Override
            public void onSuccess(ItemModel model) {
                if (cartModel.itemId.equals(model.id)) {
                    ImageUtils.loadImage(model.imageUrl.get(0), holder.binding.cartImage);
                    holder.binding.cartTitleText.setText(model.name);
                }
            }

            @Override
            public void onError(int code, String error) {

            }
        });
        holder.itemView.setOnClickListener(v -> DetailActivity.start(holder.itemView.getContext(), cartModel.itemId));
    }

    public CartItemModel getItem(int pos) {
        return datum.get(pos);
    }

    @Override
    public int getItemCount() {
        return null == datum ? 0 : datum.size();
    }

    public void update(List<CartItemModel> datum) {
        this.datum = datum;
        notifyDataSetChanged();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        private TraceItemCartLayoutBinding binding;

        public ViewHolder(@NonNull TraceItemCartLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
