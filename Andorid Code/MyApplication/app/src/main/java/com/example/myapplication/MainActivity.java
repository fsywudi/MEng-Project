package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.*;
import java.net.*;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String ipAddress = "192.168.0.103";

        String port = "80";

        MessageSender messageSender = new MessageSender(ipAddress, port);

        Button startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText e1 = (EditText) findViewById(R.id.editTextTextPersonName);
                //TextView e2 = (TextView) findViewById(R.id.textViewMain1);

                //String messageBack = messageSender.send(e1.getText().toString());
                //e2.setText(messageBack);

                Intent startIntent = new Intent(getApplicationContext(), SecondActivity.class);
                startIntent.putExtra("com.example.myapplication", ipAddress);
                startActivity(startIntent);
            }

        });
        TextView tv = (TextView) findViewById(R.id.textView3);
        Button testBtn = (Button)findViewById(R.id.testButton);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageBack = MessageSender.send("http://123123123");

                tv.post(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(messageBack);
                    }
                });
            }
        });
    }
}
