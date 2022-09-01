package com.aliyun.sls.android.producer.example.example.trace.ui.category;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import com.aliyun.sls.android.producer.example.databinding.TraceItemCategoryLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.FragmentActivity;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListFragment;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseRecyclerAdapter;
import com.aliyun.sls.android.producer.example.example.trace.utils.ImageUtils;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class CategoryFragment extends BaseListFragment<TraceItemCategoryLayoutBinding, ItemModel, CategoryViewModel> {

    @Override
    protected BaseRecyclerAdapter.IViewContract<TraceItemCategoryLayoutBinding, ItemModel> onCreateViewUpdater() {
        return new BaseRecyclerAdapter.IViewContract<TraceItemCategoryLayoutBinding, ItemModel>() {
            @Override
            public TraceItemCategoryLayoutBinding onCreateBinding(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return TraceItemCategoryLayoutBinding.inflate(inflater, parent, false);
            }

            @Override
            public void onUpdate(TraceItemCategoryLayoutBinding binding, ItemModel model, int pos) {
                binding.categoryTitleText.setText(model.name);
                binding.categoryDescText.setText(model.description);
                binding.categoryPriceText.setPrice(model.price);
                binding.categoryAddToCart.setOnClickListener(v -> ApiClient.addToCart(model.id, new ApiClient.ApiCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        Toast.makeText(binding.getRoot().getContext(), "加入购物车成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int code, String error) {
                        Toast.makeText(binding.getRoot().getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                }));

                binding.getRoot().setOnClickListener(v -> FragmentActivity.startProductDetailPage(binding.getRoot().getContext(), model.id));

                ImageUtils.loadImage(model.imageUrl.get(0), binding.categoryImage);
            }
        };
    }

}