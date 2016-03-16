package com.chukong.sdk.serv.req.objs;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;

public class ApkInfo {
    public String  apkLabel;
    public String  apkName;
    public String  apkDisName;
    public String  packageName;
    public long    downloadTime;

    public String toString() {
        String str = "";
        str += "apkDisName   : " + apkDisName + "\n";
        str += "packageName  : " + packageName + "\n";
        str += "apkName      : " + apkName + "\n";
        str += "downloadTime : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(downloadTime)) + "\n";
        return str;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static ApkInfo fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ApkInfo.class);
    }
}
