package com.bolu.camera.library.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bolu.camera.library.R;
import com.bolu.camera.library.interfaces.FaceDetectionCallback;
import com.bolu.camera.library.interfaces.PhotoSavedListener;
import com.bolu.camera.library.interfaces.PhotoTakenCallback;
import com.bolu.camera.library.interfaces.RawPhotoTakenCallback;
import com.bolu.camera.library.model.FlashMode;
import com.bolu.camera.library.model.FocusMode;
import com.bolu.camera.library.model.HDRMode;
import com.bolu.camera.library.model.Quality;
import com.bolu.camera.library.model.Ratio;
import com.bolu.camera.library.surface.CameraPreview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends Fragment implements PhotoSavedListener {
    private static final String TAG = "CameraFragment";

    public static final String QUALITY = "quality";
    public static final String RATIO = "ratio";
    public static final String FOCUS_MODE = "focus_mode";
    public static final String FLASH_MODE = "flash_mode";
    public static final String HDR_MODE = "hdr_mode";
    public static final String FRONT_CAMERA = "front_camera";
    public static final String FACE_DETECTION = "face_detection";

    private Quality quality;
    private Ratio ratio;
    private HDRMode hdrMode;
    private FlashMode flashMode;
    private FocusMode focusMode;
    private boolean useFrontCamera;
    private boolean useFaceDetectionTech;

    private int cameraId;
    private Camera camera;

    private OrientationEventListener orientationListener;
    private OnFaceDetectionCallback faceDetectionCallback;
    private Activity activity;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mNavigationBarHeight;
    private int mStatusBarHeight;

    private Map<Ratio, Camera.Size> previewSizes;
    private Map<Ratio, Map<Quality, Camera.Size>> pictureSizes;
    private Camera.Parameters parameters;

    private boolean supportedHDR = false;
    private boolean supportedFlash = false;
    private boolean supportedFaceDetection = false;
    private int outputOrientation;
    private ViewGroup previewContainer;
    private CameraPreview cameraPreview;
    private ProgressBar progressBar;
    private ImageButton mCaptureButton;
    private PhotoTakenCallback callback;
    private RawPhotoTakenCallback rawCallback;
    private boolean faceDetected = false;
    public void setCallback(PhotoTakenCallback callback) {
        this.callback = callback;
    }

    public void setRawCallback(RawPhotoTakenCallback rawCallback) {
        this.rawCallback = rawCallback;
    }


    public CameraFragment() {
    }

    public static CameraFragment newInstance(PhotoTakenCallback callback, Bundle params) {
        CameraFragment fragment = new CameraFragment();
        fragment.callback = callback;
        fragment.setArguments(params);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        useFrontCamera = getArguments().getBoolean(FRONT_CAMERA, false);
        ratio = Ratio.getRatioById(getArguments().getInt(RATIO, Ratio.R_16x9.getId()));
        quality = Quality.getQualityById(getArguments().getInt(QUALITY, Quality.HIGH.getId()));
        flashMode = FlashMode.getFlashModeById(getArguments().getInt(FLASH_MODE, FlashMode.AUTO.getId()));
        focusMode = FocusMode.getFocusModeById(getArguments().getInt(FOCUS_MODE, FocusMode.AUTO.getId()));
        hdrMode = HDRMode.getHDRModeById(getArguments().getInt(HDR_MODE, HDRMode.NONE.getId()));
        useFaceDetectionTech = getArguments().getBoolean(FACE_DETECTION, false);
        camera = getCameraInstance(useFrontCamera);
        if (camera == null) {
            return;
        }
        initScreenParams();
        parameters = camera.getParameters();
        previewSizes = buildPreviewSizesRatioMap(parameters.getSupportedPreviewSizes());
        pictureSizes = buildPictureSizesRatioMap(parameters.getSupportedPictureSizes());
        initCameraParams();
    }

    private void initCameraParams() {
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.size() > 1) { /* Device has flash */
            for (String mode : flashModes){
                if (mode.equals(flashMode)){
                    supportedFlash = true;
                    break;
                }
            }
        }
        List<String> sceneModes = parameters.getSupportedSceneModes();
        if (sceneModes != null) {
            for (String mode : sceneModes) {
                if (mode.equals(Camera.Parameters.SCENE_MODE_HDR)) {
                    supportedHDR = true;
                    break;
                }
            }
        }
        if (supportedFlash && flashMode != null){
            parameters.setFlashMode(flashMode.name());
        }
        if (supportedHDR && hdrMode != null){
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        }
        setPreviewSize(parameters, ratio);
        setPictureSize(parameters, quality, ratio);
        camera.setParameters(parameters);
    }
    /**
     * A safe way to get an instance of the Camera object.
     */
    private Camera getCameraInstance(boolean useFrontCamera) {
        Camera c = null;
        try {
            c = Camera.open(getCameraId(useFrontCamera));
        } catch (Exception e) {
            Log.e(TAG, "getCameraInstance: camera is unavailable.", e);
        }
        return c;
    }

    private int getCameraId(boolean useFrontCamera) {
        int count = Camera.getNumberOfCameras();
        int result = -1;
        if (count > 0) {
            result = 0;
            Camera.CameraInfo info = new Camera.CameraInfo();
            for (int i = 0; i < count; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK
                        && !useFrontCamera) {
                    result = i;
                    break;
                } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT
                        && useFrontCamera) {
                    result = i;
                    break;
                }
            }
        }
        cameraId = result;
        return result;
    }

    private void initScreenParams() {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mNavigationBarHeight = getNavigationBarHeight();
        mStatusBarHeight = getStatusBarHeight();
    }

    private int getNavigationBarHeight() {
        return getPixelSizeByName("navigation_bar_height");
    }

    private int getStatusBarHeight() {
        return getPixelSizeByName("status_bar_height");
    }

    private int getPixelSizeByName(String name) {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier(name, "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private Map<Ratio, Camera.Size> buildPreviewSizesRatioMap(List<Camera.Size> sizes) {
        Map<Ratio, Camera.Size> map = new HashMap<>();
        for (Camera.Size size : sizes) {
            Ratio ratio = Ratio.pickRatio(size.width, size.height);
            if (ratio != null) {
                Camera.Size oldSize = map.get(ratio);
                if (oldSize == null || (oldSize.width < size.width || oldSize.height < size.height)) {
                    map.put(ratio, size);
                }
            }
        }
        return map;
    }

    private Map<Ratio, Map<Quality, Camera.Size>> buildPictureSizesRatioMap(List<Camera.Size> sizes) {
        Map<Ratio, Map<Quality, Camera.Size>> map = new HashMap<>();
        Map<Ratio, List<Camera.Size>> ratioListMap = new HashMap<>();
        for (Camera.Size size : sizes) {
            Ratio ratio = Ratio.pickRatio(size.width, size.height);
            if (ratio != null) {
                List<Camera.Size> sizeList = ratioListMap.get(ratio);
                if (sizeList == null) {
                    sizeList = new ArrayList<>();
                    ratioListMap.put(ratio, sizeList);
                }
                sizeList.add(size);
            }
        }
        for (Ratio r : ratioListMap.keySet()) {
            List<Camera.Size> list = ratioListMap.get(r);
            ratioListMap.put(r, sortSizes(list));
            Map<Quality, Camera.Size> sizeMap = new HashMap<>();
            int i = 0;
            for (Quality q : Quality.values()) {
                Camera.Size size = null;
                if (i < list.size()) {
                    size = list.get(i++);
                }
                sizeMap.put(q, size);
            }
            map.put(r, sizeMap);
        }
        return map;
    }

    private List<Camera.Size> sortSizes(List<Camera.Size> sizes) {
        int count = sizes.size();
        while (count > 2) {
            for (int i = 0; i < count - 1; i++) {
                Camera.Size current = sizes.get(i);
                Camera.Size next = sizes.get(i + 1);
                if (current.width < next.width || current.height < next.height) {
                    sizes.set(i, next);
                    sizes.set(i + 1, current);
                }
            }
            count--;
        }
        return sizes;
    }




    private void setPictureSize(Camera.Parameters parameters, Quality quality, Ratio ratio) {
        Camera.Size size = pictureSizes.get(ratio).get(quality);
        if (size != null) {
            parameters.setPictureSize(size.width, size.height);
        }
    }

    private void setPreviewSize(Camera.Parameters parameters, Ratio ratio) {
        Camera.Size size = previewSizes.get(ratio);
        parameters.setPreviewSize(size.width, size.height);
    }

    /**
     * @param width  Screen width
     * @param height Screen height
     * @param ratio  Required ratio
     */
    private void setPreviewContainerSize(int width, int height, Ratio ratio) {
        height = (width / ratio.h) * ratio.w;
        previewContainer.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
    }

    class OnFaceDetectionCallback implements FaceDetectionCallback {

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0){
                Log.d(TAG, "face0 top: " + faces[0].rect.top + ",  bottom: " + faces[0].rect.bottom
                        +", left: " + faces[0].rect.left + ", right: " + faces[0].rect.right);
                faceDetected = true;
                cameraPreview.drawFaceBounds(faces[0].rect, useFrontCamera);
            }else{
                faceDetected = false;
                cameraPreview.drawFaceBounds(null, useFrontCamera);
            }
        }

        @Override
        public void onFaceDetectionNotSupport(Camera camera) {
            supportedFaceDetection = false;
            Toast.makeText(activity, R.string.face_detection_not_support, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFaceDetectionStart(Camera camera) {
            supportedFaceDetection = true;
        }

        @Override
        public void onFaceDetectionStop(Camera camera) {
        }
    }

    private void initOrientationListener() {
        orientationListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (camera != null && orientation != ORIENTATION_UNKNOWN) {
                    int newOutputOrientation = getCameraPictureRotation(orientation);
                    if (newOutputOrientation != outputOrientation) {
                        outputOrientation = newOutputOrientation;
                        Camera.Parameters params = camera.getParameters();
                        params.setRotation(outputOrientation);
                        try {
                            camera.setParameters(params);
                        } catch (Exception e) {
                            Log.e(TAG, "Exception updating camera parameters in orientation change", e);
                        }
                    }
                }
            }
        };
    }

    private int getCameraPictureRotation(int orientation) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation;
        orientation = (orientation + 45) / 90 * 90;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else { // back-facing camera
            rotation = (info.orientation + orientation) % 360;
        }
        return (rotation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (camera == null) {
            return inflater.inflate(R.layout.fragment_no_camera, container, false);
        }
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        try {
            previewContainer = (ViewGroup) view.findViewById(R.id.camera_preview);
        } catch (NullPointerException e) {
            throw new RuntimeException("You should add container that extends ViewGroup for CameraPreview.");
        }
        if (useFaceDetectionTech){
            faceDetectionCallback = new OnFaceDetectionCallback();
        }
        ImageView canvasFrame = new ImageView(activity);
        cameraPreview = new CameraPreview(activity, camera, canvasFrame, faceDetectionCallback);
        previewContainer.addView(cameraPreview);
        previewContainer.addView(canvasFrame);
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        mCaptureButton = (ImageButton) view.findViewById(R.id.capture);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCaptureClick();
            }
        });
        setPreviewContainerSize(mScreenWidth, mScreenHeight, ratio);
        View controls = view.findViewById(R.id.controls_layout);
        if (controls != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            params.topMargin = mStatusBarHeight;
            params.bottomMargin = mNavigationBarHeight;
            controls.setLayoutParams(params);
        }
        return view;
    }

    private void onCaptureClick(){
        if (useFaceDetectionTech){
            if (supportedFaceDetection){
                if (faceDetected){
                    takePhoto();
                }else{
                    Toast.makeText(activity, R.string.face_not_detected, Toast.LENGTH_SHORT).show();
                }
            }else{
                takePhoto();
            }
        }else{
            takePhoto();
        }
    }

    private void takePhoto(){
        mCaptureButton.setEnabled(false);
        mCaptureButton.setVisibility(View.INVISIBLE);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        camera.takePicture(null, rawPictureCallback, pictureCallback);
    }

    @Override
    public void photoSaved(String path, String name) {
        mCaptureButton.setEnabled(true);
        mCaptureButton.setVisibility(View.VISIBLE);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (callback != null) {
                callback.photoTaken(data.clone(), outputOrientation);
            }
            camera.startPreview();
            cameraPreview.onPictureTaken();
        }
    };

    private Camera.PictureCallback rawPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (rawCallback != null && data != null) {
                rawCallback.rawPhotoTaken(data.clone());
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        if (camera != null) {
            try {
                camera.reconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (orientationListener == null) {
            initOrientationListener();
        }
        orientationListener.enable();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (orientationListener != null) {
            orientationListener.disable();
            orientationListener = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
