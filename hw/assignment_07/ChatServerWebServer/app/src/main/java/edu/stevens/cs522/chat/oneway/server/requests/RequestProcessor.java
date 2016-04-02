package edu.stevens.cs522.chat.oneway.server.requests;

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

    public PostMessageResponse perform(PostMessage request) {
//    TODO: call RestMethod
        return (PostMessageResponse) new RestMethod().perform(request);
    }
}
