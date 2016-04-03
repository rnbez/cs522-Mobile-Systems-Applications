package edu.stevens.cs522.chat.oneway.server.requests;

import android.util.JsonReader;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Created by Rafael on 3/12/2016.
 */
public class RequestProcessor {
    /* 10 Points
    *  RequestProcessor provides business logic for Web service request processing:
    *  registration and posting messages.
    * */

    public RegisterResponse perform(Register request) {
//    TODO: call RestMethod
        return (RegisterResponse) new RestMethod().perform(request);
    }

    public void perform(Synchronize sync) {
//    TODO: call RestMethod
        RestMethod.StreamingResponse sr = null;
        try {
            RestMethod rest = new RestMethod();
            sr = rest.perform(sync);
            // If we assume JSON data (could be e.g. multipart)
            InputStream is = sr.getInputStream();
            JsonReader jr = new JsonReader(
                    new BufferedReader(new InputStreamReader(is, "UTF-8")));
            // Consume the download data
//            sr.disconnect();
            SyncResponse response = (SyncResponse) sync.getResponse(jr);
//            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (sr != null) sr.disconnect();
        }
//        return null;
    }

    public PostMessageResponse perform(PostMessage request) {
//    TODO: call RestMethod
        return (PostMessageResponse) new RestMethod().perform(request);
    }
}
