package edu.stevens.cs522.chat.oneway.server.entities;

import android.content.ContentValues;
import android.database.Cursor;
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
    protected double latidute;
    protected double longitude;
//    protected InetAddress address;
//    protected String port;


    public Peer() {
//        try {
//            this.address = InetAddress.getLocalHost();
//            this.port = "0";
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
    }

    public Peer(String name, double latidute, double longitude) {
        this.name = name;
//        this.latidute = latidute;
//        this.longitude = longitude;
        this.latidute = 40.7439905;
        this.longitude = -74.0323626;
    }

    public Peer(long id, String name, double latidute, double longitude) {
        this.id = id;
        this.name = name;
//        this.latidute = latidute;
//        this.longitude = longitude;
        this.latidute = 40.7439905;
        this.longitude = -74.0323626;
    }

    protected Peer(Parcel in) {
        id = in.readLong();
        name = in.readString();
        latidute = in.readDouble();
        longitude = in.readDouble();

//        address = (InetAddress) in.readValue(InetAddress.class.getClassLoader());
//        port = in.readString();
    }

    public Peer(Cursor in){
        id = PeerContract.getId(in);
        name = PeerContract.getName(in);
        latidute = PeerContract.getLatitude(in);
        longitude = PeerContract.getLongitude(in);
//        address = PeerContract.getAddress(in);
//        port = PeerContract.getPort(in);
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
        dest.writeDouble(latidute);
        dest.writeDouble(longitude);
//        dest.writeValue(address);
//        dest.writeString(port);
    }

    public void writeToProvider(ContentValues values) {
//        if(id > 0){
//            PeerContract.putId(values, id);
//        }
        PeerContract.putName(values, name);
        PeerContract.putLatitude(values, latidute);
        PeerContract.putLongitude(values, longitude);
//        PeerContract.putAddress(values, address);
//        PeerContract.putPort(values, port);
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

    public double getLatidute() {
        return latidute;
    }

    public void setLatidute(double latidute) {
        this.latidute = latidute;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    //    public InetAddress getAddress() {
//        return address;
//    }
//
//    public void setAddress(InetAddress address) {
//        this.address = address;
//    }
//
//    public String getPort() {
//        return port;
//    }
//
//    public void setPort(String port) {
//        this.port = port;
//    }
}
