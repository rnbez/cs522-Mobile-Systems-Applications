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
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.ChatroomListFragment;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.ChatroomMessagesFragment;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.ConfirmDialogFragment;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.NewChatFragment;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.NewMessageFragment;
import edu.stevens.cs522.chat.oneway.server.adapters.MessageRowAdapter;
import edu.stevens.cs522.chat.oneway.server.contracts.ChatroomContract;
import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.entities.Chatroom;
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.managers.ChatroomManager;
import edu.stevens.cs522.chat.oneway.server.managers.IContinue;
import edu.stevens.cs522.chat.oneway.server.managers.IQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.ISimpleQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.QueryBuilder;
import edu.stevens.cs522.chat.oneway.server.managers.SimpleQueryBuilder;
import edu.stevens.cs522.chat.oneway.server.managers.TypedCursor;
import edu.stevens.cs522.chat.oneway.server.requests.ServiceHelper;
import edu.stevens.cs522.chat.oneway.server.utils.App;
import edu.stevens.cs522.chat.oneway.server.utils.ResultReceiverWrapper;

public class ChatActivity
        extends FragmentActivity
        implements ChatroomListFragment.IChatroomListFragmentListener,
        ChatroomMessagesFragment.IChatroomMessagesFragmentListener,
        NewChatFragment.INewChatFragmentListener,
        NewMessageFragment.INewMessageFragmentListener,
        ConfirmDialogFragment.IConfirmDialogFragmentListener {

    final static public String TAG = ChatActivity.class.getCanonicalName();
    final static public int PREFERENCES_REQUEST = 1;
    final static public int BROADCAST_NETWORK_REQUEST = 100;

    final static public int DIALOG_NEW_CHAT_ID = 1;
    final static public int DIALOG_NEW_CHAT_CONFIRM_ID = DIALOG_NEW_CHAT_ID + 1;
    final static public int DIALOG_NEW_MSG_ID = DIALOG_NEW_CHAT_CONFIRM_ID + 1;


    private long userId;
    private long lastMessageSeqNum;
    private Chatroom currentChatroom;
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

    private Cursor chatroomListCursor;
    private Cursor messageListCursor;
    private MessageRowAdapter cursorAdapter;

    private ResultReceiverWrapper registerResultReceiverWrapper;
    private ResultReceiverWrapper.IReceiver registerResultReceiver;
    private ResultReceiverWrapper postMessageResultReceiverWrapper;
    private ResultReceiverWrapper.IReceiver postMessageResultReceiver;
    private SharedPreferences sharedPreferences;
    private ServiceHelper serviceHelper;
    private AlarmManager alarmMgr;
    PendingIntent alarmIntent;


    FragmentManager fragmentManager;
    ChatroomListFragment chatroomListFragment;
    ChatroomMessagesFragment chatroomMessagesFragment;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layt__main);

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
        currentChatroom = new Chatroom(sharedPreferences.getString(App.PREF_KEY_CHATROOM, App.PREF_DEFAULT_CHATROOM));
        userName = sharedPreferences.getString(App.PREF_KEY_USERNAME, App.PREF_DEFAULT_USER_NAME);
        String uuidString = sharedPreferences.getString(App.PREF_KEY_REGISTRATION_ID, "");
        if (!uuidString.isEmpty())
            registrationID = UUID.fromString(uuidString);


        fragmentManager = getSupportFragmentManager();
        boolean hasSavedInstanceState = savedInstanceState != null;

        launchFragments(hasSavedInstanceState);

//        cursorAdapter = new MessageRowAdapter(this, null);
//        msgList = (ListView) findViewById(R.id.main_lst_messages);
//        msgList.setAdapter(cursorAdapter);


//        if (currentChatroom.getId() == 0) {
//            ContentResolver cr = this.getContentResolver();
//            ContentValues values = new ContentValues();
//            currentChatroom.writeToProvider(values);
//            Uri uri = cr.insert(ChatroomContract.CONTENT_URI, values);
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//            SharedPreferences.Editor editor = prefs.edit();
//            currentChatroom.setId(Long.valueOf(uri.getLastPathSegment()));
//            editor.putString(App.PREF_KEY_CHATROOM, currentChatroom.toString());
//            editor.apply();
//        }

//        clientPort = Integer.valueOf(prefs.getString(PreferencesActivity.PREF_KEY_PORT, String.valueOf(DEFAULT_CLIENT_PORT)));

        serviceHelper = new ServiceHelper();
        if (isOnline() && registrationID != null) {
            Peer peer = new Peer(userId, userName, 0, 0);
            serviceHelper.syncAsync(ChatActivity.this, registrationID, peer, lastMessageSeqNum, new ArrayList<Message>());
        }

        alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);


//        messageText = (EditText) findViewById(R.id.main_edt_message);
//        sendButton = (Button) findViewById(R.id.main_btn_send);
//        sendButton.setEnabled(registrationID != null);
//        sendButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!isOnline()) {
////                    long delay = 10 * 1000;
////                    AlarmManager mgr = (AlarmManager) ChatActivity.this.getSystemService(Context.ALARM_SERVICE);
////                    Intent intent = new Intent(ChatActivity.this, SynchronizationAlarmReceiver.class);
////                    intent.putExtra(SynchronizationAlarmReceiver.EXTRA_DELAY, delay);
////                    PendingIntent listener = PendingIntent.getBroadcast(
////                            ChatActivity.this,
////                            BROADCAST_NETWORK_REQUEST,
////                            intent,
////                            0);
////
////                    mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, listener);
//
//                    Toast.makeText(getApplicationContext(), "You're offline.", Toast.LENGTH_LONG).show();
//                }
//
//                String msg = messageText.getText().toString();
//                if (!msg.isEmpty()) {
//                    ArrayList<Message> messages = new ArrayList<Message>();
//                    Message message = new Message(0, msg, userId, userName, currentChatroom.getId(), currentChatroom.getName(), System.currentTimeMillis());
//                    messages.add(message);
//                    lastMessageSeqNum = sharedPreferences.getLong(App.PREF_KEY_LAST_SEQNUM, App.PREF_DEFAULT_LAST_SEQNUM);
//                    Peer peer = new Peer(userId, userName, 0, 0);
//                    serviceHelper.syncAsync(ChatActivity.this, registrationID, peer, lastMessageSeqNum, messages);
//                    messageText.setText("");
//                }
//            }
//        });

        registerResultReceiverWrapper = new ResultReceiverWrapper(new Handler());
        registerResultReceiver = new ResultReceiverWrapper.IReceiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
//                Log.d(TAG, String.valueOf(resultCode));
//                long id = resultData.getLong(ServiceHelper.EXTRA_REGISTER_RESULT_ID);
//                UUID uuid = UUID.fromString(resultData.getString(ServiceHelper.EXTRA_REGISTER_REG_ID));
//
//                SharedPreferences.Editor editor = ChatActivity.this.sharedPreferences.edit();
//                editor.putString(App.PREF_KEY_REGISTRATION_ID, uuid.toString());
//                editor.putLong(App.PREF_KEY_USERID, id);
//                editor.apply();
//
//                registrationID = uuid;
//                userId = id;
//
//                Toast.makeText(getApplicationContext(), "Registration Succeeded", Toast.LENGTH_LONG).show();
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

    private void launchFragments(boolean hasSavedInstanceState) {
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                if (findViewById(R.id.fragment_container) != null) {
                    chatroomListFragment = new ChatroomListFragment();
                    chatroomListFragment.setArguments(getIntent().getExtras());
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    if (!hasSavedInstanceState) {
                        transaction.add(R.id.fragment_container, chatroomListFragment);
                    } else {
                        transaction.replace(R.id.fragment_container, chatroomListFragment);
                    }
                    transaction.commit();
                }
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                if (fragmentManager.findFragmentById(R.id.layt__main_chatroom_list) != null) {
                    chatroomListFragment = (ChatroomListFragment) fragmentManager.findFragmentById(R.id.layt__main_chatroom_list);
                }
                if (findViewById(R.id.fragment_container) != null) {
                    chatroomMessagesFragment = new ChatroomMessagesFragment();
                    chatroomMessagesFragment.setArguments(getIntent().getExtras());

                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    if (!hasSavedInstanceState) {
                        transaction.add(R.id.fragment_container, chatroomMessagesFragment);
                    } else {
                        transaction.replace(R.id.fragment_container, chatroomMessagesFragment);
                    }
                    transaction.commit();
                }
                break;
            default:
        }
        getChatroomListAsync();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (alarmIntent == null) {
            Log.d("ALARM", "starting alarm manager");
            Intent intent = new Intent(this, SynchronizationAlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(this, this.BROADCAST_NETWORK_REQUEST, intent, 0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 14);
            long triggerAtMillis = calendar.getTimeInMillis();

            long intervalMillis = 10 * 1000;
            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, alarmIntent);
        }
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
        alarmMgr.cancel(alarmIntent);
        alarmIntent = null;
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
            case R.id.chat_menu_new_chat:
                NewChatFragment.launch(this, DIALOG_NEW_CHAT_ID, "NEW_CHAT");
                return true;
            case R.id.chat_menu_new_msg:
                if (chatroomListCursor != null && chatroomListCursor.getCount() > 0) {
                    if (chatroomListCursor.moveToFirst()) {
                        ArrayList<Chatroom> list = new ArrayList<>();
                        int currentChat = 0;
                        do {
                            Chatroom chatroom = new Chatroom(chatroomListCursor);
                            list.add(chatroom);
                            if (chatroom.getId() == currentChatroom.getId())
                                currentChat = chatroomListCursor.getPosition();
                        } while (chatroomListCursor.moveToNext());
                        chatroomListCursor.moveToFirst();
                        NewMessageFragment.launch(this, DIALOG_NEW_MSG_ID, "NEW_MSG", list, currentChat);
                    }
                }
                return true;
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

                if (!sharedPreferences.contains(App.PREF_KEY_REGISTRATION_ID)) {
                    final UUID uuid = UUID.randomUUID();
//                final UUID uuid = UUID.fromString("54947df8-0e9e-4471-a2f9-9af509fb5889");
                    ServiceHelper helper = new ServiceHelper();
                    helper.registerAsync(this, userName, uuid);
                }

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

    @Override
    public void showChatroomDetails(Chatroom chatroom) {
        currentChatroom = chatroom;
        SharedPreferences.Editor editor = ChatActivity.this.sharedPreferences.edit();
        editor.putString(App.PREF_KEY_CHATROOM, currentChatroom.toString());
        editor.apply();

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                if (findViewById(R.id.fragment_container) != null) {
                    messageListCursor = null;
                    chatroomMessagesFragment = new ChatroomMessagesFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(ChatroomMessagesFragment.CHATROOM_DETAILS_KEY, chatroom);
                    chatroomMessagesFragment.setArguments(args);

                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragment_container, chatroomMessagesFragment);
                    transaction.addToBackStack(TAG + "contact_details_fragment");
                    transaction.commit();
                }
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                if (findViewById(R.id.fragment_container) != null) {
                    messageListCursor = null;
                    chatroomMessagesFragment = new ChatroomMessagesFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(ChatroomMessagesFragment.CHATROOM_DETAILS_KEY, chatroom);
                    chatroomMessagesFragment.setArguments(args);

                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragment_container, chatroomMessagesFragment);
//                    transaction.addToBackStack(TAG + "contact_details_fragment");
                    transaction.commit();
                }
                break;
            default:
        }

        getChatroomMessagesAsync(chatroom);
    }

    @Override
    public Cursor getChatroomListCursor() {
        return chatroomListCursor;
    }

    @Override
    public Cursor getMessageListCursor() {
        return messageListCursor;
    }

    @Override
    public void getChatroomListAsync() {
        QueryBuilder.executeQuery(TAG,
                this,
                ChatroomContract.CONTENT_URI,
                ChatroomContract.CURSOR_LOADER_ID,
                ChatroomContract.DEFAULT_ENTITY_CREATOR,
                new IQueryListener<Chatroom>() {
                    @Override
                    public void handleResults(TypedCursor<Chatroom> typedCursor) {
                        chatroomListCursor = typedCursor.getCursor();
                        chatroomListFragment.setListCursor(chatroomListCursor);
                    }

                    @Override
                    public void closeResults() {
                        chatroomListCursor = null;
                        chatroomListFragment.setListCursor(chatroomListCursor);
                    }
                });
    }

    @Override
    public void getChatroomMessagesAsync(Chatroom chatroom) {
        Uri uri = ChatroomContract.withExtendedPath(chatroom.getId());
        uri = ChatroomContract.withExtendedPath(uri, "messages");


        QueryBuilder.executeQuery(TAG,
                this,
                uri,
                MessageContract.CURSOR_LOADER_ID + 100 + (int)chatroom.getId(),
                MessageContract.DEFAULT_ENTITY_CREATOR,
                new IQueryListener<Message>() {
                    @Override
                    public void handleResults(TypedCursor<Message> typedCursor) {
                        messageListCursor = typedCursor.getCursor();
//                        if (chatroomMessagesFragment != null)
                        chatroomMessagesFragment.setListCursor(messageListCursor);
                    }

                    @Override
                    public void closeResults() {
                        messageListCursor = null;
//                        if (chatroomMessagesFragment != null)
                        chatroomMessagesFragment.setListCursor(messageListCursor);
                    }
                });
    }

    @Override
    public void newChatAcknowledge(Dialog dialog, boolean confirm, final String chatroom) {
        if (dialog != null) {
            dialog.dismiss();
            if (confirm) {
                final ChatroomManager manager = new ChatroomManager(
                        this,
                        ChatroomContract.CURSOR_LOADER_ID,
                        ChatroomContract.DEFAULT_ENTITY_CREATOR);

                final Activity context = this;
                Uri uriWithName = ChatroomContract.withExtendedPath(chatroom);
                Log.d(TAG, uriWithName.toString());

                SimpleQueryBuilder.executeQuery(
                        getContentResolver(),
                        uriWithName,
                        ChatroomContract.DEFAULT_ENTITY_CREATOR,
                        new ISimpleQueryListener<Chatroom>() {
                            @Override
                            public void handleResults(List<Chatroom> results) {
                                if (results.size() > 0) {
                                    int dialogId = DIALOG_NEW_CHAT_CONFIRM_ID;
                                    int infoMsgId = R.string.frag__new_chat_error_message;
                                    int confirm = R.string.frag__new_chat_error_ack;
                                    int cancel = 0;
                                    String tag = "CHAT_CONFIRM_ID";
                                    ConfirmDialogFragment.launch(context, dialogId, tag, infoMsgId, confirm, cancel);
                                } else {
                                    manager.persistAsync(new Chatroom(chatroom), new IContinue<Uri>() {
                                        @Override
                                        public void kontinue(Uri uri) {
                                            currentChatroom = new Chatroom(Long.valueOf(uri.getLastPathSegment()), chatroom);
                                            SharedPreferences.Editor editor = ChatActivity.this.sharedPreferences.edit();
                                            editor.putString(App.PREF_KEY_CHATROOM, currentChatroom.toString());
                                            editor.apply();
//                                            if (chatroomListFragment != null) {
//                                                int pos = 0;
//                                                if (chatroomListCursor.moveToFirst()) {
//                                                    do {
//                                                        Chatroom cursorChat = new Chatroom(chatroomListCursor);
//                                                        if (cursorChat.getId() == currentChatroom.getId()){
//                                                            pos = chatroomListCursor.getPosition();
//                                                            break;
//                                                        }
//                                                    }while (chatroomListCursor.moveToNext());
//
//                                                    chatroomListFragment.setSelectItem(pos);
//                                                }
//                                            }
//                                            showChatroomDetails(currentChatroom);
                                        }
                                    });
                                }
                            }
                        }
                );
            }
        }
    }

    @Override
    public void newMessageAcknowledge(Dialog dialog, boolean confirm, String inputText, Chatroom
            selectedChatroom) {
        if (dialog != null) {
            dialog.dismiss();
            if (confirm) {
                if (!isOnline()) {
                    Toast.makeText(getApplicationContext(), "You're offline.", Toast.LENGTH_LONG).show();
                }

                if (!inputText.isEmpty()) {
                    currentChatroom = new Chatroom(sharedPreferences.getString(App.PREF_KEY_CHATROOM, App.PREF_DEFAULT_CHATROOM));
                    lastMessageSeqNum = sharedPreferences.getLong(App.PREF_KEY_LAST_SEQNUM, App.PREF_DEFAULT_LAST_SEQNUM);

                    ArrayList<Message> messages = new ArrayList<>();
                    Message message = new Message(0, inputText, userId, userName, currentChatroom.getId(), currentChatroom.getName(), System.currentTimeMillis());
                    messages.add(message);
                    Peer peer = new Peer(userId, userName, 0, 0);
                    serviceHelper.syncAsync(ChatActivity.this, registrationID, peer, lastMessageSeqNum, messages);
                    messageText.setText("");
                }
            }
        }
    }

    @Override
    public void confirmDialogCallback(int dialogId, Dialog dialog, boolean confirm) {
        if (dialog != null)
            dialog.dismiss();
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


    static public class SynchronizationAlarmReceiver extends BroadcastReceiver {

        public static final String EXTRA_DELAY = "extra_delay";

        public boolean isOnline(Context context) {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ALARME", "Alarm executed at: " + new java.util.Date());
            if (isOnline(context)) {
                ServiceHelper serviceHelper = new ServiceHelper();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                long userId = prefs.getLong(App.PREF_KEY_USERID, App.PREF_DEFAULT_USER_ID);
                long lastMessageSeqNum = prefs.getLong(App.PREF_KEY_LAST_SEQNUM, App.PREF_DEFAULT_LAST_SEQNUM);
                String userName = prefs.getString(App.PREF_KEY_USERNAME, App.PREF_DEFAULT_USER_NAME);

                String uuidString = prefs.getString(App.PREF_KEY_REGISTRATION_ID, "");
                if (!uuidString.isEmpty()) {
                    UUID registrationID = UUID.fromString(uuidString);
                    Peer peer = new Peer(userId, userName, 0, 0);
                    serviceHelper.syncAsync(context, registrationID, peer, lastMessageSeqNum, new ArrayList<Message>());
                }
            }
        }

    }
}