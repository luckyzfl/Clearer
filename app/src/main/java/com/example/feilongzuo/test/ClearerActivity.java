package com.example.feilongzuo.test;
import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.lang.annotation.Documented;
import java.net.URI;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class ClearerActivity extends AppCompatActivity {

    public  String TAG="ClearActivity";
    public  ImageButton imageButton;
    int [] backimg =new int[]{R.drawable.back1,R.drawable.back2,R.drawable.back3};
    int whichback=0;
    //选取背景
    public Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1://选取背景
                    ConstraintLayout constraintLayout=(ConstraintLayout)findViewById(R.id.clearlayout);
                    constraintLayout.setBackgroundResource(backimg[whichback]);
                    whichback++;
                    whichback%=3;
                    break;
            }

        }
    };
    Timer timer=new Timer();
    TimerTask timerTask=new TimerTask() {
        @Override
        public void run() {
            Message msg=new Message();
            msg.what=1;
            handler.sendMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clearer_main);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }
        //定时器触发
        timer.schedule(timerTask,0,5000);
        //控件
        imageButton=(ImageButton)findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(ClearerActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                        PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "onClick: ask ");
                    ActivityCompat.requestPermissions(ClearerActivity.this,
                            new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },1);
                    Log.d(TAG, "onClick: requset success");
                }
                else{
                    Log.d(TAG, "onClick: open");
                    openAlbum();
                }
            }
        });
    }
    private void openAlbum(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,2);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: in this");
        switch (requestCode){
            case 1:
                Log.d(TAG, "onRequestPermissionsResult: in case 1");
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }
                else{
                    Toast.makeText(this,"You denied the permisson",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 2:
                if(resultCode==RESULT_OK){
                    handleImageOnKitKat(data);
                }
        }
    }
    private void handleImageOnKitKat(Intent data){
        String imagepath=null;
        Uri uri=data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            String docID=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docID.split(":")[1];
                String selection= MediaStore.Images.Media._ID+"="+id;
                imagepath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }
            else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docID));
                imagepath=getImagePath(contentUri,null);
            }
        }
        else if("content".equalsIgnoreCase(uri.getScheme())){
            imagepath=getImagePath(uri,null);
        }
        else if("file".equalsIgnoreCase(uri.getScheme())){
            imagepath=uri.getPath();
        }

        displayImage(imagepath);
    }
    private String getImagePath(Uri uri,String selection){
        String path=null;
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        Log.d(TAG, "getImagePath: getsuccess");
        return path;
    }
    private void displayImage(String imagepath){
        Log.d(TAG, "displayImage: display");
        if(imagepath!=null){
            //Toast.makeText(this,"success",Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(ClearerActivity.this,DisplayImageActivety.class);
            intent.putExtra("imgpath",imagepath);;
            startActivity(intent);
        }
    }
}
