package com.aliyun.sls.android.producer.example.example.trace.ui.mine;

import android.content.Context;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.UserModel;
import com.aliyun.sls.android.producer.example.example.trace.utils.UserUtils;

/**
 * @author gordon
 * @date 2021/09/06
 */
public class MineViewModel extends ViewModel {
    private MutableLiveData<UserModel> userLiveData;
    private MutableLiveData<Boolean> loggedIn;

    public MineViewModel() {
        this.userLiveData = new MutableLiveData<>();
        this.loggedIn = new MutableLiveData<>();
        loggedIn.setValue(false);
    }

    public LiveData<UserModel> getUser() {
        return this.userLiveData;
    }

    public LiveData<Boolean> getLoggedStatus() {
        return this.loggedIn;
    }


    public void reqCustomerInfo(Context context) {
        final String id = UserUtils.getCachedUserId(context);
        if (TextUtils.isEmpty(id)) {
            loggedIn.setValue(false);
        }

        ApiClient.getCustomers(id, new ApiClient.ApiCallback<UserModel>() {
            @Override
            public void onSuccess(UserModel userModel) {
                userLiveData.setValue(userModel);
                loggedIn.setValue(true);
            }

            @Override
            public void onError(int code, String error) {
                loggedIn.setValue(false);
            }
        });
    }
}
