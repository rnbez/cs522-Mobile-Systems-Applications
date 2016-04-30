package edu.stevens.cs522.chat.oneway.server.requests;

import android.os.Parcel;

/**
 * Created by Rafael on 3/13/2016.
 */
public class RegisterResponse extends Response {

    private long id;

    public RegisterResponse() {
    }

    public RegisterResponse(Parcel in) {
        this.id = in.readLong();

    }
    public static final Creator<RegisterResponse> CREATOR = new Creator<RegisterResponse>() {
        @Override
        public RegisterResponse createFromParcel(Parcel in) {
            return new RegisterResponse(in);
        }

        @Override
        public RegisterResponse[] newArray(int size) {
            return new RegisterResponse[size];
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
        dest.writeLong(this.id);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
