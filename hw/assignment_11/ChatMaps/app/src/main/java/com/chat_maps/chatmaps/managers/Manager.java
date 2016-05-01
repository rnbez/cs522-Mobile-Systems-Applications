package com.chat_maps.chatmaps.managers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

/**
 * Created by Rafael on 2/21/2016.
 */
public abstract class Manager<T> {

    private final String tag;
    private final Context context;
    private final int loaderID;
    private final IEntityCreator<T> creator;
    private ContentResolver syncResolver;
    private AsyncContentResolver asyncResolver;

    protected Manager(Context context, int loaderID, IEntityCreator<T> creator) {
        this.tag = this.getClass().getCanonicalName();
        this.context = context;
        this.loaderID = loaderID;
        this.creator = creator;
    }

    protected ContentResolver getSyncResolver() {
        if (syncResolver == null) {
            syncResolver = context.getContentResolver();
        }
        return syncResolver;
    }

    protected AsyncContentResolver getAsyncResolver() {
        if (asyncResolver == null) {
            asyncResolver = new AsyncContentResolver(context.getContentResolver());
        }
        return asyncResolver;
    }

    protected void executeSimpleQuery(Uri uri, ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder.executeQuery((Activity) context, uri, creator, listener);
    }

    protected void executeSimpleQuery(Uri uri,
                                      String[] projection,
                                      String selection, String[] selectionArgs,
                                      ISimpleQueryListener<T> listener) {
        SimpleQueryBuilder.executeQuery((Activity) context,
                uri,
                projection,
                selection,
                selectionArgs,
                creator,
                listener);
    }

    protected void executeQuery(Uri uri, IQueryListener<T> listener) {
        QueryBuilder.executeQuery(tag,
                (Activity) context,
                uri,
                loaderID,
                creator,
                listener);
    }

    protected void executeQuery(Uri uri,
                                String[] projection,
                                String selection,
                                String[] selectionArgs,
                                IQueryListener<T> listener) {
        QueryBuilder.executeQuery(tag,
                (Activity) context,
                uri,
                loaderID,
                projection,
                selection,
                selectionArgs,
                creator,
                listener);
    }
    protected void reexecuteQuery(Uri uri, String[] projection,
                                        String selection, String[] selectionArgs,
                                        IQueryListener<T> listener) {
        QueryBuilder.reexecuteQuery(tag,
                (Activity) context,
                uri,
                loaderID,
                projection,
                selection,
                selectionArgs,
                creator,
                listener);
    }
}
