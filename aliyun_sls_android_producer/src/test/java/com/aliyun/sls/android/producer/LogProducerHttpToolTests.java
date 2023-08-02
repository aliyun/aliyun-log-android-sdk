package com.aliyun.sls.android.producer;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import com.aliyun.sls.android.producer.utils.TimeUtils;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * @author yulong.gyl
 * @date 2023/8/2
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TimeUtils.class})
public class LogProducerHttpToolTests {
    @Test
    public void logProducerHttpTool_createConnection() {
        HttpURLConnection connection = null;
        try {
            connection = LogProducerHttpTool.createConnection("https://www.aliyun.com", "GET",
                new String[] {"k1", "v1", "k2", "v2"});
            assertThat(connection.getURL())
                .asInstanceOf(InstanceOfAssertFactories.URL_TYPE)
                .hasProtocol("https")
                .hasHost("www.aliyun.com");

            assertThat(connection.getRequestMethod()).isEqualTo("GET");

            assertThat(connection.getRequestProperties())
                .containsEntry("k1", new ArrayList<String>() {{add("v1");}})
                .containsEntry("k2", new ArrayList<String>() {{add("v2");}});

        } catch (Exception e) {
            assertThat(connection).as(e.getMessage()).isNotNull();
        }
    }

    @Test
    public void logProducerHttpTool_writeToConnection() throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        OutputStream outputStream = mock(OutputStream.class);

        when(connection.getOutputStream()).thenReturn(outputStream);
        doNothing().when(outputStream).write(any(byte[].class), any(int.class), any(int.class));

        final byte[] body = "request body".getBytes(StandardCharsets.UTF_8);
        LogProducerHttpTool.writeToConnection(connection, "POST", body);
        verify(outputStream).write(body, 0, body.length);
    }

    @Test
    public void logProducerHttpTool_processResponseHeader() {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        TimeUtils timeUtils = mock(TimeUtils.class);
        mockStatic(TimeUtils.class);
        when(TimeUtils.getInstance()).thenReturn(timeUtils);

        doNothing().when(timeUtils).updateServerTime(any(long.class));
        when(connection.getHeaderField("x-log-time")).thenReturn("1690957017");

        LogProducerHttpTool.processResponseHeader(connection);

        verify(timeUtils).updateServerTime(1690957017L);
    }

    @Test
    public void logProducerHttpTool_processResponse_with200() throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);

        assertThat(LogProducerHttpTool.processResponse(connection)).isEqualTo(200);
    }

    //@Test
    //public void logProducerHttpTool_processResponse_withNot200() throws Exception {
    //    HttpURLConnection connection = mock(HttpURLConnection.class);
    //    when(connection.getResponseCode()).thenReturn(300);
    //
    //    assertThat(LogProducerHttpTool.processResponse(connection)).isEqualTo(300);
    //}

    @Test
    public void logProducerHttpTool_shouldRetrySendData() throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(400);
        when(connection.getHeaderField("x-log-requestid")).thenReturn("12345678");
        assertThat(LogProducerHttpTool.shouldRetrySendData(connection)).isFalse();
    }

    //@Test
    //public void logProducerHttpTool_processResponse_with400() throws Exception {
    //    HttpURLConnection connection = mock(HttpURLConnection.class);
    //    InputStream inputStream = mock(InputStream.class);
    //    when(connection.getResponseCode()).thenReturn(400);
    //    when(connection.getErrorStream()).thenReturn(inputStream);
    //    when(inputStream.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(new Answer<Integer>() {
    //        int index = 2;
    //
    //        @Override
    //        public Integer answer(InvocationOnMock invocation) throws Throwable {
    //            return index--;
    //        }
    //    });
    //    //when(inputStream.read(any(byte[].class), any(int.class), any(int.class))).thenReturn(1);
    //
    //    assertThat(LogProducerHttpTool.processResponse(connection)).isEqualTo(400);
    //}
    //
    //@Test
    //public void logProducerHttpTool_processResponse_with400AndRequestId() throws Exception {
    //    HttpURLConnection connection = mock(HttpURLConnection.class);
    //    InputStream inputStream = mock(InputStream.class);
    //    when(connection.getResponseCode()).thenReturn(400);
    //    when(connection.getErrorStream()).thenReturn(inputStream);
    //    when(inputStream.read(any(byte[].class), any(int.class), any(int.class))).thenReturn(1);
    //    when(connection.getHeaderField("x-log-requestid")).thenReturn("12345678");
    //
    //    assertThat(LogProducerHttpTool.processResponse(connection)).isEqualTo(-1);
    //}
}
