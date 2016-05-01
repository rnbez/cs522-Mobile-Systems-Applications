package edu.stevens.cs522.chat.oneway.server.requests;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Parcel;
import android.util.JsonReader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * Created by Rafael on 3/12/2016.
 */
@SuppressLint("ParcelCreator")
public class Register extends Request {

    public Register(UUID regId, Peer client) {
        super(regId, client);
    }

    public Register(Parcel in) {
        super(in);
        super.registrationID = UUID.fromString(in.readString());
        super.client = in.readParcelable(Peer.class.getClassLoader());

    }

    public static final Creator<Register> CREATOR = new Creator<Register>() {
        @Override
        public Register createFromParcel(Parcel in) {
            return new Register(in);
        }

        @Override
        public Register[] newArray(int size) {
            return new Register[size];
        }
    };

    @Override
    public Map<String, String> getRequestHeaders() {
        Map<String, String> map = super.headers;
        map.put("X-latitude", String.valueOf(client.getLatitute()));
        map.put("X-longitude", String.valueOf(client.getLongitude()));
        return map;
    }

    @Override
    public Uri getRequestUri() {
//        /chat?username=joe&regid=067e6162-3b6f-4ae2-a171-2470b63dff00
        StringBuilder builder = new StringBuilder(App.DEFAULT_HOST);
        try {
            builder.append("/chat?username=")
                    .append(URLEncoder.encode(client.getName(), App.DEFAULT_ENCODING))
                    .append("&regid=")
                    .append(URLEncoder.encode(registrationID.toString(), App.DEFAULT_ENCODING));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Uri.parse(builder.toString());
    }

    @Override
    public String getRequestEntity() throws IOException {
        return null;
    }

    @Override
    public Response getResponse(HttpURLConnection connection, JsonReader rd) {
        RegisterResponse response = new RegisterResponse();
        try {
            rd.beginObject();
            while (rd.peek() != android.util.JsonToken.END_OBJECT) {
                String label = rd.nextName();
                switch (label){
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
        super.writeToParcel(dest, flags);
        dest.writeString(registrationID.toString());
        dest.writeParcelable(client, 0);
    }


}
