/*********************************************************************
 * Chat server: accept chat messages from clients.
 * <p/>
 * Sender name and GPS coordinates are encoded
 * in the messages, and stripped off upon receipt.
 * <p/>
 * Copyright (c) 2012 Stevens Institute of Technology
 **********************************************************************/
package edu.stevens.cs522.chat.oneway.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.adapters.ListAdapter;
import edu.stevens.cs522.chat.oneway.server.entities.ReceivedMessage;

public class ChatServer extends Activity implements OnClickListener {

    final static public String TAG = ChatServer.class.getCanonicalName();

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
    ArrayList<ReceivedMessage> messageList;
    ListView msgList;
    /*
     * End Todo
	 */

    Button next;

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

		/*
		 * TODO: Initialize the UI.
		 */
        messageList = new ArrayList<>();
        msgList  = (ListView) findViewById(R.id.msgList);
        ListAdapter adapter = new ListAdapter(getApplicationContext(), R.layout.main, messageList);
        msgList.setAdapter(adapter);

		/*
		 * End Todo
		 */

    }

    public void onClick(View v) {

        byte[] receiveData = new byte[1024];

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {

            serverSocket.receive(receivePacket);
            Log.i(TAG, "Received a packet");
            Log.i(TAG, "Message: " + new String(receiveData, "UTF-8"));

//            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

            InetAddress sourceIPAddress = receivePacket.getAddress();
            Log.i(TAG, "Source IP Address: " + sourceIPAddress);
			
			/*
			 * TODO: Extract sender and receiver from message and display.
			 */
            ReceivedMessage receivedMessage = new ReceivedMessage(new String(receiveData, "UTF-8"));
            messageList.add(receivedMessage);
            ListAdapter adp = (ListAdapter) msgList.getAdapter();
            adp.notifyDataSetChanged();

			/*
			 * End Todo
			 */

        } catch (Exception e) {

            Log.e(TAG, "Problems receiving packet: " + e.getMessage());
            socketOK = false;
        }

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