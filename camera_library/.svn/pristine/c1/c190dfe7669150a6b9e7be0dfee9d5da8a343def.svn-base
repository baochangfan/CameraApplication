package com.bolu.camera.library.surface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.bolu.camera.library.interfaces.FaceDetectionCallback;

/**
 * Created by Nick on 2017/4/7.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{
    private static final String TAG = "CameraPreview";

    private static final int DISPLAY_ORIENTATION = 90;
    private static final float STROKE_WIDTH = 5f;
    private static final float FOCUS_AREA_FULL_SIZE = 2000f;
    private ImageView canvasFrame;
    private Canvas canvas;
    private Paint paint;
    private Camera mCamera;
    private FaceDetectionCallback faceDetectionCallback;

    public CameraPreview(Context context, Camera camera, ImageView canvasFrame, FaceDetectionCallback faceDetectionCallback){
        super(context);
        this.mCamera = camera;
        this.canvasFrame = canvasFrame;
        this.faceDetectionCallback = faceDetectionCallback;
        if (this.faceDetectionCallback != null){
            mCamera.setFaceDetectionListener(this.faceDetectionCallback);
        }
        initSurfaceHolder();
    }

    private void initSurfaceHolder(){
        SurfaceHolder holder = getHolder();
        if (holder != null) {
            holder.addCallback(this);
            holder.setKeepScreenOn(true);
        }
    }

    private void startPreview(SurfaceHolder holder){
        Log.d(TAG, "startPreview: start preview.");
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(DISPLAY_ORIENTATION);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            startFaceDetection();
        } catch (Exception e) {
            Log.e(TAG, "startPreview: Error starting camera preview.", e);
        }
    }

    private void stopPreview() {
        Log.d(TAG, "stopPreview: stop preview");
        try {
            stopFaceDetection();
            mCamera.stopPreview();
        } catch (Exception e) {
            Log.e(TAG, "stopPreview: Error stopping camera preview.", e);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: surface created, will start preview.");
        startPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initFocusDrawingTools(width, height);
        if (holder.getSurface() == null) {
            return;
        }
        stopPreview();
        startPreview(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed: surface destroyed, will stop preview.");
        stopPreview();
    }

    public void startFaceDetection(){
        if (faceDetectionCallback == null){
            return;
        }
        Camera.Parameters params = mCamera.getParameters();
        if (params.getMaxNumDetectedFaces() > 0){
            mCamera.startFaceDetection();
            faceDetectionCallback.onFaceDetectionStart(mCamera);
            Log.d(TAG, "startFaceDetection: face detection started.");
        }else{
            faceDetectionCallback.onFaceDetectionNotSupport(mCamera);
            Log.w(TAG, "startFaceDetection: face detection not support.");
        }
    }

    public void stopFaceDetection(){
        if (faceDetectionCallback == null){
            return;
        }
        Camera.Parameters params = mCamera.getParameters();
        if (params.getMaxNumDetectedFaces() > 0){
            mCamera.stopFaceDetection();
            faceDetectionCallback.onFaceDetectionStop(mCamera);
            Log.d(TAG, "stopFaceDetection: face detection stop.");
        }else{
            Log.w(TAG, "stopFaceDetection: face detection not support.");
        }
    }

    private void initFocusDrawingTools(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(STROKE_WIDTH);
        canvasFrame.setImageBitmap(bitmap);
    }

    public void drawFaceBounds(Rect faceRect,boolean isFrontCamera){
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (faceRect != null){
            RectF rect = new RectF(faceRect);
            Matrix matrix = new Matrix();
            matrix.setScale(isFrontCamera ?-1:1, 1);
            matrix.postRotate(DISPLAY_ORIENTATION);
            matrix.postScale(getWidth() / FOCUS_AREA_FULL_SIZE, getHeight() / FOCUS_AREA_FULL_SIZE);
            matrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
            matrix.mapRect(rect);
            canvas.drawLine(rect.left, rect.top, rect.right, rect.top, paint);
            canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, paint);
            canvas.drawLine(rect.right, rect.bottom, rect.left, rect.bottom, paint);
            canvas.drawLine(rect.left, rect.bottom, rect.left, rect.top, paint);
        }
        canvasFrame.draw(canvas);
        canvasFrame.invalidate();
    }

    public void onPictureTaken() {
    }
}
