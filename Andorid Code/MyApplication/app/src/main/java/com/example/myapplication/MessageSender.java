package com.example.myapplication;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageSender{
    //Socket s;
    //PrintWriter pw;
    Socket constantSocket;
    PrintWriter constantPw;

    static String ipAddress;
    static String port;
    private static MessageSender messageSender;

    public MessageSender(String ip, String pt){
        this.ipAddress = ip;
        this.port = pt;
        setMessageSender(this);
        try {
            constantSocket = new Socket(ipAddress, Integer.parseInt(port));
            constantPw = new PrintWriter(constantSocket.getOutputStream());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static MessageSender getinstance() {
        return messageSender;
    }

    public void setMessageSender(MessageSender messageSender) {
        MessageSender.messageSender = messageSender;
    }

    public static String send(int message){
        String messageBack = "None";
        try {
            Socket s = new Socket(ipAddress, Integer.parseInt(port));
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            pw.println(message);
            pw.flush();
            InputStreamReader ir = new InputStreamReader(s.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            messageBack =  br.readLine();
            /*
            s.close();
            pw.close();

             */
        }catch (IOException e){
            e.printStackTrace();
        }
        return messageBack;
    }

    public static String send(String message){
        String messageBack = "None";
        try {
            Socket s = new Socket(ipAddress, Integer.parseInt(port));
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            pw.println(message + "\n");
            pw.flush();
            InputStreamReader ir = new InputStreamReader(s.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            messageBack =  br.readLine();
        }catch (IOException e){
            e.printStackTrace();
        }
        return messageBack;
    }


    public void sendWithNoReply(int message){
        MessageTask messageTask = new MessageTask(ipAddress, port, message);
        new Thread(messageTask).start();
    }


    public void disConnect() throws IOException {
        // s.close();
        // pw.close();
    }
}
class MessageTask implements Runnable{
    String ipAddress, port;
    int message;
    public MessageTask(String ip, String pt, int msg){
        ipAddress = ip;
        port = pt;
        message = msg;
    }
    @Override
    public void run() {
        try {
            Socket s = new Socket(ipAddress, Integer.parseInt(port));
            s.setTcpNoDelay(true);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            pw.println(message);
            pw.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
