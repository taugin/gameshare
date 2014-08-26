package com.chukong.sdk;

import org.join.zxing.Contents;
import org.join.zxing.Intents;
import org.join.zxing.encode.QRCodeEncoder;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.chukong.sdk.Constants.Config;
import com.chukong.sdk.common.Log;
import com.chukong.sdk.receiver.OnWifiApStateChangeListener;
import com.chukong.sdk.receiver.WifiApStateReceiver;
import com.chukong.sdk.serv.WebServer.OnWebServListener;
import com.chukong.sdk.service.WebService;
import com.chukong.sdk.util.CommonUtil;
import com.chukong.sdk.wifiap.WifiApManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

public class ShareDialog extends Dialog implements OnWifiApStateChangeListener, OnWebServListener {

    private static final int W_START = 0x0101;
    private static final int W_STOP = 0x0102;
    private static final int W_ERROR = 0x0103;

    private static final int DLG_SERV_USELESS = 0x0201;
    private static final int DLG_PORT_IN_USE = 0x0202;
    private static final int DLG_TEMP_NOT_FOUND = 0x0203;
    private static final int DLG_SCAN_RESULT = 0x0204;

    protected Intent webServIntent;
    protected WebService webService;
    private boolean isBound = false;
    private String ipAddr = null;
    private CommonUtil mCommonUtil;
    private Bitmap mLogoBmp;

    private ImageView mQRImage = null;
    public ShareDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalInit globalInit = new GlobalInit(getContext());
        globalInit.init();
        globalInit.setLocalShare(true);
        webServIntent = new Intent(getContext(), WebService.class);
        mCommonUtil = CommonUtil.getSingleton();
        mQRImage = new ImageView(getContext());
        setContentView(mQRImage);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    
    @Override
    protected void onStart() {
        super.onStart();
        WifiApStateReceiver.register(getContext(), this);
        setWifiApEnabled(true);
    }

    @Override
    protected void onStop() {
        doUnbindService();
        setWifiApEnabled(false);
        WifiApStateReceiver.unregister(getContext());
        super.onStop();
    }

    @Override
    public void onWifiApStateChanged(int state) {
        if (state == WifiApStateReceiver.WIFI_AP_STATE_DISABLING || state == WifiApStateReceiver.WIFI_AP_STATE_ENABLING) {
        } else if(state == WifiApStateReceiver.WIFI_AP_STATE_DISABLED || state == WifiApStateReceiver.WIFI_AP_STATE_ENABLED) {
        }
        if (state == WifiApStateReceiver.WIFI_AP_STATE_ENABLED) {
            WifiConfiguration config = WifiApManager.getInstance(getContext()).getWifiApConfiguration();
            if (config != null) {
                //wifiApText.setText("ssid : " + config.SSID + "\n" + config.preSharedKey);
                doBindService();
            }
        } else if (state == WifiApStateReceiver.WIFI_AP_STATE_DISABLED) {
            //wifiApText.setText("");
            doUnbindService();
        }
    }
    
    @SuppressLint("NewApi")
    private void setWifiApEnabled(boolean enabled) {
        boolean apEnable = WifiApManager.getInstance(getContext()).isWifiApEnabled();
        if (apEnable == enabled) {
            return ;
        }
        if (enabled) {
            WifiConfiguration oldConfig = WifiApManager.getInstance(getContext()).getWifiApConfiguration();
            Log.d(Log.TAG, "----------------------oldConfig = " + oldConfig);
            Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            Log.d(Log.TAG, "+++++++++++++++++++++++++++++++++++oldConfig.SSID = " + oldConfig.SSID + " , oldConfig.preShareKey = " + oldConfig.preSharedKey);
            editor.putString(Constants.KEY_SAVED_SSID, oldConfig.SSID);
            editor.putString(Constants.KEY_SAVED_PASS, oldConfig.preSharedKey);
            editor.putInt(Constants.KEY_SECURITY_TYPE, WifiApManager.getSecurityTypeIndex(oldConfig));
            editor.apply();
            String SSID = "Chukong-Share";
            WifiConfiguration config = WifiApManager.getInstance(getContext()).getConfig(SSID, null, WifiApManager.OPEN_INDEX);
            Log.d(Log.TAG, "config =  " + config.SSID);
            WifiApManager.getInstance(getContext()).setWifiApConfiguration(config);
            WifiApManager.getInstance(getContext()).setSoftApEnabled(null, enabled);
        } else {
            String SSID = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Constants.KEY_SAVED_SSID, null);
            String pass = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Constants.KEY_SAVED_PASS, null);
            int securityType = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(Constants.KEY_SECURITY_TYPE, WifiApManager.OPEN_INDEX);
            WifiConfiguration config = WifiApManager.getInstance(getContext()).getConfig(SSID, pass, securityType);
            Log.d(Log.TAG, "+++++++++++++++++++++++++++++++++++ssid = " + SSID + " , preSharedKey = " + pass + ", securityType = " + securityType);
            // 还原原来的SSID会导致重启
            WifiApManager.getInstance(getContext()).setSoftApEnabled(null, enabled);
            WifiApManager.getInstance(getContext()).setWifiApConfiguration(config);
        }
    }
    
    
    private ServiceConnection servConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Log.TAG, "");
            webService = ((WebService.LocalBinder) service).getService();
            webService.setOnWebServListener(ShareDialog.this);
            webService.openServer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Log.TAG, "");
            webService = null;
        }
    };
    protected void doBindService() {
        // Restore configs of port and root here.
        Log.d(Log.TAG, "");
        //PreferActivity.restore(PreferActivity.KEY_SERV_PORT, PreferActivity.KEY_SERV_ROOT);
        getContext().bindService(webServIntent, servConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    protected void doUnbindService() {
        Log.d(Log.TAG, "isBound = " + isBound);
        if (isBound) {
            getContext().unbindService(servConnection);
            isBound = false;
        }
    }

    @Override
    public void onStarted() {
        mHandler.sendEmptyMessage(W_START);
    }

    @Override
    public void onStopped() {
        mHandler.sendEmptyMessage(W_STOP);
    }

    @Override
    public void onError(int code) {
        
    }
    
    private void setUrlText(String ipAddr) {
        ipAddr = mCommonUtil.getLocalIpAddress();
        String url = "http://" + ipAddr + ":" + Config.PORT + "/";
        setTitle(url);
        generateQRCode(url);
    }
    private void generateQRCode(String text) {
        Intent intent = new Intent(Intents.Encode.ACTION);
        intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
        intent.putExtra(Intents.Encode.DATA, text);
        try {
            int dimension = getDimension();
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(getContext(), intent, dimension, false);
            qrCodeEncoder.setLogoBmp(mLogoBmp);
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            if (bitmap == null) {
                Log.w(Log.TAG, "Could not encode barcode");
            } else {
                mQRImage.setImageBitmap(bitmap);
            }
        } catch (WriterException e) {
        }
    }

    private int getDimension() {
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        int dimension = width < height ? width : height;
        dimension = dimension * 3 / 4;
        return dimension;
    }
    
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case W_START: {
                setUrlText(ipAddr);
                mQRImage.setVisibility(View.VISIBLE);
                break;
            }
            case W_STOP: {
                //urlText.setText("");
                mQRImage.setImageResource(0);
                mQRImage.setVisibility(View.GONE);
                break;
            }
            case W_ERROR:
                doUnbindService();
                return;
            }
        }

    };
    public void setLogoBmp(Bitmap bmp) {
        mLogoBmp = bmp;
    }
}
