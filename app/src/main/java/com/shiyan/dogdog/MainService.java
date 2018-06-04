package com.shiyan.dogdog;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.shiyan.nets.GlobalSocket;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class MainService extends Service {

    boolean isFinish=false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(() -> {
            String cmd = "";
            char once_char[]=new char[200];
            NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            try {
                while(!isFinish){
                    while (true){
                        if (GlobalSocket.br.read(once_char)!=-1){
                            cmd+=String.valueOf(once_char);
                        } else {
                            sleep(300);
                            break;
                        }
                    }
                    Log.e("收到消息",cmd);
                    String[] ss=cmd.split("/");
                    Notification notification=new NotificationCompat.Builder(MainService.this)
                            .setContentTitle(ss[0])
                            .setContentText(ss[1])
                            .setWhen(System.currentTimeMillis())
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round))
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .build();
                    manager.notify(1,notification);
                    sendBroadcast(new Intent().setAction("new_message").putExtra("new",cmd));
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Notification notification= null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            notification = new Notification.Builder(MainService.this.getApplicationContext())
//                    .setContentTitle(getApplication().getPackageName()+"正在运行")
//                    .setContentText("后台正在运行")
//                    .setWhen(System.currentTimeMillis())
//                    .setChannelId("2")
//                    .build();
//        }
//        startForeground(110,notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isFinish=true;
        GlobalSocket.ps.close();
        try {
            GlobalSocket.br.close();
            GlobalSocket.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

