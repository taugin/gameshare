package com.chukong.sdkdemo.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.chukong.sdkdemo.util.TimeSync;

public class RemoteService extends Service {

    private LocalBinder mLocalBinder = new LocalBinder();

    public class LocalBinder extends TimeSyncControl.Stub {
        @Override
        public void sync() throws RemoteException {
            TimeSync.getInstance().sync();
        }

        @Override
        public long getNetworkTime() throws RemoteException {
            return TimeSync.getInstance().getNetworkTime();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("taugin2", "RemoteService onBind");
        return mLocalBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("taugin2", "RemoteService onCreate");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mRecevier, filter);
    }

    @Override
    public void onDestroy() {
        Log.d("taugin2", "RemoteService onDestroy");
        TimeSync.getInstance().getNetworkTime();
        unregisterReceiver(mRecevier);
        super.onDestroy();
    }

    private BroadcastReceiver mRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            Log.d("taugin2", "intent = " + intent.getAction());
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
                    .getAction())) {
                TimeSync.getInstance().onNetworkChanged();
            }
        }
    };
}
