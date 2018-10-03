package im.penghao.sdk;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * JSON看起来像这样:{"from":"penghao","to":"yuntao","when":0,"msgSize"=20,"type"=0}
 * 没有换行
 */
public class Message {

    private int type;
    private long when;
    private String from;
    private String to;
    private String content;

    public Message(){}

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getWhen() {
        return when;
    }

    public void setWhen(long when) {
        this.when = when;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Message(String msgByJson){
        try {
            JSONObject jsonObject=new JSONObject(msgByJson);
            type=jsonObject.getInt("type");
            when=jsonObject.getLong("when");
            from=jsonObject.getString("from");
            to=jsonObject.getString("to");
            content=jsonObject.getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("type",type);
            jsonObject.put("when",when);
            jsonObject.put("from",from);
            jsonObject.put("to",to);
            jsonObject.put("content",content);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObject.toString();
    }
}