package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText IPAddrText,textAcceX,textAcceY,textAcceZ;
    private Button bt0,bt1,bt2;
    ConnectThread ct = null;
    SensorService sensorService;
    public static Handler mhandler;
    Boolean[] Colorflag = new Boolean[1];
    ListenThread lt = null;
    Object lock = new Object();
    Boolean[] connectOrNot = new Boolean[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this,SensorService.class);
        bindService(intent,conn,BIND_AUTO_CREATE);

        Colorflag[0] = false;
        connectOrNot[0] = true;

        mhandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                String valX = bundle.getString("valX");
                String valY = bundle.getString("valY");
                String valZ = bundle.getString("valZ");
                int t = msg.arg1;
                if(11 == t)
                {
                    Toast.makeText(getApplicationContext(), "服务器已断开",
                            Toast.LENGTH_SHORT).show();
                    bt2.performClick();
                }
                textAcceX.setText(valX);
                textAcceY.setText(valY);
                textAcceZ.setText(valZ);
            }
        };

        IPAddrText = findViewById(R.id.et0);
        textAcceX = findViewById(R.id.et1);
        textAcceY = findViewById(R.id.et2);
        textAcceZ = findViewById(R.id.et3);
        IPAddrText.setText("10.15.16.48");

        bt0 = findViewById(R.id.bt0);
        bt1 = findViewById(R.id.bt1);
        bt2 = findViewById(R.id.bt2);
        bt0.setEnabled(true);
        bt1.setEnabled(false);
        bt2.setEnabled(false);
        bt1.setActivated(Colorflag[0]);
        bt0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (lock){
                    try {
                        ct = new ConnectThread(lock,connectOrNot,IPAddrText.getText().toString() );
                        ct.start();
                        lock.wait();
                        if(connectOrNot[0]){
                            lt = new ListenThread(ct.getinputstreams(),sensorService,Colorflag,
                                    bt1,connectOrNot);
                            lt.start();
                            bt0.setEnabled(false);
                            bt1.setEnabled(true);
                            bt2.setEnabled(true);
                        }else{
                            Toast.makeText(getApplicationContext(), "无法连接服务器",
                                    Toast.LENGTH_SHORT).show();
                            ct.interrupt();
                            ct = null;
                            lt = null;
                        }
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connectOrNot[0] ){
                    sensorService.switchSendData();
                    Colorflag[0] = !Colorflag[0];
                    bt1.setActivated(Colorflag[0]);
                    bt2.setEnabled(true);
                }
                else {
                    Toast.makeText(getApplicationContext(), "服务器已断开",
                            Toast.LENGTH_SHORT).show();
                    bt2.performClick();
                }
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Colorflag[0]){
                    sensorService.switchSendData();
                    Colorflag[0] =  !Colorflag[0];
                    bt1.setActivated(Colorflag[0]);
                }
                bt0.setEnabled(true);
                bt1.setEnabled(false);
                bt2.setEnabled(false);
                ct.interrupt();
                ct = null;
                lt = null;
            }
        });
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sensorService = ((SensorService.myBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}
