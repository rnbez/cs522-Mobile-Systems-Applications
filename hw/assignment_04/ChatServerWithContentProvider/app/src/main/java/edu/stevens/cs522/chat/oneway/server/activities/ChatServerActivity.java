/*********************************************************************
 * Chat server: accept chat messages from clients.
 * <p/>
 * Sender name and GPS coordinates are encoded
 * in the messages, and stripped off upon receipt.
 * <p/>
 * Copyright (c) 2012 Stevens Institute of Technology
 **********************************************************************/
package edu.stevens.cs522.chat.oneway.server.activities;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.adapters.ListAdapter;
import edu.stevens.cs522.chat.oneway.server.adapters.MessageAdapter;
import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.managers.IContinue;
import edu.stevens.cs522.chat.oneway.server.managers.IQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.ISimpleQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.MessageManager;
import edu.stevens.cs522.chat.oneway.server.managers.PeerManager;
import edu.stevens.cs522.chat.oneway.server.managers.QueryBuilder;
import edu.stevens.cs522.chat.oneway.server.managers.SimpleQueryBuilder;
import edu.stevens.cs522.chat.oneway.server.managers.TypedCursor;
import edu.stevens.cs522.chat.oneway.server.providers.PeerMessageProvider;

public class ChatServerActivity extends Activity implements OnClickListener {

    final static public String TAG = ChatServerActivity.class.getCanonicalName();

    /*
     * Socket used both for sending and receiving
     */
    private DatagramSocket serverSocket;

    /*
     * True as long as we don't get socket errors
     */
    private boolean socketOK = true;

    /*
     * TODO: Declare UI.
     */
//    ArrayList<Message> messageList;
    ListView msgList;
    private Button next;
    private MessageAdapter cursorAdapter;

    /*
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /**
         * Let's be clear, this is a HACK to allow you to do network communication on the main thread.
         * This WILL cause an ANR, and is only provided to simplify the pedagogy.  We will see how to do
         * this right in a future assignment (using a Service managing background threads).
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            /*
             * Get port information from the resources.
			 */
            int port = Integer.parseInt(getResources().getString(R.string.app_port));
            serverSocket = new DatagramSocket(port);
            Log.i(TAG, "Socket successfully opened.");
        } catch (Exception e) {
            Log.e(TAG, "Cannot open socket" + e.getMessage());
            return;
        }

        this.cursorAdapter = new MessageAdapter(this, null);
        this.msgList = (ListView) findViewById(R.id.msgList);
        this.msgList.setAdapter(this.cursorAdapter);

        QueryBuilder.executeQuery(TAG,
                this,
                MessageContract.CONTENT_URI,
                MessageContract.CURSOR_LOADER_ID,
                MessageContract.DEFAULT_ENTITY_CREATOR,
                new IQueryListener<Message>() {
                    @Override
                    public void handleResults(TypedCursor<Message> cursor) {
                        cursorAdapter.swapCursor(cursor.getCursor());
                    }

                    @Override
                    public void closeResults() {
                        cursorAdapter.swapCursor(null);
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.chat_menu_contacts:
                Intent intent = new Intent(this, ContactBookActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void onClick(View v) {

        byte[] receiveData = new byte[1024];

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {

            serverSocket.receive(receivePacket);
            Log.i(TAG, "Received a packet");
//            Log.i(TAG, "Message: " + new String(receiveData, "UTF-8"));

//            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

            InetAddress sourceIPAddress = receivePacket.getAddress();
//            Log.i(TAG, "Source IP Address: " + sourceIPAddress);

            Message message = new Message(new String(receivePacket.getData(), "UTF-8"));
            Peer sender = new Peer(message.getSender(), sourceIPAddress, String.valueOf(receivePacket.getPort()));
            handleMessage(sender, message);

        } catch (Exception e) {
            Log.e(TAG, "Problems receiving packet: " + e.getMessage());
            socketOK = false;
        }

    }

    private void handleMessage(final Peer sender, final Message message) {
        Log.d(TAG, sender.getName() + " >> " + message.getMessageText());
        final PeerManager peerManager = new PeerManager(this, PeerContract.CURSOR_LOADER_ID, PeerContract.DEFAULT_ENTITY_CREATOR);
        final MessageManager messageManager = new MessageManager(this, MessageContract.CURSOR_LOADER_ID, MessageContract.DEFAULT_ENTITY_CREATOR);

        Uri uriWithName = PeerContract.withExtendedPath(message.getSender());
        Log.d(TAG, uriWithName.toString());

        SimpleQueryBuilder.executeQuery(this,
                uriWithName,
                PeerContract.DEFAULT_ENTITY_CREATOR,
                new ISimpleQueryListener<Peer>() {
                    @Override
                    public void handleResults(List<Peer> results) {
                        if (results.size() > 0) {
                            ContentValues values = new ContentValues();
                            Peer peer = results.get(0);
                            long peerId = peer.getId();
                            Log.d(TAG, peer.getId() + " >> " + peer.getName());
                            message.setPeerId(peerId);
                            Uri uriWithId = PeerContract.withExtendedPath(peerId);
                            peerManager.updateAsync(uriWithId, sender, new IContinue<Integer>() {
                                @Override
                                public void kontinue(Integer value) {
                                    messageManager.persistAsync(message, null);
                                }
                            });
                        } else {
                            peerManager.persistAsync(sender, new IContinue<Uri>() {
                                @Override
                                public void kontinue(Uri uri) {
                                    long peerId = PeerContract.getId(uri);
                                    Log.d(TAG, peerId + " >> " + sender.getName());
                                    message.setPeerId(peerId);
                                    Log.d(TAG, message.getPeerId() + " >> " + message.getMessageText());
                                    messageManager.persistAsync(message, null);
                                }
                            });
                        }
                    }
                }
        );
//        QueryBuilder.executeQuery(TAG,
//                this,
//                uriWithName,
//                PeerContract.CURSOR_LOADER_ID,
//                PeerContract.DEFAULT_ENTITY_CREATOR,
//                new IQueryListener<Peer>() {
//                    @Override
//                    public void handleResults(TypedCursor<Peer> cursor) {
//                        if (cursor.getCount() > 0) {
//                            if (cursor.moveToFirst()) {
//                                ContentValues values = new ContentValues();
//                                Peer peer = cursor.getEntity();
//                                long peerId = peer.getId();
//                                Log.d(TAG, peer.getId() + " >> " + peer.getName());
//                                message.setPeerId(peerId);
//                                Uri uriWithId = PeerContract.withExtendedPath(peerId);
//                                peerManager.updateAsync(uriWithId, sender, new IContinue<Integer>() {
//                                    @Override
//                                    public void kontinue(Integer value) {
//                                        messageManager.persistAsync(message, null);
//                                    }
//                                });
//                            }
//                        } else {
//                            peerManager.persistAsync(sender, new IContinue<Uri>() {
//                                @Override
//                                public void kontinue(Uri uri) {
//                                    long peerId = PeerContract.getId(uri);
//                                    Log.d(TAG, peerId + " >> " + sender.getName());
//                                    message.setPeerId(peerId);
//                                    Log.d(TAG, message.getPeerId() + " >> " + message.getMessageText());
//                                    messageManager.persistAsync(message, null);
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void closeResults() {
//                        cursorAdapter.swapCursor(null);
//                    }
//                });
//        messageList.add(message_row);
//        ListAdapter adp = (ListAdapter) msgList.getAdapter();
//        adp.notifyDataSetChanged();
    }

    /*
     * Close the socket before exiting application
     */
    public void closeSocket() {
        serverSocket.close();
    }

    /*
     * If the socket is OK, then it's running
     */
    boolean socketIsOK() {
        return socketOK;
    }

}