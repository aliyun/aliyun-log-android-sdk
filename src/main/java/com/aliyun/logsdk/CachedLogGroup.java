package com.aliyun.logsdk;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by megrez on 2016/12/25.
 */
public class CachedLogGroup extends LogGroup {
    private static final String TAG = CachedLogGroup.class.getSimpleName();
    public static final int CounterThreshold = 10;

    private String topic;
    private String source;
    private LinkedBlockingQueue<Log> logs;

    public CachedLogGroup(String topic,String source) {
        logs = new LinkedBlockingQueue<Log>();
        this.topic = topic;
        this.source = source;
    }

    @Override
    public void PutTopic(String topic){
        this.topic = topic;
    }

    @Override
    public void PutSource(String source){
        this.source = source;
    }

    public LogGroup takeOneLogGroup() {
        if (logs.size() == 0) {
            return null;
        }
        LogGroup group = new LogGroup(this.topic,this.source);
        for (int i = 0;i < CounterThreshold;i++) {
            Log log = logs.poll();
            if (log == null) {
                break;
            }
            group.PutLog(log);
        }
        return group;
    }

    public void addLogGroup(LogGroup logGroup) {
        for (Log log : logGroup.mContent) {
            logs.offer(log);
        }
    }

    @Override
    public void PutLog(Log log) {
        logs.offer(log);
    }
}
