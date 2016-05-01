package edu.stevens.cs522.chat.oneway.server.requests;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ResultReceiver;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.managers.IContinue;
import edu.stevens.cs522.chat.oneway.server.managers.ISimpleQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.MessageManager;
import edu.stevens.cs522.chat.oneway.server.managers.PeerManager;
import edu.stevens.cs522.chat.oneway.server.managers.SimpleQueryBuilder;
import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * Created by Rafael on 3/12/2016.
 */
public class ServiceHelper {

    private static char[] databaseKey = null;

    public static void setDatabaseKey(char[] key){
        if (databaseKey != null) {
            for (char c :
                    databaseKey) {
                c = ' ';
            }
        }
        databaseKey = key;
    }

    public void registerAsync(Activity activity, UUID regId, Peer peer) {
        Register request = new Register(regId, peer);
        Intent i = new Intent(activity, RequestService.class);
        i.setAction(RequestService.REGISTER_ACTION);
        if (databaseKey != null)
            i.putExtra(App.EXTRA_SECURITY_DATABASE_KEY, databaseKey);
        i.putExtra(RequestService.EXTRA_REGISTER, request);
        activity.startService(i);
    }

    public void unregisterAsync(Activity activity, UUID regId, Peer peer) {
        Unregister request = new Unregister(regId, peer);
        Intent i = new Intent(activity, RequestService.class);
        i.setAction(RequestService.UNREGISTER_ACTION);
        if (databaseKey != null)
            i.putExtra(App.EXTRA_SECURITY_DATABASE_KEY, databaseKey);
        i.putExtra(RequestService.EXTRA_UNREGISTER, request);
        activity.startService(i);
    }

    public void syncAsync(Context context, UUID regId, Peer peer, long seqnum, ArrayList<Message> messages) {
//        TODO: create a request object
//        TODO: attach object to intent service
        Synchronize request = new Synchronize(regId, peer, seqnum, messages);
        Intent i = new Intent(context, RequestService.class);
        i.setAction(RequestService.SYNCHRONIZE_ACTION);
        if (databaseKey != null)
            i.putExtra(App.EXTRA_SECURITY_DATABASE_KEY, databaseKey);
        i.putExtra(RequestService.EXTRA_SYNCHRONIZE, request);
//        i.putExtra(RequestService.EXTRA_CALLBACK, callback);
        context.startService(i);

    }




}
