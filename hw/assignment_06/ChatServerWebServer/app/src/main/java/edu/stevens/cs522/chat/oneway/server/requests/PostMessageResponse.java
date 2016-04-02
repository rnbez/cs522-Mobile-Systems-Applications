package edu.stevens.cs522.chat.oneway.server.requests;

import android.os.Parcel;

/**
 * Created by Rafael on 3/13/2016.
 */
public class PostMessageResponse extends Response {

    private long id;

    public PostMessageResponse() {

    }

    public PostMessageResponse(Parcel in) {
        this.id = in.readLong();

    }
    public static final Creator<PostMessageResponse> CREATOR = new Creator<PostMessageResponse>() {
        @Override
        public PostMessageResponse createFromParcel(Parcel in) {
            return new PostMessageResponse(in);
        }

        @Override
        public PostMessageResponse[] newArray(int size) {
            return new PostMessageResponse[size];
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
