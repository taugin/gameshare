package com.chukong.sdkdemo;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class ConnInfosActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private class ConnInfosAdapter extends ArrayAdapter<ConnInfo> {

        public ConnInfosAdapter(Context context, int resource,
                List<ConnInfo> objects) {
            super(context, resource, objects);
        }
    }
    
    private class ConnInfo{
        public String ipAddr;
        public String mac;
    }
}
