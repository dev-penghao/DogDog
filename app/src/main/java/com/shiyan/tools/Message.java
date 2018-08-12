package com.shiyan.tools;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * JSON看起来像这样:{"from":"penghao","to":"yuntao","when":0,"msgSize"=20,"type"=0}
 * 没有换行
 */
public class Message {

    private String from;
    private String to;
    private long when;
    private long msgSize;
    private int type;

    private String textContent;

    public Message(){

    }

    public Message(String msgByString){
        try {
            JSONObject jsonObject=new JSONObject(msgByString);
            from=   jsonObject.getString("from");
            to=     jsonObject.getString("to");
            when=   jsonObject.getLong("when");
            msgSize=jsonObject.getLong("msgSize");
            type=   jsonObject.getInt("type");
            textContent=jsonObject.getString("textContent");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public long getWhen() {
        return when;
    }

    public void setWhen(long when) {
        this.when = when;
    }

    public long getMsgSize() {
        return msgSize;
    }

    public void setMsgSize(long msgSize) {
        this.msgSize = msgSize;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    @Override
    public String toString() {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("from",from);
            jsonObject.put("to",to);
            jsonObject.put("when",when);
            jsonObject.put("msgSize",msgSize);
            jsonObject.put("type",type);
            jsonObject.put("textContent",textContent);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject.toString();
    }
}