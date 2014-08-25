package com.chukong.sdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.chukong.sdk.ShareDialog;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        ShareDialog dialog = new ShareDialog(this);
        dialog.show();
    }
}
