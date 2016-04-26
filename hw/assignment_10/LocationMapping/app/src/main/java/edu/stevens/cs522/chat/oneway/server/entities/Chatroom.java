package edu.stevens.cs522.chat.oneway.server.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.chat.oneway.server.contracts.ChatroomContract;

/**
 * Created by Rafael on 4/10/2016.
 */
public class Chatroom implements Parcelable {

    private final static String SEPARATOR = "###";

    protected long id;
    protected String name;

    public Chatroom() {
    }

    public Chatroom(String chatroom) {
        String[] arr = chatroom.split(SEPARATOR);
        if (arr.length > 1) {
            this.id = Long.valueOf(arr[0]);
            this.name = arr[1];
        } else {
            this.name = arr[0];
        }
    }

    public Chatroom(long id, String name) {
        this.id = id;
        this.name = name;
    }

    protected Chatroom(Parcel in) {
        id = in.readLong();
        name = in.readString();
    }

    public Chatroom(Cursor in) {
        id = ChatroomContract.getId(in);
        name = ChatroomContract.getName(in);
    }


    public static final Creator<Chatroom> CREATOR = new Creator<Chatroom>() {
        @Override
        public Chatroom createFromParcel(Parcel in) {
            return new Chatroom(in);
        }

        @Override
        public Chatroom[] newArray(int size) {
            return new Chatroom[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
    }

    public void writeToProvider(ContentValues values) {
        ChatroomContract.putName(values, name);
    }

    @Override
    public String toString() {
        return String.valueOf(this.id) + SEPARATOR + this.name;
    }
}
