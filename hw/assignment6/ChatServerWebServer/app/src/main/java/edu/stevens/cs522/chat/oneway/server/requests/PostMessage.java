package edu.stevens.cs522.chat.oneway.server.requests;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Rafael on 3/12/2016.
 */
@SuppressLint("ParcelCreator")
public class PostMessage extends Request {

    private long clientId;
    private String chatroom;
    private long timestamp;
    private String text;

    public PostMessage(UUID regid, int clientId, String text) {
        super();
        super.registrationID = regid;
        this.clientId = clientId;
        this.chatroom = "_default";
        this.timestamp = System.currentTimeMillis();
        this.text = text;

        super.headers.put("CONTENT_TYPE", "application/json");
        //            connection.setRequestProperty("CONTENT_TYPE","application/json");
    }

    public PostMessage(Parcel in) {
        super();
        super.registrationID = ((ParcelUuid)in.readParcelable(ParcelUuid.class.getClassLoader())).getUuid();
        this.clientId = in.readLong();
        this.chatroom = in.readString();
        this.timestamp = in.readLong();
        this.text = in.readString();
    }

    @Override
    public Map<String, String> getRequestHeaders() {
        return super.headers;
    }

    @Override
    public Uri getRequestUri() {
//        http://localhost:81/chat/1?regid=067e6162-3b6f-4ae2-a171-2470b63dff00
        StringBuilder builder = new StringBuilder(Request.DEFAULT_HOST);
        try {
            builder.append("/")
                    .append(URLEncoder.encode(String.valueOf(clientId), Request.DEFAULT_ENCODING))
                    .append("?regid=")
                    .append(URLEncoder.encode(registrationID.toString(), Request.DEFAULT_ENCODING));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Uri.parse(builder.toString());
    }

    @Override
    public String getRequestEntity() throws IOException {
        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.beginObject();
        writer.name("chatroom");
        writer.value(chatroom);
        writer.name("timestamp");
        writer.value(timestamp);
        writer.name("text");
        writer.value(text);
        writer.endObject();
        return out.toString();
    }

    @Override
    public Response getResponse(HttpURLConnection connection, JsonReader rd) {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(clientId);
        dest.writeParcelable(new ParcelUuid(registrationID), 0);
        dest.writeString(chatroom);
        dest.writeLong(timestamp);
        dest.writeString(text);

    }
}
