package edu.stevens.cs522.chat.oneway.server.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.chat.oneway.server.entities.Chatroom;
import edu.stevens.cs522.chat.oneway.server.managers.IEntityCreator;
import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * Created by Rafael on 4/10/2016.
 */
public class ChatroomContract {
    public static final String APP_NAMESPACE = App.APP_NAMESPACE;
    public static final String SCHEME = "content";
    public static final String AUTHORITY = APP_NAMESPACE;
    public static final String CONTENT = "chatroom";
    public static final int CURSOR_LOADER_ID = 3;

    public static final Uri CONTENT_URI = new Uri.Builder()
            .scheme(SCHEME)
            .authority(AUTHORITY)
            .path(CONTENT + "s")
            .build();
    public static final String CONTENT_TYPE = "vnd.android.cursor/vnd."
            + APP_NAMESPACE + "."
            + CONTENT + "s";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd."
            + APP_NAMESPACE + "."
            + CONTENT;

    public static final String TABLE_NAME = CONTENT + "s";
    public static final String ID = "_id";
    public static final String ID_FULL = TABLE_NAME + "." + ID;
    public static final String NAME = "name";
    public static final String NAME_FULL =  TABLE_NAME + "." + NAME;

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    ID + " INTEGER PRIMARY KEY," +
                    NAME + " TEXT NOT NULL UNIQUE" +
                    ");";

    public static final IEntityCreator<Chatroom> DEFAULT_ENTITY_CREATOR = new IEntityCreator<Chatroom>() {
        @Override
        public Chatroom create(Cursor cursor) {
            return new Chatroom(cursor);
        }
    };


    public static Uri withExtendedPath(Object path) {
        return withExtendedPath(CONTENT_URI, path);
    }

    public static Uri withExtendedPath(Uri uri, Object path){
        if (path != null) {
            String stringPath = String.valueOf(path);
            Uri.Builder builder = uri.buildUpon();
            if (!stringPath.isEmpty()) {
                builder.appendPath(stringPath);
            }
            return builder.build();
        } else {
            throw new IllegalArgumentException("Null argument path: " + path);
        }
    }

    public static Uri getMessagesUri(long id) {
        Uri uri = withExtendedPath(withExtendedPath(id), "messages");
        return uri;
    }

    public static long getId(Uri uri) {
        return Long.parseLong(uri.getLastPathSegment());
    }

    public static long getId(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(ID));
    }

    public static void putId(ContentValues values, long id) {
        values.put(ID, id);
    }

    public static String getName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(NAME));
    }

    public static void putName(ContentValues values, String name) {
        values.put(NAME, name);
    }

}
