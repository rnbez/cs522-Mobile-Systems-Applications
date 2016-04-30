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

/**
 * Created by Rafael on 3/12/2016.
 */
public class ServiceHelper {
/* 10 Points
*  ServiceHelper class used by UI to access Web services
*
*  The service helper will create a request object and
*  attach it to the intent that fires the request service.
*
* */
    public final static String EXTRA_REGISTER_RESULT_ID = RequestService.EXTRA_REGISTER_RESULT_ID;
    public final static String EXTRA_REGISTER_REG_ID = RequestService.EXTRA_REGISTER_REG_ID;
    public final static String EXTRA_POST_MSG_RESULT_ID = RequestService.EXTRA_POST_MSG_RESULT_ID;

    public void registerAsync(Activity activity, UUID regId, Peer peer) {
        Register request = new Register(regId, peer);
        Intent i = new Intent(activity, RequestService.class);
        i.setAction(RequestService.REGISTER_ACTION);
        i.putExtra(RequestService.EXTRA_REGISTER, request);
        activity.startService(i);
    }

    public void unregisterAsync(Activity activity, UUID regId, Peer peer) {
        Unregister request = new Unregister(regId, peer);
        Intent i = new Intent(activity, RequestService.class);
        i.setAction(RequestService.UNREGISTER_ACTION);
        i.putExtra(RequestService.EXTRA_UNREGISTER, request);
        activity.startService(i);
    }

    public void syncAsync(Context context, UUID regId, Peer peer, long seqnum, ArrayList<Message> messages) {
//        TODO: create a request object
//        TODO: attach object to intent service
        Synchronize request = new Synchronize(regId, peer, seqnum, messages);
        Intent i = new Intent(context, RequestService.class);
        i.setAction(RequestService.SYNCHRONIZE_ACTION);
        i.putExtra(RequestService.EXTRA_SYNCHRONIZE, request);
//        i.putExtra(RequestService.EXTRA_CALLBACK, callback);
        context.startService(i);

    }




}
