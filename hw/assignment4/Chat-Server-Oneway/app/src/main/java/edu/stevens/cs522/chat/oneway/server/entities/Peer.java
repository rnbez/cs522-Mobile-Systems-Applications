package edu.stevens.cs522.chat.oneway.server.entities;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import java.net.InetAddress;
import java.net.UnknownHostException;

import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;

/**
 * Created by Rafael on 2/22/2016.
 */
public class Peer implements Parcelable{
    protected long id;
    protected String name;
    protected InetAddress address;
    protected String port;


    public Peer() {
    }

    public Peer(long id, String name, InetAddress address, String port) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.port = port;
    }

    protected Peer(Parcel in) {
        id = in.readLong();
        name = in.readString();
        byte[] addr = new byte[1024];
        in.readByteArray(addr);
        try {
            address = InetAddress.getByAddress(addr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        port = in.readString();
    }

    public static final Creator<Peer> CREATOR = new Creator<Peer>() {
        @Override
        public Peer createFromParcel(Parcel in) {
            return new Peer(in);
        }

        @Override
        public Peer[] newArray(int size) {
            return new Peer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeValue(address);
        dest.writeString(port);
    }

    public void writeToProvider(ContentValues values) {
        PeerContract.putName(values, name);
        PeerContract.putAddress(values, address);
        PeerContract.putPort(values, port);
    }

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

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
