package com.chat_maps.chatmaps.managers;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;


import com.chat_maps.chatmaps.contracts.MessageContract;
import com.chat_maps.chatmaps.entities.Message;
import com.chat_maps.chatmaps.utils.App;


/**
 * Created by Rafael on 2/21/2016.
 */
public class MessageManager extends Manager<Message> {

    final static Uri CONTENT_URI = MessageContract.CONTENT_URI;
    private char[] databaseKey;

    public MessageManager(Context context, char[] databaseKey, int loaderId, IEntityCreator<Message> creator) {
        super(context, loaderId, creator);
        this.databaseKey = databaseKey;
    }

    public void persistAsync(final Message message, final IContinue<Uri> callback) {
        ContentValues values = new ContentValues();
        message.writeToProvider(values);
        AsyncContentResolver asyncResolver = getAsyncResolver();
//        asyncResolver.insertAsync(CONTENT_URI, values, callback);
        Uri uri = MessageContract.withDatabaseKeyUri(this.databaseKey);
        asyncResolver.insertAsync(uri,
                values,
                new IContinue<Uri>() {
                    public void kontinue(Uri uri) {
                        message.setId(MessageContract.getId(uri));
                        getSyncResolver().notifyChange(uri, null);
                        if (callback != null) {
                            callback.kontinue(uri);
                        }
                    }
                });
    }

    public void updateAsync(final Uri updateUri, final Message message, final IContinue<Integer> callback) {
        String k = updateUri.getQueryParameter(App.DATABASE_KEY_URI_PARAM);
        final Uri uri;
        if (k == null) {
            uri = MessageContract.withDatabaseKeyUri(databaseKey, updateUri);
        } else {
            uri = updateUri;
        }

        AsyncContentResolver asyncResolver = getAsyncResolver();
        ContentValues values = new ContentValues();
        message.writeToProvider(values);
        asyncResolver.updateAsync(uri,values, new IContinue<Integer>() {
            @Override
            public void kontinue(Integer value) {
                if (value > 0) {
                    getSyncResolver().notifyChange(uri, null);
                }
                if (callback != null) {
                    callback.kontinue(value);
                }
            }
        });

    }

    public void deleteAsync(final Uri deleteUri, final IContinue<Integer> callback) {
        String k = deleteUri.getQueryParameter(App.DATABASE_KEY_URI_PARAM);
        final Uri uri;
        if (k == null) {
            uri = MessageContract.withDatabaseKeyUri(databaseKey, deleteUri);
        } else {
            uri = deleteUri;
        }

        AsyncContentResolver asyncResolver = getAsyncResolver();
        asyncResolver.deleteAsync(uri, null, null, new IContinue<Integer>() {
            @Override
            public void kontinue(Integer value) {
                if (value > 0) {
                    getSyncResolver().notifyChange(uri, null);
                }
                if(callback != null) {
                    callback.kontinue(value);
                }
            }
        });

    }

    @Override
    protected void executeQuery(Uri uri, IQueryListener<Message> listener) {
        super.executeQuery(uri, listener);
    }
}
