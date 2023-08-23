package com.aliyun.sls.android.producer.example.example.trace.auto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.aliyun.sls.android.core.SLSLog;
import com.aliyun.sls.android.ot.Span;
import com.aliyun.sls.android.trace.Tracer;

/**
 * @author gordon
 * @date 2022/11/29
 */
public class Auto {
    private static final Random r = new Random();

    private static final List<String> catalogueIds = new ArrayList<String>() {
        {
            add("03fef6ac-1896-4ce8-bd69-b798f85c6e0b");
            add("3395a43e-2d88-40de-b95f-e00e1502085b");
            add("510a0d7e-8e83-4193-b483-e27e09ddc34d");
            add("6d62d909-f957-430e-8689-b5129c0bb75e");
            add("808a2de1-1aaa-4c25-a9b9-6612e8f29a38");
        }
    };

    public static void startCreateOrderNormal1() {
        createOrderNormal();
    }

    public static void startCreateOrderNormal2() {
        createOrderNormal2();
    }

    public static void startCreateOrderAnomaly1() {
        createOrderAnomaly();
    }

    public static void startAutoCreateOrder() {
        for (int i = 0; i < 1000; i++) {
            int random = r.nextInt(2000);
            if (random % 100 == 0) {
                SLSLog.e("startAutoCreateOrder", "anomaly1");
                startCreateOrderAnomaly1();
            } else if (random % 30 == 0) {
                SLSLog.w("startAutoCreateOrder", "normal2");
                startCreateOrderNormal2();
            } else {
                SLSLog.d("startAutoCreateOrder", "normal1");
                startCreateOrderNormal1();
            }
        }
    }

    private static void addToCart() {
        click("加购", () -> {
            final Span span = Tracer.startSpan("调用加购接口", true);
            sleep();
            span.end();
            //ApiClient.addToCart(catalogueIds.get(r.nextInt(4)), new ApiCallback<Boolean>() {
            //    @Override
            //    public void onSuccess(Boolean aBoolean) {
            //        span.end();
            //    }
            //
            //    @Override
            //    public void onError(int code, String error) {
            //        span.setStatus(StatusCode.ERROR);
            //        span.setStatusMessage(error);
            //        span.end();
            //    }
            //});
        });
    }

    private static void createOrder() {
        click("去结算", () -> {
            Tracer.withinSpan("调用去结算接口", Auto::sleepR);
            //final Span span = Tracer.startSpan("调用去结算接口", true);
            //ApiClient.createOrder(new ApiCallback<Boolean>() {
            //    @Override
            //    public void onSuccess(Boolean aBoolean) {
            //        span.end();
            //    }
            //
            //    @Override
            //    public void onError(int code, String error) {
            //        span.setStatus(StatusCode.ERROR);
            //        span.setStatusMessage(error);
            //        span.end();
            //    }
            //});
        });
    }

    private static void atoCommon1() {
        //1. 先进入到首页
        pageStart("首页");
        //2. 点击分类
        click("分类");
        // 3. 进入分类
        pageStart("分类");
    }

    private static void createOrderNormal() {
        Tracer.withinSpan("正常下单1", () -> {
            atoCommon1();
            // 4. 加购
            addToCart();

            sleep();

            // 5. 进入到购物车
            pageStart("购物车");

            click("提交订单");
            pageStart("下单");

            // 6. 提交订单
            createOrder();
        });

    }

    private static void createOrderNormal2() {
        Tracer.withinSpan("正常下单2", () -> {
            atoCommon1();

            addToCart();
            sleep();
            addToCart();
            sleep();
            addToCart();
            sleep();

            pageStart("购物车");

            click("提交订单");
            pageStart("下单");

            click("切换地址");
            click("选择地址");
            pageStart("购物车");

            click("提交订单");
            pageStart("下单");
            createOrder();
        });

    }

    private static void createOrderAnomaly() {
        Tracer.withinSpan("异常下单1", () -> {
            atoCommon1();

            addToCart();

            pageStart("购物车");

            click("提交订单");
            pageStart("下单");

            click("切换地址");
            click("选择地址");
            pageStart("下单页");

            createOrder();
        });

    }

    private static void pageStart(String pageName) {
        Tracer.withinSpan("Page: " + pageName, Auto::sleepR);
    }

    private static void click(String action) {
        click(action, Auto::sleepR);
    }

    private static void click(String action, Runnable r) {
        Tracer.withinSpan("Click: " + action, r);
    }

    private static void sleepR() {
        try {
            Thread.sleep(r.nextInt(100));
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(120);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
