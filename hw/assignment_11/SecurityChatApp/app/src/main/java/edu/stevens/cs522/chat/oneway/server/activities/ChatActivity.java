/*********************************************************************
 * Chat server: accept chat messages from clients.
 * <p>
 * Sender name and GPS coordinates are encoded
 * in the messages, and stripped off upon receipt.
 * <p>
 * Copyright (c) 2012 Stevens Institute of Technology
 **********************************************************************/
package edu.stevens.cs522.chat.oneway.server.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.activities.base.BaseFragmentActivity;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.ChatroomListFragment;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.ChatroomMessagesFragment;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.ConfirmDialogFragment;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.NewChatFragment;
import edu.stevens.cs522.chat.oneway.server.activities.fragments.NewMessageFragment;
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
        extends BaseFragmentActivity
        implements ChatroomListFragment.IChatroomListFragmentListener,
        ChatroomMessagesFragment.IChatroomMessagesFragmentListener,
        NewChatFragment.INewChatFragmentListener,
        NewMessageFragment.INewMessageFragmentListener,
        ConfirmDialogFragment.IConfirmDialogFragmentListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    final static public String TAG = ChatActivity.class.getCanonicalName();
    final static public int PREFERENCES_REQUEST = 1;
    final static public int PLAY_SERVICES_RESOLUTION_REQUEST = PREFERENCES_REQUEST + 1;
    final static public int CONNECTION_FAILURE_RESOLUTION_REQUEST = PLAY_SERVICES_RESOLUTION_REQUEST + 1;
    final static public int PLAY_SERVICES_LOCATION_PERMISION_REQUEST = CONNECTION_FAILURE_RESOLUTION_REQUEST + 1;
    final static public int BROADCAST_NETWORK_REQUEST = 100;

    final static public int DIALOG_NEW_CHAT_ID = 1;
    final static public int DIALOG_NEW_CHAT_CONFIRM_ID = DIALOG_NEW_CHAT_ID + 1;
    final static public int DIALOG_NEW_MSG_ID = DIALOG_NEW_CHAT_CONFIRM_ID + 1;


    private long userId;
    private long lastMessageSeqNum;
    private Chatroom currentChatroom;
    private String userName;
    private double userLatitude;
    private double userLongitude;
    private UUID registrationID;


    /*
     * TODO: Declare UI.
     */
//    ArrayList<Message> messageList;
    private Cursor chatroomListCursor;
    private Cursor messageListCursor;

    private ResultReceiverWrapper registerResultReceiverWrapper;
    private ResultReceiverWrapper.IReceiver registerResultReceiver;
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
    private GoogleApiClient googleApiClient;
    LocationManager locationManager;
    private LocationListener locationListener;


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
        String host = sharedPreferences.getString(App.PREF_KEY_HOST, "");
        if (host.isEmpty()) {
            SharedPreferences.Editor prefEditor = sharedPreferences.edit();
            prefEditor.putString(App.PREF_KEY_HOST, "http://10.0.2.2:81");
            prefEditor.apply();
        }

        userId = sharedPreferences.getLong(App.PREF_KEY_USERID, App.PREF_DEFAULT_USER_ID);
        lastMessageSeqNum = sharedPreferences.getLong(App.PREF_KEY_LAST_SEQNUM, App.PREF_DEFAULT_LAST_SEQNUM);
        currentChatroom = new Chatroom(sharedPreferences.getString(App.PREF_KEY_CHATROOM, App.PREF_DEFAULT_CHATROOM));
        userName = sharedPreferences.getString(App.PREF_KEY_USERNAME, App.PREF_DEFAULT_USER_NAME);
        userLatitude = (double) sharedPreferences.getFloat(App.PREF_KEY_LATITUDE, 0);
        userLongitude = (double) sharedPreferences.getFloat(App.PREF_KEY_LONGITUDE, 0);
        String uuidString = sharedPreferences.getString(App.PREF_KEY_REGISTRATION_ID, "");
        if (!uuidString.isEmpty())
            registrationID = UUID.fromString(uuidString);


        fragmentManager = getSupportFragmentManager();
        boolean hasSavedInstanceState = savedInstanceState != null;

        launchFragments(hasSavedInstanceState);

        ServiceHelper.setDatabaseKey(super.getDatabaseKey());
        serviceHelper = new ServiceHelper();
        if (isOnline() && registrationID != null) {
            Peer peer = new Peer(userId, userName, userLatitude, userLongitude);
            serviceHelper.syncAsync(ChatActivity.this, registrationID, peer, lastMessageSeqNum, new ArrayList<Message>());
        }

        alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);


//        registerResultReceiverWrapper = new ResultReceiverWrapper(new Handler());
//        registerResultReceiver = new ResultReceiverWrapper.IReceiver() {
//            @Override
//            public void onReceiveResult(int resultCode, Bundle resultData) {
//            }
//        };

//        postMessageResultReceiverWrapper = new ResultReceiverWrapper(new Handler());
//        postMessageResultReceiver = new ResultReceiverWrapper.IReceiver() {
//            @Override
//            public void onReceiveResult(int resultCode, Bundle resultData) {
//                messageText.setText("");
//                updateListView();
//            }
//        };

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                SharedPreferences.Editor editor = ChatActivity.this.sharedPreferences.edit();
                editor.putFloat(App.PREF_KEY_LATITUDE, (float) location.getLatitude());
                editor.putFloat(App.PREF_KEY_LONGITUDE, (float) location.getLongitude());
                editor.apply();

                Log.d(TAG, "updating location");
            }
        };
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
        googleApiClient.connect();
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

        super.onStart();
    }

    @Override
    protected void onStop() {
        alarmMgr.cancel(alarmIntent);
        alarmIntent = null;
        googleApiClient.disconnect();
//        unregisterReceiver(broadcastRceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
//        registerResultReceiverWrapper.setReceiver(null);
//        postMessageResultReceiverWrapper.setReceiver(null);
//        stopService(new Intent(this, ChatReceiverService.class));
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        registerResultReceiverWrapper.setReceiver(registerResultReceiver);
//        postMessageResultReceiverWrapper.setReceiver(postMessageResultReceiver);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
//        String uuidString = sharedPreferences.getString(App.PREF_KEY_REGISTRATION_ID, "");
        MenuItem menuItem;
        boolean isVisible = registrationID != null;

        menuItem = menu.findItem(R.id.chat_menu_unregister);
        if (menuItem != null) menuItem.setVisible(isVisible);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String uuidString = sharedPreferences.getString(App.PREF_KEY_REGISTRATION_ID, "");
        switch (item.getItemId()) {
            case R.id.chat_menu_new_chat:
                if (!uuidString.isEmpty()) {
                    NewChatFragment.launch(this, DIALOG_NEW_CHAT_ID, "NEW_CHAT");
                } else {
                    Toast.makeText(getApplicationContext(), "You must register first.", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.chat_menu_new_msg:
                if (!uuidString.isEmpty()) {
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
                } else {
                    Toast.makeText(getApplicationContext(), "You must register first.", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.chat_menu_contacts:
                startActivity(new Intent(this, ContactBookActivity.class));
                return true;
            case R.id.chat_menu_prefs:
                startActivityForResult(new Intent(this, PreferencesActivity.class), PREFERENCES_REQUEST);
                return true;
            case R.id.chat_menu_unregister:
                Peer peer = new Peer(userId, userName, userLatitude, userLongitude);
                serviceHelper.unregisterAsync(this, registrationID, peer);
                userId = App.PREF_DEFAULT_USER_ID;
//                lastMessageSeqNum = App.PREF_DEFAULT_LAST_SEQNUM;
//                currentChatroom = new Chatroom("");
                userName = "";
                registrationID = null;
                invalidateOptionsMenu();
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
                if (!sharedPreferences.contains(App.PREF_KEY_REGISTRATION_ID)) {
                    userName = sharedPreferences.getString(App.PREF_KEY_USERNAME, App.PREF_DEFAULT_USER_NAME);
                    final UUID uuid = UUID.randomUUID();
//                final UUID uuid = UUID.fromString("54947df8-0e9e-4471-a2f9-9af509fb5889");
                    ServiceHelper helper = new ServiceHelper();
                    helper.registerAsync(this, uuid, new Peer(userName, userLatitude, userLongitude));
                    registrationID = uuid;
                    invalidateOptionsMenu();
                }
                break;
        }
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
//                    messageListCursor = null;
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
//                    messageListCursor = null;
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
                ChatroomContract.withDatabaseKeyUri(super.getDatabaseKey()),
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
        Uri uri = ChatroomContract.getMessagesUri(chatroom.getId());

        QueryBuilder.executeQuery(TAG,
                this,
                ChatroomContract.withDatabaseKeyUri(super.getDatabaseKey(), uri),
                MessageContract.CURSOR_LOADER_ID + (100 * (int) chatroom.getId()),
                MessageContract.DEFAULT_ENTITY_CREATOR,
                new IQueryListener<Message>() {
                    @Override
                    public void handleResults(TypedCursor<Message> typedCursor) {
                        messageListCursor = typedCursor.getCursor();
                        if (chatroomMessagesFragment != null)
                            chatroomMessagesFragment.setListCursor(messageListCursor);
                    }

                    @Override
                    public void closeResults() {
                        messageListCursor = null;
                        if (chatroomMessagesFragment != null)
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
                        super.getDatabaseKey(),
                        ChatroomContract.CURSOR_LOADER_ID,
                        ChatroomContract.DEFAULT_ENTITY_CREATOR);

                final Activity context = this;
                Uri uriWithName = ChatroomContract.withExtendedPath(chatroom);
                Log.d(TAG, uriWithName.toString());

                SimpleQueryBuilder.executeQuery(
                        getContentResolver(),
                        ChatroomContract.withDatabaseKeyUri(super.getDatabaseKey(), uriWithName),
                        ChatroomContract.DEFAULT_ENTITY_CREATOR,
                        new ISimpleQueryListener<Chatroom>() {
                            @Override
                            public void handleResults(List<Chatroom> results) {
                                if (results.size() > 0) {
                                    int dialogId = DIALOG_NEW_CHAT_CONFIRM_ID;
                                    int infoMsgId = R.string.frag__new_chat_error_message;
                                    int confirm = R.string.frag__new_chat_error_ack;
                                    int cancel = -1;
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
//
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
                    userId = sharedPreferences.getLong(App.PREF_KEY_USERID, App.PREF_DEFAULT_USER_ID);
                    lastMessageSeqNum = sharedPreferences.getLong(App.PREF_KEY_LAST_SEQNUM, App.PREF_DEFAULT_LAST_SEQNUM);
                    userLatitude = (double) sharedPreferences.getFloat(App.PREF_KEY_LATITUDE, 0);
                    userLongitude = (double) sharedPreferences.getFloat(App.PREF_KEY_LONGITUDE, 0);
//                    currentChatroom = new Chatroom(sharedPreferences.getString(App.PREF_KEY_CHATROOM, App.PREF_DEFAULT_CHATROOM));
                    userName = sharedPreferences.getString(App.PREF_KEY_USERNAME, App.PREF_DEFAULT_USER_NAME);
                    String uuidString = sharedPreferences.getString(App.PREF_KEY_REGISTRATION_ID, "");
                    if (!uuidString.isEmpty())
                        registrationID = UUID.fromString(uuidString);

                    ArrayList<Message> messages = new ArrayList<>();
                    Message message = new Message(0, inputText,
                            userId, userName,
                            selectedChatroom.getId(), selectedChatroom.getName(),
                            System.currentTimeMillis(),
                            userLatitude, userLongitude);
                    messages.add(message);
                    Peer peer = new Peer(userId, userName, userLatitude, userLongitude);
                    serviceHelper.syncAsync(ChatActivity.this, registrationID, peer, lastMessageSeqNum, messages);
                    showChatroomDetails(selectedChatroom);
                }
            }
        }
    }

    @Override
    public void confirmDialogCallback(int dialogId, Dialog dialog, boolean confirm) {
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "location: onConnected");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "location: onConnected - PERMISSION DENIED");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String[] permissions = new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                };
                requestPermissions(permissions, PLAY_SERVICES_LOCATION_PERMISION_REQUEST);
            }
            return;
        }

        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Log.d(TAG, "location: onConnected - PERMISSION GRANTED");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        criteria.setSpeedRequired(false);
        String provider = locationManager.getBestProvider(criteria, true);

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "location: onConnectionSuspended");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "location: onConnectionSuspended - PERMISSION DENIED");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String[] permissions = new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                };
                requestPermissions(permissions, PLAY_SERVICES_LOCATION_PERMISION_REQUEST);
            }
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        requestLocationUpdates();
    }

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
                double userLatitude = (double) prefs.getFloat(App.PREF_KEY_LATITUDE, 0);
                double userLongitude = (double) prefs.getFloat(App.PREF_KEY_LONGITUDE, 0);

                String uuidString = prefs.getString(App.PREF_KEY_REGISTRATION_ID, "");
                if (!uuidString.isEmpty()) {
                    UUID registrationID = UUID.fromString(uuidString);
                    Peer peer = new Peer(userId, userName, userLatitude, userLongitude);
                    serviceHelper.syncAsync(context, registrationID, peer, lastMessageSeqNum, new ArrayList<Message>());
                }
            }
        }

    }

    public static boolean checkPlayServices(Activity context) {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil
                    .isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, context,
                        PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.e(TAG, "This device is not supported.");
                context.finish();
            }
            return false;
        }
        return true;
    }
}