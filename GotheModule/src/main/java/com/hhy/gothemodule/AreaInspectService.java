package com.hhy.gothemodule;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.util.Timer;
import java.util.TimerTask;

public class AreaInspectService extends Service implements AMapLocationListener {
    private static final String CHANNEL_ONE_ID = "1";
    private static final CharSequence CHANNEL_ONE_NAME = "CHANNEL_ONE_ID";
    private static final String TAG = "123132";
    private static final int ONE_Miniute = 10*1000;
    private static final int PENDING_REQUEST=0;
    public AMapLocationClientOption mLocationOption = null;
    private AMapLocationClient mlocationClient;
    public String returnData;
    private Intent intent = new Intent("com.hhy.location.RECEIVER");
    private  AlarmManager alarmManager;
    private  Notification noti;

    @Override
    public IBinder onBind(Intent intent) {
        return (IBinder) new MsgBinder();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        Log.d(TAG, String.valueOf(aMapLocation));
        if (null != aMapLocation) {
            //解析定位结果
            returnData = GDUtils.getLocationStr(aMapLocation);
            if (returnData != null) {
                intent.putExtra("returnData", returnData);
                sendBroadcast(intent);
            }
        } else {
            Log.d(TAG, "定位失败");
        }
    }

    public class MsgBinder extends Binder {
        /**
         * 获取当前Service的实例
         * @return
         */
        public AreaInspectService getService(){
            return AreaInspectService.this;
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public void onDestroy() {
        stopVoice();
        stopForeground(true);
        stopPollingService(this, AlarmReceive.class);
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
        }
        noti = null;
        super.onDestroy();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: 9999888");
                startWork();
            }
        }).start();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long triggerAtMillis = SystemClock.elapsedRealtime()+ONE_Miniute;

        Intent i=new Intent(this, AlarmReceive.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,PENDING_REQUEST,i,PENDING_REQUEST);
        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "onStartCommand: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//  4.4
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtMillis, pendingIntent);
        }

        return super.onStartCommand(intent, flags, startId);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startWork() {
        //进行8.0的判断

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
        PendingIntent notificationIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        if (noti == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 noti = new Notification.Builder(this)
                        .setChannelId(CHANNEL_ONE_ID)
                        .setPriority(Notification.PRIORITY_MIN)
                        .setContentTitle("好货云正在运行，请勿关闭应用")
                        .setContentText("持续定位中")
                        .setContentIntent(notificationIntent)
                        .build();
                startForeground(123456,noti);
            } else {
                 noti = new Notification.Builder(this)
                        .setPriority(Notification.PRIORITY_MIN)
                        .setContentTitle("好货云正在运行，请勿关闭应用")
                        .setContentText("持续定位中")
                        .setContentIntent(notificationIntent)
                        .build();
                startForeground(123456,noti);
            }
        }


        start_mp3();
        startLocation();
    }

      //停止轮询服务
      public static void stopPollingService(Context context, Class<?> cls) {
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,PENDING_REQUEST,intent,PENDING_REQUEST);
        //取消正在执行的服务
        manager.cancel(pendingIntent);
    }

    private void startLocation() {
        mlocationClient = new AMapLocationClient(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(this);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms

        mLocationOption.setInterval(ONE_Miniute);
        mLocationOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        mlocationClient.startLocation();
    }

    //结束定位
    private void stopLocation() {
        mlocationClient.stopLocation();
    }

    private MediaPlayer mediaPlayer;

    //开始播放声音
    private void start_mp3() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.silent);//silent为无声音乐，网上搜索下载就可以
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //停止播放声音
    public  void stopVoice(){
        if(null!=mediaPlayer) {
            mediaPlayer.stop();
        }
    }
    public String getNum() {
        return returnData;
    }

}

