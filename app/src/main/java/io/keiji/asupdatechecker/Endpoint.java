package io.keiji.asupdatechecker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Endpoint {

    private static final String ENDPOINT_URL = "https://dl.google.com/android/studio/patches/updates.xml";

    public static final String getUpdateState() throws IOException {

        HttpsURLConnection httpsURLConnection = null;

        try {
            URL url = new URL(ENDPOINT_URL);
            httpsURLConnection = (HttpsURLConnection) url.openConnection();
            return httpsURLConnection.getContentEncoding();
        } catch (MalformedURLException e) {
        } finally {
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }
        }
        return null;
    }
}
