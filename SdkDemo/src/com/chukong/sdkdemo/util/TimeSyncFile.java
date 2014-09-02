package com.chukong.sdkdemo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.util.Log;

public class TimeSyncFile implements Runnable {

    private static final String NETWORK_TIME = "network_time";
    private static final String ELAPSED_TIME = "elapsed_time";
    private static final String NETWORK_SYNC = "network_sync";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss SSS");
    private static TimeSyncFile sTimeSync;

    private Context mContext = null;
    private boolean mSyncing = false;
    private Object[] mObject = new Object[0];
    private long mSavedNetworkTime = 0;
    private long mSavedElapsedTime = 0;

    private TimeSyncFile(Context context) {
        mContext = context;
    }

    public static TimeSyncFile getInstance(Context context) {
        if (sTimeSync == null) {
            sTimeSync = new TimeSyncFile(context);
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
            if (!saveTimeToFile(networkTime, elapsedTime)) {
                Log.d("taugin2", "save time to System");
                System.setProperty(NETWORK_TIME, String.valueOf(networkTime));
                System.setProperty(ELAPSED_TIME, String.valueOf(elapsedTime));
            }
        }
    }

    private boolean saveTimeToFile(long networkTime, long elapsedTime) {
        Log.d("taugin2", "save time to File");
        try {
            SharedPreferences sharepPreferences = mContext
                    .getSharedPreferences("network_time",
                            Context.MODE_MULTI_PROCESS);
            Editor editor = sharepPreferences.edit();
            editor.putLong(NETWORK_TIME, networkTime);
            editor.putLong(ELAPSED_TIME, elapsedTime);
            editor.commit();
            return true;
        } catch (Exception e) {
            Log.d("taugin2", "e = " + e);
        }
        return false;
    }

    private boolean readTimeFromFile() {
        Log.d("taugin2", "read time from File");
        try {
            SharedPreferences sharepPreferences = mContext
                    .getSharedPreferences("network_time",
                            Context.MODE_MULTI_PROCESS);
            Log.d("taugin2", "sharepPreferences ============== "
                    + sharepPreferences);
            String networkTime = sharepPreferences
                    .getString(NETWORK_TIME, null);
            Log.d("taugin2", "networkTime = " + networkTime);
            String elapsedTime = sharepPreferences
                    .getString(ELAPSED_TIME, null);
            Log.d("taugin2", "networkTime = " + networkTime
                    + " , elapsedTime = " + elapsedTime);
            mSavedNetworkTime = networkTime != null ? Long
                    .parseLong(networkTime) : 0;
            mSavedElapsedTime = elapsedTime != null ? Long
                    .parseLong(elapsedTime) : 0;
            return true;
        } catch (Exception e) {
            Log.d("taugin", "e = " + e);
        }
        return false;
    }

    private void readSavedNetworkTimeAndElapsedTime(long networkTime,
            long elapsedTime) {
        if (readTimeFromFile()) {
            return;
        }
        if (readTimeFromSystem()) {
            return;
        }
        mSavedNetworkTime = networkTime;
        mSavedElapsedTime = elapsedTime;
    }

    private boolean readTimeFromSystem() {
        Log.d("taugin2", "read time from System");
        String networkTime = System.getProperty(NETWORK_TIME);
        String elapsedTime = System.getProperty(ELAPSED_TIME);
        if (networkTime != null && elapsedTime != null) {
            try {
                mSavedNetworkTime = Long.valueOf(networkTime).longValue();
                mSavedElapsedTime = Long.valueOf(elapsedTime).longValue();
                return true;
            } catch (Exception e) {
                Log.d("taugin2", e.getLocalizedMessage());
            }
        }
        return false;
    }

    public long getNetworkTime() {
        synchronized (mObject) {
            long currentTime = System.currentTimeMillis();
            long elapsedRealTime = SystemClock.elapsedRealtime();

            readSavedNetworkTimeAndElapsedTime(currentTime, elapsedRealTime);
            long networkNow = elapsedRealTime - mSavedElapsedTime
                    + mSavedNetworkTime;
            return networkNow;
        }
    }
}
