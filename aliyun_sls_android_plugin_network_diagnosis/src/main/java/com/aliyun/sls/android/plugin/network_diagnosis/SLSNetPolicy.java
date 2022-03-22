package com.aliyun.sls.android.plugin.network_diagnosis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gordon
 * @date 2022/3/22
 */
public class SLSNetPolicy implements Serializable {

    SLSNetPolicy() {
    }

    /**
     * 策略是否开启，true： 开启
     */
    public boolean enable;
    /**
     * 业务名称，policy_为前缀，后面名称不影响
     */
    public String type;
    /**
     * 策略版本，只要有更新这个字段必须变大
     */
    public int version;
    /**
     * 一次性策略还是周期性策略，false代表一次性策略，此时忽略灰度和白名单，下发到的客户端都会执行
     */
    public boolean periodicity;
    /**
     * 探测周期
     */
    public int interval;
    /**
     * 可选，策略有效期
     */
    public long expiration;
    /**
     * periodicity为true时生效，本策略灰度比例，千分制，这里10代表千分之10
     */
    public int ratio;
    /**
     * 灰度之外的白名单
     */
    public List<String> whitelist = new ArrayList<>();
    /**
     * 启用的探测方法
     */
    public List<String> methods = new ArrayList<>();
    /**
     *
     */
    public List<Destination> destination = new ArrayList<>();

    public static class Destination implements Serializable {
        /**
         * SDK初始化需要传入siteId字段，用于区分用户所在地域，public所有地域都探测，其他值则需要和用户所在地匹配才进行探测
         */
        public final String siteId = "public";
        /**
         * 服务器所在的az，可忽略
         */
        public String az;
        /**
         *
         */
        public List<String> ips;
        /**
         *
         */
        public List<String> urls;
    }
}
