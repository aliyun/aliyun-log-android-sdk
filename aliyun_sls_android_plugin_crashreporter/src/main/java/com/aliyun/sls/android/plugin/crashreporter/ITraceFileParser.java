package com.aliyun.sls.android.plugin.crashreporter;

import java.io.File;

import com.aliyun.sls.android.SLSConfig;

/**
 * @author gordon
 * @date 2021/04/15
 */
public interface ITraceFileParser {
    /**
     * parse the trace file.
     *
     * @param type trace file type.
     * @param file trace file.
     */
    void parseTraceFile(String type, File file);

    void updateConfig(SLSConfig config);
}
