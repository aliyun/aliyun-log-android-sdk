package com.aliyun.sls.android.ot;

import com.aliyun.sls.android.ot.context.ContextManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CoroutinesExtensionsTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void runWithContext() {
        Span contextSpan = new Span();
        ContextManager.INSTANCE.makeCurrent(contextSpan);

    }
}