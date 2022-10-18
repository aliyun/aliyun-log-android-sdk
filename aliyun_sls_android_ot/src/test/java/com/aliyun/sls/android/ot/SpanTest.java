package com.aliyun.sls.android.ot;

import java.util.ArrayList;
import java.util.List;

import android.util.Pair;
import com.aliyun.sls.android.ot.Span.StatusCode;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author gordon
 * @date 2022/10/18
 */
public class SpanTest {
    @Test
    public void span_construct() {
        Span span = new Span();
        assertNotNull(span.attribute);
        assertNotNull(span.events);
        assertNotNull(span.resource);
        assertNotNull(span.links);
    }

    @Test
    public void span_setName() {
        Span span = new Span();
        span.setName("test_name");
        assertEquals("test_name", span.getName());
    }

    @Test
    public void span_setKind() {
        Span span = new Span();
        span.setKind(SpanKind.SERVER);
        assertEquals(SpanKind.SERVER, span.getKind());
    }

    @Test
    public void span_setTraceId() {
        Span span = new Span();
        span.setTraceId("traceid");
        assertEquals("traceid", span.getTraceId());
    }

    @Test
    public void span_setSpanId() {
        Span span = new Span();
        span.setSpanId("spanid");
        assertEquals("spanid", span.getSpanId());
    }

    @Test
    public void span_setParentSpanId() {
        Span span = new Span();
        span.setParentSpanId("parentspanid");
        assertEquals("parentspanid", span.getParentSpanId());
    }

    @Test
    public void span_setParent() {
        Span parent = new Span();
        parent.setTraceId("p-traceid");
        parent.setSpanId("p-spanid");

        Span span = new Span();
        span.setParent(parent);
        assertEquals("p-traceid", span.getTraceId());
        assertEquals("p-spanid", span.getParentSpanId());
    }

    @Test
    public void span_setStart() {
        final long start = System.nanoTime();
        Span span = new Span();
        span.setStart(start);
        assertEquals(start, span.getStart());
    }

    @Test
    public void span_setEnd() {
        final long end = System.nanoTime();
        Span span = new Span();
        span.setEnd(end);
        assertEquals(end, span.getEnd());
    }

    @Test
    public void span_setDuration() {
        final long duration = 222L;
        Span span = new Span();
        span.setDuration(duration);
        assertEquals(duration, span.getDuration());
    }

    @Test
    public void span_setStatus() {
        Span span = new Span();
        span.setStatus(StatusCode.OK);
        assertEquals(StatusCode.OK, span.getStatusCode());
    }

    @Test
    public void span_setStatusMessage() {
        Span span = new Span();
        span.setStatusMessage("status_message");
        assertEquals("status_message", span.getStatusMessage());
    }

    @Test
    public void span_setHost() {
        Span span = new Span();
        span.setHost("host");
        assertEquals("host", span.getHost());
    }

    @Test
    public void span_setService() {
        Span span = new Span();
        span.setService("service");
        assertEquals("service", span.getService());
    }

    @Test
    public void span_addAttribute() {
        Span span = new Span();
        Attribute attribute = Attribute.of("key", "value");
        span.addAttribute(attribute);
        assertTrue(span.attribute.contains(attribute));
    }

    @Test
    public void span_addAttribute2() {
        Span span = new Span();
        Attribute attribute1 = Attribute.of("key", "value");
        Attribute attribute2 = Attribute.of("key2", "value2");
        span.addAttribute(attribute1, attribute2);
        assertTrue(span.attribute.contains(attribute1));
        assertTrue(span.attribute.contains(attribute2));
    }

    @Test
    public void span_addAttribute3() {
        Span span = new Span();
        List<Attribute> attributes = new ArrayList<Attribute>() {
            {
                add(Attribute.of("key", "value"));
                add(Attribute.of("key2", "value2"));
            }
        };
        span.addAttribute(attributes);
        for (Attribute attribute : attributes) {
            assertTrue(span.attribute.contains(attribute));
        }
    }


    @Test
    public void span_addResource() {
        Span span = new Span();
        Resource res = Resource.of(Pair.create("key", "value"));
        span.addResource(res);

        for (Attribute attribute : span.resource.attributes) {
            assertEquals("key", attribute.key);
            assertEquals("value", attribute.value);
        }
    }
}
