package im.penghao.sdk;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class IMService {

    // All of theme are package-private
    boolean isFinish=false;
    Socket mSocket;
    MyInputStream mMis;
    PrintStream mPs;
    Context context;
    List<OnReceiveMessageListener> onReceiveMessageListenerList=new ArrayList<>();

    private final int MESSAGE_BYTE_MAX_LENGTH=1024;


    public IMService(Context context){
        this.context=context;
    }

    public synchronized void login(String num,String passwd,LoginCallBack callBack){
        new Thread(() -> {
            String result=null;
            try {
                mSocket=new Socket(IMClient.SERVER_HOST,38380);
                mPs=new PrintStream(mSocket.getOutputStream());
                mMis=new MyInputStream(mSocket.getInputStream());
                String request="sign_in/"+num+"/"+passwd+"/";
                mPs.println(request);
                result=mMis.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if ("OK".equals(result)) {
                Intent intent=new Intent(context,MessageLooper.class);
                context.startService(intent);
                Log.d("NOTE:","Service is RUNGING now!");
                callBack.onSuccess();
            } else {
                try {
                    mPs.close();
                    mMis.close();
                    mSocket.close();
                    mPs=null;
                    mMis=null;
                    mSocket=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                callBack.onFailed(result);
            }
        }).start();
    }

    public void logout(){
        try {
            mMis.close();
            mPs.close();
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent=new Intent(context,MessageLooper.class);
        context.stopService(intent);
    }

    public void sendMessage(im.penghao.sdk.Message msg, SendMessageCallBack callBack){
        // 开始发送消息
        new Thread(() -> {
            byte[] bytes=msg.toString().getBytes(Charset.forName("UTF-8"));
            // 消息过长则不予发送
            if (bytes.length>MESSAGE_BYTE_MAX_LENGTH){
                callBack.onFailed(msg,"This message is too long");
                return;
            }
            try {
                mPs.write(bytes);
                mPs.write((new byte[1])[0]=0);
                // After sending is success
                // 将本条消息存入数据库中
                saveMessage(msg);
                callBack.onSuccess(msg);
            } catch (IOException e) {
                e.printStackTrace();
                callBack.onFailed(msg,e.toString());
            }
        }).start();
    }

    public synchronized void setOnMessageReceiveListener(OnReceiveMessageListener listener){
        onReceiveMessageListenerList.add(listener);
    }

    public synchronized boolean saveMessage(im.penghao.sdk.Message msg){
        if (msg==null){
            return false;
        }
        String witch;
        if (IMClient.ME.equals(msg.getFrom())){
            witch=msg.getTo();
        } else {
            witch=msg.getFrom();
        }
        MyDatabaseHelper myDBHelper=new MyDatabaseHelper(context,"MsgLibs.db",null,1,witch);
        SQLiteDatabase db=myDBHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("type",msg.getType());
        values.put("msg_when",msg.getWhen());
        values.put("msg_from",msg.getFrom());
        values.put("msg_to",msg.getTo());
        values.put("content",msg.getContent());
        try{
            db.insert(witch,null,values);
            values.clear();
            db.close();
            return true;
        } catch (SQLException e){
            e.printStackTrace();
            values.clear();
            db.close();
            return false;
        }
    }

    public String getFriendList(String num){
        Request request = new Request();
        request.putType("get_friend_list");
        request.putContent(num);
        return request.sendRequest();
    }

    public String register(String name,String num,String passwd){
        Request request=new Request();
        request.putType("sign_up");
        request.putContent(name);
        request.putContent(num);
        request.putContent(passwd);
        return request.sendRequest();
    }

    public String searchUser(String userName){
        Request request=new Request();
        request.putType("search_user");
        request.putContent(userName);
        return request.sendRequest();
    }

    public String beFriend(String meNum,String objNum){
        Request request=new Request();
        request.putType("add_friend");
        request.putContent(meNum);
        request.putContent(objNum);
        return request.sendRequest();
    }

    protected void doNothing(){}
}

