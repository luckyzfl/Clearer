package com.example.feilongzuo.test;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class DisplayImageActivety extends AppCompatActivity {

    public ImageButton butdo;
    public ImageButton butcancel;
    public ImageView img;
    public Bitmap bitmap;
    public ProgressDialog progressDialog;
    //处理初始图片
    public static Bitmap anspic;
    //处理结果图片
    public Bitmap inipic;
    //Post Result
    public String post_result = null;
    //rgb
    public byte[] rgbnew;

    //子线程处理
    public  Handler handle=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    if(progressDialog.isShowing()==true){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(DisplayImageActivety.this,"Finish",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getApplicationContext(),ResultActivity.class);
                    startActivity(intent);
                    break;
                case 2:
                    if(progressDialog.isShowing()==true){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(DisplayImageActivety.this,"图片过大",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };
    static{ System.loadLibrary("opencv_java3"); }
    private static final String TAG = "MainActivity";
    private final static int CWJ_HEAP_SIZE = 1024* 1024* 1024 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imgdisplaylayout);

//        VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE);
        //去除顶部标题栏
        ActionBar toolbar=getSupportActionBar();
        if(toolbar!=null){
            toolbar.hide();
        }
        //控件
        butdo=(ImageButton)findViewById(R.id.imageButtonok);
        butcancel=(ImageButton) findViewById(R.id.imageButtoncancel);
        img=(ImageView)findViewById(R.id.img);

        Intent intent=getIntent();
        String imagepath=intent.getStringExtra("imgpath");
        if(imagepath!=null){
            bitmap= BitmapFactory.decodeFile(imagepath);
            img.setImageBitmap(bitmap);
            Toast.makeText(this,"success",Toast.LENGTH_SHORT).show();
            butdo.setVisibility(View.VISIBLE);
            butcancel.setVisibility(View.VISIBLE);
        }

        butdo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //回收
                //bitmap.recycle();
                System.gc();

                progressDialog=new ProgressDialog(DisplayImageActivety.this);
                progressDialog.setTitle("Clearing");
                progressDialog.setMessage("Waiting...");
                progressDialog.setCancelable(true);
                if(progressDialog.isShowing()==false){
                    progressDialog.show();
                }
                //new Handle().execute();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //锐化
//                        PictureHandle2.handle(bitmap);
                        //tensor
                        Log.d(TAG, "run: handle");
                        handle("test1",bitmap);
                        if(anspic!=null){
                            Message message=new Message();
                            message.what=1;
                            handle.sendMessage(message);
                        }
                    }
                }).start();
            }
        });
        butcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    public byte[] bitmap2RGB(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();  //返回可用于储存此位图像素的最小字节数

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //  使用allocate()静态方法创建字节缓冲区
        bitmap.copyPixelsToBuffer(buffer); // 将位图的像素复制到指定的缓冲区

        byte[] rgba = buffer.array();
        byte[] pixels = new byte[(rgba.length / 4) * 3];

        int count = rgba.length / 4;

        //Bitmap像素点的色彩通道排列顺序是RGBA
        for (int i = 0; i < count; i++) {

            pixels[i * 3] = rgba[i * 4];        //R
            pixels[i * 3 + 1] = rgba[i * 4 + 1];    //G
            pixels[i * 3 + 2] = rgba[i * 4 + 2];       //B

        }

        return pixels;
    }
    // 将一个byte数转成int
    // 实现这个函数的目的是为了将byte数当成无符号的变量去转化成int
    // 将纯RGB数据数组转化成int像素数组
    public Bitmap base64ToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    public void handle(String imname, Bitmap ipic){
        byte[] rgb=bitmap2RGB(ipic);
        inipic=ipic;
        rgbnew=null;
        int height=ipic.getHeight();
        int width=ipic.getWidth();
        //Post  捕获异常
        try {
            postData(imname,height,width, Base64.encodeToString(rgb,Base64.DEFAULT));
            anspic=base64ToBitmap(post_result);
        }
        catch (OutOfMemoryError e){
            Message msg=new Message();
            msg.what=2;
            handle.sendMessage(msg);
        }
    }
    private void postData(final String name,final int height,final int width,final String pic64){
        Log.d(TAG, "postData: post");
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("height", Integer.toString(height));
        params.put("width", Integer.toString(width));
        params.put("data", pic64);
        try {
            post_result = HttpUtils.submitPostData(params, "utf-8");
            Log.i("POST_RESULT", post_result);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
//                Message msg=new Message();
//                msg.what=2;
//                handle.sendMessage(msg);
    }

}

