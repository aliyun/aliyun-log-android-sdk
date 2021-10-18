package com.aliyun.sls.android.producer.example.example.trace.ui.mine;

import android.content.Context;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.sls.android.producer.example.R;
import com.aliyun.sls.android.producer.example.databinding.TraceMineItemLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.model.UserModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.FragmentActivity;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListFragment;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseRecyclerAdapter;
import com.aliyun.sls.android.producer.example.example.trace.utils.UserUtils;

/**
 * @author gordon
 * @date 2021/09/06
 */
public class MineFragment extends BaseListFragment<TraceMineItemLayoutBinding, MineModel, MineViewModel> {

    private MutableLiveData<UserModel> userModelMutableLiveData = new MutableLiveData<>();

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

                    if (null == userModelMutableLiveData.getValue()) {
                        binding.mineUserNameText.setVisibility(View.GONE);
                        binding.mineUserLoginBtn.setVisibility(View.VISIBLE);
                    } else {
                        binding.mineUserNameText.setVisibility(View.VISIBLE);
                        binding.mineUserLoginBtn.setVisibility(View.GONE);
                        binding.mineUserNameText.setText(userModelMutableLiveData.getValue().username);
                    }

                    userModelMutableLiveData.observe(getViewLifecycleOwner(), userModel -> {
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
            userModelMutableLiveData.setValue(UserUtils.userModel);
        }
    }
}
