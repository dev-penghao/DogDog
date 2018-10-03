package im.penghao.sdk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.shiyan.dogdog.R;

import java.io.IOException;

public class MessageLooper extends Service {

    String msgByString;
    im.penghao.sdk.Message msg;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("NOTE:","MessageLooper is RUNNING now!");
        new Thread(() -> {
            while (!IMClient.service.isFinish){
                try {
                    msgByString=IMClient.service.mMis.readString();
                    msg = new im.penghao.sdk.Message(msgByString);
                    Log.d("收到消息:",msg.toString());
                    showNotification(msg);
                    IMClient.service.saveMessage(msg);
                    // Notify all listener that we had received a new message
                    for (OnReceiveMessageListener listener:IMClient.service.onReceiveMessageListenerList){
                        listener.onReceive(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showNotification(im.penghao.sdk.Message msg){
        NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final String CHANNEL_ID = "channel_id_1";
        final String CHANNEL_NAME = "channel_name_1";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //只在Android O之上需要渠道
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            //如果这里用IMPORTANCE_NOENE就需要在系统的设置里面开启渠道，
            //通知才能正常弹出
            if (manager != null) {
                manager.createNotificationChannel(notificationChannel);
            }
        }

        Notification notification=new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle(msg.getFrom())
                .setContentText(msg.getContent())
                .setWhen(System.currentTimeMillis())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();
        if (manager != null) {
            manager.notify(1,notification);
        }
    }
}
