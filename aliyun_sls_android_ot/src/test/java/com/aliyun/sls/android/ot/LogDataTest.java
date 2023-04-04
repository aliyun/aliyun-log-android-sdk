package com.aliyun.sls.android.ot;

import android.util.Pair;
import com.aliyun.sls.android.ot.logs.LogData.Builder;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author gordon
 * @date 2023/2/2
 */
public class LogDataTest {

    @Test
    public void testBuilder() throws Exception {
        // no parameters
        assertJSON(newBuilder().build().toJson());

        // null resource
        assertJSON(newBuilder().setResource(null).build().toJson());

        // null attribute
        assertJSON(newBuilder().setAttribute(null).build().toJson());

        // null or empty log content
        assertJSON(newBuilder().setLogContent(null).build().toJson());
        assertJSON(newBuilder().setLogContent("").build().toJson());
    }

    @Test
    public void testBuilder_resource() throws Exception {
        System.out.println(newBuilder().setResource(Resource.of("key", "value")).build().toJson());
        JSONAssert.assertEquals(
            "{"
                + "  \"resource\": {"
                + "    \"attributes\": ["
                + "      {"
                + "        \"key\": \"key\","
                + "        \"value\": {"
                + "          \"stringValue\": \"value\""
                + "        }"
                + "      }"
                + "    ]"
                + "  },"
                + "  \"scopeLogs\": ["
                + "    {"
                + "      \"scope\": {"
                + "        \"name\": \"log\","
                + "        \"attributes\": ["
                + "          "
                + "        ],"
                + "        \"version\": 1"
                + "      },"
                + "      \"logRecords\": ["
                + "        {"
                + "          \"severityText\": \"ERROR\","
                + "          \"timeUnixNano\": 1675304593811004,"
                + "          \"attributes\": ["
                + "            "
                + "          ],"
                + "          \"severityNumber\": \"SEVERITY_NUMBER_ERROR\","
                + "          \"body\": {"
                + "            "
                + "          }"
                + "        }"
                + "      ]"
                + "    }"
                + "  ]"
                + "}",
            newBuilder().setResource(Resource.of("key", "value")).build().toJson(),
            false);
    }

    @Test
    public void testBuilder_body() throws Exception{
        System.out.println(newBuilder().setLogContent("test body").build().toJson());
        JSONAssert.assertEquals(
            "{"
                + "  \"resource\": {"
                + "    "
                + "  },"
                + "  \"scopeLogs\": ["
                + "    {"
                + "      \"scope\": {"
                + "        \"name\": \"log\","
                + "        \"attributes\": ["
                + "          "
                + "        ],"
                + "        \"version\": 1"
                + "      },"
                + "      \"logRecords\": ["
                + "        {"
                + "          \"severityText\": \"ERROR\","
                + "          \"timeUnixNano\": 1675304593811004,"
                + "          \"attributes\": ["
                + "            "
                + "          ],"
                + "          \"severityNumber\": \"SEVERITY_NUMBER_ERROR\","
                + "          \"body\": {"
                + "            \"stringValue\": \"test body\""
                + "          }"
                + "        }"
                + "      ]"
                + "    }"
                + "  ]"
                + "}",
            newBuilder().setLogContent("test body").build().toJson(),
            false);
    }

    @Test
    public void testBuilder_attribute() throws Exception{
        System.out.println(newBuilder().setLogContent("test body").setAttribute(Attribute.of(Pair.create("test_key", "test_value"))).build().toJson());
        JSONAssert.assertEquals(
            "{"
                + "  \"resource\": {"
                + "    "
                + "  },"
                + "  \"scopeLogs\": ["
                + "    {"
                + "      \"scope\": {"
                + "        \"name\": \"log\","
                + "        \"attributes\": ["
                + "          "
                + "        ],"
                + "        \"version\": 1"
                + "      },"
                + "      \"logRecords\": ["
                + "        {"
                + "          \"severityText\": \"ERROR\","
                + "          \"timeUnixNano\": 1675304593811004,"
                + "          \"attributes\": ["
                + "            "
                + "          ],"
                + "          \"severityNumber\": \"SEVERITY_NUMBER_ERROR\","
                + "          \"body\": {"
                + "            \"stringValue\": \"test body\""
                + "          },"
                + "          \"attributes\": ["
                + "            {"
                + "              \"key\": \"test_key\","
                + "              \"value\": {"
                + "                \"stringValue\": test_value"
                + "              }"
                + "            }"
                + "          ]"
                + "          "
                + "        }"
                + "      ]"
                + "    }"
                + "  ]"
                + "}",
            newBuilder().setLogContent("test body").setAttribute(Attribute.of(Pair.create("test_key", "test_value"))).build().toJson(),
            false);
    }

    private Builder newBuilder() {
        return new Builder().setEpochNanos(1675304593811004L * 1000);
    }

    private void assertJSON(JSONObject object) throws Exception {
        JSONAssert.assertEquals(
            "{\"resource\":{},\"scopeLogs\":[{\"scope\":{\"name\":\"log\",\"attributes\":[],\"version\":1},"
                + "\"logRecords\":[{\"severityText\":\"ERROR\",\"timeUnixNano\":1675304593811004,\"attributes\":[],"
                + "\"severityNumber\":\"SEVERITY_NUMBER_ERROR\",\"body\":{}}]}]}"
            , object
            , false);
    }
}
