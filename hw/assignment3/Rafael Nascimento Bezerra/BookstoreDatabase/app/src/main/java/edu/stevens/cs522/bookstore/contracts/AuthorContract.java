package edu.stevens.cs522.bookstore.contracts;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Rafael on 2/14/2016.
 */
public class AuthorContract {
    public static final String ID = "_id";
    public static final String FISRT_NAME = "first_name";
    public static final String MID_NAME = "mid_name";
    public static final String LAST_NAME = "last_name";
    public static final String BOOK_ID = "book_fk";

    public static int getId(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(ID));
    }

    public static void putId(ContentValues values, int id) {
        values.put(ID, id);
    }

    public static String getFirstName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(FISRT_NAME));
    }

    public static void putFirstName(ContentValues values, String first_name) {
        values.put(FISRT_NAME, first_name);
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

    public static void putBookId(ContentValues values, int book_id) {
        values.put(BOOK_ID, book_id);
    }
}
