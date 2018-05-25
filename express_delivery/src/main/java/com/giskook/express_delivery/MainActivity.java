package com.giskook.express_delivery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.cameraview.CameraView;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = "____MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private CameraView mCameraView;
    private Handler mBackgroundHandler;
    private Runnable mRunnableTakePicture = new Runnable() {
        @Override
        public void run() {
//            mCameraView.takePicture();
            getBackgroundHandler().postDelayed(mRunnableTakePicture,2000);
        }
    };

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private CameraView.Callback mCallback
            = new CameraView.Callback() {
        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.wtf(TAG, String.valueOf(cameraView.getTop()));
            Log.wtf(TAG, String.valueOf(cameraView.getBottom()));
            Log.wtf(TAG, String.valueOf(cameraView.getLeft()));
            Log.wtf(TAG, String.valueOf(cameraView.getRight()));
            Log.wtf(TAG,String.valueOf(data.length));
            Bitmap bmp= BitmapFactory.decodeByteArray(data,0,data.length);
            Log.wtf(TAG, "bmp_height " + String.valueOf(bmp.getHeight()));
            Log.wtf(TAG, "bmp_width " + String.valueOf(bmp.getWidth()));
        }

        @Override
        public void onFramePreview(CameraView cameraView, byte[] data, int width, int height, int orientation) {
            Log.wtf(TAG,String.valueOf(data.length));
//            Log.wtf(TAG, "bmp_width " + String.valueOf(width));
            Log.wtf(TAG, "bmp_height " + String.valueOf(height));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = findViewById(R.id.camera);
        if(mCameraView != null) {
            mCameraView.setFlash(CameraView.FLASH_OFF);
            mCameraView.addCallback(mCallback);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            mCameraView.start();
            mCameraView.setScanning(true);

            getBackgroundHandler().post(mRunnableTakePicture);
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            Log.d(TAG, "not allow use camera");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        getBackgroundHandler().removeCallbacks(mRunnableTakePicture);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (permissions.length != 1 || grantResults.length != 1) {
                    throw new RuntimeException("Error on requesting camera permission.");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.camera_permission_not_granted,
                            Toast.LENGTH_SHORT).show();
                }
                // No need to start camera here; it is handled by onResume
                break;
        }
    }
}
