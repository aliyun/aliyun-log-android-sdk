package com.aliyun.sls.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aliyun.sls.android.plugin.IPlugin;
import com.aliyun.sls.android.producer.HttpConfigProxy;

/**
 * @author gordon
 * @date 2021/04/14
 */
public class SLSAdapter {
    private static final String TAG = "SLSAdapter";

    private String channel;
    private String channelName;
    private String userNick;
    private String longLoginNick;
    private String loginType;

    private List<IPlugin> plugins = new ArrayList<>();

    private SLSAdapter() {
        //no instance
    }

    public static SLSAdapter getInstance() {
        return Holder.INSTANCE;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public void setLongLoginNick(String longLoginNick) {
        this.longLoginNick = longLoginNick;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public void init(final SLSConfig config) {
        if (config.debuggable) {
            SLSLog.v(TAG, "init, start.");
        }

        if (!checkConfig(config)) {
            return;
        }

        for (IPlugin plugin : plugins) {
            if (config.debuggable) {
                SLSLog.v(TAG, SLSLog.format("init plugin %s start. plugin: ", plugin.name()));
            }

            plugin.init(config);
            // add plugin version to user-agent
            HttpConfigProxy.addPluginUserAgent(plugin.name(), plugin.version());

            if (config.debuggable) {
                SLSLog.v(TAG, SLSLog.format("init plugin %s end. plugin: ", plugin.name()));
            }
        }

        //Thread t = new Thread(() -> StatCache.init(config.context));
        //t.setPriority(Thread.NORM_PRIORITY);
        //t.start();

        if (config.debuggable) {
            SLSLog.v(TAG, "init, end.");
        }
    }

    public void updateConfig(SLSConfig config) {
        for (IPlugin plugin : plugins) {
            plugin.updateConfig(config);
        }
    }

    public void resetSecurityToken(String accessKeyId, String accessKeySecret, String securityToken) {
        for (IPlugin plugin : plugins) {
            plugin.resetSecurityToken(accessKeyId, accessKeySecret, securityToken);
        }
    }

    public void resetProject(String endpoint, String project, String logstore) {
        for (IPlugin plugin : plugins) {
            plugin.resetProject(endpoint, project, logstore);
        }
    }

    public SLSAdapter addPlugin(IPlugin plugin) {
        if (null == plugin) {
            throw new IllegalArgumentException("plugin must not be null");
        }

        this.plugins.add(plugin);
        return this;
    }

    /**
     * 上报自定义数据
     *
     * @param eventKey 自定义数据 key
     * @param properties 自定义数据 properties
     */
    public void reportCustomEvent(final String eventKey, final Map<String, String> properties) {
        for (IPlugin plugin : plugins) {
            if (null != plugin && "crash_reporter".equals(plugin.name())) {
                plugin.reportCustomEvent(eventKey, properties);
                break;
            }
        }
    }

    private boolean checkConfig(SLSConfig config) {
        if (null == config) {
            throw new IllegalArgumentException("SLSConfig must not be null.");
        }

        if (null == config.context) {
            throw new IllegalArgumentException("SLSConfig.context must not be null.");
        }

        return true;
    }

    private static class Holder {
        private final static SLSAdapter INSTANCE = new SLSAdapter();
    }
}
