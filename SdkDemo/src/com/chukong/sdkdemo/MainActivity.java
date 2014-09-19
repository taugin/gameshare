package com.chukong.sdkdemo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;
import cn.cmgame.billing.api.GameInterface;
import cn.cmgame.billing.api.GameInterface.GameExitCallback;

import com.chukong.sdk.common.Log;
import com.chukong.sdkdemo.service.TimeSyncControl;

public class MainActivity extends Activity implements OnClickListener,
        OnCheckedChangeListener {
    private static final int THEME_LIGHT_FULLSCREEN = android.R.style.Theme_Light_NoTitleBar_Fullscreen;
    private static final int THEME_HOLO_FULLSCREEN = android.R.style.Theme_NoTitleBar_Fullscreen;
    private static final int THEME_LIGHT = android.R.style.Theme_Light_NoTitleBar;
    private static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    private Button mNetworkTime;
    private Button mLocalTime;
    private Button mSync;
    private ToggleButton mBind;
    private Handler mHandler;
    private TimeSyncControl mTimeSyncControl = null;
    private boolean mStop = false;

    private boolean enterAppAnimation(Intent intent) {
        boolean needAnimation = true;
        if (!needAnimation) {
            return false;
        }
        if (intent != null) {
            Set<String> categtoies = intent.getCategories();
            if (Intent.ACTION_MAIN.equals(intent.getAction())
                    && (categtoies != null && categtoies
                            .contains(Intent.CATEGORY_LAUNCHER))) {
                Intent newIntent = new Intent(this,
                        cn.cmgame.billing.ui.GameOpenActivity.class);
                startActivity(newIntent);
                setTheme(THEME_HOLO_FULLSCREEN);
                finish();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("taugin", "onCreate");
        if (enterAppAnimation(getIntent())) {
            return;
        }
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
    public void onBackPressed() {
        GameInterface.exit(this, new GameExitCallback() {
            @Override
            public void onConfirmExit() {
                finish();
            }

            @Override
            public void onCancelExit() {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBind != null && mBind.isChecked()) {
            mBind.setChecked(false);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.bindservice) {
            if (isChecked) {
                mStop = false;
                mHandler.post(mRunnable);
                Intent intent = new Intent(
                        "com.chukong.sdkdemo.action.REMOTE_SERVICE");
                Log.d("taugin2", "bindService");
                bindService(intent, mServiceConnection,
                        Context.BIND_AUTO_CREATE);
            } else {
                Log.d("taugin2", "unbindService");
                unbindService(mServiceConnection);
                mStop = true;
            }
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.sync_time) {
            if (mTimeSyncControl != null) {
                try {
                    mTimeSyncControl.sync();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (view.getId() == R.id.test) {
            /**
             * ShareDialog dialog = new ShareDialog(this); Bitmap bmp =
             * BitmapFactory.decodeResource(getResources(), R.drawable.icon);
             * dialog.setLogoBmp(bmp); dialog.show();
             */
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
