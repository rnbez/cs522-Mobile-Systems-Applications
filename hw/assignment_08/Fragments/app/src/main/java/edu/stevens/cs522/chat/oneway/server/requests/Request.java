package edu.stevens.cs522.chat.oneway.server.requests;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * Created by Rafael on 3/12/2016.
 */
public abstract class Request implements Parcelable {

    //    public long clientID;
    public Peer client;
    public UUID registrationID; // sanity check
    protected Map<String, String> headers;
    protected String requestEntity;

    public Request() {
        headers = new HashMap<>();
    }

    public Request(UUID registrationID, Peer client) {
        headers = new HashMap<>();
        this.client = client;
        this.registrationID = registrationID;
    }

    protected Request(Parcel in) {
        this();
        requestEntity = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(requestEntity);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // App-specific HTTP request headers.
    public abstract Map<String, String> getRequestHeaders();

    // Chat service URI with parameters e.g. query string parameters.
    public abstract Uri getRequestUri();

    // JSON body (if not null) for request data not passed in headers.
    public abstract String getRequestEntity() throws IOException;

    // Define your own Response class, including HTTP response code.
    public abstract Response getResponse(HttpURLConnection connection, JsonReader rd /* Null for streaming */);

    public UUID getRegistrationID() {
        return registrationID;
    }

}
