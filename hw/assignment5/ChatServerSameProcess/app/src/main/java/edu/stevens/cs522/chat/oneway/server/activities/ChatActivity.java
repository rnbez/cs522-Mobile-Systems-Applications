/*********************************************************************
 * Chat server: accept chat messages from clients.
 * <p/>
 * Sender name and GPS coordinates are encoded
 * in the messages, and stripped off upon receipt.
 * <p/>
 * Copyright (c) 2012 Stevens Institute of Technology
 **********************************************************************/
package edu.stevens.cs522.chat.oneway.server.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
//import android.os.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import edu.stevens.cs522.chat.oneway.server.R;
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
import edu.stevens.cs522.chat.oneway.server.services.ChatReceiverService;
import edu.stevens.cs522.chat.oneway.server.services.ChatSendService;

public class ChatActivity extends Activity implements OnClickListener {

    final static public String TAG = ChatActivity.class.getCanonicalName();

    /*
     * True as long as we don't get socket errors
     */
    private boolean socketOK = true;
    public static final String DEFAULT_CLIENT_NAME = "client";
    private static final int PREFERENCES_REQUEST = 1;

    public static final String CLIENT_PORT_KEY = "client_port";

    public static final int DEFAULT_CLIENT_PORT = 6666;

    public static final String DEFAULT_SERVER_ADDR = "127.0.0.1";


    private String clientName;
    private int clientPort;

    /*
     * TODO: Declare UI.
     */
//    ArrayList<Message> messageList;
    private ListView msgList;
    private Button sendButton;
    private EditText destinationHost;
    private EditText destinationPort;
    private EditText messageText;
    private MessageAdapter cursorAdapter;

    /*
     * Called when the activity is first created.
     */

    private ChatReceiverService receiverService;
    private ChatSendService sendService;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            sendService = ((ChatSendService.SendServiceBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sendService = null;
        }
    };
    private Receiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_chat);

        /**
         * Let's be clear, this is a HACK to allow you to do network communication on the main thread.
         * This WILL cause an ANR, and is only provided to simplify the pedagogy.  We will see how to do
         * this right in a future assignment (using a Service managing background threads).
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        cursorAdapter = new MessageAdapter(this, null);
        msgList = (ListView) findViewById(R.id.msgList);
        msgList.setAdapter(cursorAdapter);

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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        clientName = prefs.getString(PreferencesActivity.PREF_KEY_USERNAME, DEFAULT_CLIENT_NAME);
        clientPort = Integer.valueOf(prefs.getString(PreferencesActivity.PREF_KEY_PORT, String.valueOf(DEFAULT_CLIENT_PORT)));

        destinationHost = (EditText) findViewById(R.id.destination_host);
        destinationPort = (EditText) findViewById(R.id.destination_port);
        messageText = (EditText) findViewById(R.id.message_text);
        sendButton = (Button) findViewById(R.id.send_button);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendService != null) {
                    try {
                        Log.d(TAG, "Thread id: " + Thread.currentThread().getId());
                        Log.d(TAG, "Process id: " + android.os.Process.myTid());

                        InetAddress destAddr = InetAddress.getByName(destinationHost.getText().toString());
                        String strDestPort = destinationPort.getText().toString();
                        int destPort = strDestPort != null && !strDestPort.isEmpty() ?
                                Integer.parseInt(strDestPort) :
                                DEFAULT_CLIENT_PORT + 1;
                        String msg = clientName + "#" + messageText.getText().toString();

                        sendService.sendMessage(msg, destPort, destAddr, destPort);
                        messageText.setText("");
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        receiver = new Receiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent receiverIntent = new Intent(this, ChatReceiverService.class);
        receiverIntent.putExtra(ChatReceiverService.EXTRA_SOCKET_PORT, clientPort);
        startService(receiverIntent);

        IntentFilter filter = new IntentFilter(ChatReceiverService.NEW_MESSAGE_BROADCAST);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        stopService(new Intent(this, ChatReceiverService.class));

        unregisterReceiver(receiver);

        super.onStop();
    }

//    @Override
//    protected void onDestroy() {
//        stopService(new Intent(this, ChatReceiverService.class));
//        super.onDestroy();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent bindIntent = new Intent(this, ChatSendService.class);
//        bindIntent.putExtra(ChatReceiverService.EXTRA_SOCKET_PORT, clientPort);
        bindService(bindIntent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(conn);
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
                startActivity(new Intent(this, ContactBookActivity.class));
                return true;
            case R.id.chat_menu_prefs:
                startActivityForResult(new Intent(this, PreferencesActivity.class), PREFERENCES_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // TODO Handle results from the Search and Checkout activities.
        Log.d(TAG, "returned from preferences");
        switch (requestCode) {
            case PREFERENCES_REQUEST:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                clientName = prefs.getString(PreferencesActivity.PREF_KEY_USERNAME, DEFAULT_CLIENT_NAME);
                clientPort = Integer.valueOf(prefs.getString(PreferencesActivity.PREF_KEY_PORT, String.valueOf(DEFAULT_CLIENT_PORT)));
                break;
        }
    }

    public void onClick(View v) {

        byte[] receiveData = new byte[1024];

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {

//            serverSocket.receive(receivePacket);
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
    }

    public class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            QueryBuilder.executeQuery(TAG,
                    ChatActivity.this,
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
    }
}