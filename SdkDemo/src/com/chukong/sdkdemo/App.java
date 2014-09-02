package com.chukong.sdkdemo;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("taugin", "sdkdemo onCreate = " + getCurProcessName(this));
    }

    String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
