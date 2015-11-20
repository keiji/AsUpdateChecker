package io.keiji.asupdatechecker;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Endpoint {
    private static final String TAG = Endpoint.class.getSimpleName();

    private static final String ENDPOINT_URL = "https://dl.google.com/android/studio/patches/updates.xml";

    public static final String getUpdateState() throws IOException {
        StringBuffer sb = new StringBuffer();

        HttpsURLConnection httpsURLConnection = null;

        try {
            URL url = new URL(ENDPOINT_URL);
            httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));

            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException", e);

        } finally {
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }
        }
        return sb.toString();
    }
}
