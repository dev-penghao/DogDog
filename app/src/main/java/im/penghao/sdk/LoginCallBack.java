package im.penghao.sdk;

public interface LoginCallBack {
    void onSuccess();
    void onFailed(String errorCode);
}