package edu.stevens.cs522.chat.oneway.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final int CHAT_REQUEST = 1;

    public static final String EXTRA_KEY_USER_NAME = TAG + "user_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void btnNextOnClick(View v){
        Log.i(TAG, "next button clicked");

        EditText userNameEdit = (EditText) findViewById(R.id.txtUserName);
        EditText portEdit = (EditText) findViewById(R.id.txtUserPort);
        String username = userNameEdit.getText().toString();
        int userport = Integer.parseInt(portEdit.getText().toString());

        Log.i(TAG, "user: " + username + " @ port: " + userport);

        Intent i = new Intent(this, ChatClient.class);
        i.putExtra(ChatClient.CLIENT_NAME_KEY, username);
        i.putExtra(ChatClient.CLIENT_PORT_KEY, userport);
        startActivityForResult(i, CHAT_REQUEST);
    }
}
