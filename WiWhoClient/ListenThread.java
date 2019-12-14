package com.example.myapplication;

import android.os.Looper;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;

public class ListenThread extends Thread {
    InputStream inputStream = null;
    int i;
    byte[] bytes = new byte[1],bytes1 = new byte[1],bytes0 = new byte[1];
    SensorService sensorService = null;
    Boolean[] Colorflag = null;
    Boolean[] connOrNot = null;
    Button buttonWithColor = null;

    public ListenThread(InputStream inputStream, SensorService sensorService, Boolean[] colorflag,
                        Button buttonWithColor,Boolean[] connOrNot) {
        this.inputStream = inputStream;
        this.sensorService = sensorService;
        this.Colorflag = colorflag;
        this.buttonWithColor = buttonWithColor;
        this.connOrNot = connOrNot;
    }

    public void run() {
        super.run();
        bytes1[0] = (byte)'1';
        bytes0[0] = (byte)'0';
        Looper.prepare();
        try {
            while ((i = inputStream.read(bytes)) != -1) {
                System.out.println("开始test");
                System.out.println( new String(bytes1) );
                System.out.println( new String(bytes) );
                if(new String(bytes).equals(new String(bytes1) ) )
                {
                    System.out.println("开始发送");
                    this.sensorService.startsendData();
                    this.Colorflag[0] = true;
                    this.buttonWithColor.setActivated(Colorflag[0]);
                }
                else if(new String(bytes).equals(new String(bytes0) ) )
                {
                    sensorService.stopsendData();
                    this.Colorflag[0] = false;
                    this.buttonWithColor.setActivated(Colorflag[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(-1 == i){
            this.connOrNot[0] = false;
        }
        Looper.loop();
    }
}
