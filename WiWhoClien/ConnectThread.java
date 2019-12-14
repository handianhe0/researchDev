package com.example.myapplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;

import static com.example.myapplication.MainActivity.mhandler;

public class ConnectThread extends Thread{
    Socket sock = null;
    OutputStream outputStream = null;
    InputStream inputStream = null;
    public static Handler handler1;
    Object lock;
    Boolean[] ConOrN = null;
    String IPAddress;

    public ConnectThread(Object lock, Boolean[] ConnectOrNot, String IPaddr) {
        this.lock = lock;
        this.ConOrN = ConnectOrNot;
        this.IPAddress = IPaddr;
    }

    public void run(){
        super.run();
        synchronized (this.lock){
            try{
                sock = new Socket();
                SocketAddress sockAddress = new InetSocketAddress(this.IPAddress,6800);
                sock.connect(sockAddress,300);
                this.ConOrN[0] = true;
            }catch (UnknownHostException e){
                e.printStackTrace();
                this.ConOrN[0] = false;
                this.lock.notify();
            }catch (ConnectException e){
                e.printStackTrace();
                this.ConOrN[0] = false;
                this.lock.notify();
            }catch (IOException e) {
                e.printStackTrace();
                this.ConOrN[0] = false;
                this.lock.notify();
            }

            try{
                inputStream = sock.getInputStream();
            }catch (IOException e) {
                e.printStackTrace();
            }

            try{
                outputStream = sock.getOutputStream();
            }catch (IOException e) {
                e.printStackTrace();
            }

            this.lock.notify();
        }

        Looper.prepare();
        handler1 = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                String valX = bundle.getString("valX");
                String valY = bundle.getString("valY");
                String valZ = bundle.getString("valZ");
                try {
                    outputStream.write(valX.getBytes());
                    outputStream.write(" ".getBytes());
                    outputStream.write(valY.getBytes());
                    outputStream.write(" ".getBytes());
                    outputStream.write(valZ.getBytes());
                    outputStream.write(" ".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    Message msgToSend = Message.obtain();
                    msgToSend.arg1 = 11;
                    mhandler.sendMessage(msgToSend);
                }
            }
        };
        Looper.loop();
    }

    public InputStream getinputstreams(){
        return inputStream;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        try {
            sock.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
