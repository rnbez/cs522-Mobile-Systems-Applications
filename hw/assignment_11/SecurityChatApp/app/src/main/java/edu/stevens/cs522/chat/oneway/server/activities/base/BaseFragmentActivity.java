package edu.stevens.cs522.chat.oneway.server.activities.base;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import edu.stevens.cs522.chat.oneway.server.utils.App;
import edu.stevens.cs522.chat.oneway.server.utils.CaptureClient;

/**
 * Created by Rafael on 5/1/2016.
 */
public class BaseFragmentActivity extends FragmentActivity {

    final static public String TAG = BaseFragmentActivity.class.getCanonicalName();
    private static final int CAMERA_APP_REQUEST = 100;
    private static final int CAMERA_PERMISION_REQUEST = CAMERA_APP_REQUEST + 1;

    private char[] databaseKey;

    public final char[] getDatabaseKey() {
        return databaseKey;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseKey = getIntent().getCharArrayExtra(App.EXTRA_SECURITY_DATABASE_KEY);
//        databaseKey = "1234".toCharArray();
        if (databaseKey == null) {
//            throw new IllegalArgumentException("SECURITY_DATABASE_KEY...");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "camera: PERMISSION DENIED");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String[] permissions = new String[]{
                            Manifest.permission.CAMERA,
                    };
                    requestPermissions(permissions, CAMERA_PERMISION_REQUEST);
                }
                return;
            }
            startCaptureClient();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISION_REQUEST)
            startCaptureClient();
    }

    public void startCaptureClient() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "camera: PERMISSION DENIED");
            return;
        }
        CaptureClient.launch(this, CAMERA_APP_REQUEST, "Scan your QR Code");

    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_APP_REQUEST) {
            String toastMessage = "";
            switch (resultCode) {
                case CaptureClient.CAPTURE_OK:
                    //	Launch	chat	activity	with	the	key
                    String key = data.getStringExtra(CaptureClient.RESULT_KEY);
//                databaseKey = key.toCharArray();
                    Intent intent = getIntent();
                    intent.putExtra(App.EXTRA_SECURITY_DATABASE_KEY, key.toCharArray());
                    finish();
                    startActivity(intent);
                    break;
                case CaptureClient.CAPTURE_CANCELED:
                    //	Client	cancelled	app	startup
//                    toastMessage = "Client cancelled app startup.";
//                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
                    break;
                case CaptureClient.CAMERA_UNAVAILABLE:
                    //	No	camera	available,	cannot	proceed
//                    toastMessage = "No camera available, cannot proceed.";
//                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
                    break;
                default:
                    throw new IllegalArgumentException("...");
            }
        }
    }
}
