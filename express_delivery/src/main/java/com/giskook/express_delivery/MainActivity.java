package com.giskook.express_delivery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.cameraview.CameraView;

import java.io.ByteArrayOutputStream;


public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = "____MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private CameraView mCameraView;
    private Handler mBackgroundHandler;
    private FrameLayoutWithHole mFilter;
    private ImageView mImageView;
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

        private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
        {
            byte [] yuv = new byte[imageWidth*imageHeight*3/2];
            // Rotate the Y luma
            int i = 0;
            for(int x = 0;x < imageWidth;x++)
            {
                for(int y = imageHeight-1;y >= 0;y--)
                {
                    yuv[i] = data[y*imageWidth+x];
                    i++;
                }
            }
            // Rotate the U and V color components
            i = imageWidth*imageHeight*3/2-1;
            for(int x = imageWidth-1;x > 0;x=x-2)
            {
                for(int y = 0;y < imageHeight/2;y++)
                {
                    yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
                    i--;
                    yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
                    i--;
                }
            }
            return yuv;
        }

        @Override
        public void onFramePreview(CameraView cameraView, byte[] data, int width, int height, int orientation) {
            Log.wtf(TAG,String.valueOf(data.length));
            Log.wtf(TAG, "bmp_width " + String.valueOf(width));
            Log.wtf(TAG, "bmp_height " + String.valueOf(height));

//            byte[] yuv = rotateYUV420Degree90(data, width, height);

            byte[] dst = yuv_crop(data, height, width, 0, 0, 200, 200);
            YuvImage yuvimage=new YuvImage(dst, ImageFormat.NV21,200,200,null);
//            ByteArrayOutputStream outer = new ByteArrayOutputStream();
//            yuvimage.compressToJpeg(new Rect(0,0,200, 200), 100, outer);
//            byte[] imageBytes = outer.toByteArray();
//            Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//
//            Bitmap bit_hm = Bitmap.createBitmap(bmp, 0, 0, 200, 200);
//            mImageView.setImageBitmap(bit_hm);

//            mImageView.setImageBitmap(bit_hm);
//            YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21,width,height,null);
//
//            ByteArrayOutputStream outer = new ByteArrayOutputStream();
//            yuvimage.compressToJpeg(new Rect(0,0,width, height), 100, outer);
//            byte[] imageBytes = outer.toByteArray();
//            Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//
//            Bitmap bit_hm = Bitmap.createBitmap(bmp, mFilter.mRect.left, mFilter.mRect.top, mFilter.mRect.width(), mFilter.mRect.height());
//
//            mImageView.setImageBitmap(bit_hm);

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
        Log.wtf(TAG, yuv_string());

        mFilter = findViewById(R.id.filter);
        mImageView = findViewById(R.id.cut_image);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            mCameraView.start();
            mCameraView.setScanning(true);

//            getBackgroundHandler().post(mRunnableTakePicture);
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
    public native String yuv_string();
    public native byte[] yuv_crop(byte[] src, int height, int width, int left, int top, int dheight, int dwidth);
    static {
        System.loadLibrary("yuv-lib");
    }
}
