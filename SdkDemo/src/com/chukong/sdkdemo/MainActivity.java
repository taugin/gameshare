package com.chukong.sdkdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.chukong.sdk.ShareDialog;
import com.chukong.sdk.common.Log;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        ShareDialog dialog = new ShareDialog(this);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        dialog.setLogoBmp(bmp);
        dialog.show();
    }

    @Override
    protected void onStart() {
        Log.d("taugin", "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d("taugin", "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("taugin", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("taugin", "onStop");
        super.onStop();
    }
}
