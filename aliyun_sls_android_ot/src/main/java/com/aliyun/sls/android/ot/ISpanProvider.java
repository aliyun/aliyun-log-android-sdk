package com.aliyun.sls.android.ot;

import java.util.List;

/**
 * @author gordon
 * @date 2022/4/25
 */
public interface ISpanProvider {
    Resource provideResource();
    List<Attribute> provideAttribute();
}
