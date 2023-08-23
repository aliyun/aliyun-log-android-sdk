package com.aliyun.sls.android.blockdetection;

/**
 * @author gordon
 * @date 2022/9/2
 */
public final class BlockDetection {

    private static BlockDetectionFeature blockDetectionFeature;

    private BlockDetection() {
        //no instance
    }

    static void setBlockDetectionFeature(BlockDetectionFeature feature) {
        blockDetectionFeature = feature;
    }

    public static void setEnabled(boolean enable) {
        if (null == blockDetectionFeature) {
            return;
        }

        blockDetectionFeature.setFeatureEnabled(enable);
    }
}
