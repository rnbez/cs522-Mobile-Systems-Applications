package edu.stevens.cs522.bookstore.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Objects;
import java.util.regex.Pattern;

import edu.stevens.cs522.bookstore.common.CommonAssumptions;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;

/**
 * Created by Rafael on 2/13/2016.
 */
public final class BookContract {

    public static final String SCHEME = "content";
    public static final String AUTHORITY = CommonAssumptions.APP_NAMESPACE;
    public static final String CONTENT = "book";

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
    public static final String TITLE = "title";
    public static final String ISBN = "isbn";
    public static final String PRICE = "price";
    public static final String AUTHORS = "authors";
    public static final String ID_FULL = TABLE_NAME + "." + ID;
    public static final String TITLE_FULL = TABLE_NAME + "." + TITLE;
    public static final String ISBN_FULL = TABLE_NAME + "." + ISBN;
    public static final String PRICE_FULL = TABLE_NAME + "." + PRICE;
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    ID + " INTEGER PRIMARY KEY," +
                    TITLE + " TEXT NOT NULL," +
                    PRICE + " REAL NOT NULL," +
                    ISBN + " TEXT NOT NULL" +
                    ");";

    public static Uri withExtendedPath(Object path) {
        if (path != null) {
            String stringPath = String.valueOf(path);
            Uri.Builder builder = CONTENT_URI.buildUpon();
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

    public static final char SEPARATOR_CHAR = '|';
    private static final Pattern SEPARATOR =
            Pattern.compile(Character.toString(SEPARATOR_CHAR), Pattern.LITERAL);

    public static long getId(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(ID));
    }

    public static String getTitle(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
    }

    public static void putTitle(ContentValues values, String title) {
        values.put(TITLE, title);
    }

    public static String getIsbn(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(ISBN));
    }

    public static void putIsbn(ContentValues values, String isbn) {
        values.put(ISBN, isbn);
    }

    public static Double getPrice(Cursor cursor) {
        return cursor.getDouble(cursor.getColumnIndexOrThrow(PRICE));
    }

    public static void putPrice(ContentValues values, double price) {
        values.put(PRICE, price);
    }

    public static Author[] getAuthors(Cursor cursor) {
        String stringAuthors = cursor.getString(cursor.getColumnIndexOrThrow(AUTHORS));

        return getAuthorsFromString(stringAuthors, SEPARATOR_CHAR);
    }

    public static Author[] getAuthorsFromString(String stringAuthors, char pattern) {
        if (stringAuthors != null && !stringAuthors.isEmpty()) {
            Pattern separator =
                    Pattern.compile(Character.toString(pattern), Pattern.LITERAL);
            String[] splittedAuthors = separator.split(stringAuthors);
            Author[] authors = new Author[splittedAuthors.length];
            for (int i = 0; i < splittedAuthors.length; i++) {
                authors[i] = new Author(splittedAuthors[i]);
            }
            return authors;
        } else {
            return null;
        }
    }

    public static void putAuthors(ContentValues values, Author[] authors) {
        StringBuilder builder = new StringBuilder(authors[0].toString());
        if (authors.length > 1) {
            for (int i = 1; i < authors.length; i++) {
                builder.append(BookContract.SEPARATOR_CHAR)
                        .append(authors[i].toString());
            }
        }
        values.put(AUTHORS, builder.toString());
    }

//    public static String[] readStringArray(String in)	{
//        return SEPARATOR.split(in);
//    }

    public static void putAll(ContentValues values, Book book) {
        BookContract.putTitle(values, book.getTitle());
        BookContract.putIsbn(values, book.getIsbn());
        BookContract.putPrice(values, book.getPrice());
        BookContract.putAuthors(values, book.getAuthors());
    }
}
