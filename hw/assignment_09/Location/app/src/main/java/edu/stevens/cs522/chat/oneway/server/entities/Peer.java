package edu.stevens.cs522.chat.oneway.server.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;

/**
 * Created by Rafael on 2/22/2016.
 */
public class Peer implements Parcelable{
    protected long id;
    protected String name;
    protected double latitute;
    protected double longitude;
    protected String address;
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

    public Peer(String name, double latitute, double longitude) {
        this.name = name;
        this.latitute = latitute;
        this.longitude = longitude;
        this.address = "-";
    }

    public Peer(long id, String name, double latitute, double longitude) {
        this.id = id;
        this.name = name;
        this.latitute = latitute;
        this.longitude = longitude;
        this.address = "-";
//        this.latitute = 40.7439905;
//        this.longitude = -74.0323626;
    }

    protected Peer(Parcel in) {
        id = in.readLong();
        name = in.readString();
        latitute = in.readDouble();
        longitude = in.readDouble();
        address = in.readString();

//        address = (InetAddress) in.readValue(InetAddress.class.getClassLoader());
//        port = in.readString();
    }

    public Peer(Cursor in){
        id = PeerContract.getId(in);
        name = PeerContract.getName(in);
        latitute = PeerContract.getLatitude(in);
        longitude = PeerContract.getLongitude(in);
        address = PeerContract.getAddress(in);
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
        dest.writeDouble(latitute);
        dest.writeDouble(longitude);
        dest.writeString(address);
    }

    public void writeToProvider(ContentValues values) {
        PeerContract.putName(values, name);
        PeerContract.putLatitude(values, latitute);
        PeerContract.putLongitude(values, longitude);
        PeerContract.putAddress(values, address);
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
