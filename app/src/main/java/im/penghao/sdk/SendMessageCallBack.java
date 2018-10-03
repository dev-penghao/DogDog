package im.penghao.sdk;

public interface SendMessageCallBack {
    void onSuccess(Message msg);
    void onFailed(Message msg,String errorCode);
}