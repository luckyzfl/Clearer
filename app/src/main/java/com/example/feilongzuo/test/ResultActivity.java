package com.example.feilongzuo.test;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ResultActivity extends AppCompatActivity {
    public ImageView imageView;
    public ImageButton saveBut;
    public ImageButton returnBut;
    public ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resultlayout);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }
        imageView=(ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(DisplayImageActivety.anspic);
        saveBut=(ImageButton)findViewById(R.id.loadButton);
        returnBut=(ImageButton)findViewById(R.id.returnButton);
        saveBut.setVisibility(View.GONE);
        returnBut.setVisibility(View.GONE);
        saveBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog=new ProgressDialog(ResultActivity.this);
                progressDialog.setTitle("Clearing");
                progressDialog.setMessage("正在保存...");
                progressDialog.setCancelable(true);
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveImageToGallery(ResultActivity.this,DisplayImageActivety.anspic);
                        Message mm=new Message();
                        mm.what=1;
                        handler.sendMessage(mm);
                    }
                }).start();
            }
        });
        returnBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(saveBut.getVisibility()==View.GONE){
                    saveBut.setVisibility(View.VISIBLE);
                    returnBut.setVisibility(View.VISIBLE);
                    setDark();
                }
                return true;
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(saveBut.getVisibility()==View.VISIBLE){
                    saveBut.setVisibility(View.GONE);
                    returnBut.setVisibility(View.GONE);
                    setEmpty();
                }
            }
        });

    }
    private void setEmpty() {
        WindowManager.LayoutParams lp=getWindow().getAttributes();
        lp.alpha=1.0f;
        getWindow().setAttributes(lp);
    }
    public void setDark(){
        WindowManager.LayoutParams lp=getWindow().getAttributes();
        lp.alpha=0.7f;
        getWindow().setAttributes(lp);
    }
    public static void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "Clearer");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse(Environment.getExternalStorageDirectory() +"Clearer")));
    }

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    progressDialog.dismiss();
                    imageView.callOnClick();
                    Toast.makeText(ResultActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
                    finish();
            }
        }
    };
}
