package com.codingnick.cameraapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bolu.camera.library.activity.CameraControl;

/**
 * Created by Administrator on 2017/4/6.
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ImageView imageView;
    private static String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private MyHandler myHandler = new MyHandler();
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        imageView = (ImageView) findViewById(R.id.image_view);
    }

    public void onButtonClick(View view){
        if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permissions[1]) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permissions[2]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissions, 1);
        }else{
            openCamera();
        }
    }

    public void openCamera(){
        Intent intent = new Intent(this, CameraControl.class);
        intent.putExtra(CameraControl.RATIO, 1);//1
        intent.putExtra(CameraControl.HDR_MODE, 1);//1
        intent.putExtra(CameraControl.FLASH_MODE, 1);//1
        intent.putExtra(CameraControl.FOCUS_MODE, 0);//0
        intent.putExtra(CameraControl.QUALITY, 0);//0
        intent.putExtra(CameraControl.FRONT_CAMERA, true);//false
        intent.putExtra(CameraControl.FACE_DETECTION, false);//false
        startActivityForResult(intent, CameraControl.REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = true;
        for (int r : grantResults){
            if (r != PackageManager.PERMISSION_GRANTED){
                granted = false;
                break;
            }
        }
        if (granted){
            openCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraControl.REQUEST_CODE && resultCode == RESULT_OK){
            final String filename = data.getStringExtra("name");
            final String filepath = data.getStringExtra("path");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    bitmap = BitmapFactory.decodeFile(filepath);
                    myHandler.sendEmptyMessage(1);
                }
            }).start();
        }
    }

    private class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    imageView.setImageBitmap(bitmap);
                    break;
            }
        }
    }
}
