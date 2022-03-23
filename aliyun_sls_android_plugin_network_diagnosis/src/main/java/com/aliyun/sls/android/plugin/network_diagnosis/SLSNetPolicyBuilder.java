package com.aliyun.sls.android.plugin.network_diagnosis;

import java.util.List;

import android.content.Context;
import com.aliyun.sls.android.plugin.network_diagnosis.SLSNetPolicy.Destination;
import com.aliyun.sls.android.utdid.Utdid;

/**
 * @author gordon
 * @date 2022/3/22
 */
public class SLSNetPolicyBuilder {

    private final SLSNetPolicy policy;

    public SLSNetPolicyBuilder() {
        policy = new SLSNetPolicy();
        policy.enable = true;
        policy.type = "";
        policy.version = 1;
        policy.periodicity = true;
        policy.interval = 3 * 60;
        policy.expiration = System.currentTimeMillis() / 1000 + 7 * 24 * 60 * 60;
        policy.ratio = 1000;
    }

    /**
     * 策略是否开启。
     * @param enable true/false。 默认开启。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setEnable(boolean enable) {
        policy.enable = enable;
        return this;
    }

    /**
     * 设置业务类型。可不配置。
     * @param type 业务名称。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setType(String type) {
        policy.type = type;
        return this;
    }

    /**
     * 策略版本。注意：只要有更新这个字段必须变大。
     * @param version 版本号
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setVersion(int version) {
        policy.version = version;
        return this;
    }

    /**
     * 是否为周期性策略。false代表一次性策略，此时忽略灰度和白名单，下发到的客户端都会执行。
     * @param periodicity true/false。默认为true。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setPeriodicity(boolean periodicity) {
        policy.periodicity = periodicity;
        return this;
    }

    /**
     * 设置探测周期。
     * @param interval 两次探测之间的时间间隔，单位为秒。默认为3分钟。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setInterval(int interval) {
        policy.interval = interval;
        return this;
    }

    /**
     * 设置有效期。
     * @param expiration 策略的有效期，unix时间戳表示法。默认为7天。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setExpiration(long expiration) {
        policy.expiration = expiration;
        return this;
    }

    /**
     * 设置灰度比例。
     * @param ratio 取值范围[0, 1000]。0时表示全部不生效，1000时表示全部生效。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setRatio(int ratio) {
        policy.ratio = ratio;
        return this;
    }

    /**
     * 设置白名单。白名单的值需要通过{@link Utdid#getUtdid(Context)}获取。
     * @param whitelist 白名单列表。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setWhiteList(List<String> whitelist) {
        policy.whitelist = whitelist;
        return this;
    }

    /**
     * 增加一组白名单。{@link #setWhiteList(List)}
     * @param whitelist 白名单。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder addWhiteList(List<String> whitelist) {
        policy.whitelist.addAll(whitelist);
        return this;
    }

    /**
     * 增加一个白名单。{@link #setWhiteList(List)}
     * @param whitelist 白名单。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder addWhiteList(String whitelist) {
        policy.whitelist.add(whitelist);
        return this;
    }

    /**
     * 设置生效的探测方式。探测方式当前支持：mtr、ping、tcpping、http。
     * @param methods 探测方法列表。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setMethods(List<String> methods) {
        policy.methods = methods;
        return this;
    }

    /**
     * 启用MTR探测方式。{@link #setMethods(List)}。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setEnableMTRMethod() {
        if (policy.methods.contains("mtr")) {
            return this;
        }

        policy.methods.add("mtr");
        return this;
    }

    /**
     * 启用PING探测方式。{@link #setMethods(List)}。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setEnablePingMethod() {
        if (policy.methods.contains("ping")) {
            return this;
        }

        policy.methods.add("ping");
        return this;
    }

    /**
     * 启用TcpPing探测方式。{@link #setMethods(List)}。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setEnableTcpPingMethod() {
        if (policy.methods.contains("tcpping")) {
            return this;
        }

        policy.methods.add("tcpping");
        return this;
    }

    /**
     * 启用Http探测方式。{@link #setMethods(List)}。
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setEnableHttpMethod() {
        if (policy.methods.contains("http")) {
            return this;
        }

        policy.methods.add("http");
        return this;
    }

    /**
     * 设置目的地信息。
     * @param destination {@link Destination}
     * @return {@link SLSNetPolicyBuilder}
     */
    public SLSNetPolicyBuilder setDestination(List<Destination> destination) {
        policy.destination = destination;
        return this;
    }

    /**
     * 增加目的地信息。{@link #setDestination(List)}。
     * @param ips IP地址列表，可以为域名。如：10.10.0.2:443/80/8080，表示tcp探测时会同时探测443/80/8080这三个端口。
     * @param urls Url地址列表。仅当探测方式为http时生效。
     * @return {@link SLSNetPolicyBuilder}
     */
    public  SLSNetPolicyBuilder addDestination(List<String> ips, List<String> urls) {
        Destination destination = new Destination();
        //destination.siteId = siteId;
        //destination.az = az;
        destination.ips = ips;
        destination.urls = urls;
        policy.destination.add(destination);
        return this;
    }

    /**
     * 构建 {@link SLSNetPolicy}
     * @return {@link SLSNetPolicy}
     */
    public SLSNetPolicy create() {
        return policy;
    }
}
