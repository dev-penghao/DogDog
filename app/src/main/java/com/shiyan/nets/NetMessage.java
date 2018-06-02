package com.shiyan.nets;

public class NetMessage {
    private int type;// 消息类型，0代表接收的消息，1代表发送的消息
    private String message;// 消息的内容
    private String obj;// 如果是接收的消息则是发来的人，如果是发送的消息则是发送的目标
    public NetMessage(int type,String message,String obj){
        this.type=type;
        this.message=message;
        this.obj=obj;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getObj() {
        return obj;
    }
}