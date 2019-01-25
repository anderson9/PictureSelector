package com.luck.picture.lib.camare;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.luck.picture.lib.R;
import com.luck.picture.lib.camare.listener.ClickListener;
import com.luck.picture.lib.camare.listener.JCameraListener;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;

/**
 * -------------------------------------------
 * Created by Android:Luojiusan 2019/1/24
 * Pacage_Name: com.cjt2325.cameralibrary
 * Dec: 视频拍摄页面
 * -------------------------------------------
 **/
public class TakeCamareActivity extends AppCompatActivity {

    public static void toTakeCamareActivity(Activity mActivity, int requeCode, String cameraPath) {
        Intent intent = new Intent();
        intent.setClass(mActivity, TakeCamareActivity.class);
        intent.putExtra("cameraPath", cameraPath);
        intent.putExtra("requeCode", requeCode);
        mActivity.startActivityForResult(intent, requeCode);
    }


    String cameraPath;
    int requeCode;
    public static final String TOSELECT = "toselect";

    private final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码
    private JCameraView jCameraView;
    private boolean granted = false;
    public static final int REQUEST_VEDIO_CUSTOM = 105;//自定义录制的视频 跟益村耦合了一个编码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_takecamare);

        jCameraView = (JCameraView) findViewById(R.id.jcameraview);
        jCameraView.setDuration(20 * 1000);
        //设置视频保存路径
        cameraPath = getIntent().getStringExtra("cameraPath");
        requeCode = getIntent().getIntExtra("requeCode", 0);
        jCameraView.setSaveVideoPath(cameraPath);
        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_HIGH);
        jCameraView.setFeatures(JCameraView.BUTTON_STATE_ONLY_RECORDER);
        jCameraView.setTip("长按开始录制");
        jCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {

            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                Intent intent = new Intent();
                if (!TextUtils.isEmpty(url)) {
                    intent.putExtra("url", url);
                }
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        jCameraView.setRturnClickClickListener(new ClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });
        jCameraView.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {
                if (requeCode == REQUEST_VEDIO_CUSTOM) {//表示取消录制 需要源activity去跳转选择视频
                    Intent intent = new Intent();
                    intent.putExtra("url", TOSELECT);
                    setResult(Activity.RESULT_OK, intent);
                } else { //直接跳转选择视频
                    PictureSelector.create(TakeCamareActivity.this)
                            .openGallery(PictureMimeType.ofVideo())
                            .theme(R.style.picture_default_style)
                            .maxSelectNum(1)
                            .imageSpanCount(4)
                            .selectionMode(PictureConfig.SINGLE)
                            .previewVideo(true)
                            .previewImage(true)// 是否可预览图片 true or false
                            .isCamera(true)// 是否显示拍照按钮 true or false
                            .compress(true)// 是否压缩 true or false
                            .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                            .isGif(true)// 是否显示gif图片 true or false
                            .openClickSound(false)// 是否开启点击声音 true or false
                            .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中) true or false
                            .cropCompressQuality(90)// 裁剪压缩质量 默认90 int
                            .videoQuality(1)// 视频录制质量 0 or 1 int
                            .videoMaxSecond(180)// 显示多少秒以内的视频or音频也可适用 int
                            .recordVideoSecond(20)//视频秒数录制 默认60s int
                            .toSelect();//结果回调onActivityResult code
                }
                finish();
            }
        });
        //JCameraView监听
        //6.0动态权限获取
        getPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (granted) {
            jCameraView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        jCameraView.onPause();
    }

    /**
     * 获取权限
     */
    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //具有权限
                granted = true;
            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(TakeCamareActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA}, GET_PERMISSION_REQUEST);
                granted = false;
            }
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_PERMISSION_REQUEST) {
            int size = 0;
            if (grantResults.length >= 1) {
                int writeResult = grantResults[0];
                //读写内存权限
                boolean writeGranted = writeResult == PackageManager.PERMISSION_GRANTED;//读写内存权限
                if (!writeGranted) {
                    size++;
                }
                //录音权限
                int recordPermissionResult = grantResults[1];
                boolean recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!recordPermissionGranted) {
                    size++;
                }
                //相机权限
                int cameraPermissionResult = grantResults[2];
                boolean cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!cameraPermissionGranted) {
                    size++;
                }
                if (size == 0) {
                    granted = true;
                    jCameraView.onResume();
                } else {
                    Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}
