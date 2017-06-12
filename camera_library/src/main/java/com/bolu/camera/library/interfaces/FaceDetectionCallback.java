package com.bolu.camera.library.interfaces;

import android.hardware.Camera;

/**
 * Created by Administrator on 2017/4/10.
 */

public interface FaceDetectionCallback extends Camera.FaceDetectionListener {

    @Override
    void onFaceDetection(Camera.Face[] faces, Camera camera);

    void onFaceDetectionNotSupport(Camera camera);

    void onFaceDetectionStart(Camera camera);

    void onFaceDetectionStop(Camera camera);
}
