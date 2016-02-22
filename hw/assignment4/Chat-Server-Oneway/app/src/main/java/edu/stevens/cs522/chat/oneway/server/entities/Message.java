package edu.stevens.cs522.chat.oneway.server.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;

/**
 * Created by Rafael on 2/22/2016.
 */
public class Message implements Parcelable {

    protected long id;
    protected String messageText;
    protected long peerId;
    protected String sender;

    public Message() {
    }

    public Message(long id, String messageText, String sender) {
        this.id = id;
        this.messageText = messageText;
        this.sender = sender;
    }

    public Message(long id, String messageText, String sender, long peerId) {
        this.id = id;
        this.messageText = messageText;
        this.sender = sender;
        this.peerId = peerId;
    }

    public Message(Parcel in) {
        id = in.readLong();
        messageText = in.readString();
        sender = in.readString();
        peerId = in.readInt();
    }

    public Message(Cursor in){
        id = MessageContract.getId(in);
        messageText = MessageContract.getMessage(in);
        sender = MessageContract.getSender(in);
        peerId = MessageContract.getPeerId(in);
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(messageText);
        dest.writeString(sender);
    }

    public void writeToProvider(ContentValues values) {
        MessageContract.putMessage(values, messageText);
        MessageContract.putPeerId(values, peerId);
    }

    @Override
    public String toString() {
        return sender + ">> " + messageText;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public static Creator<Message> getCREATOR() {
        return CREATOR;
    }
}
