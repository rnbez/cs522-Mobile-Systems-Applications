package com.chat_maps.chatmaps.managers;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.chat_maps.chatmaps.contracts.PeerContract;
import com.chat_maps.chatmaps.entities.Peer;
import com.chat_maps.chatmaps.utils.App;

/**
 * Created by Rafael on 2/21/2016.
 */
public class PeerManager extends Manager<Peer> {

    final static Uri CONTENT_URI = PeerContract.CONTENT_URI;
    private char[] databaseKey;

    public PeerManager(Context context, char[] databaseKey, int loaderId, IEntityCreator<Peer> creator) {
        super(context, loaderId, creator);
        this.databaseKey = databaseKey;

    }

    public void persistAsync(final Peer peer, final IContinue<Uri> callback) {
        Uri uri = PeerContract.withDatabaseKeyUri(databaseKey);

        ContentValues values = new ContentValues();
        peer.writeToProvider(values);
        AsyncContentResolver asyncResolver = getAsyncResolver();
        asyncResolver.insertAsync(uri,
                values,
                new IContinue<Uri>() {
                    public void kontinue(Uri uri) {
                        peer.setId(PeerContract.getId(uri));
                        getSyncResolver().notifyChange(uri, null);
                        if (callback != null) {
                            callback.kontinue(uri);
                        }
                    }
                });
    }

    public void deleteAsync(final Uri deleteUri, final IContinue<Integer> callback) {
        String k = deleteUri.getQueryParameter(App.DATABASE_KEY_URI_PARAM);
        final Uri uri;
        if (k == null) {
            uri = PeerContract.withDatabaseKeyUri(databaseKey, deleteUri);
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
                if (callback != null) {
                    callback.kontinue(value);
                }
            }
        });
    }

    public void updateAsync(final Uri updateUri, final Peer peer, final IContinue<Integer> callback) {
        String k = updateUri.getQueryParameter(App.DATABASE_KEY_URI_PARAM);
        final Uri uri;
        if (k == null) {
            uri = PeerContract.withDatabaseKeyUri(databaseKey, updateUri);
        } else {
            uri = updateUri;
        }

        ContentValues values = new ContentValues();
        peer.writeToProvider(values);
        AsyncContentResolver asyncResolver = getAsyncResolver();
        asyncResolver.updateAsync(uri, values, new IContinue<Integer>() {
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

    @Override
    protected void executeQuery(Uri uri, IQueryListener<Peer> listener) {
        super.executeQuery(uri, listener);
    }
}
