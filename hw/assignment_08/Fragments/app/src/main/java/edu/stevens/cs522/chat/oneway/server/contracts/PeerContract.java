package edu.stevens.cs522.chat.oneway.server.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.net.InetAddress;
import java.net.UnknownHostException;

import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.managers.IEntityCreator;
import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * Created by Rafael on 2/22/2016.
 */
public class PeerContract {
    public static final String APP_NAMESPACE = App.APP_NAMESPACE;
    public static final String SCHEME = "content";
    public static final String AUTHORITY = APP_NAMESPACE;
    public static final String CONTENT = "peer";
    public static final int CURSOR_LOADER_ID = 2;

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
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE= "longitude";
    //    public static final String ADDRESS = "address";
//    public static final String PORT = "port";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    ID + " INTEGER PRIMARY KEY," +
                    NAME + " TEXT NOT NULL UNIQUE," +
                    LATITUDE + " REAL NOT NULL," +
                    LONGITUDE + " REAL NOT NULL" +
//                    ADDRESS + " BLOB NOT NULL," +
//                    PORT + " TEXT NOT NULL" +
                    ");";
    public static final IEntityCreator<Peer> DEFAULT_ENTITY_CREATOR = new IEntityCreator<Peer>() {
        @Override
        public Peer create(Cursor cursor) {
            return new Peer(cursor);
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

    public static long getId(Uri uri) {
        return Long.parseLong(uri.getLastPathSegment());
    }

    public static long getId(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(ID));
    }

    public static void putId(ContentValues values,  long id) {
        values.put(ID, id);
    }

    public static String getName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(NAME));
    }

    public static void putName(ContentValues values, String name) {
        values.put(NAME, name);
    }

    public static double getLatitude(Cursor cursor) {
        return cursor.getDouble(cursor.getColumnIndexOrThrow(LATITUDE));
    }

    public static void putLatitude(ContentValues values, double latidute) {
        values.put(LATITUDE, latidute);
    }

    public static double getLongitude(Cursor cursor) {
        return cursor.getDouble(cursor.getColumnIndexOrThrow(LONGITUDE));
    }

    public static void putLongitude(ContentValues values, double longitude) {
        values.put(LONGITUDE, longitude);
    }

//    public static InetAddress getAddress(Cursor cursor) {
//        byte[] addr = cursor.getBlob(cursor.getColumnIndexOrThrow(ADDRESS));
//        try {
//            return InetAddress.getByAddress(addr);
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static void putAddress(ContentValues values, InetAddress address) {
//        values.put(ADDRESS, address.getAddress());
//    }
//
//
//    public static String getPort(Cursor cursor) {
//        return cursor.getString(cursor.getColumnIndexOrThrow(PORT));
//    }
//
//    public static void putPort(ContentValues values, String port) {
//        values.put(PORT, port);
//    }


}
