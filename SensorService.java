package com.example.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import static com.example.myapplication.ConnectThread.handler1;
import static com.example.myapplication.MainActivity.mhandler;

public class SensorService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private boolean IsSendData = false;
    float [] values ;
    int sensorType;
    StringBuilder stringBuilderX = null,stringBuilderY = null,stringBuilderZ = null;
    // Binder内部类
    public class myBinder extends Binder{
        public SensorService getService(){
            return SensorService.this;
        }
    }

    public SensorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
    }

    public void switchSendData(){
        IsSendData = !IsSendData;
    }

    public void startsendData() { IsSendData = true; }

    public void stopsendData() { IsSendData = false; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        values = event.values;
        sensorType = event.sensor.getType();
        if(sensorType == Sensor.TYPE_ACCELEROMETER ){
            stringBuilderX = new StringBuilder();
            stringBuilderY = new StringBuilder();
            stringBuilderZ = new StringBuilder();
            stringBuilderX.append(values[0]);
            stringBuilderY.append(values[1]);
            stringBuilderZ.append(values[2]);

            Message mmsMessage = new Message();
            Bundle mbundle = new Bundle();
            mbundle.putString("valX",String.valueOf(values[0]) );
            mbundle.putString("valY",String.valueOf(values[1]) );
            mbundle.putString("valZ",String.valueOf(values[2]) );
            mmsMessage.setData(mbundle);
            mhandler.sendMessage(mmsMessage);

            if(IsSendData) {
                Message msMessage = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("valX",String.valueOf(values[0]) );
                bundle.putString("valY",String.valueOf(values[1]) );
                bundle.putString("valZ",String.valueOf(values[2]) );
                msMessage.setData(bundle);
                handler1.sendMessage(msMessage);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new myBinder();
    }
}
