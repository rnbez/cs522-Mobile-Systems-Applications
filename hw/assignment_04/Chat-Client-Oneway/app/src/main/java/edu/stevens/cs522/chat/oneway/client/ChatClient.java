/*********************************************************************
 * Client for sending chat messages to the server.
 * <p/>
 * Copyright (c) 2012 Stevens Institute of Technology
 **********************************************************************/
package edu.stevens.cs522.chat.oneway.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/*
 * @author dduggan
 * 
 */
public class ChatClient extends Activity implements OnClickListener {

    final static private String TAG = ChatClient.class.getSimpleName();

    /*
     * Client name (should be entered in a parent activity, provided as an extra in the intent).
     */
    public static final String CLIENT_NAME_KEY = "client_name";

    public static final String DEFAULT_CLIENT_NAME = "client";
    private static final int PREFERENCES_REQUEST = 1;

    private String clientName;

    /*
     * Client UDP port (should be entered in a parent activity, provided as an extra in the intent).
     */
    public static final String CLIENT_PORT_KEY = "client_port";

    public static final int DEFAULT_CLIENT_PORT = 6666;
    public static final String DEFAULT_CLIENT_PORT_STRING = "6666";

    public static final String DEFAULT_SERVER_ADDR = "127.0.0.1";

    public static final int DEFAULT_SERVER_PORT = DEFAULT_CLIENT_PORT;

    /*
     * Socket used for sending
     */
    private DatagramSocket clientSocket;

    private int clientPort;

    /*
     * Widgets for dest address, message text, send button.
     */
    private EditText destinationHost;

    private EditText destinationPort;

    private EditText messageText;

    private Button sendButton;

    /*
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_chat);

//        Intent callingIntent = getIntent();
//        if (callingIntent != null && callingIntent.getExtras() != null) {
//
//            clientName = callingIntent.getExtras().getString(CLIENT_NAME_KEY, DEFAULT_CLIENT_NAME);
//            clientPort = callingIntent.getExtras().getInt(CLIENT_PORT_KEY, DEFAULT_CLIENT_PORT);
//
//        } else {
//
//            clientName = DEFAULT_CLIENT_NAME;
//            clientPort = DEFAULT_CLIENT_PORT;
//
//        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        clientName = prefs.getString(PreferencesActivity.PREF_KEY_USERNAME, DEFAULT_CLIENT_NAME);
        clientPort = Integer.valueOf(prefs.getString(PreferencesActivity.PREF_KEY_PORT, DEFAULT_CLIENT_PORT_STRING));
        /**
         * Let's be clear, this is a HACK to allow you to do network communication on the client_chat thread.
         * This WILL cause an ANR, and is only provided to simplify the pedagogy.  We will see how to do
         * this right in a future assignment (using a Service managing background threads).
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // TODO initialize the UI.

        destinationHost = (EditText) findViewById(R.id.destination_host);
        destinationPort = (EditText) findViewById(R.id.destination_port);
        messageText = (EditText) findViewById(R.id.message_text);
        sendButton = (Button) findViewById(R.id.send_button);

        // End todo

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.client_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.chat_menu_contacts:
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivityForResult(intent, PREFERENCES_REQUEST);
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
                clientPort = Integer.valueOf(prefs.getString(PreferencesActivity.PREF_KEY_PORT, DEFAULT_CLIENT_PORT_STRING));
                break;

        }


    }

    /*
     * Callback for the SEND button.
     */
    public void onClick(View v) {
        try {

            clientSocket = new DatagramSocket(clientPort);

            /*
             * On the emulator, which does not support WIFI stack, we'll send to
			 * (an AVD alias for) the host loopback interface, with the server
			 * port on the host redirected to the server port on the server AVD.
			 */

            InetAddress destAddr = null;

            int destPort = 0;

            byte[] sendData = null;  // Combine sender and message text; default encoding is UTF-8

            // TODO get data from UI

            destAddr = InetAddress.getByName(destinationHost.getText().toString());
            destPort = Integer.getInteger(destinationPort.getText().toString(), DEFAULT_CLIENT_PORT);
            String msg = this.clientName + "#" + messageText.getText().toString();
            Log.d(TAG, msg);
            sendData = msg.getBytes("UTF-8");

            // End todo

            DatagramPacket sendPacket = new DatagramPacket(sendData,
                    sendData.length, destAddr, destPort);

            clientSocket.send(sendPacket);
            clientSocket.close();

            Log.i(TAG, "Sent packet: " + messageText);

        } catch (UnknownHostException e) {
            Log.e(TAG, "Unknown host exception: ", e);
        } catch (IOException e) {
            Log.e(TAG, "IO exception: ", e);
        } catch (Exception e) {
            Log.e(TAG, "Cannot open socket: " + e.getMessage(), e);
            return;
        }

        messageText.setText("");
    }

}