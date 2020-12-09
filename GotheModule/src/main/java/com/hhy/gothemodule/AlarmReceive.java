package com.hhy.gothemodule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceive extends BroadcastReceiver {
    private Context context;
    private Intent intent;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
        //循环启动Service
        Intent i = new Intent(context, AreaInspectService.class);
        context.startService(i);
    }
}