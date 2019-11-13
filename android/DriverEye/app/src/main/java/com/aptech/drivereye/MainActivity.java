package com.aptech.drivereye;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_CODE = 5000;
    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.txt_status)
    TextView txtStatus;
    private boolean isActive = false;
    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder recorder;
    boolean isAlarmActive = false;
    private final String TAG = "MAIN_ACTIVITY";
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        cameraPermissions();
        mp = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mp.seekTo(0);
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isActive) {
                    if (startSystem()) {
                        btnStart.setText("Stop System");
                        isActive = true;
                    } else {
                        Log.d("MAIN_ACTIVITY","Problem");
                        Toast.makeText(getApplicationContext(), "Unable To Start", Toast.LENGTH_LONG);
                    }
                } else {
                    if (stopSystem()) {
                        isActive = false;
                        btnStart.setText("Start System");
                    } else {
                        Toast.makeText(getApplicationContext(), "Unable To Stop", Toast.LENGTH_LONG);
                    }
                }
            }
        });
        txtStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAlarmActive) {
                    stopAlarm();
                } else {
                    startAlarm();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAMERA_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    private boolean startSystem() {
        try {
            if (checkCameraHardware(getApplicationContext())) {
                File f  = File.createTempFile("Driver","Eye");
                mCamera = getCameraInstance();
                // Create our Preview view and set it as the content of our activity.
                mPreview = new CameraPreview(getApplicationContext(), mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);
                mCamera.unlock();
                recorder = new MediaRecorder();
                recorder.setCamera(mCamera);
                //recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
                recorder.setOutputFile(f);
                recorder.prepare();
                recorder.start();
                return true;
            }else{
                Log.d("MAIN","Pnga Here");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MAIN","Pnga Here 2");
            return false;
        }
        return false;
    }


    private boolean stopSystem() {
        try {
            recorder.stop();
            recorder.reset();
            recorder.release();
            mCamera.lock();
            mCamera.stopPreview();
            mCamera.release();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(1); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    private void startAlarm() {
        if (!isAlarmActive) {
            mp.start();
            isAlarmActive = true;
        }
    }

    private void stopAlarm() {
        if (isAlarmActive) {
            mp.seekTo(0);
            mp.stop();
            isAlarmActive = false;
        }
    }


    void cameraPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_CODE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

    }
}
