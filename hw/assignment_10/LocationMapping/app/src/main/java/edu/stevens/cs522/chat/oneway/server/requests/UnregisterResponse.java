package edu.stevens.cs522.chat.oneway.server.requests;

import android.os.Parcel;

/**
 * Created by Rafael on 3/13/2016.
 */
public class UnregisterResponse extends Response {

    public UnregisterResponse() {
    }

    public UnregisterResponse(Parcel in) {
//        this.id = in.readLong();

    }
    public static final Creator<UnregisterResponse> CREATOR = new Creator<UnregisterResponse>() {
        @Override
        public UnregisterResponse createFromParcel(Parcel in) {
            return new UnregisterResponse(in);
        }

        @Override
        public UnregisterResponse[] newArray(int size) {
            return new UnregisterResponse[size];
        }
    };

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

//        dest.writeLong(this.id);
    }

//    public long getId() {
//        return id;
//    }
//
//    public void setId(long id) {
//        this.id = id;
//    }
}
