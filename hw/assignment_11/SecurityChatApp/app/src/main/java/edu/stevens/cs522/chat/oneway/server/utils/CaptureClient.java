package edu.stevens.cs522.chat.oneway.server.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.Camera;
import android.util.Log;

/**
 * Created by Rafael on 5/4/2016.
 */
public class CaptureClient {

    private static final String TAG = CaptureClient.class.getCanonicalName();

    public static final int CAPTURE_OK = Activity.RESULT_OK;

    public static final int CAPTURE_CANCELED = Activity.RESULT_CANCELED;

    public static final int CAMERA_UNAVAILABLE = Activity.RESULT_FIRST_USER+1;

    public static final String RESULT_KEY = "result_code";

    public static final String MESSAGE_KEY = "message";

    public static final String CAMERA_KEY = "camera_id";

    public static int getCameraByDirection(int direction) {
        int numCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int camid = 0; camid < numCameras; camid++) {
            Camera.getCameraInfo(camid, info);
            if (info.facing == direction) {
                return camid;
            }
        }
        return -1;
    }

    public static int getFrontFacingCamera() {
        return getCameraByDirection(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    public static int getBackFacingCamera() {
        return getCameraByDirection(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    public static boolean launch(final Activity context, int requestCode, String message) {
		/*
		 * Favor back-facing over front-facing camera.
		 */
        int camid = getBackFacingCamera();
        if (camid < 0) {
            camid = getFrontFacingCamera();
        }
        if (camid < 0) {
            Log.e(TAG, "No front-facing or back-facing camera detected.");
            return false;
        }
        // TODO launch CameraActivity
        Intent intent = new Intent();
        ComponentName name = new ComponentName("edu.stevens.cs522.capture", "edu.stevens.cs522.capture.client.CaptureActivity");
        intent.setComponent(name);

        intent.putExtra(CaptureClient.CAMERA_KEY, camid);
        intent.putExtra(CaptureClient.MESSAGE_KEY, message);

        context.startActivityForResult(intent, requestCode);

        return true;

    }

}
