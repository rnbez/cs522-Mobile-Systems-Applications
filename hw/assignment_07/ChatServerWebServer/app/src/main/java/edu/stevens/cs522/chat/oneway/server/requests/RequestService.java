package edu.stevens.cs522.chat.oneway.server.requests;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

import edu.stevens.cs522.chat.oneway.server.contracts.MessageContract;
import edu.stevens.cs522.chat.oneway.server.contracts.PeerContract;
import edu.stevens.cs522.chat.oneway.server.entities.Message;
import edu.stevens.cs522.chat.oneway.server.entities.Peer;
import edu.stevens.cs522.chat.oneway.server.managers.IContinue;
import edu.stevens.cs522.chat.oneway.server.managers.ISimpleQueryListener;
import edu.stevens.cs522.chat.oneway.server.managers.MessageManager;
import edu.stevens.cs522.chat.oneway.server.managers.PeerManager;
import edu.stevens.cs522.chat.oneway.server.managers.SimpleQueryBuilder;
import edu.stevens.cs522.chat.oneway.server.utils.CommonSettings;

/**
 * Created by Rafael on 3/12/2016.
 */
public class RequestService extends IntentService {

    /*  10 Points
        *   RequestService (extending IntentService) for performing Web service requests on
        *   background thread (serializing service requests through the service handler thread).
        * */

    public static final String TAG = RequestService.class.getCanonicalName();
    public static final String REGISTER_ACTION = TAG + "_register_action";
    public static final String POST_MESSAGE_ACTION = TAG + "_post_message_action";
    public static final String EXTRA_REGISTER = "extra_register";
    public static final String EXTRA_POST_MESSAGE = "extra_post_message";
    public static final String EXTRA_CALLBACK = "extra_callback";
    public static final String EXTRA_REGISTER_RESULT_ID = "extra_register_result_id";
    public static final String EXTRA_REGISTER_REG_ID = "extra_register_reg_id";
    public static final String EXTRA_POST_MSG_RESULT_ID = "extra_post_msg_result_id";

    public RequestService() {
        super("RequestService");
    }

    public String setServerHost(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String host = prefs.getString(CommonSettings.PREF_KEY_HOST, "");
        Request.DEFAULT_HOST = host;
        return host;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        setServerHost();
        ResultReceiver resultReceiver = intent.getParcelableExtra(EXTRA_CALLBACK);
//        final Bundle result = new Bundle();
//      RESPONSE
        if (REGISTER_ACTION.equals(intent.getAction())) {
            Register req = intent.getParcelableExtra(EXTRA_REGISTER);
            RegisterResponse res = new RequestProcessor().perform(req);
//            result.putParcelable(EXTRA_REGISTER, res);
            Bundle bundle = new Bundle();
            bundle.putLong(EXTRA_REGISTER_RESULT_ID, res.getId());
            bundle.putString(EXTRA_REGISTER_REG_ID, req.getRegistrationID().toString());
            resultReceiver.send(0, bundle);
        } else {
            if (POST_MESSAGE_ACTION.equals(intent.getAction())) {
                PostMessage req = intent.getParcelableExtra(EXTRA_POST_MESSAGE);

                Message m = new Message(0, req.getText(), req.getClientName(), req.getClientId());
                handleMessage(m, true, resultReceiver);

                PostMessageResponse res = new RequestProcessor().perform(req);
//                result.putParcelable(EXTRA_POST_MESSAGE, res);
                m = new Message(res.getId(), req.getText(), req.getClientName(), req.getClientId());
                handleMessage(m, false, resultReceiver);
            } else {
                throw new IllegalArgumentException("Unknown action " + intent.getAction());
            }
        }
    }

    private void handleMessage(Message message, boolean newMessage, final ResultReceiver resultReceiver) {
        Context context = getApplicationContext();
        ContentResolver contentResolver = getContentResolver(); //ChatReceiverService.this.getContentResolver();
        final MessageManager messageManager = new MessageManager(
                context,
                MessageContract.CURSOR_LOADER_ID,
                MessageContract.DEFAULT_ENTITY_CREATOR);

        if(newMessage == true) {
            messageManager.persistAsync(message, new IContinue<Uri>() {
                @Override
                public void kontinue(Uri value) {
                    Log.d(TAG, "message persisted with id 0");
                    resultReceiver.send(0, new Bundle());
                }
            });
        }
        else {
            Uri uri = MessageContract.withExtendedPath(0);
            messageManager.updateAsync(uri, message, new IContinue<Integer>() {
                @Override
                public void kontinue(Integer value) {
                    Log.d(TAG, "message updated with id " + value);
                    resultReceiver.send(0, new Bundle());
                }
            });
        }


    }

}
