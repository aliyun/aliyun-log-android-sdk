package com.aliyun.sls.android.producer.example.example.trace.ui.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aliyun.sls.android.producer.example.example.trace.core.TraceViewModel;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.UserModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListViewModel;
import com.aliyun.sls.android.producer.example.example.trace.utils.UserUtils;

/**
 * @author gordon
 * @date 2021/10/17
 */
public class LoginViewModel extends TraceViewModel {

    private MutableLiveData<UserModel> userModel;

    public LoginViewModel() {
        super("login");
        userModel = new MutableLiveData<>();
    }

    public LiveData<UserModel> getUserModel() {
        return userModel;
    }

    public void doLogin(final String userName, final String password) {
        ApiClient.login(userName, password, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String loginId) {
                UserUtils.loginId = loginId;
                getCustomerInfo(loginId);
            }

            @Override
            public void onError(int code, String error) {
                status.setValue(BaseListViewModel.Status.error(String.valueOf(code), error));
            }
        });
    }

    public void getCustomerInfo(String loginId) {
        ApiClient.getCustomerInfo(loginId, new ApiClient.ApiCallback<UserModel>() {
            @Override
            public void onSuccess(UserModel userModel) {
                UserUtils.userModel = userModel;
                LoginViewModel.this.userModel.setValue(userModel);
                status.setValue(BaseListViewModel.Status.success());
            }

            @Override
            public void onError(int code, String error) {

            }
        });
    }

}
