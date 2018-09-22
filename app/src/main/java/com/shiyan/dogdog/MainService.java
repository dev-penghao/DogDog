package com.shiyan.dogdog;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.shiyan.tools.GlobalSocket;
import com.shiyan.tools.Message;
import com.shiyan.tools.MyDatabaseHelper;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

import static com.shiyan.tools.Me.msgNow;

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
            String msgByString;
            while (!isFinish){
                try {
                    msgByString=GlobalSocket.mis.readString();
                    msgNow=new Message(msgByString);
                    Log.d("收到消息:",msgNow.toString());

                    NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    Notification notification=new NotificationCompat.Builder(MainService.this)
                            .setContentTitle(msgNow.getFrom())
                            .setContentText(msgNow.getTextContent())
                            .setWhen(System.currentTimeMillis())
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round))
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .build();
                    manager.notify(1,notification);
                    sendBroadcast(new Intent().setAction("new_message"));
                    MyDatabaseHelper myDBHelper=new MyDatabaseHelper(this,"MsgLibs.db",null,1,msgNow.getFrom());
                    SQLiteDatabase db=myDBHelper.getWritableDatabase();
                    ContentValues values=new ContentValues();
                    values.put("msg_from",msgNow.getFrom());
                    values.put("msg_to",msgNow.getTo());
                    values.put("msg_when",msgNow.getWhen());
                    values.put("msgSize",msgNow.getMsgSize());
                    values.put("type",msgNow.getType());
                    values.put("textContent",msgNow.getTextContent());
                    try{
                        db.insert(msgNow.getFrom(),null,values);
                    } catch (SQLException e){
                        e.printStackTrace();
                    }
                    db.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

//            String cmd;
//            char[] once_char = new char[4000];
//            NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            try {
//                while(!isFinish){
//                    GlobalSocket.br.read(once_char);
//                    cmd=valueOf(once_char);
//                    for (int i=0;i<once_char.length;i++){
//                        once_char[i]=0;
//                    }
//                    Log.d("收到消息",cmd);
//                    String[] ss=cmd.split("/");
//                    Notification notification=new NotificationCompat.Builder(MainService.this)
//                            .setContentTitle(ss[0])
//                            .setContentText(ss[1])
//                            .setWhen(System.currentTimeMillis())
//                            .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round))
//                            .setSmallIcon(R.mipmap.ic_launcher_round)
//                            .setDefaults(NotificationCompat.DEFAULT_ALL)
//                            .build();
//                    manager.notify(1,notification);
//                    sendBroadcast(new Intent().setAction("new_message").putExtra("new",cmd));
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }).start();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        isFinish=true;
        GlobalSocket.ps.close();
        try {
            GlobalSocket.mis.close();
            GlobalSocket.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveMessage(Message msg){
        String path="/sdcard/DogDog/"+msg.getFrom();
        File outFile=new File(path);
        if (!outFile.isFile()){
            outFile.delete();
        }
        try {
            RandomAccessFile raf=new RandomAccessFile(outFile,"rw");
            raf.seek(raf.length());// 追加内容
            raf.write(msg.toString().getBytes(Charset.forName("UTF-8")));
            raf.write((new byte[1])[0]=0);
            raf.close();
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

