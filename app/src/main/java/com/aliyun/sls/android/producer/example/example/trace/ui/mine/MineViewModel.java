package com.aliyun.sls.android.producer.example.example.trace.ui.mine;

import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gordon
 * @date 2021/09/06
 */
public class MineViewModel extends BaseListViewModel<MineModel> {

    public MineViewModel() {
        super("mine");
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

        items.setValue(models);
        status.setValue(Status.success());
    }
}
