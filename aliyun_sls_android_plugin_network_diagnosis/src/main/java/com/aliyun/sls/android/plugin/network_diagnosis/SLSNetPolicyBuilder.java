package com.aliyun.sls.android.plugin.network_diagnosis;

import java.util.List;

import com.aliyun.sls.android.plugin.network_diagnosis.SLSNetPolicy.Destination;

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

    public SLSNetPolicyBuilder setEnable(boolean enable) {
        policy.enable = enable;
        return this;
    }

    public SLSNetPolicyBuilder setType(String type) {
        policy.type = type;
        return this;
    }

    public SLSNetPolicyBuilder setVersion(int version) {
        policy.version = version;
        return this;
    }

    public SLSNetPolicyBuilder setPeriodicity(boolean periodicity) {
        policy.periodicity = periodicity;
        return this;
    }

    public SLSNetPolicyBuilder setInterval(int interval) {
        policy.interval = interval;
        return this;
    }

    public SLSNetPolicyBuilder setExpiration(long expiration) {
        policy.expiration = expiration;
        return this;
    }

    public SLSNetPolicyBuilder setRatio(int ratio) {
        policy.ratio = ratio;
        return this;
    }

    public SLSNetPolicyBuilder setWhiteList(List<String> whitelist) {
        policy.whitelist = whitelist;
        return this;
    }


    public SLSNetPolicyBuilder addWhiteList(List<String> whitelist) {
        policy.whitelist.addAll(whitelist);
        return this;
    }

    public SLSNetPolicyBuilder addWhiteList(String whitelist) {
        policy.whitelist.add(whitelist);
        return this;
    }

    public SLSNetPolicyBuilder setMethods(List<String> methods) {
        policy.methods = methods;
        return this;
    }

    public SLSNetPolicyBuilder setEnableMTRMethod() {
        if (policy.methods.contains("mtr")) {
            return this;
        }

        policy.methods.add("mtr");
        return this;
    }
    public SLSNetPolicyBuilder setEnablePingMethod() {
        if (policy.methods.contains("ping")) {
            return this;
        }

        policy.methods.add("ping");
        return this;
    }

    public SLSNetPolicyBuilder setEnableTcpPingMethod() {
        if (policy.methods.contains("tcpping")) {
            return this;
        }

        policy.methods.add("tcpping");
        return this;
    }

    public SLSNetPolicyBuilder setEnableHttpMethod() {
        if (policy.methods.contains("http")) {
            return this;
        }

        policy.methods.add("http");
        return this;
    }

    public SLSNetPolicyBuilder setDestination(List<Destination> destination) {
        policy.destination = destination;
        return this;
    }

    public  SLSNetPolicyBuilder addDestination(List<String> ips, List<String> urls) {
        Destination destination = new Destination();
        //destination.siteId = siteId;
        //destination.az = az;
        destination.ips = ips;
        destination.urls = urls;
        policy.destination.add(destination);
        return this;
    }

    public SLSNetPolicy create() {
        return policy;
    }
}
