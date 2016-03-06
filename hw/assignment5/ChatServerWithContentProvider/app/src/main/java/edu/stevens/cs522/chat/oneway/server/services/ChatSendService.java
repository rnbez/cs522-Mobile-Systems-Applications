package edu.stevens.cs522.chat.oneway.server.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ChatSendService extends Service {
    //TODO: (5 pts) Bound service (client calls bindService)
    //TODO: (10 pts) Messaging send operation is part of binder interface (implements IBind)
    /*
        You should use the same logic for both parts of the assignment,
        using a HandlerThread to perform the message sending.
     */

    final static public String TAG = ChatSendService.class.getCanonicalName();
    final static public String EXTRA_MESSAGE = TAG + "_msg";
    final static public String EXTRA_SOCKET_PORT = TAG + "_port";
    final static public String EXTRA_DEST_ADDR = TAG + "_ipAddress";
    final static public String EXTRA_DEST_PORT = TAG + "_destPort";
    private	Looper looper;
    private	MessageHandler handler;
    private final IBinder binder = new SendServiceBinder();
    private DatagramSocket clientSocket;

    public ChatSendService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return binder;
    }

    public void sendMessage(String msg, int port, InetAddress destAddr, int destPort) {
        Log.d(TAG, "Thread id: " + Thread.currentThread().getId());
        Log.d(TAG, "Process id: " + android.os.Process.myTid());

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_MESSAGE, msg);
        bundle.putInt(EXTRA_SOCKET_PORT, port);
        bundle.putByteArray(EXTRA_DEST_ADDR, destAddr.getAddress());
        bundle.putInt(EXTRA_DEST_PORT, destPort);
        Message message = handler.obtainMessage();
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public class SendServiceBinder extends Binder {
        public ChatSendService getService() {
            return ChatSendService.this;
        }

    }

    @Override
    public void onCreate() {
        HandlerThread messagerThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        messagerThread.start();
        looper = messagerThread.getLooper();
        handler = new MessageHandler(looper);
    }

    class MessageHandler extends Handler {
        final public String TAG = MessageHandler.class.getCanonicalName();

        private Looper looper;

        public MessageHandler(Looper looper) {
            super(looper);
            this.looper = looper;
        }

        @Override
        public void handleMessage(Message message) {

            Log.d(TAG, "Thread id: " + Thread.currentThread().getId());
            Log.d(TAG, "Process id: " + android.os.Process.myTid());

            super.handleMessage(message);
            Bundle bundle = message.getData();
            byte[] addr = bundle.getByteArray(ChatSendService.EXTRA_DEST_ADDR);
            InetAddress destAddr = null;
            try {
                destAddr = InetAddress.getByAddress(addr);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            String msg = bundle.getString(ChatSendService.EXTRA_MESSAGE);
            int port = bundle.getInt(ChatSendService.EXTRA_SOCKET_PORT);
            int destPort = bundle.getInt(ChatSendService.EXTRA_DEST_PORT);

            // ... do stuff ...
            Log.d(TAG, msg);
            try {

                clientSocket = new DatagramSocket(port);

                byte[] sendData = null;  // Combine sender and message text; default encoding is UTF-8
                // TODO get data from UI
//            destAddr = InetAddress.getByName(destinationHost.getText().toString());
//            destPort = Integer.getInteger(destinationPort.getText().toString(), DEFAULT_CLIENT_PORT);
//            String msg = this.clientName + "#" + messageText.getText().toString();
//            Log.d(TAG, msg);
                sendData = msg.getBytes("UTF-8");

                // End todo

                DatagramPacket sendPacket = new DatagramPacket(sendData,
                        sendData.length, destAddr, destPort);

                clientSocket.send(sendPacket);
                clientSocket.close();

            } catch (SocketException e) {
                Log.e(TAG, "Cannot open socket: " + e.getMessage(), e);
                return;
//            e.printStackTrace();
            } catch (UnknownHostException e) {
                Log.e(TAG, "Unknown host exception: ", e);
                return;
            } catch (IOException e) {
                Log.e(TAG, "IO exception: ", e);
                return;
            }
        }
    }

}
