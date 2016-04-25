package edu.stevens.cs522.chat.oneway.server.requests;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import edu.stevens.cs522.chat.oneway.server.contracts.ChatroomContract;
import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Chatroom;
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

        if (!isOnline(ctx)) {
            return;
        }

        Cursor c = contentResolver.query(
                MessageContract.CONTENT_URI,
                null,
                MessageContract.SEQ_NUM + "=?",
                new String[]{"0"},
                null);
        if (c != null) {
            if (c.moveToFirst()) {
                sync.messages = new ArrayList<>();
                do {
                    sync.messages.add(new Message(c));
                } while (c.moveToNext());
            }
        }
        c.close();

        RestMethod.StreamingResponse sr = null;
        SyncResponse response = null;
        try {
            RestMethod rest = new RestMethod();
            sr = rest.perform(sync);


            // If we assume JSON data (could be e.g. multipart)
            OutputStream out = sr.getOutputStream();
            JsonWriter jw = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
            sync.write(jw);
            rest.throwErrors(sr.getGetConnection());
            InputStream in = sr.getInputStream();
            JsonReader jr = new JsonReader(
                    new BufferedReader(new InputStreamReader(in, "UTF-8")));
            // Consume the download data
//            sr.disconnect();
            response = (SyncResponse) sync.getResponse(jr);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sr != null) sr.disconnect();
        }

        if (response != null) {
            if (response.clients != null && !response.clients.isEmpty()) {
                insertUpdatePeers(contentResolver, response.clients);
            }

            if (response.messages != null && !response.messages.isEmpty()) {
                contentResolver.delete(
                        MessageContract.CONTENT_URI,
                        MessageContract.SEQ_NUM + "=?",
                        new String[]{"0"});

                insertChatroomsIfNotExist(contentResolver, response.messages);
                insertMessages(contentResolver, response.messages);

                long lastSeqNum = response.messages.get(response.messages.size() - 1).getSequentialNumber();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(App.PREF_KEY_LAST_SEQNUM, lastSeqNum);
                editor.apply();
            }
        }

    }


    private long getPeerId(ContentResolver contentResolver, String peerName) {
        Uri uri = PeerContract.withExtendedPath(peerName);
        Cursor c = contentResolver.query(uri, null, null, null, null);

        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                Peer p = new Peer(c);
                c.close();
                return p.getId();
            }
        }

        c.close();
        return -1;

    }

    private void insertUpdatePeers(ContentResolver contentResolver, ArrayList<Peer> peers) {
        for (Peer peer : peers) {
            ContentValues values = new ContentValues();
            long peerId = getPeerId(contentResolver, peer.getName());

            if (peerId != -1) {
                peer.setId(peerId);
                Uri uri = PeerContract.withExtendedPath(peer.getId());
                peer.writeToProvider(values);
                contentResolver.update(uri, values, null, null);
            } else {
                peer.writeToProvider(values);
                contentResolver.insert(PeerContract.CONTENT_URI, values);
            }
        }
    }

    private long getChatroomId(ContentResolver contentResolver, String chatroom) {
        Uri uri = ChatroomContract.withExtendedPath(chatroom);
        Cursor c = contentResolver.query(uri, null, null, null, null);

        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                Chatroom chat = new Chatroom(c);
                c.close();
                return chat.getId();
            }
        }

        c.close();
        return -1;

    }

    private void insertChatroomsIfNotExist(ContentResolver contentResolver, ArrayList<Message> messages) {
        HashMap<String, Long> idMap = new HashMap<>();

        for (Message m : messages) {
            long chatroomId = -1;
            if (idMap.containsKey(m.getChatroom())) chatroomId = idMap.get(m.getChatroom());
            else {
                chatroomId = getChatroomId(contentResolver, m.getChatroom());
                if (chatroomId == -1) {
                    Chatroom chat = new Chatroom(m.getChatroom());
                    ContentValues values = new ContentValues();
                    chat.writeToProvider(values);
                    Uri uri = contentResolver.insert(ChatroomContract.CONTENT_URI, values);
                    chatroomId = Long.valueOf(uri.getLastPathSegment());

                }
                idMap.put(m.getChatroom(), chatroomId);
            }

            m.setChatroomId(chatroomId);
//            ContentValues values = new ContentValues();
//            m.writeToProvider(values);
//            contentResolver.insert(MessageContract.CONTENT_URI, values);

        }

    }

    private void insertMessages(ContentResolver contentResolver, ArrayList<Message> messages) {
        HashMap<String, Long> idMap = new HashMap<>();
        for (Message m : messages) {
            long peerId = -1;
            if (idMap.containsKey(m.getSender())) peerId = idMap.get(m.getSender());
            else {
                peerId = getPeerId(contentResolver, m.getSender());
                if (peerId == -1) {
                    ArrayList<Peer> list = new ArrayList<>();
                    Peer peer = new Peer(m.getSender(), 0, 0);
                    ContentValues values = new ContentValues();
                    peer.writeToProvider(values);
                    Uri uri = contentResolver.insert(PeerContract.CONTENT_URI, values);
                    peerId = Long.valueOf(uri.getLastPathSegment());
                }
                idMap.put(m.getSender(), peerId);
            }
            m.setPeerId(peerId);
            ContentValues values = new ContentValues();
            m.writeToProvider(values);
            contentResolver.insert(MessageContract.CONTENT_URI, values);

        }
    }

    public boolean isOnline(Context context) {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }


}
