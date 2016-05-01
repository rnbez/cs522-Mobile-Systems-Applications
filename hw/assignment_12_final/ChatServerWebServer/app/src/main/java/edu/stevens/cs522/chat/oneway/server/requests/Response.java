package edu.stevens.cs522.chat.oneway.server.requests;

import android.os.Parcelable;

/**
 * Created by Rafael on 3/12/2016.
 */
public abstract class Response implements Parcelable {
    public abstract boolean isValid();
}