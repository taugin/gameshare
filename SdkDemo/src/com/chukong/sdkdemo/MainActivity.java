package com.chukong.sdkdemo;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.chukong.sdk.ShareDialog;
import com.chukong.sdkdemo.service.TimeSyncControl;

public class MainActivity extends Activity implements OnClickListener,
        OnCheckedChangeListener {

    private static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    private Button mNetworkTime;
    private Button mLocalTime;
    private Button mSync;
    private ToggleButton mBind;
    private Handler mHandler;
    private TimeSyncControl mTimeSyncControl = null;
    private boolean mStop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNetworkTime = (Button) findViewById(R.id.network_time);
        mLocalTime = (Button) findViewById(R.id.local_time);
        mBind = (ToggleButton) findViewById(R.id.bindservice);
        mBind.setOnCheckedChangeListener(this);
        mSync = (Button) findViewById(R.id.sync_time);
        mSync.setOnClickListener(this);
        mHandler = new Handler();
    }

    @Override
    protected void onDestroy() {
        if (mBind.isChecked()) {
            mBind.setChecked(false);
        }
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.bindservice) {
            if (isChecked) {
                // mStop = false;
                // mHandler.post(mRunnable);
                Intent intent = new Intent(
                        "com.chukong.sdkdemo.action.REMOTE_SERVICE");
                Log.d("taugin2", "bindService");
                bindService(intent, mServiceConnection,
                        Context.BIND_AUTO_CREATE);
            } else {
                Log.d("taugin2", "unbindService");
                unbindService(mServiceConnection);
                // mStop = true;
            }
        }
    }

    public void onClick(View view) {
        ShareDialog dialog = new ShareDialog(this);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.icon);
        dialog.setLogoBmp(bmp);
        dialog.show();

        if (view.getId() == R.id.sync_time) {
            if (mTimeSyncControl != null) {
                try {
                    mTimeSyncControl.sync();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            long networkTime = System.currentTimeMillis();
            long now = System.currentTimeMillis();
            if (mTimeSyncControl != null) {
                try {
                    networkTime = mTimeSyncControl.getNetworkTime();
                } catch (RemoteException e) {
                    Log.d("taugin2", "e = " + e);
                }
            }
            String nTime = sdf.format(new Date(networkTime));
            String lTime = sdf.format(new Date(now));
            mNetworkTime.setText(nTime);
            mLocalTime.setText(lTime);
            if (!mStop) {
                mHandler.postDelayed(mRunnable, 1000);
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("taugin2", "onServiceDisconnected-------------------------");
            mTimeSyncControl = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("taugin2", "onServiceConnected ++++++++++++++++++++++++++++");
            mTimeSyncControl = TimeSyncControl.Stub.asInterface(service);
        }
    };
}
