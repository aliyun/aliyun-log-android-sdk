package com.aliyun.sls.android.producer.example.example.trace.ui.mine;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import com.aliyun.sls.android.producer.example.R;
import com.aliyun.sls.android.producer.example.databinding.TraceMineItemLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.auto.Auto;
import com.aliyun.sls.android.producer.example.example.trace.ui.FragmentActivity;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListFragment;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseRecyclerAdapter;
import com.aliyun.sls.android.producer.example.example.trace.utils.UserUtils;

/**
 * @author gordon
 * @date 2021/09/06
 */
public class MineFragment extends BaseListFragment<TraceMineItemLayoutBinding, MineModel, MineViewModel> {

    @Override
    protected void onInitRecyclerView(Context context, RecyclerView recyclerView) {
        super.onInitRecyclerView(context, recyclerView);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(context.getResources().getDrawable(R.drawable.divider_horizontal_line));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected BaseRecyclerAdapter.IViewContract<TraceMineItemLayoutBinding, MineModel> onCreateViewUpdater() {

        return new BaseRecyclerAdapter.IViewContract<TraceMineItemLayoutBinding, MineModel>() {
            @Override
            public TraceMineItemLayoutBinding onCreateBinding(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return TraceMineItemLayoutBinding.inflate(inflater, parent, false);
            }

            @Override
            public void onUpdate(TraceMineItemLayoutBinding binding, MineModel model, int pos) {
                int viewType = getItemViewType(pos);
                if (1 == viewType) {
                    binding.mineUserLayout.setVisibility(View.VISIBLE);
                    binding.mineItemLayout.setVisibility(View.GONE);

                    binding.mineUserNameText.setText(model.title);
                    binding.mineUserLoginBtn.setOnClickListener(v -> FragmentActivity.startLoginPage(getActivity()));

                    if (null == viewModel.userModelMutableLiveData.getValue()) {
                        binding.mineUserNameText.setVisibility(View.GONE);
                        binding.mineUserLoginBtn.setVisibility(View.VISIBLE);
                    } else {
                        binding.mineUserNameText.setVisibility(View.VISIBLE);
                        binding.mineUserLoginBtn.setVisibility(View.GONE);
                        binding.mineUserNameText.setText(viewModel.userModelMutableLiveData.getValue().username);
                    }

                    viewModel.userModelMutableLiveData.observe(getViewLifecycleOwner(), userModel -> {
                        if (null == userModel) {
                            binding.mineUserNameText.setVisibility(View.GONE);
                            binding.mineUserLoginBtn.setVisibility(View.VISIBLE);
                        } else {
                            binding.mineUserNameText.setVisibility(View.VISIBLE);
                            binding.mineUserLoginBtn.setVisibility(View.GONE);
                            binding.mineUserNameText.setText(userModel.username);
                        }
                    });
                } else {
                    binding.mineUserLayout.setVisibility(View.GONE);
                    binding.mineItemLayout.setVisibility(View.VISIBLE);

                    binding.mineItemTitle.setText(model.title);

                    if (model.type == 2) {
                        binding.getRoot().setOnClickListener(v -> FragmentActivity.startOrderListPage(getActivity()));
                    } else if (model.type == 3) {
                        binding.getRoot().setOnClickListener(v -> Auto.startCreateOrderNormal1());
                    } else if (model.type == 4) {
                        binding.getRoot().setOnClickListener(v -> Auto.startCreateOrderNormal2());
                    } else if (model.type == 5) {
                        binding.getRoot().setOnClickListener(v -> Auto.startCreateOrderAnomaly1());
                    } else if (model.type == 6) {
                        binding.getRoot().setOnClickListener(v -> Auto.startAutoCreateOrder());
                    }
                }
            }

            @Override
            public int getItemViewType(int pos) {
                return pos == 0 ? 1 : 2;
            }
        };
    }

    @Override
    protected void onVisibilityChanged(boolean visible) {
        super.onVisibilityChanged(visible);
        if (visible) {
            if (!TextUtils.isEmpty(UserUtils.getLoginId()) && UserUtils.userModel == null) {
                viewModel.getCustomerInfo(UserUtils.getLoginId());
            }
        }
    }
}
