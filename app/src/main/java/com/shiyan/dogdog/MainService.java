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
            String cmd;
            char[] once_char = new char[4000];
            NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            try {
                while(!isFinish){
                    GlobalSocket.br.read(once_char);
                    cmd=valueOf(once_char);
                    for (int i=0;i<once_char.length;i++){
                        once_char[i]=0;
                    }
                    Log.d("收到消息",cmd);
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
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

    /*
     *  Created by Penghao on 2018.06.06
     */
    // String.valueOf(char[] data)就是个坑！
    private String valueOf(char[] chars){
        StringBuilder sb=new StringBuilder();
        for (int i=0;i<chars.length;i++){
            if (chars[i]!=0){
                sb.append(chars[i]);
            } else {
                break;
            }
        }
        return sb.toString();
    }
}

