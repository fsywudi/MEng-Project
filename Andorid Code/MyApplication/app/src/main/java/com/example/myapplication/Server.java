package com.example.myapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        int i = 0;
        ServerSocket ss = new ServerSocket(5000);

        while (true) {
            Socket s = ss.accept();
            if (i == 0)
                System.out.println("Client conencted!");
            InputStreamReader ir = new InputStreamReader(s.getInputStream());
            BufferedReader br = new BufferedReader(ir);
            String message;
            message = br.readLine();
            System.out.println("Client " + String.valueOf(i) + "th message:"+ message);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            pw.println("Got it!" + String.valueOf(i) + "th message received");
            pw.flush();
            br.close();
            pw.close();
            i ++;
        }
    }
}
