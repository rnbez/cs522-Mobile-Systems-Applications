package edu.stevens.cs522.chat.oneway.server.requests;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

/**
 * Created by Rafael on 3/12/2016.
 */
public class RequestService extends IntentService {

    /*  10 Points
        *   RequestService (extending IntentService) for performing Web service requests on
        *   background thread (serializing service requests through the service handler thread).
        * */

    public static final String EXTRA_REGISTER = "extra_register";
    public static final String EXTRA_POST_MESSAGE = "extra_post_message";

    public RequestService() {
        super("RequestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent.hasExtra(EXTRA_REGISTER)){
            Register req = intent.getParcelableExtra(EXTRA_REGISTER);
            new RequestProcessor().perform(req);
        }
        if(intent.hasExtra(EXTRA_POST_MESSAGE)){
            PostMessage req = intent.getParcelableExtra(EXTRA_POST_MESSAGE);
            new RequestProcessor().perform(req);
        }

//      RESPONSE
        ResultReceiver resultReceiver = intent.getParcelableExtra("RECEIVER");
        Bundle result = new Bundle();
        result.putInt("ACK", 1);
        resultReceiver.send(0, result);
    }

}
