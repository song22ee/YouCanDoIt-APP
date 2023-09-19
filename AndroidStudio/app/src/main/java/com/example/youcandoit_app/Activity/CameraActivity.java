package com.example.youcandoit_app.Activity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.youcandoit_app.R;
import com.example.youcandoit_app.Task.CertifyTask;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    ConstraintLayout pLayout, cLayout;
    ImageView imageView;
    PreviewView previewView;
    ImageButton take, sw, close;
    Button reset, certify;
    View.OnClickListener cl;
    FrameLayout frameShutter;
    Animation cameraAnimation;

    // 카메라 사용을 위해 선언
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ProcessCameraProvider cameraProvider;
    CameraSelector cameraSelector;
    Preview preview;
    ImageCapture imageCapture;
    // 카메라 선택
    int lensFacing = CameraSelector.LENS_FACING_BACK;
    // 바인딩이 처음인지 여부
    boolean isFirst = true;
    // 사진 저장 경로
    Uri saveUri = null;
    SharedPreferences user_preferences;
    String id;
    Intent i;
    String groupNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_page);

        pLayout = findViewById(R.id.previewLayout);
        cLayout = findViewById(R.id.captureLayout);
        previewView = findViewById(R.id.previewView);
        take = findViewById(R.id.take);
        sw = findViewById(R.id.switchBtn);
        imageView = findViewById(R.id.imageView);
        reset = findViewById(R.id.reset);
        certify = findViewById(R.id.certify);
        close = findViewById(R.id.close);
        frameShutter = findViewById(R.id.frameLayoutShutter);

        // 애니메이션 연결
        cameraAnimation = AnimationUtils.loadAnimation(this, R.anim.camera_shutter);
        // 애니메이션이 종료되면 레이아웃을 비활성화
        cameraAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                frameShutter.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        i = getIntent();
        groupNumber = i.getStringExtra("number");

        user_preferences = getSharedPreferences("login", MODE_PRIVATE);
        id = user_preferences.getString("id", null);

        bindCamera();

        cl = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.take: // 촬영
                        takePhoto();
                        frameShutter.setVisibility(View.VISIBLE);
                        frameShutter.startAnimation(cameraAnimation);
                        break;
                    case R.id.switchBtn: // 카메라 바꾸기
                        if(lensFacing == CameraSelector.LENS_FACING_BACK)
                            lensFacing = CameraSelector.LENS_FACING_FRONT;
                        else
                            lensFacing = CameraSelector.LENS_FACING_BACK;
                        bindCamera();
                        break;
                    case R.id.reset: // 다시찍기
                        getContentResolver().delete(saveUri, null, null);
                        cLayout.setVisibility(View.INVISIBLE);
                        pLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.certify: // 인증하기
                        Cursor c = getContentResolver().query(saveUri, null,null,null,null);
                        c.moveToNext();
                        String path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
                        String uri = String.valueOf(Uri.fromFile(new File(path)));
                        c.close();

                        CertifyTask task = new CertifyTask();
                        task.execute(id, groupNumber, uri);

                        Toast.makeText(getApplicationContext(), "인증이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                        Intent r = new Intent();
                        setResult(100, r);
                        finish();
                        break;
                    case R.id.close:
                        finish();
                }
            }
        };
        take.setOnClickListener(cl);
        sw.setOnClickListener(cl);
        reset.setOnClickListener(cl);
        certify.setOnClickListener(cl);
        close.setOnClickListener(cl);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraProvider.unbindAll();
        if(saveUri != null)
            getContentResolver().delete(saveUri, null, null);
    }

    /** 카메라 실행(binding) */
    public void bindCamera() {
        // 인스턴스 생성
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                if(isFirst) {
                    // 생명주기에 binding 할 수 있는 Provider 객체 추출
                    cameraProvider = cameraProviderFuture.get();

                    if(!cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                        sw.setVisibility(View.INVISIBLE);
                    }

                    // Preview 선언
                    preview = new Preview.Builder().build();

                    imageCapture = new ImageCapture.Builder().build();

                    // 카메라 비율 고정
                    previewView.setScaleType(PreviewView.ScaleType.FIT_CENTER);
                    // 미리보기 view에 SurfaceProvider 제공
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                    isFirst = false;
                }

                // 카메라 렌즈 설정
                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build();

                cameraProvider.unbindAll();
                // 바인딩
                cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("Camera-Error", "message : " + e);
            } catch (CameraInfoUnavailableException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /** 카메라 촬영 */
    public void takePhoto() {
        long timestamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/YouCanDoIt");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        Log.i("ddd", "value : " + outputFileResults.getSavedUri());
                        saveUri = outputFileResults.getSavedUri();
                        imageView.setImageURI(saveUri);
                        pLayout.setVisibility(View.INVISIBLE);
                        cLayout.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onError(ImageCaptureException error) {
                        Log.e("error", "value : ", error);
                    }
                }
        );
    }
}