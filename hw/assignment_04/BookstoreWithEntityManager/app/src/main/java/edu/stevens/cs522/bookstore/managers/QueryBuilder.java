package edu.stevens.cs522.bookstore.managers;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import java.lang.IllegalArgumentException;import java.lang.IllegalStateException;import java.lang.Override;import java.lang.String;
import edu.stevens.cs522.bookstore.contracts.BookContract;

/**
 * Created by Rafael on 2/21/2016.
 */
public class QueryBuilder<T> implements LoaderManager.LoaderCallbacks<Cursor> {

    private String tag;
    private Context context;
    private Uri uri;
    private int loaderId;
    private IEntityCreator<T> creator;
    private IQueryListener<T> listener;

    public QueryBuilder(String tag, Context context, Uri uri, int loaderId, IEntityCreator<T> creator, IQueryListener<T> listener) {
        this.tag = tag;
        this.context = context;
        this.uri = uri;
        this.loaderId = loaderId;
        this.creator = creator;
        this.listener = listener;
    }

    public static <T> void executeQuery(String tag,
                                        Activity context,
                                        Uri uri,
                                        int loaderID,
                                        IEntityCreator<T> creator,
                                        IQueryListener<T> listener) {
        QueryBuilder<T> qb = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
        LoaderManager lm = context.getLoaderManager();
        lm.initLoader(loaderID, null, qb);
    }

    public static <T> void executeQuery(String tag, Activity context, Uri uri, int loaderID, String[] projection, String selection, String[] selectionArgs, IEntityCreator<T> creator, IQueryListener<T> listener) {
        QueryBuilder<T> qb = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
        LoaderManager lm = context.getLoaderManager();
        lm.initLoader(loaderID, null, qb);
    }


    public static <T> void reexecuteQuery(String tag, Activity context, Uri uri, int loaderID, String[] projection, String selection, String[] selectionArgs, IEntityCreator<T> creator, IQueryListener<T> listener) {
        QueryBuilder<T> qb = new QueryBuilder<T>(tag, context, uri, loaderID, creator, listener);
        LoaderManager lm = context.getLoaderManager();
        lm.destroyLoader(loaderID);
        lm.initLoader(loaderID, null, qb);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == loaderId) {
            String[] projection = new String[]{
                    BookContract.ID_FULL,
                    BookContract.TITLE,
                    BookContract.ISBN,
                    BookContract.PRICE,
                    BookContract.AUTHORS,
            };
            return new CursorLoader(context,
                    uri,
                    projection,
                    null,
                    null,
                    null);
        }
        throw new IllegalArgumentException("Unexpected loader id: " + id);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == loaderId) {
            listener.handleResults(new TypedCursor<T>(cursor, creator));
        } else {
            throw new IllegalStateException("Unexpected loader callback");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == loaderId) {
            listener.closeResults();
        } else {
            throw new IllegalStateException("Unexpected loader callback");
        }
    }
}
