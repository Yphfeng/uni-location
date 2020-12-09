package com.hhy.gothemodule;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.os.IBinder;

import android.view.View;

public class MainActivity extends Activity {
    public static final  Integer CODE_FOR_WRITE_PERMISSION = 1;
    private static final String TAG = "123132";
    public AMapLocationClientOption mLocationOption = null;
    private AMapLocationClient mlocationClient;
    public Integer num = 0;
    private AreaInspectService areaInspectService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, AreaInspectService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
//        initLocation();
    }

    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            areaInspectService = ((AreaInspectService.MsgBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public void location(View view) {
        /**
         android6.0 以上要动态申请权限
         */
        //使用兼容库就无需判断系统版本


//        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION)
//                .subscribe(new Observer<Boolean>() {
//                    @Override
//                    public void onChanged(Boolean aBoolean) {
//
//                    }
//
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onNext(Boolean aBoolean) {
//                        mlocationClient.startLocation();
//                    }
//                });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mlocationClient.stopLocation();
    }


}