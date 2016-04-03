package edu.stevens.cs522.chat.oneway.server.requests;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.JsonReader;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.utils.App;

/**
 * Created by Rafael on 3/12/2016.
 */
public class RequestProcessor {
    /* 10 Points
    *  RequestProcessor provides business logic for Web service request processing:
    *  registration and posting messages.
    * */

    public RegisterResponse perform(Register request) {
//    TODO: call RestMethod
        return (RegisterResponse) new RestMethod().perform(request);
    }

    public void perform(Context ctx, Synchronize sync) {
//    TODO: call RestMethod

        ContentResolver contentResolver = ctx.getContentResolver();
        insertMessages(contentResolver, sync.messages);

        RestMethod.StreamingResponse sr = null;
        SyncResponse response = null;
        try {
            RestMethod rest = new RestMethod();
            sr = rest.perform(sync);
            // If we assume JSON data (could be e.g. multipart)
            InputStream is = sr.getInputStream();
            JsonReader jr = new JsonReader(
                    new BufferedReader(new InputStreamReader(is, "UTF-8")));
            // Consume the download data
//            sr.disconnect();
            response = (SyncResponse) sync.getResponse(jr);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sr != null) sr.disconnect();
        }

        if (response != null){
            if(response.clients != null && !response.clients.isEmpty()) {
                insertUpdatePeers(contentResolver, response.clients);
            }

            if(response.messages != null && !response.messages.isEmpty()) {
                contentResolver.delete(
                        MessageContract.CONTENT_URI,
                        MessageContract.SEQ_NUM + "=?",
                        new String[]{"0"});

                insertMessages(contentResolver, response.messages);
            }
        }

        long lastSeqNum = response.messages.get(response.messages.size()-1).getSequentialNumber();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(App.PREF_KEY_LAST_SEQNUM, lastSeqNum);
        editor.apply();

    }

    private boolean peerExists(ContentResolver contentResolver, String peerName, Peer out) {
        Uri uri = PeerContract.withExtendedPath(peerName);
        Cursor c = contentResolver.query(uri, null, null, null, null);

        if (c.getCount() > 0) {
            if(c.moveToFirst()){
                out = new Peer(c);
                return true;
            }
            else{
                return false;
            }
        } else {
            return false;
        }
    }

    private void insertUpdatePeers(ContentResolver contentResolver, ArrayList<Peer> peers) {
        for (Peer peer : peers) {
            ContentValues values = new ContentValues();
            peer.writeToProvider(values);
            Peer out = null;
            if (peerExists(contentResolver, peer.getName(), out)) {
                Uri uri = PeerContract.withExtendedPath(peer.getName());
                contentResolver.update(uri, values, null, null);
            } else {
                contentResolver.insert(MessageContract.CONTENT_URI, values);
            }
        }
    }

    private void insertMessages(ContentResolver contentResolver, ArrayList<Message> messages) {
        for (Message m : messages) {
            Peer out = null;
            if (!peerExists(contentResolver, m.getSender(), out)) {
                ArrayList<Peer> list = new ArrayList<>();
                try {
                    list.add(new Peer(m.getSender(), InetAddress.getLocalHost(), "0" ));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                insertUpdatePeers(contentResolver, list);
            }
            ContentValues values = new ContentValues();
            m.writeToProvider(values);
            Uri uri = contentResolver.insert(MessageContract.CONTENT_URI, values);

        }
    }

    public PostMessageResponse perform(PostMessage request) {
//    TODO: call RestMethod
        return (PostMessageResponse) new RestMethod().perform(request);
    }


}
