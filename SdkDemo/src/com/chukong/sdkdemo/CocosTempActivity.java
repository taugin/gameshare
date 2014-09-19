package com.chukong.sdkdemo;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

public class CocosTempActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String activityName = getCurrentMainEnter();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, activityName));
        startActivity(intent);
        finish();
    }

    private String getCurrentMainEnter() {
        PackageManager pm = getPackageManager();
        String currentPackageName = getPackageName();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = pm.queryIntentActivities(intent,
                PackageManager.GET_INTENT_FILTERS);
        for (ResolveInfo info : list) {
            if (info.activityInfo.packageName.equals(currentPackageName)) {
                return info.activityInfo.name;
            }
        }
        return null;
    }

}
