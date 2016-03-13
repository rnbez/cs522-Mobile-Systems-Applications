package edu.stevens.cs522.chat.oneway.server.requests;

/**
 * Created by Rafael on 3/12/2016.
 */
public class RequestProcessor {
    /* 10 Points
    *  RequestProcessor provides business logic for Web service request processing:
    *  registration and posting messages.
    * */

    public void perform(Register request) {
//    TODO: call RestMethod
        new RestMethod().perform(request);
    }

    public void perform(PostMessage request) {
//    TODO: call RestMethod
        new RestMethod().perform(request);
    }
}
