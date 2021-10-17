package com.aliyun.sls.android.producer.example.example.trace.ui.mine;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.sls.android.producer.example.R;
import com.aliyun.sls.android.producer.example.databinding.TraceMineItemLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.ui.FragmentActivity;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListFragment;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseRecyclerAdapter;

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

        final int padding = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, context.getResources().getDisplayMetrics()) + 0.5f);
        recyclerView.setPadding(recyclerView.getPaddingLeft() + padding
                , recyclerView.getPaddingTop()
                , recyclerView.getPaddingRight() + padding
                , recyclerView.getPaddingBottom());
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

}
