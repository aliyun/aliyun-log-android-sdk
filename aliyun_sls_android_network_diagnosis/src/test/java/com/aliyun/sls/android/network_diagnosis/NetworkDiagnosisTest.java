package com.aliyun.sls.android.network_diagnosis;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class NetworkDiagnosisTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    private NetworkDiagnosis diagnosis;

    @Before
    public void setup() {
        diagnosis = NetworkDiagnosis.getInstance();
        assertFalse(diagnosis.checkNetworkDiagnosis());

        NetworkDiagnosis.getInstance().setNetworkDiagnosis(mockNetworkDiagnosis);

        assertFalse(diagnosis.checkNetworkDiagnosis());
    }



    private INetworkDiagnosis mockNetworkDiagnosis = new INetworkDiagnosis() {
        @Override
        public void setPolicyDomain(String domain) {

        }

        @Override
        public void disableExNetworkInfo() {

        }

        @Override
        public void setMultiplePortsDetect(boolean enable) {

        }

        @Override
        public void registerCallback(Callback callback) {

        }

        @Override
        public void registerCallback(Callback2 callback) {

        }

        @Override
        public void updateExtensions(Map<String, String> extension) {

        }

        @Override
        public void registerHttpCredentialCallback(HttpCredentialCallback callback) {

        }

        @Override
        public void http(String url) {

        }

        @Override
        public void http(String url, Callback callback) {

        }

        @Override
        public void http(String url, Callback callback, HttpCredential credential) {

        }

        @Override
        public void http(HttpRequest request) {

        }

        @Override
        public void http(HttpRequest request, Callback2 callback) {

        }

        @Override
        public void ping(String domain) {

        }

        @Override
        public void ping(String domain, Callback callback) {

        }

        @Override
        public void ping(String domain, int size, Callback callback) {

        }

        @Override
        public void ping(String domain, int maxTimes, int timeout, Callback callback) {

        }

        @Override
        public void ping(String domain, int size, int maxTimes, int timeout, Callback callback) {

        }

        @Override
        public void ping(PingRequest pingRequest) {

        }

        @Override
        public void ping(PingRequest pingRequest, Callback2 callback) {

        }

        @Override
        public void tcpPing(String domain, int port) {

        }

        @Override
        public void tcpPing(String domain, int port, Callback callback) {

        }

        @Override
        public void tcpPing(String domain, int port, int maxTimes, Callback callback) {

        }

        @Override
        public void tcpPing(String domain, int port, int maxTimes, int timeout, Callback callback) {

        }

        @Override
        public void tcpPing(TcpPingRequest request) {

        }

        @Override
        public void tcpPing(TcpPingRequest request, Callback2 callback2) {

        }

        @Override
        public void mtr(String domain) {

        }

        @Override
        public void mtr(String domain, Callback callback) {

        }

        @Override
        public void mtr(String domain, int maxTTL, Callback callback) {

        }

        @Override
        public void mtr(String domain, int maxTTL, int maxPaths, Callback callback) {

        }

        @Override
        public void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, Callback callback) {

        }

        @Override
        public void mtr(String domain, int maxTTL, int maxPaths, int maxTimes, int timeout, Callback callback) {

        }

        @Override
        public void mtr(MtrRequest request) {

        }

        @Override
        public void mtr(MtrRequest request, Callback2 callback) {

        }

        @Override
        public void dns(String domain) {

        }

        @Override
        public void dns(String domain, Callback callback) {

        }

        @Override
        public void dns(String nameServer, String domain, Callback callback) {

        }

        @Override
        public void dns(String nameServer, String domain, String type, Callback callback) {

        }

        @Override
        public void dns(String nameServer, String domain, String type, int timeout, Callback callback) {

        }

        @Override
        public void dns(DnsRequest dnsRequest) {

        }

        @Override
        public void dns(DnsRequest dnsRequest, Callback2 callback) {

        }
    };


}