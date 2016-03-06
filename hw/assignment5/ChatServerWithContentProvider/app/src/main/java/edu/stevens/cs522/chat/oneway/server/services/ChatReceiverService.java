package edu.stevens.cs522.chat.oneway.server.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;

public class ChatReceiverService extends Service {

    //TODO: (10 pts) Started service (client calls startService)
    //TODO: (10 pts) Use AsyncTask to define thread that receives messages.
    //TODO: (10 pts) Updates messages content provider, use broadcast to notify interested parties

    /*
        For message receipt, your started service executes a
        loop on a background thread where it does a blocking
        receive for the next message.

        The rubric says to use AsyncTask for this background thread.
    * */

    final static public String TAG = ChatReceiverService.class.getCanonicalName();
    final static public String EXTRA_SOCKET_PORT = TAG + "_port";

    private int REQUEST_ID;
    private DatagramSocket serverSocket;

    AsyncTask<Void, Void, Integer> msgLstrTask = new AsyncTask<Void, Void, Integer>() {
        @Override
        protected Integer doInBackground(Void... params) {
            byte[] receiveData = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {

                serverSocket.receive(receivePacket);

                Log.d(TAG, "Thread id: " + Thread.currentThread().getId());
                Log.d(TAG, "Process id: " + android.os.Process.myTid());
//                Log.i(TAG, "Received a packet");
//                Log.i(TAG, "Message: " + new String(receiveData, "UTF-8"));

                InetAddress sourceIPAddress = receivePacket.getAddress();
//            Log.i(TAG, "Source IP Address: " + sourceIPAddress);

                Message message = new Message(new String(receivePacket.getData(), "UTF-8"));
                Peer sender = new Peer(message.getSender(), sourceIPAddress, String.valueOf(receivePacket.getPort()));
//                handleMessage(sender, message);

            } catch (Exception e) {
                Log.e(TAG, "Problems receiving packet: " + e.getMessage());
//                socketOK = false;
            }
            return 0;
        }
    };


    public ChatReceiverService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        new	Thread(messageListener).start();
        REQUEST_ID = startId;

        try {
            int port = intent.getIntExtra(EXTRA_SOCKET_PORT, 0);
            serverSocket = new DatagramSocket(port);
            msgLstrTask.execute();
            Log.i(TAG, "Socket successfully opened.");
        } catch (Exception e) {
            Log.e(TAG, "Cannot open socket" + e.getMessage());
        }

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        serverSocket.close();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
