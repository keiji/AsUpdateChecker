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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class UpdateState {
    private static final String TAG = UpdateState.class.getSimpleName();

    public final Map<String, Product> products = new HashMap<>();

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
        NodeList productList = rootNode.getChildNodes();

        for (int i = 0; i < productList.getLength(); i++) {
            Node productNode = productList.item(i);
            Product product = new Product(productNode);
            products.put(product.name, product);
        }
    }

    public static class Product {

        public final String name;

        public final Map<String, Channel> channels = new HashMap<>();

        private Product(Node node) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                name = null;
                return;
            }
            Element element = (Element) node;

            name = element.getAttribute("name");

            NodeList channelList = node.getChildNodes();
            for (int i = 0; i < channelList.getLength(); i++) {
                Node channelNode = channelList.item(i);
                Channel channel = new Channel(channelNode);
                channels.put(channel.status, channel);
            }
        }

        public static class Channel {

            public final String status;
            public final List<Build> builds = new ArrayList<>();

            private Channel(Node node) {
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    status = null;
                    return;
                }
                Element element = (Element) node;

                status = element.getAttribute("status");

                NodeList buildList = node.getChildNodes();
                for (int i = 0; i < buildList.getLength(); i++) {
                    Node buildNode = buildList.item(i);
                    Build build = new Build(buildNode);
                    builds.add(build);
                }

            }

            public static class Build {

                public final String number;
                public final String version;

                private Build(Node node) {
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
