package com.aliyun.sls.android.producer.example.example.trace.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.aliyun.sls.android.producer.example.databinding.TraceItemLayoutBinding;
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
            }
        };
    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
    }
}