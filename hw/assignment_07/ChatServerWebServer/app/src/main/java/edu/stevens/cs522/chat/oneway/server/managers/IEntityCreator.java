package edu.stevens.cs522.chat.oneway.server.managers;

import android.database.Cursor;

/**
 * Created by Rafael on 2/21/2016.
 */
public interface IEntityCreator<T> {
    public T create(Cursor cursor);
}
