package com.aliyun.sls.android.ot;

import com.aliyun.sls.android.ot.utils.TimeUtils;

/**
 * @author gordon
 * @date 2022/4/13
 */
public class RecordableSpan extends Span {
    private final ISpanProcessor spanProcessor;

    RecordableSpan(ISpanProcessor spanProcessor) {
        super();
        this.spanProcessor = spanProcessor;
    }

    @Override
    public boolean end() {
        this.end = TimeUtils.instance.now();
        boolean ret = super.end();

        if (!ret) {
            return false;
        }

        this.start = this.start / 1000;
        this.end = this.end / 1000;

        if (null != spanProcessor) {
            return spanProcessor.onEnd(this);
        }

        return false;
    }
}
