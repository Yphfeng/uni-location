package com.hhy.gothemodule;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.common.UniModule;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import androidx.core.app.ActivityCompat;

public class GotheModule extends UniModule  {
    private AreaInspectService areaInspectService;
    public Integer num = 1;
    public String returnData;
    private static final String TAG = "aaa";
    private static final int BAIDU_READ_PHONE_STATE = 100;//定位权限请求
    private static final int PRIVATE_CODE = 1315;//开启GPS权限
    private LocationManager lm;
    private String[] LOCATIONGPS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,};

    //开始定位
    @UniJSMethod(uiThread = false)
    public void startLocation(JSONObject options, UniJSCallback callback) {
        Log.e(TAG, "testAsyncFunc--"+options);
        //注册动态广播
        //动态注册广播接收器
        MsgReceiver msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.hhy.location.RECEIVER");
        mUniSDKInstance.getContext().registerReceiver(msgReceiver, intentFilter);

        if(callback != null) {
            if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                Intent intent = new Intent(mUniSDKInstance.getContext(), AreaInspectService.class);
                mUniSDKInstance.getContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
                mUniSDKInstance.getContext().startService(intent);
                JSONObject data = new JSONObject();
                data.put("code", "success");
                callback.invoke(data);
            } else {
                JSONObject data = new JSONObject();
                data.put("code", "fail");
                callback.invoke(data);
            }
        }
    }


    //开始打卡
    @UniJSMethod(uiThread = false)
    public void startOneLocation(JSONObject options, UniJSCallback callback) {
        Log.e(TAG, "testAsyncFunc--"+options);
        //注册动态广播
        //动态注册广播接收器
        MsgReceiver msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.uniplugin_location.RECEIVER");
        mUniSDKInstance.getContext().registerReceiver(msgReceiver, intentFilter);

        if(callback != null) {
            if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
                Intent intent = new Intent(mUniSDKInstance.getContext(), AreaInspectService.class);
                mUniSDKInstance.getContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
                mUniSDKInstance.getContext().startService(intent);
                JSONObject data = new JSONObject();
                data.put("code", "success");
                callback.invoke(data);
            } else {
                JSONObject data = new JSONObject();
                data.put("code", "fail");
                callback.invoke(data);
            }
        }
    }


    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            areaInspectService = ((AreaInspectService.MsgBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    /**
     * 广播接收器
     * @author len
     *
     */
    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //拿到进度，传递值
            String returnData = intent.getStringExtra("returnData");
            Map<String,Object> params=new HashMap<>();
            params.put("locationData",returnData);
            mUniSDKInstance.fireGlobalEventCallback("locationData", params);

        }

    }

    //android6。0之后判断是否有定位权限
    @UniJSMethod(uiThread = false)
    public void showGPSContacts(UniJSCallback callback) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
            if (ActivityCompat.checkSelfPermission(mUniSDKInstance.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PERMISSION_GRANTED) {// 没有权限，申请权限。
                ActivityCompat.requestPermissions(((Activity)mUniSDKInstance.getContext()), LOCATIONGPS, BAIDU_READ_PHONE_STATE);
            } else {
//                    initLocationOption();//有权限，进行相应的处理
                JSONObject data = new JSONObject();
                data.put("code", "1");
                callback.invoke(data);
            }

        } else {
//                initLocationOption();//有权限，进行相应的处理
            JSONObject data = new JSONObject();
            data.put("code", "1");
            callback.invoke(data);
        }

    }

    //开启定位权限
    @UniJSMethod(uiThread = false)
    public  void startLocationPower(UniJSCallback callback) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        ((Activity)mUniSDKInstance.getContext()).startActivityForResult(intent, PRIVATE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BAIDU_READ_PHONE_STATE) {

        } else {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PRIVATE_CODE) {
//            Log.e("TestModule", "原生页面返回----"+data.getStringExtra("respond"));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //判断是否已加入白名单
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) mUniSDKInstance.getContext().getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isIgnoring = powerManager.isIgnoringBatteryOptimizations(mUniSDKInstance.getContext().getPackageName());
            }
        }
        return isIgnoring;
    }
    //申请加入系统白名单，退出省电模式
    @UniJSMethod(uiThread = false)
    public void requestIgnoreBatteryOptimizations(UniJSCallback callback) {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + mUniSDKInstance.getContext().getPackageName()));
            mUniSDKInstance.getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //判断是否加入了白名单
    @UniJSMethod(uiThread = false)
    public void isIgnoringBattery(UniJSCallback callback) {
        Boolean isIgnoring = isIgnoringBatteryOptimizations();
        if (isIgnoring) {
            JSONObject data = new JSONObject();
            data.put("code", "1");
            callback.invoke(data);
        } else {
            JSONObject data = new JSONObject();
            data.put("code", "0");
            callback.invoke(data);
        }
    }
    //关闭定位服务
    @UniJSMethod(uiThread = false)
    public  void locationComplete(UniJSCallback callback) {
      try {
          Intent intent = new Intent(mUniSDKInstance.getContext(), AreaInspectService.class);
          if (areaInspectService != null) {
              mUniSDKInstance.getContext().stopService(intent);
              mUniSDKInstance.getContext().unbindService(conn);
              callback.invoke(1);
          } else {
              callback.invoke(1);
          }
      } catch (Exception error) {

      }
    }

    //华为跳转自启动管理
    @UniJSMethod(uiThread = false)
    public  void  gohuawei(UniJSCallback callback) {
        ComponentName componentName = null;
        int sdkVersion = Build.VERSION.SDK_INT;
        try {
            callback.invoke(1);
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //跳自启动管理
            if (sdkVersion >= 28){//9:已测试
                componentName = ComponentName.unflattenFromString("com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity");//跳自启动管理
            }else if (sdkVersion >= 26){//8：已测试
                componentName = ComponentName.unflattenFromString("com.huawei.systemmanager/.appcontrol.activity.StartupAppControlActivity");
            }else if (sdkVersion >= 23){//7.6：已测试
                componentName = ComponentName.unflattenFromString("com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity");
            }else if (sdkVersion >= 21){//5
                componentName = ComponentName.unflattenFromString("com.huawei.systemmanager/com.huawei.permissionmanager.ui.MainActivity");
            }
            //componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity");//锁屏清理
            intent.setComponent(componentName);
            mUniSDKInstance.getContext().startActivity(intent);
        }catch (Exception e){
            //跳转失败
            callback.invoke(0);
        }
    }
}

