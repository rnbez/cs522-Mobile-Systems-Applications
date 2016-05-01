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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * Created by Rafael on 3/12/2016.
 */
public class PostMessage extends Request {

    private long clientId;
    private String clientName;
    private String chatroom;
    private long timestamp;
    private String text;

    public PostMessage(UUID regid, long clientId, String clientName, String text) {
        super();
        super.registrationID = regid;
        this.clientId = clientId;
        this.chatroom = "_default";
        this.timestamp = System.currentTimeMillis();
        this.text = text;
        this.clientName = clientName;
    }

    public PostMessage(Parcel in) {
        super();
        this.clientId = in.readLong();
        String registrationID = in.readString();
        super.registrationID = UUID.fromString(registrationID);
        this.chatroom = in.readString();
        this.timestamp = in.readLong();
        this.text = in.readString();
        this.clientName = in.readString();
    }

    public static final Creator<PostMessage> CREATOR = new Creator<PostMessage>() {
        @Override
        public PostMessage createFromParcel(Parcel in) {
            return new PostMessage(in);
        }

        @Override
        public PostMessage[] newArray(int size) {
            return new PostMessage[size];
        }
    };

    @Override
    public Map<String, String> getRequestHeaders() {
        Map<String, String> map = super.headers;
        map.put("Content-Type", "application/json");
        return map;
    }

    @Override
    public Uri getRequestUri() {
//        http://localhost:81/chat/1?regid=067e6162-3b6f-4ae2-a171-2470b63dff00
        StringBuilder builder = new StringBuilder(App.DEFAULT_HOST);
        try {
            builder.append("/chat/")
                    .append(URLEncoder.encode(String.valueOf(clientId), App.DEFAULT_ENCODING))
                    .append("?regid=")
                    .append(URLEncoder.encode(registrationID.toString(), App.DEFAULT_ENCODING));

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
        PostMessageResponse response = new PostMessageResponse();
        try {
            rd.beginObject();
            while (rd.peek() != android.util.JsonToken.END_OBJECT) {
                String label = rd.nextName();
                switch (label) {
                    case "id":
                        response.setId(rd.nextLong());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown Label " + label);
                }
            }
            rd.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(clientId);
        dest.writeString(String.valueOf(registrationID));
        dest.writeString(chatroom);
        dest.writeLong(timestamp);
        dest.writeString(text);
        dest.writeString(clientName);

    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
