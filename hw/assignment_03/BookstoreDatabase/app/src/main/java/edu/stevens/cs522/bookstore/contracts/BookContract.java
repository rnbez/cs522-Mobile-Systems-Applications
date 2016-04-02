package edu.stevens.cs522.bookstore.contracts;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.regex.Pattern;

import edu.stevens.cs522.bookstore.entities.Author;

/**
 * Created by Rafael on 2/13/2016.
 */
public class BookContract {

    public static final String ID = "_id";
    public static final String TITLE = "title";
    public static final String ISBN = "isbn";
    public static final String PRICE = "price";
    public static final String AUTHORS = "authors";



    public static final char SEPARATOR_CHAR =	'|';
    private static final Pattern SEPARATOR =
            Pattern.compile(Character.toString(SEPARATOR_CHAR),	Pattern.LITERAL);

    public static int getId(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(ID));
    }

    public static void putId(ContentValues values, int id) {
        values.put(ID, id);
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

        if (stringAuthors != null && !stringAuthors.isEmpty()) {
            String[] splittedAuthors = readStringArray(stringAuthors);
            Author[] authors = new Author[splittedAuthors.length];
            for (int i = 0; i < splittedAuthors.length; i++) {
                authors[i] = new Author(splittedAuthors[i], "","");
            }
            return authors;
        } else {
            return null;
        }
    }

    public static void putAuthors(ContentValues values, Author[] authors) {
        //values.put(AUTHORS, aut);
    }

    public static String[]	readStringArray(String	in)	{
        return SEPARATOR.split(in);
    }
}
