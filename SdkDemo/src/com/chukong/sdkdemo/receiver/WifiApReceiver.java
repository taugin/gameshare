package com.chukong.sdkdemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.chukong.sdkdemo.service.ApWebAuthService;

public class WifiApReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        Log.d("taugin", "intent = " + intent.getAction());
        if (ApWebAuthService.WIFI_AP_STATE_CHANGED_ACTION.equals(intent
                .getAction())) {
            Intent service = new Intent(
                    ApWebAuthService.WIFI_AP_STATE_CHANGED_ACTION);
            int state = intent.getIntExtra(
                    ApWebAuthService.EXTRA_WIFI_AP_STATE, -1);
            service.putExtra(ApWebAuthService.EXTRA_WIFI_AP_STATE, state);
            context.startService(service);
        }
    }

}
