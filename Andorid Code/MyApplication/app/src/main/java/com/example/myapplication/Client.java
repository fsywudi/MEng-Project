package com.example.myapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException{
        Socket s  = new Socket("192.168.1.82", 5000);
        PrintWriter pw = new PrintWriter(s.getOutputStream());
        pw.println("This is a message from client!");
        pw.flush();

        InputStreamReader ir = new InputStreamReader(s.getInputStream());
        BufferedReader br = new BufferedReader(ir);

        String str = br.readLine();
        System.out.println("This is a message from server: " + str);

    }
}
