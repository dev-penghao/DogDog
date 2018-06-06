package com.shiyan.nets;

public class NetMessage {

    private int type;// 消息类型，0代表接收的消息，1代表发送的消息
    private String obj;// 如果是接收的消息则是发来的人，如果是发送的消息则是发送的目标
//    private long when;// 消息被创建时的时间
    private String message;// 消息的内容

    public NetMessage(int type,String obj,String message){
        this.type=type;
        this.obj=obj;
//        this.when=when;
        this.message=message;
    }

    public int getType() {
        return type;
    }

    public String getObj() {
        return obj;
    }

//    public long getWhen(){
//        return when;
//    }

    public String getMessage() {
        return message;
    }

}