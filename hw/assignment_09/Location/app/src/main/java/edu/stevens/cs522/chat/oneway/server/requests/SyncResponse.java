package edu.stevens.cs522.chat.oneway.server.requests;

import android.annotation.SuppressLint;
import android.os.Parcel;

import java.util.ArrayList;

import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;

/**
 * Created by Rafael on 4/3/2016.
 */
//@SuppressLint("ParcelCreator")
public class SyncResponse extends Response {

    ArrayList<Peer> clients = null;
    ArrayList<Message> messages = null;

    public SyncResponse() {
        this.clients = new ArrayList<>();
        this.messages = new ArrayList<>();

    }

    public SyncResponse(Parcel in) {
//        this.clients = (ArrayList<Peer>) in.readValue(ArrayList.class.getClassLoader());
//        this.messages = (ArrayList<Message>) in.readValue(ArrayList.class.getClassLoader());

//        in.readList(this.clients, null);
//        in.readList(this.messages, null);
        this.clients = new ArrayList<>();
        in.readTypedList(this.clients, Peer.CREATOR);
        this.messages = new ArrayList<>();
        in.readTypedList(this.messages, Message.CREATOR);
    }



    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeValue(messages);
//        dest.writeValue(messages);
//        dest.writeList(clients);
//        dest.writeList(messages);
        dest.writeTypedList(clients);
        dest.writeTypedList(messages);
    }
    public static final Creator<SyncResponse> CREATOR = new Creator<SyncResponse>() {
        @Override
        public SyncResponse createFromParcel(Parcel in) {
            return new SyncResponse(in);
        }

        @Override
        public SyncResponse[] newArray(int size) {
            return new SyncResponse[size];
        }
    };
}
