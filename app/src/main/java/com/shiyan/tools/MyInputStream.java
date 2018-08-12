package com.shiyan.tools;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MyInputStream {

    private InputStream is;
    private final int BYTE_BUFFER_SIZE=1024;

    public MyInputStream(InputStream is){
        this.is=is;
    }

    // 读取一行字符串
    public String readLine() throws IOException {
        byte[] atom=new byte[1];
        ByteBuffer byteBuffer=ByteBuffer.allocate(BYTE_BUFFER_SIZE);
        while(true){// 一次读一个字节
            if (is.read(atom)==-1) return null;
            if (atom[0]=='\n'||atom[0]==0){// 如果是回车就break
                break;
            } else {
                if (byteBuffer.limit()==BYTE_BUFFER_SIZE){
                    byteBuffer.put(atom);
                } else break;
            }
        }
        int len=0;
        byte[] byteBufferArray=byteBuffer.array();
        for (int i=0;i<byteBufferArray.length;i++){
            if (byteBufferArray[i]==0){
                len=i;
                break;
            }
        }
        return new String(byteBuffer.array(), 0, len, Charset.forName("UTF-8"));
    }

    // 读取一个字符串
    public String readString() throws IOException {
        byte[] atom=new byte[1];
        ByteBuffer byteBuffer=ByteBuffer.allocate(BYTE_BUFFER_SIZE);
        while(true){
            if (is.read(atom)==-1) return null;
            if (atom[0]==0){// 如果是0就break
                break;
            } else {
                if (byteBuffer.limit()==BYTE_BUFFER_SIZE){
                    byteBuffer.put(atom);
                } else break;
            }
        }
        int len=0;
        byte[] byteBufferArray=byteBuffer.array();
        for (int i=0;i<byteBufferArray.length;i++){
            if (byteBufferArray[i]==0){
                len=i;
                break;
            }
        }
        return new String(byteBuffer.array(), 0, len, Charset.forName("UTF-8"));
    }


    // byte[]转为char[],将两个byte的类型合并成一个char
    private char[] bytesToChars(byte[] bytes){
        if (bytes.length%2!=0) return null;
        char[] chars=new char[bytes.length/2];
        for (int i=0;i<chars.length;i++){
            chars[i]=bytesToChar(bytes[i*2],bytes[i*2+1]);
        }
        return chars;
    }

    private char bytesToChar(byte height,byte low){
        char cha;
        cha= (char) height;
        cha= (char) (cha << 8);
        cha+=low;
        return cha;
    }

    private char bytesToChar(byte[] bytes){
        char cha;
        cha= (char) bytes[0];
        cha= (char) (cha << 8);
        cha+=bytes[1];
        return cha;
    }

    private byte[] charToBytes(char cha){
        byte[] bytes=new byte[2];
        bytes[0]=(byte) (cha>>8);// 高8位
        bytes[1]= (byte) cha;// 低8位
        return bytes;
    }

    public void close() throws IOException {
        is.close();
    }
}
