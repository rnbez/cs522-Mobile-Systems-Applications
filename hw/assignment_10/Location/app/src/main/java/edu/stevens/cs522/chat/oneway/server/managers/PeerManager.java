package edu.stevens.cs522.chat.oneway.server.managers;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;


/**
 * Created by Rafael on 2/21/2016.
 */
public class PeerManager extends Manager<Peer> {

    final static Uri CONTENT_URI = PeerContract.CONTENT_URI;

    public PeerManager(Context context, int loaderId, IEntityCreator<Peer> creator) {
        super(context, loaderId, creator);
    }

    public void persistAsync(final Peer peer, final IContinue<Uri> callback) {
        ContentValues values = new ContentValues();
        peer.writeToProvider(values);
        AsyncContentResolver asyncResolver = getAsyncResolver();
        asyncResolver.insertAsync(CONTENT_URI,
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

    public void deleteAsync(final Uri uri, final IContinue<Integer> callback) {
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

    public void updateAsync(final Uri uri, final Peer peer, final IContinue<Integer> callback) {
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
