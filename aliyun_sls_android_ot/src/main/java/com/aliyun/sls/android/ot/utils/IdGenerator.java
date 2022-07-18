package com.aliyun.sls.android.ot.utils;

import java.util.Random;

/**
 * @author gordon
 * @date 2022/3/31
 */
public final class IdGenerator {
    private static final int TRACE_ID_BYTES_LENGTH = 16;
    private static final int TRACE_ID_HEX_LENGTH = 2 * TRACE_ID_BYTES_LENGTH;

    private static final int SPAN_ID_BYTES_LENGTH = 8;
    public static final int SPAN_ID_HEX_LENGTH = 2 * SPAN_ID_BYTES_LENGTH;

    private static final long INVALID_ID = 0;
    private static final ThreadLocal<char[]> CHAR_ARRAY = new ThreadLocal<>();

    private static final Random random = new Random();

    public static String generateTraceId() {
        long idHi = random.nextLong();
        long idLo;
        do {
            idLo = random.nextLong();
        } while (INVALID_ID == idLo);

        char[] chars = temporaryBuffers(TRACE_ID_HEX_LENGTH);
        OtelEncodingUtils.longToBase16String(idHi, chars, 0);
        OtelEncodingUtils.longToBase16String(idLo, chars, 16);
        return new String(chars, 0, TRACE_ID_HEX_LENGTH);
    }

    public static String generateSpanId() {
        long id;
        do {
            id = random.nextLong();
        } while (id == INVALID_ID);
        char[] result = temporaryBuffers(SPAN_ID_HEX_LENGTH);
        OtelEncodingUtils.longToBase16String(id, result, 0);
        return new String(result, 0, SPAN_ID_HEX_LENGTH);
    }

    private static char[] temporaryBuffers(int len) {
        char[] buffer = CHAR_ARRAY.get();
        if (null == buffer || buffer.length < len) {
            buffer = new char[len];
            CHAR_ARRAY.set(buffer);
        }
        return buffer;
    }

}
