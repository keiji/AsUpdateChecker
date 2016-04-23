package io.keiji.asupdatechecker;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class UpdateState {
    private static final String TAG = UpdateState.class.getSimpleName();

    public final List<Product> products = new ArrayList<>();

    public static UpdateState parse(InputStream is) {

        try {
            return new UpdateState(is);
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "ParserConfigurationException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        } catch (SAXException e) {
            Log.e(TAG, "SAXException", e);
        }

        return null;
    }

    private UpdateState(InputStream is) throws ParserConfigurationException, IOException, SAXException {
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(is);
        Element rootNode = document.getDocumentElement();
        NodeList productList = rootNode.getElementsByTagName("product");

        for (int i = 0; i < productList.getLength(); i++) {
            Node productNode = productList.item(i);
            products.add(new Product(productNode));
        }
    }

    public static class Product {

        public final String name;

        public final List<Channel> channels = new ArrayList<>();

        private Product(Node node) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                name = null;
                return;
            }
            Element element = (Element) node;

            name = element.getAttribute("name");

            NodeList channelList = element.getElementsByTagName("channel");
            for (int i = 0; i < channelList.getLength(); i++) {
                Node channelNode = channelList.item(i);
                channels.add(new Channel(channelNode));
            }
        }

        public static class Channel {

            public final String id;
            public final int majorVersion;
            public final String status;
            public final List<Build> builds = new ArrayList<>();

            private Channel(Node node) {
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    id = null;
                    majorVersion = -1;
                    status = null;
                    return;
                }
                Element element = (Element) node;

                id = element.getAttribute("id");

                int version = -1;
                try {
                    version = Integer.parseInt(element.getAttribute("majorVersion"));
                } catch (NumberFormatException e) {
                }

                majorVersion = version;
                status = element.getAttribute("status");

                NodeList buildList = element.getElementsByTagName("build");
                for (int i = 0; i < buildList.getLength(); i++) {
                    Node buildNode = buildList.item(i);
                    builds.add(new Build(id, buildNode));
                }

            }

            public static class Build {

                public final String channelId;
                public final String number;
                public final String version;

                private Build(String channelId, Node node) {
                    this.channelId = channelId;

                    if (node.getNodeType() != Node.ELEMENT_NODE) {
                        number = null;
                        version = null;
                        return;
                    }
                    Element element = (Element) node;
                    number = element.getAttribute("number");
                    version = element.getAttribute("version");
                }
            }
        }
    }

}
