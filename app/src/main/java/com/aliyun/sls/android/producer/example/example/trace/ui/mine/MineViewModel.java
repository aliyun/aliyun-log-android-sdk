package com.aliyun.sls.android.producer.example.example.trace.ui.mine;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.UserModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListViewModel;
import com.aliyun.sls.android.producer.example.example.trace.utils.UserUtils;

/**
 * @author gordon
 * @date 2021/09/06
 */
public class MineViewModel extends BaseListViewModel<MineModel> {

    public MutableLiveData<UserModel> userModelMutableLiveData;

    public MineViewModel() {
        super("mine");
        userModelMutableLiveData = new MutableLiveData<>();
        userModelMutableLiveData.setValue(UserUtils.userModel);
    }

    @Override
    protected void fetchItemsFromServer() {
        List<MineModel> models = new ArrayList<>();
        MineModel model = new MineModel();
        model.type = 1;
        model.title = "登录";
        models.add(model);

        model = new MineModel();
        model.type = 2;
        model.title = "我的订单";
        models.add(model);

        model = new MineModel();
        model.type = 3;
        model.title = "自动化-正常1";
        models.add(model);

        model = new MineModel();
        model.type = 4;
        model.title = "自动化-正常2";
        models.add(model);

        model = new MineModel();
        model.type = 5;
        model.title = "自动化-异常1";
        models.add(model);

        model = new MineModel();
        model.type = 6;
        model.title = "自动化执行";
        models.add(model);

        items.setValue(models);
        status.setValue(Status.success());
    }

    public void getCustomerInfo(String loginId) {
        ApiClient.getCustomerInfo(loginId, new ApiClient.ApiCallback<UserModel>() {
            @Override
            public void onSuccess(UserModel userModel) {
                UserUtils.userModel = userModel;
                userModelMutableLiveData.setValue(userModel);
                status.setValue(BaseListViewModel.Status.success());
            }

            @Override
            public void onError(int code, String error) {

            }
        });
    }
}
