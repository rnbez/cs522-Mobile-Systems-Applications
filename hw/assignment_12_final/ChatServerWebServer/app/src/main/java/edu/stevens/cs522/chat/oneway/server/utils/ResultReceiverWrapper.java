package edu.stevens.cs522.chat.oneway.server.utils;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Rafael on 3/6/2016.
 */
@SuppressLint("ParcelCreator")
public class ResultReceiverWrapper extends ResultReceiver {

    private IReceiver receiver;

    public ResultReceiverWrapper(Handler handler) {
        super(handler);
    }

    public void setReceiver(IReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
//            super.onReceiveResult(resultCode, resultData);
        if (receiver != null){
            receiver.onReceiveResult(resultCode, resultData);
        }
    }

    public interface IReceiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }
}