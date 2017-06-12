package com.bolu.camera.library.activity;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.bolu.camera.library.R;
import com.bolu.camera.library.fragment.CameraFragment;
import com.bolu.camera.library.interfaces.PhotoSavedListener;
import com.bolu.camera.library.interfaces.PhotoTakenCallback;
import com.bolu.camera.library.interfaces.RawPhotoTakenCallback;
import com.bolu.camera.library.util.SavingPhotoTask;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraControl extends AppCompatActivity implements PhotoTakenCallback, RawPhotoTakenCallback,PhotoSavedListener{
    private static final String TAG = "CameraControl";
    public static final int REQUEST_CODE = 1001;

    public static final String QUALITY = "quality";
    public static final String RATIO = "ratio";
    public static final String FOCUS_MODE = "focus_mode";
    public static final String FLASH_MODE = "flash_mode";
    public static final String HDR_MODE = "hdr_mode";
    public static final String FRONT_CAMERA = "front_camera";
    public static final String FACE_DETECTION = "face_detection";

    public static final String PATH = "path";
    private static final String IMG_PREFIX = "IMG_";
    private static final String IMG_POSTFIX = ".jpg";
    private static final String TIME_FORMAT = "yyyyMMdd_HHmmss";

    private PhotoSavedListener photoSavedListener;

    private String path;
    private int quality;
    private int ratio;
    private int focusMode;
    private int flashMode;
    private int hdrMode;
    private boolean useFrontCamera;
    private boolean useFaceDetection;
    private boolean saving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_control);
        if (TextUtils.isEmpty(path = getIntent().getStringExtra(PATH))) {
            path = Environment.getExternalStorageDirectory().getPath();
        }
        quality = getIntent().getIntExtra(QUALITY, 0);
        ratio = getIntent().getIntExtra(RATIO, 1);
        focusMode = getIntent().getIntExtra(FOCUS_MODE, 0);
        flashMode = getIntent().getIntExtra(FLASH_MODE, 1);
        hdrMode = getIntent().getIntExtra(HDR_MODE, 1);
        useFrontCamera = getIntent().getBooleanExtra(FRONT_CAMERA, false);
        useFaceDetection = getIntent().getBooleanExtra(FACE_DETECTION, false);
        CameraFragment fragment = CameraFragment.newInstance(this, createCameraParams());
        photoSavedListener = fragment;
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .commit();
    }

    private Bundle createCameraParams() {
        Bundle bundle = new Bundle();
        bundle.putInt(CameraFragment.RATIO, ratio);//1
        bundle.putInt(CameraFragment.HDR_MODE, hdrMode);//1
        bundle.putInt(CameraFragment.FLASH_MODE, flashMode);//1
        bundle.putInt(CameraFragment.FOCUS_MODE, focusMode);//0
        bundle.putInt(CameraFragment.QUALITY, quality);//0
        bundle.putBoolean(CameraFragment.FRONT_CAMERA, useFrontCamera);//false
        bundle.putBoolean(CameraFragment.FACE_DETECTION, useFaceDetection);//false
        return bundle;
    }

    private String createName() {
        String timeStamp = new SimpleDateFormat(TIME_FORMAT).format(new Date());
        return IMG_PREFIX + timeStamp + IMG_POSTFIX;
    }

    @Override
    public void photoTaken(byte[] data, int orientation) {
        savePhoto(data, createName(), path, orientation);
    }

    @Override
    public void photoSaved(String path, String name) {
        saving = false;
        Toast.makeText(this, "Photo " + name + " saved", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Photo " + name + " saved");
        if (photoSavedListener != null) {
            photoSavedListener.photoSaved(path, name);
        }
        Intent intent = new Intent();
        intent.putExtra(PATH, path);
        intent.putExtra("name", name);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void savePhoto(byte[] data, String name, String path, int orientation) {
        saving = true;
        new SavingPhotoTask(data, name, path, orientation, this).execute();
    }

    @Override
    public void rawPhotoTaken(byte[] data) {
        Log.d(TAG, String.format("rawPhotoTaken: data[%1d]", data.length));
    }

    @Override
    public void onBackPressed() {
        if (!saving) {
            super.onBackPressed();
        }
    }
}
