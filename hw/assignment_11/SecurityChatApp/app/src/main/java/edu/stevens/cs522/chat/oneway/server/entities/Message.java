package edu.stevens.cs522.chat.oneway.server.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.regex.Pattern;

import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;

/**
 * Created by Rafael on 2/22/2016.
 */
public class Message implements Parcelable {

    private static final char SEPARATOR_CHAR = '#';
    private static final Pattern SEPARATOR =
            Pattern.compile(Character.toString(SEPARATOR_CHAR), Pattern.LITERAL);

    protected long id;
    protected long sequentialNumber;
    protected String messageText;
    protected long timestamp;
    protected double latitute;
    protected double longitude;
    protected long peerId;
    protected String sender;
    protected long chatroomId;
    protected String chatroom;

    public Message() {
    }

    public Message(String receivedMessage) {
        String[] splitted = SEPARATOR.split(receivedMessage);
        if (splitted.length > 1) {
            this.sender = splitted[0];
            this.messageText = splitted[1].trim();
            this.timestamp = System.currentTimeMillis();
        }
    }

    public Message(long sequentialNumber, String messageText, String sender, String chatroom, double latitute, double longitude) {
//        this.id = id;
        this.sequentialNumber = sequentialNumber;
        this.messageText = messageText;
        this.sender = sender;
        this.chatroom = chatroom;
        this.latitute = latitute;
        this.longitude = longitude;
        this.timestamp = System.currentTimeMillis();
    }

    public Message(long sequentialNumber, String messageText, long peerId, String sender, long chatroomId, String chatroom, long timestamp, double latitute, double longitude) {
        this.chatroom = chatroom;
        this.chatroomId = chatroomId;
        this.messageText = messageText;
        this.peerId = peerId;
        this.sender = sender;
        this.sequentialNumber = sequentialNumber;
        this.timestamp = timestamp;
        this.latitute = latitute;
        this.longitude = longitude;
    }

    public Message(Cursor in) {
        id = MessageContract.getId(in);
        sequentialNumber = MessageContract.getSequentialNumber(in);
        messageText = MessageContract.getMessage(in);
        timestamp = MessageContract.getTimestamp(in);
        latitute = MessageContract.getLatitude(in);
        longitude = MessageContract.getLongitude(in);
        sender = MessageContract.getSender(in);
        peerId = MessageContract.getPeerId(in);
        chatroom = MessageContract.getChatroom(in);
        chatroomId = MessageContract.getChatroomId(in);
    }

    protected Message(Parcel in) {
        id = in.readLong();
        sequentialNumber = in.readLong();
        messageText = in.readString();
        timestamp = in.readLong();
        latitute = in.readDouble();
        longitude = in.readDouble();
        peerId = in.readLong();
        sender = in.readString();
        chatroomId = in.readLong();
        chatroom = in.readString();
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
        dest.writeLong(sequentialNumber);
        dest.writeString(messageText);
        dest.writeLong(timestamp);
        dest.writeDouble(latitute);
        dest.writeDouble(longitude);
        dest.writeLong(peerId);
        dest.writeString(sender);
        dest.writeLong(chatroomId);
        dest.writeString(chatroom);
    }

    public void writeToProvider(ContentValues values) {
//        MessageContract.putId(values, id);
        MessageContract.putSequentialNumber(values, sequentialNumber);
        MessageContract.putMessage(values, messageText);
        MessageContract.putTimestamp(values, timestamp);
        MessageContract.putPeerId(values, peerId);
        MessageContract.putChatroomId(values, chatroomId);
        MessageContract.putLatitude(values, latitute);
        MessageContract.putLongitude(values, longitude);
    }

    @Override
    public String toString() {
        return sender + "#" + messageText;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSequentialNumber() {
        return sequentialNumber;
    }

    public void setSequentialNumber(long sequential_number) {
        this.sequentialNumber = sequential_number;
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

    public long getPeerId() {
        return peerId;
    }

    public void setPeerId(long peerId) {
        this.peerId = peerId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getChatroom() {
        return chatroom;
    }

    public void setChatroom(String chatroom) {
        this.chatroom = chatroom;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(long chatroomId) {
        this.chatroomId = chatroomId;
    }

    public double getLatitute() {
        return latitute;
    }

    public void setLatitute(double latitute) {
        this.latitute = latitute;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public static Creator<Message> getCREATOR() {
        return CREATOR;
    }

}
