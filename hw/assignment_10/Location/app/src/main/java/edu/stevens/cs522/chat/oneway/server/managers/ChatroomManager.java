package edu.stevens.cs522.chat.oneway.server.managers;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import edu.stevens.cs522.chat.oneway.server.contracts.ChatroomContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Chatroom;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;


/**
 * Created by Rafael on 2/21/2016.
 */
public class ChatroomManager extends Manager<Chatroom> {

    final static Uri CONTENT_URI = ChatroomContract.CONTENT_URI;

    public ChatroomManager(Context context, int loaderId, IEntityCreator<Chatroom> creator) {
        super(context, loaderId, creator);
    }

    public void persistAsync(final Chatroom chatroom, final IContinue<Uri> callback) {
        ContentValues values = new ContentValues();
        chatroom.writeToProvider(values);
        AsyncContentResolver asyncResolver = getAsyncResolver();
        asyncResolver.insertAsync(CONTENT_URI,
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

    public void updateAsync(final Uri uri, final Chatroom chatroom, final IContinue<Integer> callback) {
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
