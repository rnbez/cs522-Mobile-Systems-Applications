package edu.stevens.cs522.chat.oneway.server.requests;

import android.app.Activity;
import android.content.Intent;

import java.util.UUID;

/**
 * Created by Rafael on 3/12/2016.
 */
public class ServiceHelper {
/* 10 Points
*  ServiceHelper class used by UI to access Web services
*
*  The service helper will create a request object and
*  attach it to the intent that fires the request service.
*
* */

    public void registerAsync(Activity activity, String userName, UUID regId) {
//        TODO: create a request object
//        TODO: attach object to intent service
        Register request = new Register(userName, regId);
        Intent i = new Intent(activity, RequestService.class);
        i.putExtra(RequestService.EXTRA_REGISTER, request);
        activity.startService(i);
    }

    public void postMessageAsync(Activity activity, UUID regId, int cliendId, String message) {
//        TODO: create a request object
//        TODO: attach object to intent service
        PostMessage request = new PostMessage(regId, cliendId, message);
        Intent i = new Intent(activity, RequestService.class);
        i.putExtra(RequestService.EXTRA_POST_MESSAGE, request);
        activity.startService(i);

    }

}
