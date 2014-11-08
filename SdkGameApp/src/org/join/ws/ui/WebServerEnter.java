package org.join.ws.ui;

import org.join.web.serv.R;
import org.join.ws.WSApplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WebServerEnter extends Activity implements OnClickListener {

    private Button mWebServerNoHotpot;
    private Button mWebServerWithHotpot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webserver_enter);
        WSApplication.getInstance().startWsService();
        mWebServerNoHotpot = (Button) findViewById(R.id.webserver_no_hotpot);
        mWebServerNoHotpot.setOnClickListener(this);
        mWebServerWithHotpot = (Button) findViewById(R.id.webserver_with_hotpot);
        mWebServerWithHotpot.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch(v.getId()) {
        case R.id.webserver_no_hotpot:
            intent = new Intent(this, WebServerNoHotpot.class);
            startActivity(intent);
            break;
        case R.id.webserver_with_hotpot:
            intent = new Intent(this, WebServerWithHotpot.class);
            startActivity(intent);
            break;
        default:
            break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        WSApplication.getInstance().stopWsService();
    }

    
}
