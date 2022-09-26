package com.aliyun.sls.android.ot;

import java.util.LinkedList;
import java.util.List;

import android.os.Build;
import android.text.TextUtils;
import android.util.Pair;

/**
 * @author gordon
 * @date 2022/3/31
 */
@SuppressWarnings("UnusedReturnValue")
public class Resource {
    public final List<Attribute> attributes = new LinkedList<>();
    private final static Resource DEFAULT = new Resource();

    static {
        DEFAULT.add("sdk.language", "Android");
        DEFAULT.add("host.name", "Android");

        // device specification, ref: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/device.md
        DEFAULT.add("device.model.identifier", Build.MODEL);
        DEFAULT.add("device.model.name", Build.PRODUCT);
        DEFAULT.add("device.manufacturer", Build.MANUFACTURER);

        // os specification, ref: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/os.md
        DEFAULT.add("os.type", "Linux");
        DEFAULT.add("os.description", Build.DISPLAY);
        DEFAULT.add("os.name", "Android");
        DEFAULT.add("os.version", Build.VERSION.RELEASE);
        DEFAULT.add("os.sdk", Build.VERSION.SDK);

        // host specification, ref: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/semantic_conventions/host.md
        DEFAULT.add("host.name", Build.HOST);
        DEFAULT.add("host.type", Build.TYPE);
        DEFAULT.add("host.arch", Build.CPU_ABI + (TextUtils.isEmpty(Build.CPU_ABI2) ? "" : (", " + Build.CPU_ABI2)));
        DEFAULT.add("sls.sdk.version", BuildConfig.VERSION_NAME);
    }

    Resource() {

    }

    // region instance
    public static Resource getDefault() {
        return new Resource().merge(DEFAULT);
    }

    public static Resource of(String key, Object value) {
        Resource resource = new Resource();
        resource.add(key, value);
        return resource;
    }

    @SafeVarargs
    public static Resource of(Pair<String, Object>... resources) {
        Resource resource = new Resource();
        if (null != resources) {
            for (Pair<String, Object> pair : resources) {
                resource.add(pair.first, pair.second);
            }
        }
        return resource;
    }

    public static Resource of(List<Attribute> attributes) {
        Resource resource = new Resource();
        if (null == attributes) {
            return resource;
        }

        for (Attribute attribute : attributes) {
            resource.add(attribute.key, attribute.value);
        }
        return resource;
    }
    // endregion

    // region operation
    public Resource add(String key, Object value) {
        this.attributes.add(Attribute.of(key, value));
        return this;
    }

    public Resource merge(Resource resource) {
        if (null != resource) {
            attributes.addAll(resource.attributes);
        }
        return this;
    }
    // endregion

}
