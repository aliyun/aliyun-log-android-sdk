package com.aliyun.sls.android.producer.example.example.trace.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import com.aliyun.sls.android.producer.databinding.TraceItemLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient.ApiCallback;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.FragmentActivity;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListFragment;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseRecyclerAdapter;
import com.aliyun.sls.android.producer.example.example.trace.utils.ImageUtils;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class HomeFragment extends BaseListFragment<TraceItemLayoutBinding, ItemModel, HomeViewModel> {

    @Override
    protected BaseRecyclerAdapter.IViewContract<TraceItemLayoutBinding, ItemModel> onCreateViewUpdater() {
        return new BaseRecyclerAdapter.IViewContract<TraceItemLayoutBinding, ItemModel>() {
            @Override
            public TraceItemLayoutBinding onCreateBinding(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return TraceItemLayoutBinding.inflate(inflater, parent, false);
            }

            @Override
            public void onUpdate(TraceItemLayoutBinding binding, ItemModel itemModel, int pos) {
                ImageUtils.loadImage(itemModel.imageUrl.get(0), binding.itemImage);
                binding.itemTitle.setText(itemModel.name);
                binding.itemDesc.setText(itemModel.description);
                binding.itemPrice.setPrice(itemModel.price);

                binding.itemViewDetail.setOnClickListener(v -> FragmentActivity.startProductDetailPage(binding.getRoot().getContext(), itemModel.id));
                binding.itemAddToCart.setOnClickListener(v -> ApiClient.addToCart(itemModel.id, new ApiCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        Toast.makeText(binding.getRoot().getContext(), "加购成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int code, String error) {
                        Toast.makeText(binding.getRoot().getContext(), "加购失败", Toast.LENGTH_SHORT).show();
                    }
                }));
            }
        };
    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
    }
}