package edu.stevens.cs522.chat.oneway.server.services;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

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

    public static final String TAG = ChatReceiverService.class.getCanonicalName();
    public static final String EXTRA_SOCKET_PORT = TAG + "_port";
    public static final String NEW_MESSAGE_BROADCAST = TAG + "_NewMessageBroadcast";

    private int REQUEST_ID;
    private DatagramSocket serverSocket;

    AsyncTask<Void, Object, Integer> msgLstrTask = new AsyncTask<Void, Object, Integer>() {

        @Override
        protected Integer doInBackground(Void... params) {

            while (true) {
                try {
                    byte[] receiveData = new byte[1024];

                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    Log.d(TAG, "Thread id: " + Thread.currentThread().getId());
                    Log.d(TAG, "Process id: " + android.os.Process.myTid());

                    InetAddress sourceIPAddress = receivePacket.getAddress();

                    Message message = new Message(new String(receivePacket.getData(), "UTF-8"));
                    Peer sender = new Peer(message.getSender(), sourceIPAddress, String.valueOf(receivePacket.getPort()));
//                handleMessage(sender, message);
//                    _message = message;
//                    _peer = sender;
                    publishProgress(sender, message);

                } catch (Exception e) {
                    Log.e(TAG, "Problems receiving packet: " + e.getMessage());
                }
            }
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            handleMessage((Peer)values[0], (Message)values[1]);
            super.onProgressUpdate(values);
        }

        private void handleMessage(final Peer peer, final Message message) {
            Log.d(TAG, peer.getName() + " >> " + message.getMessageText());
            Context context = getApplicationContext();
            ContentResolver contentResolver = getContentResolver(); //ChatReceiverService.this.getContentResolver();
            final PeerManager peerManager = new PeerManager(
                    context,
                    PeerContract.CURSOR_LOADER_ID,
                    PeerContract.DEFAULT_ENTITY_CREATOR);
            final MessageManager messageManager = new MessageManager(
                    context,
                    MessageContract.CURSOR_LOADER_ID,
                    MessageContract.DEFAULT_ENTITY_CREATOR);

            Uri uriWithName = PeerContract.withExtendedPath(message.getSender());
            Log.d(TAG, uriWithName.toString());


            try {
                SimpleQueryBuilder.executeQuery(
                        contentResolver,
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
                                    peerManager.updateAsync(uriWithId, peer, new IContinue<Integer>() {
                                        @Override
                                        public void kontinue(Integer value) {
                                            messageManager.persistAsync(message, new IContinue<Uri>() {
                                                @Override
                                                public void kontinue(Uri value) {
                                                    sendBroadcast(new Intent(NEW_MESSAGE_BROADCAST));
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    peerManager.persistAsync(peer, new IContinue<Uri>() {
                                        @Override
                                        public void kontinue(Uri uri) {
                                            long peerId = PeerContract.getId(uri);
                                            Log.d(TAG, peerId + " >> " + peer.getName());
                                            message.setPeerId(peerId);
                                            Log.d(TAG, message.getPeerId() + " >> " + message.getMessageText());
                                            messageManager.persistAsync(message, new IContinue<Uri>() {
                                                @Override
                                                public void kontinue(Uri value) {
                                                    sendBroadcast(new Intent(NEW_MESSAGE_BROADCAST));
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                );
            } catch (Exception e) {
                Log.e(TAG, "Problems connecting with the db: " + e.getMessage());
            }

        }
    };


    public ChatReceiverService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        new	Thread(messageListener).start();
        REQUEST_ID = startId;

        try {
            if(serverSocket == null || serverSocket.isClosed()) {
                int port = intent.getIntExtra(EXTRA_SOCKET_PORT, 0);
                serverSocket = new DatagramSocket(port);
                Log.i(TAG, "Socket successfully opened.");
            }
            if(msgLstrTask.getStatus() != AsyncTask.Status.RUNNING) {
                msgLstrTask.execute();
            }
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
