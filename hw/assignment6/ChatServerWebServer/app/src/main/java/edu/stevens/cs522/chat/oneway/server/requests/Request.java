package edu.stevens.cs522.chat.oneway.server.requests;

import android.net.Uri;
import android.os.Parcelable;
import android.util.JsonReader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Rafael on 3/12/2016.
 */
public abstract class Request implements Parcelable {
    public static final String DEFAULT_HOST = "http://localhost:81";
    public static final String DEFAULT_ENCODING = "UTF-8";

//    public long clientID;
    public UUID registrationID; // sanity check
    protected Map<String, String> headers;
    protected String requestEntity;

    public Request() {
        headers = new HashMap<>();
    }

    // App-specific HTTP request headers.
    public abstract Map<String, String> getRequestHeaders();

    // Chat service URI with parameters e.g. query string parameters.
    public abstract Uri getRequestUri();

    // JSON body (if not null) for request data not passed in headers.
    public abstract String getRequestEntity() throws IOException;

    // Define your own Response class, including HTTP response code.
    public abstract Response getResponse(HttpURLConnection connection, JsonReader rd /* Null for streaming */);
}