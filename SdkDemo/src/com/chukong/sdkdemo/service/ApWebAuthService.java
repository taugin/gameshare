package com.chukong.sdkdemo.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.chukong.sdkdemo.util.CmdExecutor;
import com.chukong.sdkdemo.util.CommonUtil;
import com.chukong.sdkdemo.util.IptableSet;

public class ApWebAuthService extends IntentService {
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
    public ApWebAuthService() {
        super("ApWebAuthService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return ;
        }
        int apState = 0;
        if (WIFI_AP_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            apState = intent.getIntExtra(EXTRA_WIFI_AP_STATE, WIFI_AP_STATE_DISABLED);
            Log.d("taugin", "ApWebAuthService apState = " + apState);
            if (apState == WIFI_AP_STATE_ENABLED) {
                StringBuilder builder = new StringBuilder();
                String addr = null;
                while((addr = CommonUtil.getLocalIpAddress()) == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("taugin", "add = " + addr);
                try {
                    CmdExecutor.runScriptAsRoot("netcfg", builder);
                    String addrMast = CommonUtil.pickIpAndMask(builder.toString(), addr);
                    Log.d("taugin", "addrMast = " + addrMast);
                    builder.delete(0, builder.length());
                    CmdExecutor.runScriptAsRoot(IptableSet.generateIpCheckRule(addrMast), builder);
                    Log.d("taugin", "builder = " + builder.toString());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (apState == WIFI_AP_STATE_DISABLED) {
                StringBuilder builder = new StringBuilder();
                try {
                    CmdExecutor.runScriptAsRoot(IptableSet.generateClearIpRule(), builder);
                    Log.d("taugin", "builder = " + builder.toString());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

}
