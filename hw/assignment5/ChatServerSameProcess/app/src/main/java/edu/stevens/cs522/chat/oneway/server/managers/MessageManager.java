package edu.stevens.cs522.chat.oneway.server.managers;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.entities.Message;

/**
 * Created by Rafael on 2/21/2016.
 */
public class MessageManager extends Manager<Message> {

    final static Uri CONTENT_URI = MessageContract.CONTENT_URI;

    public MessageManager(Context context, int loaderId, IEntityCreator<Message> creator) {
        super(context, loaderId, creator);
    }

    public void persistAsync(final Message message, final IContinue<Uri> callback) {
        ContentValues values = new ContentValues();
        message.writeToProvider(values);
        AsyncContentResolver asyncResolver = getAsyncResolver();
//        asyncResolver.insertAsync(CONTENT_URI, values, callback);
        asyncResolver.insertAsync(CONTENT_URI,
                values,
                new IContinue<Uri>() {
                    public void kontinue(Uri uri) {
                        message.setId(MessageContract.getId(uri));
                        getSyncResolver().notifyChange(uri, null);
                        if(callback != null){
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
