package com.example.camera_test;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    //Tạo 1 một luồng xử lý ProcessCameraProvider
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    //PreviewView dùng để hiện thị khá giống cách hoạt động của tureview
    PreviewView previewView;
    // Sử dụng ImageCapture để lưu hình ảnh sau khi chup
    private ImageCapture imageCapture;
    // Biến camera để dữ liệu camera
    Camera camera;
    Button chuphinh,reload;
    CameraControl cameraControl;
    CameraInfo cameraInfo;
    SeekBar zoom;
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //xin quyền camera
        xinquyen_camera();
        //ánh xạ các các thành phần
        previewView = findViewById(R.id.previewView);
        chuphinh = findViewById(R.id.chuphinh);
        reload = findViewById(R.id.reload);
        zoom = findViewById(R.id.zoom);
        //Get width height thiết bị
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height_screen = displayMetrics.heightPixels;
        int width_screen = displayMetrics.widthPixels;
        //Dựa vào width xuất ra view có tỉ lệ khung hình cần 3:4 4:3 16:9 9:16
        // (chỉ dụng để hiện thị, ở đây bạn có  thể tạo ra 1 biến button để đổi hiện thị, dựa vào đó bản có thể đổi tỉ lệ khung hình của ảnh
        int height_new = (width_screen/9)*16;
        //Set khung hình
        previewView.setLayoutParams(new ConstraintLayout.LayoutParams(width_screen,height_new));
        //Hàm thực thi gọi camera và setview lên previewView;
        start();
        //set giá trị cho seekbar để zoom
        zoom.setMax(100);
        zoom.setProgress(0);

        // thực thi chụp hình
        chuphinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chuphinh();
            }
        });
        //Sau khi chụp sẽ dừng lại frame đã chup (tạo hiệu ứng hình đã được chụp ) chọn vào nút chụp lại để bind lại vòng đợi camera
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();

            }
        });

    }
    //Hàm chụp hình
    public void chuphinh()
    {
        //Tạo nơi mà ảnh sẽ lưu, ở đây tôi chọn vùng cache để lưu
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(new File(getCacheDir(),"images.jpg")).build();
        //Chụp ảnh
        //ContextCompat.getMainExecutor(MainActivity.this) là luồng xử lý ảnh ở dây tôi dùng trên luồng chính
        //takePicture có 2 phương thức, nếu bạn không truyền biến outputFileoption (Khởi tạo nơi lưu) thì mặt định nó sẽ lưu ở file cache =]]
        //Ừ thì cái outputFileOptions cũng lưu ở file cache
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(MainActivity.this), new ImageCapture.OnImageSavedCallback() {
            @SuppressLint("RestrictedApi")
            @Override
            //sau khi chụp thành công các sự kiện xảy ra, như kiểu nếu thành công bạn sẽ làm gì
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                //lấy uri nơi lưu hình
                outputFileResults.getSavedUri();
                try {
                    //sao khi chụp thành công tôi dùng khung hình lại  tạo hiệu ứng lấy ảnh
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    cameraProvider.unbindAll();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Toast.makeText(MainActivity.this, "Lưu hình thành công", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {

            }
        });
    }
    public void xinquyen_camera()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},7979);
        }else {

        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 7979)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                xinquyen_camera();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void start()
    {
        //Ở đây chúng ta sẽ ràng buộc luồng xử lý vào vòng đời camera, và cameraX sẽ tự hiểu vòng đòi của chính thứ chứa nó ở đây là vòng đời hàm main
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                //Hàm có thể coi như là setview từ camera lên previewView
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
            }
        }, ContextCompat.getMainExecutor(this));
    }
    @SuppressLint("RestrictedApi")
    @RequiresApi(api = Build.VERSION_CODES.R)
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        //Hủy mọi liên kết vòng đời của cameraX trước khi liên kết lại
        cameraProvider.unbindAll();
        //Build ImageCapture
        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(this.getDisplay().getRotation())
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)//tỉ lệ hình ảnh
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)//tốc độ luồng xử lý và chất lượng hiình ảnh
                .build();
        //Phân tích và xử lý hình ảnh nếu muốn dùng thì có thể đọc thêm doc của google về cameraX
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();
        //Build preview hình ảnh
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        //kết nối Preview vào PreviewView
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        //cameraProvider.bindToLifecycle sẽ trả về một đối tượng camera và đồng thời sẽ liên kết vòng đợi camera để xử dụng
        //ở đây họ dùng thuật ngữ use_case, nhưng bạn có trẻ truyền đối số theo mẫu dưới là đc
        camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector,preview,imageAnalysis,imageCapture);
        //Ở camera chúng ta sẽ có 2 đối tượng để xử lý ảnh đầu ra và vào đồng thời các tính năng mặc định như set độ phơi sáng, bật đèn flash, zoom ra zoom vào
        cameraControl = camera.getCameraControl();
        cameraInfo = camera.getCameraInfo();

        zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // ở đây tôi dùng seekbar để zoom ra zoom vào camera, hàm setLinearZoom nhận giá trị  từ 0-1
                cameraControl.setLinearZoom(i/100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}