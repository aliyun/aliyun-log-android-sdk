package com.aliyun.sls.android.ot;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Pair;
import com.aliyun.sls.android.ot.Span.StatusCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void span_addEvent() {
        Span span = new Span();
        span.addEvent("e-name");

        for (Event event : span.events) {
            assertEquals("e-name", event.getName());
        }
    }

    @Test
    public void span_addEvent2() {
        Span span = new Span();
        span.addEvent("e-name", Attribute.of("key", "value"), Attribute.of("key2", "value2"));

        for (Event event : span.events) {
            assertEquals("e-name", event.getName());
            assertEquals("key", event.getAttributes().get(0).key);
            assertEquals("value", event.getAttributes().get(0).value);

            assertEquals("key2", event.getAttributes().get(1).key);
            assertEquals("value2", event.getAttributes().get(1).value);
        }
    }

    @Test
    public void span_addEvent3() {
        Span span = new Span();
        List<Attribute> attributes = new ArrayList<Attribute>() {
            {
                add(Attribute.of("key", "value"));
                add(Attribute.of("key2", "value2"));
            }
        };
        span.addEvent("e-name", attributes);

        for (Event event : span.events) {
            assertEquals("e-name", event.getName());

            assertEquals("key", event.getAttributes().get(0).key);
            assertEquals("value", event.getAttributes().get(0).value);

            assertEquals("key2", event.getAttributes().get(1).key);
            assertEquals("value2", event.getAttributes().get(1).value);
        }
    }

    @Test
    public void span_recordException() {
        Span span = new Span();
        Throwable t = new IllegalArgumentException("illegal argument");
        span.recordException(t);

        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            t.printStackTrace(printWriter);
        }

        for (Event event : span.events) {
            assertEquals("exception", event.getName());
            assertEquals("exception.type", event.getAttributes().get(0).key);
            assertEquals(t.getClass().getCanonicalName(), event.getAttributes().get(0).value);

            assertEquals("exception.message", event.getAttributes().get(1).key);
            assertEquals((TextUtils.isEmpty(t.getMessage()) ? "" : t.getMessage()), event.getAttributes().get(1).value);

            assertEquals("exception.stacktrace", event.getAttributes().get(2).key);
            assertEquals(stringWriter.toString(), event.getAttributes().get(2).value);
        }
    }

    @Test
    public void span_addLink() {
        Span span = new Span();
        Link link = Link.create("00000018361438910000001022299876", "6c264809643e04e6");
        span.addLink(link);

        for (Link lin : span.links) {
            assertEquals("00000018361438910000001022299876", lin.getTraceId());
            assertEquals("6c264809643e04e6", lin.getSpanId());
        }
    }

    @Test
    public void span_addLink2() {
        Span span = new Span();
        Link link = Link.create("00000018361438910000001022299876", "6c264809643e04e6");
        link.addAttribute(Attribute.of("key", "value"));

        span.addLink(link);

        for (Link lin : span.links) {
            assertEquals("00000018361438910000001022299876", lin.getTraceId());
            assertEquals("6c264809643e04e6", lin.getSpanId());

            assertEquals("key", lin.getAttributes().get(0).key);
            assertEquals("value", lin.getAttributes().get(0).value);
        }
    }

    @Test
    public void span_addLink3() {
        Span span = new Span();
        Link link = Link.create("00000018361438910000001022299876", "6c264809643e04e6");
        link.addAttribute(Attribute.of("key", "value"), Attribute.of("key2", "value2"));

        span.addLink(link);

        for (Link lin : span.links) {
            assertEquals("00000018361438910000001022299876", lin.getTraceId());
            assertEquals("6c264809643e04e6", lin.getSpanId());

            assertEquals("key", lin.getAttributes().get(0).key);
            assertEquals("value", lin.getAttributes().get(0).value);

            assertEquals("key2", lin.getAttributes().get(1).key);
            assertEquals("value2", lin.getAttributes().get(1).value);
        }
    }

    @Test
    public void span_addLink4() {
        Span span = new Span();
        Link link = Link.create("00000018361438910000001022299876", "6c264809643e04e6");
        List<Attribute> attributes = new ArrayList<Attribute>() {
            {
                add(Attribute.of("key", "value"));
                add(Attribute.of("key2", "value2"));
            }
        };
        link.addAttribute(attributes);

        span.addLink(link);

        for (Link lin : span.links) {
            assertEquals("00000018361438910000001022299876", lin.getTraceId());
            assertEquals("6c264809643e04e6", lin.getSpanId());

            assertEquals("key", lin.getAttributes().get(0).key);
            assertEquals("value", lin.getAttributes().get(0).value);

            assertEquals("key2", lin.getAttributes().get(1).key);
            assertEquals("value2", lin.getAttributes().get(1).value);
        }
    }

    @Test
    public void span_end() {
        Span span = new Span();
        assertFalse(span.isEnd());
        assertEquals(0L, span.duration);
        assertEquals(0L, span.end);

        span.end();
        assertTrue(span.isEnd());
        assertEquals(span.duration, (span.end - span.start) / 1000);
    }

    @Test
    public void span_toData() {
        final long start = System.nanoTime();
        final long end = start + 1002;
        Span span = new Span();
        span.setStart(start);
        span.setEnd(end);
        span.setName("s-name");
        span.setTraceId("00000018361438910000001022299876");
        span.setSpanId("6c264809643e04e6");
        span.end();

        final Map<String, String> data = span.toData();
        assertEquals(start, Long.parseLong(data.get("start")));
        assertEquals(end, Long.parseLong(data.get("end")));
        assertEquals(span.duration, Long.parseLong(data.get("duration")));
        assertEquals("s-name", data.get("name"));
        assertEquals(span.kind.kind, data.get("kind"));
        assertEquals("00000018361438910000001022299876", data.get("traceID"));
        assertEquals("6c264809643e04e6", data.get("spanID"));
        assertEquals(null, data.get("parentSpanID"));
        assertEquals(null, data.get("sid"));
        assertEquals(null, data.get("pid"));
        assertEquals(StatusCode.UNSET.code, data.get("statusCode"));
        assertEquals(null, data.get("statusMessage"));
        assertEquals(null, data.get("host"));
        assertEquals("Android", data.get("service"));

    }

}
