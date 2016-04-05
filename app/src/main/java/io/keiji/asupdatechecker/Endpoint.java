package io.keiji.asupdatechecker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import io.realm.Realm;

public class Endpoint {
    private static final String TAG = Endpoint.class.getSimpleName();

    private static final String ENDPOINT_URL = "https://dl.google.com/android/studio/patches/updates.xml";

    public static EndpointResult getUpdateState() {
        HttpsURLConnection httpsURLConnection = null;

        try {
            URL url = new URL(ENDPOINT_URL);
            httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.connect();

            UpdateState updateState = UpdateState.parse(httpsURLConnection.getInputStream());

            return new EndpointResult(updateState);
        } catch (MalformedURLException e) {
            return new EndpointResult(e);
        } catch (IOException e) {
            return new EndpointResult(e);
        } finally {
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }
        }
    }

    public static class EndpointResult {
        public final UpdateState updateState;
        public final Exception exception;

        public EndpointResult(UpdateState updateState) {
            this.updateState = updateState;
            this.exception = null;
        }

        public EndpointResult(Exception exception) {
            this.updateState = null;
            this.exception = exception;
        }
    }
}
