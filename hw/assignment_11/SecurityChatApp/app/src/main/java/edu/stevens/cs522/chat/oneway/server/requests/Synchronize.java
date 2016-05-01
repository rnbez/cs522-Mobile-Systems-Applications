package edu.stevens.cs522.chat.oneway.server.requests;

import android.os.Parcel;
import android.util.JsonWriter;
import android.net.Uri;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * Created by Rafael on 4/2/2016.
 */
//public class Synchronize extends Request {
//@SuppressLint("ParcelCreator")
public class Synchronize extends Request {

    final static String DEFAULT_CHATROOM = "_default";
    long sequentialNumber;
    ArrayList<Message> messages = null;

    public Synchronize(UUID uuid, Peer client, long seqnum, ArrayList<Message> messages) {
        super(uuid, client);
        this.sequentialNumber = seqnum;
        this.messages = messages;
    }

    public Synchronize(Parcel in) {
        super();
        super.registrationID = UUID.fromString(in.readString());
        super.client = in.readParcelable(Peer.class.getClassLoader());
        this.sequentialNumber = in.readLong();
        this.messages = new ArrayList<>();
        in.readTypedList(this.messages, Message.CREATOR);
    }

    @Override
    public Map<String, String> getRequestHeaders() {
        Map<String, String> map = super.headers;
        map.put("Content-Type", "application/json");
        map.put("X-latitude", String.valueOf(client.getLatitute()));
        map.put("X-longitude", String.valueOf(client.getLongitude()));
        return map;
    }

    @Override
    public Uri getRequestUri() {
//        http://localhost:81/chat/1?regid=067e6162-3b6f-4ae2-a171-2470b63dff00&seqnum=0
        StringBuilder builder = new StringBuilder(App.DEFAULT_HOST);
        try {
            builder.append("/chat/")
                    .append(URLEncoder.encode(String.valueOf(client.getId()), App.DEFAULT_ENCODING))
                    .append("?regid=")
                    .append(URLEncoder.encode(registrationID.toString(), App.DEFAULT_ENCODING))
                    .append("&seqnum=")
                    .append(URLEncoder.encode(String.valueOf(sequentialNumber), App.DEFAULT_ENCODING));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Uri.parse(builder.toString());
    }

    @Override
    public String getRequestEntity() throws IOException {
        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        this.write(writer);
        return out.toString();
    }

    public void write(JsonWriter wr) throws IOException {
        wr.beginArray();
        for (Message m : this.messages) {
            wr.beginObject();
            wr.name("chatroom");
            wr.value(m.getChatroom());
            wr.name("timestamp");
            wr.value(m.getTimestamp());
            wr.name("latitude");
            wr.value(m.getLatitute());
            wr.name("longitude");
            wr.value(m.getLongitude());
            wr.name("text");
            wr.value(m.getMessageText());
            wr.endObject();
        }

        wr.endArray();
        wr.flush();
    }

    @Override
    public Response getResponse(HttpURLConnection connection, JsonReader rd) {
        if (rd != null) {
            return getResponse(rd);
        } else {
            return null;
        }
    }

    public Response getResponse(JsonReader rd) {
        SyncResponse response = new SyncResponse();
        try {
            rd.beginObject();
            while (rd.peek() != JsonToken.END_OBJECT) {
                String label = rd.nextName();
                switch (label) {
                    case "clients":
                        rd.beginArray();
                        while (rd.peek() != JsonToken.END_ARRAY) {
                            rd.beginObject();
                            Peer client = null;
                            while (rd.peek() != JsonToken.END_OBJECT) {
                                if (client == null) client = new Peer();
                                label = rd.nextName();
                                switch (label) {
                                    case "sender":
                                        client.setName(rd.nextString());
                                        break;
                                    case "latitude":
                                        client.setLatitute(rd.nextDouble());
                                        break;
                                    case "longitude":
                                        client.setLongitude(rd.nextDouble());
                                        break;
                                    default:
                                        throw new IllegalArgumentException("Unknown Label " + label);
                                }
                            }
                            rd.endObject();
                            if (client != null) response.clients.add(client);
                        }
                        rd.endArray();
                        break;
                    case "messages":
                        rd.beginArray();
                        while (rd.peek() != JsonToken.END_ARRAY) {
                            rd.beginObject();
                            Message message = null;
                            while (rd.peek() != JsonToken.END_OBJECT) {
                                if (message == null) message = new Message();
                                label = rd.nextName();
                                switch (label) {
                                    case "timestamp":
                                        message.setTimestamp(rd.nextLong());
                                        break;
                                    case "latitude":
                                        message.setLatitute(rd.nextDouble());
                                        break;
                                    case "longitude":
                                        message.setLongitude(rd.nextDouble());
                                        break;
                                    case "seqnum":
                                        message.setSequentialNumber(rd.nextLong());
                                        break;
                                    case "sender":
                                        message.setSender(rd.nextString());
                                        break;
                                    case "text":
                                        message.setMessageText(rd.nextString());
                                        break;
                                    case "chatroom":
                                        message.setChatroom(rd.nextString());
                                        break;
                                    case "X-latitude":
                                    case "X-longitude":
                                        rd.nextDouble();
                                        break;
                                    default:
                                        throw new IllegalArgumentException("Unknown Label " + label);
                                }
                            }
                            rd.endObject();
                            if (message != null) response.messages.add(message);
                        }
                        rd.endArray();
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


    public static final Creator<Synchronize> CREATOR = new Creator<Synchronize>() {
        @Override
        public Synchronize createFromParcel(Parcel in) {
            return new Synchronize(in);
        }

        @Override
        public Synchronize[] newArray(int size) {
            return new Synchronize[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(registrationID.toString());
        dest.writeParcelable(client, 0);
        dest.writeLong(sequentialNumber);
        dest.writeTypedList(messages);
    }

}
