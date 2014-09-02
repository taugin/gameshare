package com.chukong.sdkdemo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.SystemClock;
import android.util.Log;

public class TimeSync implements Runnable {

    private static final String NETWORK_TIME = "network_time";
    private static final String ELAPSED_TIME = "elapsed_time";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss SSS");
    private static TimeSync sTimeSync;

    private boolean mSyncing = false;
    private Object[] mObject = new Object[0];

    private TimeSync() {
    }

    public static TimeSync getInstance() {
        if (sTimeSync == null) {
            sTimeSync = new TimeSync();
        }
        return sTimeSync;
    }

    public void onNetworkChanged() {
        Log.d("taugin2", "onNetworkChanged");
        sync();
    }

    public void onTimeChanged() {
        Log.d("taugin2", "onTimeChanged");
        sync();
    }

    @Override
    public void run() {
        Log.d("taugin2",
                "Ready to sync network time1 = " + sdf.format(new Date()));
        mSyncing = true;
        SntpClient client = new SntpClient();
        if (client.requestTime("asia.pool.ntp.org", 20000)
                || client.requestTime("north-america.pool.ntp.org", 20000)
                || client.requestTime("0.africa.pool.ntp.org", 20000)) {
            Log.d("taugin2", "time sync success");
            long networkTime = client.getNtpTime()
                    + SystemClock.elapsedRealtime()
                    - client.getNtpTimeReference();
            long elapsedRealtime = SystemClock.elapsedRealtime();
            saveNetworkTimeAndElapsedTime(networkTime, elapsedRealtime);
        }
        mSyncing = false;
        Log.d("taugin2",
                "Ended to sync network time2 = " + sdf.format(new Date()));
    }

    public void sync() {
        if (!mSyncing) {
            new Thread(this).start();
        }
    }

    private void saveNetworkTimeAndElapsedTime(long networkTime,
            long elapsedTime) {
        synchronized (mObject) {
            System.setProperty(NETWORK_TIME, String.valueOf(networkTime));
            System.setProperty(ELAPSED_TIME, String.valueOf(elapsedTime));
        }
    }

    private long getSavedNetworkTime(long time) {
        String networkTime = System.getProperty(NETWORK_TIME);
        if (networkTime != null) {
            try {
                time = Long.valueOf(networkTime).longValue();
            } catch (Exception e) {
                Log.d("taugin2", e.getLocalizedMessage());
            }
        }
        Log.d("taugin2",
                "SavedNetworkTime = " + networkTime + ", ---> "
                        + sdf.format(new Date(time)));
        return time;
    }

    private long getSavedElapsedTime(long time) {
        String elapsedTime = System.getProperty(ELAPSED_TIME);
        if (elapsedTime != null) {
            try {
                time = Long.valueOf(elapsedTime).longValue();
            } catch (Exception e) {
                Log.d("taugin2", e.getLocalizedMessage());
            }
        }
        return time;
    }

    public long getNetworkTime() {
        synchronized (mObject) {
            long currentTime = System.currentTimeMillis();
            long elapsedRealTime = SystemClock.elapsedRealtime();

            long syncdNetworkTime = getSavedNetworkTime(currentTime);
            long savedElapsedTime = getSavedElapsedTime(elapsedRealTime);

            long networkNow = elapsedRealTime - savedElapsedTime
                    + syncdNetworkTime;
            return networkNow;
        }
    }
}
