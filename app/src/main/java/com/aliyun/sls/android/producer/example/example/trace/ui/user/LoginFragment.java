package com.aliyun.sls.android.producer.example.example.trace.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.aliyun.sls.android.producer.example.databinding.FragmentLoginBinding;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.VisibilityFragment;

/**
 * @author gordon
 * @date 2021/10/17
 */
public class LoginFragment extends VisibilityFragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        viewModel.getStatus().observe(getViewLifecycleOwner(), status -> {
            if (status.success) {
                getActivity().finish();
            } else {
                Toast.makeText(getActivity(), "登录失败，请重试", Toast.LENGTH_LONG).show();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.loginLoginBtn.setOnClickListener(v -> viewModel.doLogin(binding.loginUsernameEdit.getText().toString(), binding.loginPasswordEdit.getText().toString()));
    }
}
