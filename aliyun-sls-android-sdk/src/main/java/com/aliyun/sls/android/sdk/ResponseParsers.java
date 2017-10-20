package com.aliyun.sls.android.sdk;

import com.aliyun.sls.android.sdk.common.OSSHeaders;
import com.aliyun.sls.android.sdk.result.PostLogResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import okhttp3.Response;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public final class ResponseParsers {

    public static final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

    public static final class PostLogResponseParser extends AbstractResponseParser<PostLogResult> {

        @Override
        public PostLogResult parseData(Response response, PostLogResult result)
                throws IOException {

            return result;
        }
    }


    public static ServiceException parseResponseErrorXML(Response response, boolean isHeadRequest)
            throws IOException {

        int statusCode = response.code();
        String requestId = response.header(OSSHeaders.OSS_HEADER_REQUEST_ID);
        String code = null;
        String message = null;
        String hostId = null;
        String errorMessage = null;

        if (!isHeadRequest) {
            try {
                errorMessage = response.body().string();
                DocumentBuilder builder = domFactory.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(errorMessage));
                Document dom = builder.parse(is);
                Element element = dom.getDocumentElement();

                NodeList list = element.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    Node item = list.item(i);
                    String name = item.getNodeName();
                    if (name == null) continue;

                    if (name.equals("Code")) {
                        code = checkChildNotNullAndGetValue(item);
                    }
                    if (name.equals("Message")) {
                        message = checkChildNotNullAndGetValue(item);
                    }
                    if (name.equals("RequestId")) {
                        requestId = checkChildNotNullAndGetValue(item);
                    }
                    if (name.equals("HostId")) {
                        hostId = checkChildNotNullAndGetValue(item);
                    }
                }
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return new ServiceException(statusCode, message, code, requestId, hostId, errorMessage);
    }

    /**
     * Gets the first child node value if it's has a child node
     * @param item
     */
    public static String checkChildNotNullAndGetValue(Node item) {
        if (item.getFirstChild() != null) {
            return item.getFirstChild().getNodeValue();
        }
        return null;
    }

}
