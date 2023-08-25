package com.aliyun.sls.android.network_diagnosis;

import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.Response;
import com.aliyun.sls.android.network_diagnosis.INetworkDiagnosis.Type;
import com.aliyun.sls.testable.BaseTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yulong.gyl
 * @date 2023/8/18
 */
public class INetworkDiagnosisTests extends BaseTestCase {

    @Test
    public void response_Response() {
        Response response = Response.response("id", Type.DNS, "content");

        assertThat(response.context).isEqualTo("id");
        assertThat(response.type).isEqualTo(Type.DNS);
        assertThat(response.content).isEqualTo("content");
    }

    @Test
    public void response_Error() {
        Response response = Response.error("error");

        assertThat(response.error).isEqualTo("error");
    }

    @Test
    public void type() {
        assertThat(Type.of("http")).isEqualTo(Type.HTTP);
        assertThat(Type.of("ping")).isEqualTo(Type.PING);
        assertThat(Type.of("tcpping")).isEqualTo(Type.TCPPING);
        assertThat(Type.of("mtr")).isEqualTo(Type.MTR);
        assertThat(Type.of("dns")).isEqualTo(Type.DNS);
        assertThat(Type.of("xxx")).isEqualTo(Type.UNKNOWN);
    }
}
