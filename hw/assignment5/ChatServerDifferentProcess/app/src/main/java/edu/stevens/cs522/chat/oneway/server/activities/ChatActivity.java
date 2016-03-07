/*********************************************************************
 * Chat server: accept chat messages from clients.
 * <p/>
 * Sender name and GPS coordinates are encoded
 * in the messages, and stripped off upon receipt.
 * <p/>
 * Copyright (c) 2012 Stevens Institute of Technology
 **********************************************************************/
package edu.stevens.cs522.chat.oneway.server.activities;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

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
import edu.stevens.cs522.chat.oneway.server.utils.ResultReceicerWrapper;

public class ChatActivity extends Activity {

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
//    private ChatSendService sendService;
    private Messenger messenger;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
//            sendService = ((ChatSendService.SendServiceBinder) binder).getService();
            messenger = new Messenger(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
//            sendService = null;
            messenger = null;
        }
    };
    private MessageReceiver broadcastRceiver;
    private ResultReceicerWrapper.IReceiver resultReceiver;
    private ResultReceicerWrapper resultReceicerWrapper;


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
                if (messenger != null) {
                    try {
                        Log.d(TAG, "Thread id: " + Thread.currentThread().getId());
                        Log.d(TAG, "Process id: " + android.os.Process.myTid());

                        InetAddress destAddr = InetAddress.getByName(destinationHost.getText().toString());
                        String strDestPort = destinationPort.getText().toString();
                        int destPort = strDestPort != null && !strDestPort.isEmpty() ?
                                Integer.parseInt(strDestPort) :
                                DEFAULT_CLIENT_PORT + 1;
                        String msg = clientName + "#" + messageText.getText().toString();

//                        sendService.sendMessage(msg, destPort, destAddr, destPort);
                        Bundle bundle = new Bundle();
                        bundle.putString(ChatSendService.EXTRA_MESSAGE, msg);
                        bundle.putInt(ChatSendService.EXTRA_SOCKET_PORT, destPort);
                        bundle.putByteArray(ChatSendService.EXTRA_DEST_ADDR, destAddr.getAddress());
                        bundle.putInt(ChatSendService.EXTRA_DEST_PORT, destPort);
                        bundle.putParcelable(ChatSendService.EXTRA_RESULT_RECEIVER, resultReceicerWrapper);
                        android.os.Message message =
                                android.os.Message.obtain(null, 0, 0, 0);
                        message.setData(bundle);
                        messenger.send(message);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (RemoteException e){
                        e.printStackTrace();
                    }

                }
            }
        });

        broadcastRceiver = new MessageReceiver();
        resultReceicerWrapper = new ResultReceicerWrapper(new Handler());
        resultReceiver = new ResultReceicerWrapper.IReceiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                String toastMessage = "";
                switch (resultCode){
                    case ChatSendService.RESULT_RECEIVER_RESULT_CODE_SUCCESS:
                        toastMessage = "Your message was sent!";
                        messageText.setText("");
                        break;
                    case ChatSendService.RESULT_RECEIVER_RESULT_CODE_ERROR:
                        toastMessage = "Sorry, an unexpected error has occurred. Try again.";
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown Result Code: " + resultCode);
                }
                Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent receiverIntent = new Intent(this, ChatReceiverService.class);
        receiverIntent.putExtra(ChatReceiverService.EXTRA_SOCKET_PORT, clientPort);
        startService(receiverIntent);

        IntentFilter filter = new IntentFilter(ChatReceiverService.NEW_MESSAGE_BROADCAST);
        registerReceiver(broadcastRceiver, filter);
    }

    @Override
    protected void onStop() {
        stopService(new Intent(this, ChatReceiverService.class));
        unregisterReceiver(broadcastRceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        resultReceicerWrapper.setReceiver(null);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resultReceicerWrapper.setReceiver(resultReceiver);
        Intent bindIntent = new Intent(this, ChatSendService.class);
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


    public class MessageReceiver extends BroadcastReceiver {

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