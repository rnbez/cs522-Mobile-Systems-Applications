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
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.UUID;
import java.util.ArrayList;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.adapters.MessageRowAdapter;
import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.managers.IQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.QueryBuilder;
import edu.stevens.cs522.chat.oneway.server.managers.TypedCursor;
import edu.stevens.cs522.chat.oneway.server.requests.ServiceHelper;
import edu.stevens.cs522.chat.oneway.server.utils.App;
import edu.stevens.cs522.chat.oneway.server.utils.ResultReceiverWrapper;

public class ChatActivity extends Activity {

    final static public String TAG = ChatActivity.class.getCanonicalName();
    final static public int PREFERENCES_REQUEST = 1;

    private long userId;
    private long lastMessageSeqNum;
    private String userName;
    private UUID registrationID;


    /*
     * TODO: Declare UI.
     */
//    ArrayList<Message> messageList;
    private ListView msgList;
    private Button sendButton;
    private EditText destinationHost;
    private EditText destinationPort;
    private EditText messageText;
    private MessageRowAdapter cursorAdapter;

    private ResultReceiverWrapper registerResultReceiverWrapper;
    private ResultReceiverWrapper.IReceiver registerResultReceiver;
    private ResultReceiverWrapper postMessageResultReceiverWrapper;
    private ResultReceiverWrapper.IReceiver postMessageResultReceiver;
    private SharedPreferences sharedPreferences;
    private ServiceHelper serviceHelper;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        userId = sharedPreferences.getLong(App.PREF_KEY_USERID, App.PREF_DEFAULT_USER_ID);
        lastMessageSeqNum = sharedPreferences.getLong(App.PREF_KEY_LAST_SEQNUM, App.PREF_DEFAULT_LAST_SEQNUM);
        userName = sharedPreferences.getString(App.PREF_KEY_USERNAME, App.PREF_DEFAULT_USER_NAME);
        String uuidString = sharedPreferences.getString(App.PREF_KEY_REGISTRATION_ID, "");
        if (!uuidString.isEmpty())
            registrationID = UUID.fromString(uuidString);

        cursorAdapter = new MessageRowAdapter(this, null);
        msgList = (ListView) findViewById(R.id.main_lst_messages);
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
//        clientPort = Integer.valueOf(prefs.getString(PreferencesActivity.PREF_KEY_PORT, String.valueOf(DEFAULT_CLIENT_PORT)));

        serviceHelper = new ServiceHelper();

        messageText = (EditText) findViewById(R.id.main_edt_message);
        sendButton = (Button) findViewById(R.id.main_btn_send);
        sendButton.setEnabled(registrationID != null);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
                    String msg = messageText.getText().toString();
                    if (!msg.isEmpty()) {
                        ArrayList<Message> messages = new ArrayList<Message>();
                        messages.add(new Message(0, msg, userName, userId));
                        serviceHelper.syncAsync(ChatActivity.this, registrationID, userId, lastMessageSeqNum, messages);
                        messageText.setText("");
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You're offline.", Toast.LENGTH_LONG).show();
                }
            }
        });

        registerResultReceiverWrapper = new ResultReceiverWrapper(new Handler());
        registerResultReceiver = new ResultReceiverWrapper.IReceiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                Log.d(TAG, String.valueOf(resultCode));
                long id = resultData.getLong(ServiceHelper.EXTRA_REGISTER_RESULT_ID);
                UUID uuid = UUID.fromString(resultData.getString(ServiceHelper.EXTRA_REGISTER_REG_ID));

                SharedPreferences.Editor editor = ChatActivity.this.sharedPreferences.edit();
                editor.putString(App.PREF_KEY_REGISTRATION_ID, uuid.toString());
                editor.putLong(App.PREF_KEY_USERID, id);
                editor.apply();

                registrationID = uuid;
                userId = id;

                Toast.makeText(getApplicationContext(), "Registration Succeeded", Toast.LENGTH_LONG).show();
                if (!sendButton.isEnabled()) {
                    sendButton.setEnabled(true);
                }
            }
        };

        postMessageResultReceiverWrapper = new ResultReceiverWrapper(new Handler());
        postMessageResultReceiver = new ResultReceiverWrapper.IReceiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                messageText.setText("");
                updateListView();
            }
        };
//        broadcastRceiver = new MessageReceiver();
//        resultReceicerWrapper = new ResultReceiverWrapper(new Handler());
//        resultReceiver = new ResultReceiverWrapper.IReceiver() {
//            @Override
//            public void onReceiveResult(int resultCode, Bundle resultData) {
//                String toastMessage = "";
//                switch (resultCode) {
//                    case ChatSendService.RESULT_RECEIVER_RESULT_CODE_SUCCESS:
//                        toastMessage = "Your message was sent!";
//                        messageText.setText("");
//                        break;
//                    case ChatSendService.RESULT_RECEIVER_RESULT_CODE_ERROR:
//                        toastMessage = "Sorry, an unexpected error has occurred. Try again.";
//                        break;
//                    default:
//                        throw new IllegalArgumentException("Unknown Result Code: " + resultCode);
//                }
//                Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
//
//            }
//        };
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        client.connect();
//        Intent receiverIntent = new Intent(this, ChatReceiverService.class);
//        receiverIntent.putExtra(ChatReceiverService.EXTRA_SOCKET_PORT, clientPort);
//        startService(receiverIntent);
//
//        IntentFilter filter = new IntentFilter(ChatReceiverService.NEW_MESSAGE_BROADCAST);
//        registerReceiver(broadcastRceiver, filter);

    }

    @Override
    protected void onStop() {
//        unregisterReceiver(broadcastRceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        registerResultReceiverWrapper.setReceiver(null);
        postMessageResultReceiverWrapper.setReceiver(null);
//        stopService(new Intent(this, ChatReceiverService.class));
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerResultReceiverWrapper.setReceiver(registerResultReceiver);
        postMessageResultReceiverWrapper.setReceiver(postMessageResultReceiver);
//        resultReceicerWrapper.setReceiver(resultReceiver);
//        Intent bindIntent = new Intent(this, ChatSendService.class);
//        bindService(bindIntent, conn, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onPause() {
        super.onPause();
//        unbindService(conn);
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
    protected void onActivityResult(final int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // TODO Handle results from the Search and Checkout activities.
        Log.d(TAG, "returned from preferences");
        switch (requestCode) {
            case PREFERENCES_REQUEST:
                userName = sharedPreferences.getString(App.PREF_KEY_USERNAME, App.PREF_DEFAULT_USER_NAME);

//                if (!prefs.contains(App.PREF_KEY_REGISTRATION_ID)) {

//                    final UUID uuid = UUID.randomUUID();
                final UUID uuid = UUID.fromString("54947df8-0e9e-4471-a2f9-9af509fb5889");

                ServiceHelper helper = new ServiceHelper();
                helper.registerAsync(this, userName, uuid, registerResultReceiverWrapper);
//                }

                break;
        }
    }

    public void updateListView() {
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


    public boolean isOnline() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

//    public class MessageReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            QueryBuilder.executeQuery(TAG,
//                    ChatActivity.this,
//                    MessageContract.CONTENT_URI,
//                    MessageContract.CURSOR_LOADER_ID,
//                    MessageContract.DEFAULT_ENTITY_CREATOR,
//                    new IQueryListener<Message>() {
//                        @Override
//                        public void handleResults(TypedCursor<Message> cursor) {
//                            cursorAdapter.swapCursor(cursor.getCursor());
//                        }
//
//                        @Override
//                        public void closeResults() {
//                            cursorAdapter.swapCursor(null);
//                        }
//
//                    });
//        }
//    }

//    private AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//    private PendingIntent alarmIntent= PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReceiver.class), 0);
//
//
//    alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            SystemClock.elapsedRealtime() +
//            60 * 1000, alarmIntent);
//    public class AlarmReceiver extends IntentService{
//
//    };
}