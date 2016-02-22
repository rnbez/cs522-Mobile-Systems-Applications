package edu.stevens.cs522.bookstore.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import edu.stevens.cs522.bookstore.contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Book;

/**
 * Created by Rafael on 2/21/2016.
 */
public class BookManager {

    final static Uri CONTENT_URI = BookContract.CONTENT_URI;
    private Context context;
    private AsyncContentResolver asyncResolver;

    public BookManager(Context context) {
        this.context = context;
        this.asyncResolver = new AsyncContentResolver(context.getContentResolver());
    }

    public void persistAsync(final Book book, final IContinue<Uri> callback) {
        ContentValues values = new ContentValues();
        book.writeToProvider(values);
//        asyncResolver.insertAsync(CONTENT_URI, values, callback);
        asyncResolver.insertAsync(CONTENT_URI,
                values,
                new IContinue<Uri>() {
                    public void kontinue(Uri uri) {
                        book.setId(BookContract.getId(uri));
                        callback.kontinue(uri);
                    }
                });
    }





}
