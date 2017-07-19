//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bolu.camera.library.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import com.bolu.camera.library.R.id;
import com.bolu.camera.library.R.layout;
import com.bolu.camera.library.R.string;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CameraFragment extends Fragment implements PhotoSavedListener {
    private static final String TAG = "CameraFragment";
    public static final String QUALITY = "quality";
    public static final String RATIO = "ratio";
    public static final String FOCUS_MODE = "focus_mode";
    public static final String FLASH_MODE = "flash_mode";
    public static final String HDR_MODE = "hdr_mode";
    public static final String FRONT_CAMERA = "front_camera";
    public static final String FACE_DETECTION = "face_detection";
    public static final String AUTO_TAKE_PHOTO = "auto_take_photo";
    public static final String CAMERA_DISPLAY_LANDSCAPE = "camera_display_landscape";
    private Quality quality;
    private Ratio ratio;
    private HDRMode hdrMode;
    private FlashMode flashMode;
    private FocusMode focusMode;
    private boolean useFrontCamera;
    private boolean useFaceDetectionTech;
    private boolean camera_display_landscape;
    private int cameraId;
    private Camera camera;
    private OrientationEventListener orientationListener;
    private CameraFragment.OnFaceDetectionCallback faceDetectionCallback;
    private Activity activity;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mNavigationBarHeight;
    private int mStatusBarHeight;
    private Map<Ratio, Size> previewSizes;
    private Map<Ratio, Map<Quality, Size>> pictureSizes;
    private Parameters parameters;
    private boolean supportedHDR = false;
    private boolean supportedFlash = false;
    private boolean supportedAutoFocus = false;
    private boolean supportedFaceDetection = false;
    private int outputOrientation;
    private ViewGroup previewContainer;
    private CameraPreview cameraPreview;
    private ProgressBar progressBar;
    private ImageButton mCaptureButton;
    private PhotoTakenCallback callback;
    private RawPhotoTakenCallback rawCallback;
    private boolean faceDetected = false;
    private boolean auto_take_photo = false;
    private boolean isAuto_take_photo;
    private PictureCallback pictureCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if(CameraFragment.this.callback != null) {
                CameraFragment.this.callback.photoTaken((byte[])data.clone(), CameraFragment.this.outputOrientation);
            }

            camera.startPreview();
            CameraFragment.this.cameraPreview.onPictureTaken();
        }
    };
    private PictureCallback rawPictureCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if(CameraFragment.this.rawCallback != null && data != null) {
                CameraFragment.this.rawCallback.rawPhotoTaken((byte[])data.clone());
            }

        }
    };

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

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.useFrontCamera = this.getArguments().getBoolean("front_camera", false);
        this.ratio = Ratio.getRatioById(this.getArguments().getInt("ratio", Ratio.R_16x9.getId()));
        this.quality = Quality.getQualityById(this.getArguments().getInt("quality", Quality.HIGH.getId()));
        this.flashMode = FlashMode.getFlashModeById(this.getArguments().getInt("flash_mode", FlashMode.AUTO.getId()));
        this.focusMode = FocusMode.getFocusModeById(this.getArguments().getInt("focus_mode", FocusMode.AUTO.getId()));
        this.hdrMode = HDRMode.getHDRModeById(this.getArguments().getInt("hdr_mode", HDRMode.NONE.getId()));
        this.useFaceDetectionTech = this.getArguments().getBoolean("face_detection", false);
        this.camera = this.getCameraInstance(this.useFrontCamera);
        this.auto_take_photo = this.getArguments().getBoolean("auto_take_photo", false);
        this.isAuto_take_photo = false;
        this.camera_display_landscape = this.getArguments().getBoolean("camera_display_landscape", false);
        if(this.camera != null) {
            this.initScreenParams();
            this.parameters = this.camera.getParameters();
            this.previewSizes = this.buildPreviewSizesRatioMap(this.parameters.getSupportedPreviewSizes());
            this.pictureSizes = this.buildPictureSizesRatioMap(this.parameters.getSupportedPictureSizes());
            this.initCameraParams();
        }
    }

    private void initCameraParams() {
        List flashModes = this.parameters.getSupportedFlashModes();
        if(flashModes != null && flashModes.size() > 1) {
            Iterator sceneModes = flashModes.iterator();

            while(sceneModes.hasNext()) {
                String focusModes = (String)sceneModes.next();
                if(focusModes.equals(this.flashMode)) {
                    this.supportedFlash = true;
                    break;
                }
            }
        }

        List sceneModes1 = this.parameters.getSupportedSceneModes();
        if(sceneModes1 != null) {
            Iterator focusModes1 = sceneModes1.iterator();

            while(focusModes1.hasNext()) {
                String mode = (String)focusModes1.next();
                if(mode.equals("hdr")) {
                    this.supportedHDR = true;
                    break;
                }
            }
        }

        if(this.supportedFlash && this.flashMode != null) {
            this.parameters.setFlashMode(this.flashMode.name());
        }

        if(this.supportedHDR && this.hdrMode != null) {
            this.parameters.setSceneMode("auto");
        }

        List focusModes2 = this.parameters.getSupportedFocusModes();
        if(focusModes2 != null) {
            Iterator mode2 = focusModes2.iterator();

            while(mode2.hasNext()) {
                String mode1 = (String)mode2.next();
                if(mode1.equals("auto")) {
                    this.supportedAutoFocus = true;
                }
            }
        }

        if(this.supportedAutoFocus) {
            this.parameters.setFocusMode("auto");
        }
        try{
            this.setPreviewSize(this.parameters, this.ratio);
            this.setPictureSize(this.parameters, this.quality, this.ratio);
        }catch (NullPointerException e) {
            Log.e(TAG, ","+e);
        }

        this.camera.setParameters(this.parameters);
        if(this.camera_display_landscape) {
            this.toSetCameraDisplay(100, -1);
        } else {
            this.toSetCameraDisplay(0, -1);
        }

    }

    private Camera getCameraInstance(boolean useFrontCamera) {
        Camera c = null;

        try {
            c = Camera.open(this.getCameraId(useFrontCamera));
        } catch (Exception var4) {
            Log.e("CameraFragment", "getCameraInstance: camera is unavailable.", var4);
        }

        return c;
    }

    private int getCameraId(boolean useFrontCamera) {
        int count = Camera.getNumberOfCameras();
        int result = -1;
        if(count > 0) {
            result = 0;
            CameraInfo info = new CameraInfo();

            for(int i = 0; i < count; ++i) {
                Camera.getCameraInfo(i, info);
                if(info.facing == 0 && !useFrontCamera) {
                    result = i;
                    break;
                }

                if(info.facing == 1 && useFrontCamera) {
                    result = i;
                    break;
                }
            }
        }

        this.cameraId = result;
        return result;
    }

    private void initScreenParams() {
        DisplayMetrics metrics = new DisplayMetrics();
        this.activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        this.mScreenWidth = metrics.widthPixels;
        this.mScreenHeight = metrics.heightPixels;
        this.mNavigationBarHeight = this.getNavigationBarHeight();
        this.mStatusBarHeight = this.getStatusBarHeight();
    }

    private int getNavigationBarHeight() {
        return this.getPixelSizeByName("navigation_bar_height");
    }

    private int getStatusBarHeight() {
        return this.getPixelSizeByName("status_bar_height");
    }

    private int getPixelSizeByName(String name) {
        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier(name, "dimen", "android");
        return resourceId > 0?resources.getDimensionPixelSize(resourceId):0;
    }

    private Map<Ratio, Size> buildPreviewSizesRatioMap(List<Size> sizes) {
        HashMap map = new HashMap();
        Iterator var3 = sizes.iterator();

        while(true) {
            Size size;
            Ratio ratio;
            Size oldSize;
            do {
                do {
                    if(!var3.hasNext()) {
                        return map;
                    }

                    size = (Size)var3.next();
                    ratio = Ratio.pickRatio(size.width, size.height);
                } while(ratio == null);

                oldSize = (Size)map.get(ratio);
            } while(oldSize != null && oldSize.width >= size.width && oldSize.height >= size.height);

            map.put(ratio, size);
        }
    }

    private Map<Ratio, Map<Quality, Size>> buildPictureSizesRatioMap(List<Size> sizes) {
        HashMap map = new HashMap();
        HashMap ratioListMap = new HashMap();
        Iterator var4 = sizes.iterator();

        while(var4.hasNext()) {
            Size r = (Size)var4.next();
            Ratio list = Ratio.pickRatio(r.width, r.height);
            if(list != null) {
                Object sizeMap = (List)ratioListMap.get(list);
                if(sizeMap == null) {
                    sizeMap = new ArrayList();
                    ratioListMap.put(list, sizeMap);
                }

                ((List)sizeMap).add(r);
            }
        }

        var4 = ratioListMap.keySet().iterator();

        while(var4.hasNext()) {
            Ratio var14 = (Ratio)var4.next();
            List var15 = (List)ratioListMap.get(var14);
            ratioListMap.put(var14, this.sortSizes(var15));
            HashMap var16 = new HashMap();
            int i = 0;
            Quality[] var9 = Quality.values();
            int var10 = var9.length;

            for(int var11 = 0; var11 < var10; ++var11) {
                Quality q = var9[var11];
                Size size = null;
                if(i < var15.size()) {
                    size = (Size)var15.get(i++);
                }

                var16.put(q, size);
            }

            map.put(var14, var16);
        }

        return map;
    }

    private List<Size> sortSizes(List<Size> sizes) {
        for(int count = sizes.size(); count > 2; --count) {
            for(int i = 0; i < count - 1; ++i) {
                Size current = (Size)sizes.get(i);
                Size next = (Size)sizes.get(i + 1);
                if(current.width < next.width || current.height < next.height) {
                    sizes.set(i, next);
                    sizes.set(i + 1, current);
                }
            }
        }

        return sizes;
    }

    private void setPictureSize(Parameters parameters, Quality quality, Ratio ratio) {
        Size size = (Size)((Map)this.pictureSizes.get(ratio)).get(quality);
        if(size != null) {
            parameters.setPictureSize(size.width, size.height);
        }

    }

    private void setPreviewSize(Parameters parameters, Ratio ratio) {
        Size size = (Size)this.previewSizes.get(ratio);
        parameters.setPreviewSize(size.width, size.height);
    }

    private void setPreviewContainerSize(int width, int height, Ratio ratio) {
        height = width / ratio.h * ratio.w;
        this.previewContainer.setLayoutParams(new LayoutParams(width, height));
    }

    private void initOrientationListener() {
        this.orientationListener = new OrientationEventListener(this.activity) {
            public void onOrientationChanged(int orientation) {
                CameraFragment.this.toSetCameraDisplay(orientation, -1);
            }
        };
    }

    private void toSetCameraDisplay(int orientation, int orientationNum) {
        if(this.camera != null && orientation != orientationNum) {
            int newOutputOrientation = this.getCameraPictureRotation(orientation);
            if(newOutputOrientation != this.outputOrientation) {
                this.outputOrientation = newOutputOrientation;
                Parameters params = this.camera.getParameters();
                params.setRotation(this.outputOrientation);

                try {
                    this.camera.setParameters(params);
                    setCameraDisplayOrientation(this.activity, this.cameraId, this.camera);
                    Log.w("CameraFragment", "orientation=" + orientationNum);
                } catch (Exception var6) {
                    Log.e("CameraFragment", "Exception updating camera parameters in orientation change", var6);
                }
            }
        }

    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        short degrees = 0;
        switch(rotation) {
            case 0:
                degrees = 0;
                break;
            case 1:
                degrees = 90;
                break;
            case 2:
                degrees = 180;
                break;
            case 3:
                degrees = 270;
        }

        int result;
        if(info.facing == 1) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }

        camera.setDisplayOrientation(result);
    }

    private int getCameraPictureRotation(int orientation) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(this.cameraId, info);
        orientation = (orientation + 45) / 90 * 90;
        int rotation;
        if(info.facing == 1) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {
            rotation = (info.orientation + orientation) % 360;
        }

        return rotation;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.camera == null) {
            return inflater.inflate(layout.fragment_no_camera, container, false);
        } else {
            View view = inflater.inflate(layout.fragment_camera, container, false);

            try {
                this.previewContainer = (ViewGroup)view.findViewById(id.camera_preview);
            } catch (NullPointerException var8) {
                throw new RuntimeException("You should add container that extends ViewGroup for CameraPreview.");
            }

            if(this.useFaceDetectionTech) {
                this.faceDetectionCallback = new CameraFragment.OnFaceDetectionCallback();
            }

            ImageView canvasFrame = new ImageView(this.activity);
            this.cameraPreview = new CameraPreview(this.activity, this.camera, canvasFrame, this.faceDetectionCallback);
            this.previewContainer.addView(this.cameraPreview);
            this.previewContainer.addView(canvasFrame);
            this.progressBar = (ProgressBar)view.findViewById(id.progress);
            this.mCaptureButton = (ImageButton)view.findViewById(id.capture);
            this.mCaptureButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    CameraFragment.this.onCaptureClick();
                }
            });
            if(this.auto_take_photo) {
                this.progressBar.setVisibility(View.INVISIBLE);
                this.mCaptureButton.setVisibility(View.GONE);
                this.mCaptureButton.performClick();
            }

            this.setPreviewContainerSize(this.mScreenWidth, this.mScreenHeight, this.ratio);
            View controls = view.findViewById(id.controls_layout);
            if(controls != null) {
                LayoutParams params = new LayoutParams(-1, -1);
                params.topMargin = this.mStatusBarHeight;
                params.bottomMargin = this.mNavigationBarHeight;
                controls.setLayoutParams(params);
            }

            return view;
        }
    }

    public void onCaptureClick() {
        if(!this.auto_take_photo) {
            if(this.useFaceDetectionTech) {
                if(this.supportedFaceDetection) {
                    if(this.faceDetected) {
                        this.takePhoto();
                    } else {
                        Toast.makeText(this.activity, string.face_not_detected, Toast.LENGTH_LONG).show();
                    }
                } else {
                    this.takePhoto();
                }
            } else {
                this.takePhoto();
            }
        } else if(!this.isAuto_take_photo) {
            this.isAuto_take_photo = true;
            Toast.makeText(this.activity, "3秒后自动拍照...", Toast.LENGTH_LONG).show();
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                public void run() {
                    Log.w("拍照", "定时3秒拍照");
                    CameraFragment.this.camera.takePicture((ShutterCallback)null, CameraFragment.this.rawPictureCallback, CameraFragment.this.pictureCallback);
                }
            };
            timer.schedule(task, 3000L);
        }

    }

    private void takePhoto() {
        this.mCaptureButton.setEnabled(false);
        this.mCaptureButton.setVisibility(View.INVISIBLE);
        if(this.progressBar != null) {
            this.progressBar.setVisibility(View.VISIBLE);
        }

        this.camera.takePicture((ShutterCallback)null, this.rawPictureCallback, this.pictureCallback);
    }

    public void photoSaved(String path, String name) {
        if(!this.auto_take_photo) {
            this.mCaptureButton.setEnabled(true);
            this.mCaptureButton.setVisibility(View.VISIBLE);
        }

        if(this.progressBar != null) {
            this.progressBar.setVisibility(View.GONE);
        }

    }

    public void onResume() {
        super.onResume();
        if(this.camera != null) {
            try {
                this.camera.reconnect();
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

        if(this.orientationListener == null) {
            this.initOrientationListener();
        }

        this.orientationListener.enable();
    }

    public void onPause() {
        super.onPause();
        if(this.orientationListener != null) {
            this.orientationListener.disable();
            this.orientationListener = null;
        }

    }

    public void onDestroy() {
        super.onDestroy();
        if(this.camera != null) {
            this.camera.release();
            this.camera = null;
        }

    }

    class OnFaceDetectionCallback implements FaceDetectionCallback {
        OnFaceDetectionCallback() {
        }

        public void onFaceDetection(Face[] faces, Camera camera) {
            if(faces.length > 0) {
                Log.d("CameraFragment", "face0 top: " + faces[0].rect.top + ",  bottom: " + faces[0].rect.bottom + ", left: " + faces[0].rect.left + ", right: " + faces[0].rect.right);
                CameraFragment.this.faceDetected = true;
                CameraFragment.this.cameraPreview.drawFaceBounds(faces[0].rect, CameraFragment.this.useFrontCamera);
            } else {
                CameraFragment.this.faceDetected = false;
                CameraFragment.this.cameraPreview.drawFaceBounds((Rect)null, CameraFragment.this.useFrontCamera);
            }

        }

        public void onFaceDetectionNotSupport(Camera camera) {
            CameraFragment.this.supportedFaceDetection = false;
            Toast.makeText(CameraFragment.this.activity, string.face_detection_not_support, Toast.LENGTH_LONG).show();
        }

        public void onFaceDetectionStart(Camera camera) {
            CameraFragment.this.supportedFaceDetection = true;
        }

        public void onFaceDetectionStop(Camera camera) {
        }
    }
}
