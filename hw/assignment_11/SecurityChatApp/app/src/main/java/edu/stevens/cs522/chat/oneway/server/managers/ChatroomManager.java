package edu.stevens.cs522.chat.oneway.server.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.CursorJoiner;
import android.net.Uri;

import edu.stevens.cs522.chat.oneway.server.contracts.ChatroomContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Chatroom;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.utils.App;


/**
 * Created by Rafael on 2/21/2016.
 */
public class ChatroomManager extends Manager<Chatroom> {

    final static Uri CONTENT_URI = ChatroomContract.CONTENT_URI;
    private char[] databaseKey;

    public ChatroomManager(Context context, char[] databaseKey, int loaderId, IEntityCreator<Chatroom> creator) {
        super(context, loaderId, creator);
        this.databaseKey = databaseKey;

    }

    public void persistAsync(final Chatroom chatroom, final IContinue<Uri> callback) {
        Uri uri = ChatroomContract.withDatabaseKeyUri(databaseKey);
        ContentValues values = new ContentValues();
        chatroom.writeToProvider(values);
        AsyncContentResolver asyncResolver = getAsyncResolver();
        asyncResolver.insertAsync(uri,
                values,
                new IContinue<Uri>() {
                    public void kontinue(Uri uri) {
                        chatroom.setId(ChatroomContract.getId(uri));
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
            uri = ChatroomContract.withDatabaseKeyUri(databaseKey, deleteUri);
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

    public void updateAsync(final Uri updateUri, final Chatroom chatroom, final IContinue<Integer> callback) {
        String k = updateUri.getQueryParameter(App.DATABASE_KEY_URI_PARAM);
        final Uri uri;
        if (k == null) {
            uri = PeerContract.withDatabaseKeyUri(databaseKey, updateUri);
        } else {
            uri = updateUri;
        }

        ContentValues values = new ContentValues();
        chatroom.writeToProvider(values);
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
    protected void executeQuery(Uri uri, IQueryListener<Chatroom> listener) {
        super.executeQuery(uri, listener);
    }
}
