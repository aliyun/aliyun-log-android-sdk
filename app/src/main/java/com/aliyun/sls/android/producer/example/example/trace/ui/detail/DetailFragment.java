package com.aliyun.sls.android.producer.example.example.trace.ui.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aliyun.sls.android.producer.example.R;
import com.aliyun.sls.android.producer.example.databinding.FragmentDetailBinding;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.example.example.trace.utils.ImageUtils;

import java.util.List;

public class DetailFragment extends Fragment {

    private DetailViewModel viewModel;
    private FragmentDetailBinding detailBinding;

    private String itemId;

    private Runnable updateImageCallback = new Runnable() {
        @Override
        public void run() {
            updateImage((List<String>) detailBinding.traceDetailImage.getTag());
        }
    };

    public static DetailFragment newInstance(String id) {
        Bundle args = new Bundle();
        args.putString("item_id", id);
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        detailBinding = FragmentDetailBinding.inflate(inflater, container, false);
        if (null != getArguments()) {
            this.itemId = getArguments().getString("item_id");
        }
        return detailBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        detailBinding.traceDetailAddToCartBtn.setClickable(false);
        detailBinding.traceDetailAddToCartBtn.setOnClickListener(v -> viewModel.addToCart(getActivity(), itemId));

        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        viewModel.getItemModel().observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.requestData(itemId);
    }

    private void updateUI(ItemModel model) {
        detailBinding.traceDetailDescText.setText(model.description);

        updateImage(model.imageUrl);
    }

    private void updateImage(List<String> images) {
        if (null == images || images.isEmpty() || !detailBinding.traceDetailImage.isShown()) {
            detailBinding.traceDetailImage.removeCallbacks(updateImageCallback);
            detailBinding.traceDetailImage.setTag(R.id.trace_detail_image, 0);
            return;
        }

        Integer tag = (Integer) detailBinding.traceDetailImage.getTag(R.id.trace_detail_image);
        int index = null == tag ? 0 : tag;

        if (index >= images.size()) {
            index = 0;
        }

        ImageUtils.loadImage(images.get(index), detailBinding.traceDetailImage);
        detailBinding.traceDetailImage.setTag(R.id.trace_detail_image, index + 1);
        detailBinding.traceDetailImage.setTag(images);
        detailBinding.traceDetailImage.postDelayed(updateImageCallback, 4000);
    }

}