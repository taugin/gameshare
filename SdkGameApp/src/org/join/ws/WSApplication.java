package org.join.ws;

import org.join.ws.ui.PreferActivity;

import android.app.Application;
import android.content.Intent;

import com.chukong.sdk.Constants.Config;
import com.chukong.sdk.GlobalInit;

/**
 * @brief 应用全局
 * @author join
 */
public class WSApplication extends Application {

    private static WSApplication self;

    private Intent wsServIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        self = this;
        wsServIntent = new Intent(WSService.ACTION);
        GlobalInit init = new GlobalInit(this);
        init.init();
        if (!Config.DEV_MODE) {
            /* 全局异常崩溃处理 */
            new CrashHandler(this);
        }

        PreferActivity.restoreAll();
    }

    public static WSApplication getInstance() {
        return self;
    }

    /**
     * @brief 开启全局服务
     */
    public void startWsService() {
        startService(wsServIntent);
    }

    /**
     * @brief 停止全局服务
     */
    public void stopWsService() {
        stopService(wsServIntent);
    }
}
