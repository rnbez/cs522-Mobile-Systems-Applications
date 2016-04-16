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

import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * Created by Rafael on 3/12/2016.
 */
public class PostMessage extends Request {

    Message message;

    public PostMessage(UUID regid, Peer client, Message message) {
        super(regid, client);
        this.message = message;
    }

    public PostMessage(Parcel in) {
        super(in);
        String registrationID = in.readString();
        super.registrationID = UUID.fromString(registrationID);
        this.message = in.readParcelable(Message.class.getClassLoader());
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
        map.put("X-latitude", String.valueOf(client.getLatidute()));
        map.put("X-longitude", String.valueOf(client.getLongitude()));
        return map;
    }

    @Override
    public Uri getRequestUri() {
//        http://localhost:81/chat/1?regid=067e6162-3b6f-4ae2-a171-2470b63dff00
        StringBuilder builder = new StringBuilder(App.DEFAULT_HOST);
        try {
            builder.append("/chat/")
                    .append(URLEncoder.encode(String.valueOf(message.getPeerId()), App.DEFAULT_ENCODING))
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
        writer.value(message.getChatroom());
        writer.name("timestamp");
        writer.value(message.getTimestamp());
        writer.name("text");
        writer.value(message.getMessageText());
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
        dest.writeString(String.valueOf(registrationID));
        dest.writeParcelable(message, 0);

    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
