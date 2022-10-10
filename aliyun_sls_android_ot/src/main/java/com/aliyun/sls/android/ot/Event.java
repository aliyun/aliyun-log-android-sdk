package com.aliyun.sls.android.ot;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.aliyun.sls.android.ot.utils.TimeUtils;

/**
 * @author gordon
 * @date 2022/10/10
 */
public class Event {
    private String name;

    private final List<Attribute> attributes = new LinkedList<>();
    private final Object lock = new Object();

    private long epochNanos;

    private int totalAttributeCount = 0;

    Event(String name) {
        this.name = name;
        this.epochNanos = TimeUtils.instance.now();
    }

    public static Event create(String name) {
        return new Event(name);
    }

    public Event addAttribute(Attribute... attributes) {
        if (null == attributes) {
            return this;
        }

        synchronized (lock) {
            this.attributes.addAll(Arrays.asList(attributes));
            this.totalAttributeCount += attributes.length;
        }

        return this;
    }

    public Event addAttribute(List<Attribute> attributes) {
        if (null == attributes) {
            return this;
        }

        synchronized (lock) {
            this.attributes.addAll(attributes);
            this.totalAttributeCount += attributes.size();
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public Event setName(String name) {
        this.name = name;
        return this;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public long getEpochNanos() {
        return epochNanos;
    }

    public Event setEpochNanos(long epochNanos) {
        this.epochNanos = epochNanos;
        return this;
    }

    public int getTotalAttributeCount() {
        return totalAttributeCount;
    }

    public Event setTotalAttributeCount(int totalAttributeCount) {
        this.totalAttributeCount = totalAttributeCount;
        return this;
    }
}
