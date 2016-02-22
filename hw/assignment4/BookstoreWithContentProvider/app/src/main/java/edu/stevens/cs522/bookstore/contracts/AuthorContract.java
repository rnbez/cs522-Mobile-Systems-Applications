package edu.stevens.cs522.bookstore.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.bookstore.common.CommonAssumptions;
import edu.stevens.cs522.bookstore.entities.Author;

/**
 * Created by Rafael on 2/14/2016.
 */
public class AuthorContract {
    public static final String SCHEME = "content";
    public static final String AUTHORITY = CommonAssumptions.APP_NAMESPACE;
    public static final String CONTENT = "author";

    public static final Uri CONTENT_URI = new Uri.Builder()
            .scheme(SCHEME)
            .authority(AUTHORITY)
            .path(CONTENT + "s")
            .build();
    public static final String CONTENT_TYPE = "vnd.android.cursor/vnd."
            + CommonAssumptions.APP_NAMESPACE + "."
            + CONTENT + "s";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd."
            + CommonAssumptions.APP_NAMESPACE + "."
            + CONTENT;

    public static final String TABLE_NAME = CONTENT + "s";
    public static final String ID = "_id";
    public static final String FIRST_NAME = "first_name";
    public static final String MID_NAME = "mid_name";
    public static final String LAST_NAME = "last_name";
    public static final String BOOK_ID = "book_fk";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                ID + " INTEGER PRIMARY KEY," +
                FIRST_NAME + " TEXT NOT NULL," +
                MID_NAME + " TEXT," +
                LAST_NAME + " TEXT NOT NULL," +
                BOOK_ID + " INTEGER NOT NULL," +
                "FOREIGN KEY(" + BOOK_ID + ") REFERENCES " + BookContract.TABLE_NAME + "(_id) ON DELETE CASCADE" +
            ");"
            + "CREATE INDEX AuthorsBookIndex ON " + TABLE_NAME + "(" + BOOK_ID + ");";


    public static long getId(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(ID));
    }

    public static String getFirstName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(FIRST_NAME));
    }

    public static void putFirstName(ContentValues values, String first_name) {
        values.put(FIRST_NAME, first_name);
    }

    public static String getMiddleName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(MID_NAME));
    }

    public static void putMiddleName(ContentValues values, String mid_name) {
        values.put(MID_NAME, mid_name);
    }

    public static String getLastName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(LAST_NAME));
    }

    public static void putLastName(ContentValues values, String last_name) {
        values.put(LAST_NAME, last_name);
    }

    public static int getBookId(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(BOOK_ID));
    }

    public static void putBookId(ContentValues values, long book_id) {
        values.put(BOOK_ID, book_id);
    }

}
