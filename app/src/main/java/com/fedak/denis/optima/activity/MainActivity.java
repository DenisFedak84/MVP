package com.fedak.denis.optima.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fedak.denis.optima.R;

import com.fedak.denis.optima.WikitudeSDKConstants;
import com.fedak.denis.optima.fragment.AlanFragment;
import com.fedak.denis.optima.fragment.SocialFragment;
import com.fedak.denis.optima.rendering.external.CustomSurfaceView;
import com.fedak.denis.optima.rendering.external.Driver;
import com.fedak.denis.optima.rendering.external.GLRenderer;
import com.fedak.denis.optima.rendering.external.StrokedRectangle;
import com.fedak.denis.optima.util.DropDownAlert;
import com.wikitude.NativeStartupConfiguration;
import com.wikitude.WikitudeSDK;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.rendering.ExternalRendering;
import com.wikitude.tracker.ImageTarget;
import com.wikitude.tracker.ImageTracker;
import com.wikitude.tracker.ImageTrackerListener;
import com.wikitude.tracker.TargetCollectionResource;
import com.wikitude.tracker.TargetCollectionResourceLoadingCallback;

import java.util.Locale;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends BaseActivity implements ImageTrackerListener, ExternalRendering {

    private static final String TAG = "SimpleClientTracking";
    private static final String ALAN = "Alan";
    private static final String TABLE = "table";
    private static final String SOCIAL = "social";
    private static final String EMAIL = "email";


    private WikitudeSDK mWikitudeSDK;
    private CustomSurfaceView mView;
    private Driver mDriver;
    private GLRenderer mGLRenderer;

    private TargetCollectionResource mTargetCollectionResource;
    private DropDownAlert mDropDownAlert;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAugmentReality();
    }

    private void createAugmentReality() {
        mWikitudeSDK = new WikitudeSDK(this);
        NativeStartupConfiguration startupConfiguration = new NativeStartupConfiguration();
        startupConfiguration.setLicenseKey(WikitudeSDKConstants.WIKITUDE_SDK_KEY);
        startupConfiguration.setCameraPosition(CameraSettings.CameraPosition.BACK);
        startupConfiguration.setCameraResolution(CameraSettings.CameraResolution.AUTO);

        mWikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);

        mTargetCollectionResource = mWikitudeSDK.getTrackerManager().createTargetCollectionResource("file:///android_asset/tracker.wtc", new TargetCollectionResourceLoadingCallback() {
            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.v(TAG, "Failed to load target collection resource. Reason: " + errorMessage);
            }

            @Override
            public void onFinish() {
                mWikitudeSDK.getTrackerManager().createImageTracker(mTargetCollectionResource, MainActivity.this, null);
            }
        });

        mDropDownAlert = new DropDownAlert(this);
        mDropDownAlert.setText("Scan targets:");
        mDropDownAlert.addImages("table.jpg", "social.jpg", "Alan.jpg");
        mDropDownAlert.setTextWeight(0.5f);
        mDropDownAlert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWikitudeSDK.onResume();
        mView.onResume();
        mDriver.start();

        MainActivityPermissionsDispatcher.onStartAugmentRealityWithPermissionCheck(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWikitudeSDK.onPause();
        mView.onPause();
        mDriver.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWikitudeSDK.clearCache();
        mWikitudeSDK.onDestroy();
    }

    @Override
    public void onRenderExtensionCreated(RenderExtension renderExtension) {
        mGLRenderer = new GLRenderer(renderExtension);
        mView = new CustomSurfaceView(getApplicationContext(), mGLRenderer);
        mDriver = new Driver(mView, 30);

        FrameLayout viewHolder = new FrameLayout(getApplicationContext());
        setContentView(viewHolder);

        viewHolder.addView(mView);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        RelativeLayout controls = (RelativeLayout) inflater.inflate(R.layout.info_dialog, null);
        viewHolder.addView(controls);
    }

    @Override
    public void onTargetsLoaded(ImageTracker imageTracker) {
        Log.v(TAG, "Image tracker loaded");
    }

    @Override
    public void onErrorLoadingTargets(ImageTracker imageTracker, int i, String errorMessage) {
        Log.v(TAG, "Unable to load image tracker. Reason: " + errorMessage);
    }

    @Override
    public void onImageRecognized(ImageTracker imageTracker, final ImageTarget target) {
        Log.v(TAG, "Recognized target " + target.getName());

        mDropDownAlert.dismiss();

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                switch (target.getName()){
                    case TABLE:
//                        messageView.setText(getString(R.string.table));
//                        containerTextView.setVisibility(View.VISIBLE);
                        break;
                    case ALAN:
                        addFragment(R.id.container, AlanFragment.newInstance(), AlanFragment.FRAGMENT_TAG);
                        break;
                    case SOCIAL:
                        addFragment(R.id.container, SocialFragment.newInstance(), SocialFragment.FRAGMENT_TAG );
                        break;
                    case EMAIL:
                        System.out.println();
                        break;
                }
            }
        });

        StrokedRectangle strokedRectangle = new StrokedRectangle(StrokedRectangle.Type.STANDARD);
        mGLRenderer.setRenderablesForKey(target.getName() + target.getUniqueId(), strokedRectangle, null);
    }


    @Override
    public void onImageTracked(ImageTracker imageTracker, ImageTarget target) {
        StrokedRectangle strokedRectangle = (StrokedRectangle) mGLRenderer.getRenderableForKey(target.getName() + target.getUniqueId());

        if (strokedRectangle != null) {
            strokedRectangle.projectionMatrix = target.getProjectionMatrix();
            strokedRectangle.viewMatrix = target.getViewMatrix();

            strokedRectangle.setXScale(target.getTargetScale().x);
            strokedRectangle.setYScale(target.getTargetScale().y);
        }
    }

    @Override
    public void onImageLost(ImageTracker imageTracker, final ImageTarget target) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                switch (target.getName()){
                    case TABLE:
                        System.out.println();
                        break;
                    case ALAN:
                        removeFragment(AlanFragment.FRAGMENT_TAG);
                        break;
                    case SOCIAL:
                        removeFragment(SocialFragment.FRAGMENT_TAG);
                        break;
                    case EMAIL:
                        System.out.println();
                        break;
                }
            }
        });

        Log.v(TAG, "Lost target " + target.getName());
        mGLRenderer.removeRenderablesForKey(target.getName() + target.getUniqueId());
    }

    @Override
    public void onExtendedTrackingQualityChanged(ImageTracker imageTracker, ImageTarget imageTarget, int i, int i1) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(android.Manifest.permission.CAMERA)
    void onStartAugmentReality() {
//        Toast.makeText(this, getResources().getString(R.string.permission_camera_denied), Toast.LENGTH_SHORT).show();
    }

    @OnPermissionDenied(android.Manifest.permission.CAMERA)
    void onPermissionDenied() {
        Toast.makeText(this, getResources().getString(R.string.permission_camera_denied), Toast.LENGTH_SHORT).show();
    }

    @OnShowRationale(android.Manifest.permission.CAMERA)
    void onShowRationale(final PermissionRequest permissionRequest) {

        new AlertDialog.Builder(this)
                .setMessage(R.string.permission_camera_rationale)
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        permissionRequest.proceed();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        permissionRequest.cancel();
                    }
                })
                .show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void onNeverAskAgain() {
        Toast.makeText(this, getResources().getString(R.string.permission_camera_neverask), Toast.LENGTH_SHORT).show();
    }

}
