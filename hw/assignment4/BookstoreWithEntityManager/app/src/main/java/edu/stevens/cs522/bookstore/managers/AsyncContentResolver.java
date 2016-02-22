package edu.stevens.cs522.bookstore.managers;


import android.database.SQLException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by Rafael on 2/21/2016.
 */
public class AsyncContentResolver extends AsyncQueryHandler {

    public AsyncContentResolver(ContentResolver cr) {
        super(cr);
    }

    public void insertAsync(Uri uri, ContentValues values, IContinue<Uri> callback) {
        this.startInsert(0, callback, uri, values);
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);
        if (cookie != null) {
            @SuppressWarnings("unchecked")
            IContinue<Uri> callback = (IContinue<Uri>) cookie;
            callback.kontinue(uri);
        }
    }

    public void queryAsync(Uri uri, String[] projection,
                           String selection, String[] selectionArgs,
                           String sortOrder,
                           IContinue<Cursor> callback) {
        this.startQuery(0, callback, uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);
        if (cookie != null) {
            @SuppressWarnings("unchecked")
            IContinue<Cursor> callback = (IContinue<Cursor>) cookie;
            callback.kontinue(cursor);
        }
    }

    public void deleteAsync(Uri uri, String selection, String[] selectionArgs, IContinue<Integer> callback) {
        this.startDelete(0, callback, uri, selection, selectionArgs);
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        super.onDeleteComplete(token, cookie, result);
        if (cookie != null) {
            if (result > 0) {
                @SuppressWarnings("unchecked")
                IContinue<Integer> callback = (IContinue<Integer>) cookie;
                callback.kontinue(result);
            } else {
                throw new SQLException("No rows were deleted");
            }
        }
    }

    public void updateAsync(Uri uri, String selection, String[] selectionArgs, IContinue<Integer> callback) {
        this.startDelete(0, callback, uri, selection, selectionArgs);
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        super.onDeleteComplete(token, cookie, result);
        if (cookie != null) {
            @SuppressWarnings("unchecked")
            IContinue<Integer> callback = (IContinue<Integer>) cookie;
            callback.kontinue(result);
        }
    }
}
