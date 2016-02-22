package edu.stevens.cs522.bookstore.managers;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by Rafael on 2/21/2016.
 */
public class QueryBuilder<T> implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context context;
    private IEntityCreator<T> helper;
    private IQueryListener<T> listener;

    public QueryBuilder(String tag,
                        Context context,
                        Uri uri,
                        int loaderId,
                        IEntityCreator<T> helper,
                        IQueryListener<T> listener) {
        this.context = context;
        this.helper = helper;
        this.listener = listener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
