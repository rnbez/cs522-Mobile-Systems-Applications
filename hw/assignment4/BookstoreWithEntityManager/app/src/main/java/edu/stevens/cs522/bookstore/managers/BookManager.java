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
public class BookManager extends Manager<Book>{

    final static Uri CONTENT_URI = BookContract.CONTENT_URI;

    public BookManager(Context context, int loaderId, IEntityCreator<Book> creator) {
        super(context, loaderId, creator);
    }

    public void persistAsync(final Book book, final IContinue<Uri> callback) {
        ContentValues values = new ContentValues();
        book.writeToProvider(values);
        AsyncContentResolver asyncResolver = getAsyncResolver();
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

    public void deleteAsync(long id, final IContinue<Uri> callback) {
        AsyncContentResolver asyncResolver = getAsyncResolver();
        Uri uri = BookContract.withExtendedPath(id);
        asyncResolver.deleteAsync(uri, null, null, null);

    }

    @Override
    protected void executeQuery(Uri uri, IQueryListener<Book> listener) {
        super.executeQuery(uri, listener);
    }
}
