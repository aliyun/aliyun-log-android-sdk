package com.aliyun.sls.android.sdk;

import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class LogEntity {
    @Id(autoincrement = true)
    private Long id;

    private String endPoint;
    private String project;
    private String store;
    private String jsonString;
    private Long timestamp;
    @Generated(hash = 1946367947)
    public LogEntity(Long id, String endPoint, String project, String store,
            String jsonString, Long timestamp) {
        this.id = id;
        this.endPoint = endPoint;
        this.project = project;
        this.store = store;
        this.jsonString = jsonString;
        this.timestamp = timestamp;
    }
    @Generated(hash = 1472642729)
    public LogEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEndPoint() {
        return this.endPoint;
    }
    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }
    public String getProject() {
        return this.project;
    }
    public void setProject(String project) {
        this.project = project;
    }
    public String getStore() {
        return this.store;
    }
    public void setStore(String store) {
        this.store = store;
    }
    public String getJsonString() {
        return this.jsonString;
    }
    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }
    public Long getTimestamp() {
        return this.timestamp;
    }
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
