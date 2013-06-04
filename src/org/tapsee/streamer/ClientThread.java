package org.tapsee.streamer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;

import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ClientThread implements Runnable {
	
	Socket theClient;
	Handler progressHandler;
	String tag = "IVAN";
	String BOUNDARY = "kjsdfkhsdkjfsdkjfsd";
	
	
	public ClientThread(Socket client, Handler handler) {
	       
		theClient = client;
		progressHandler = handler;
		
	}


	
    public void run() {
    	
    	Log.d(tag, "in new client thread");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(theClient.getInputStream()));
            //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(theClient.getOutputStream()));
            
            DataOutputStream out = new DataOutputStream(theClient.getOutputStream());

            out.write(("HTTP/1.0 200 OK\r\n" +
                    "Server: iRecon\r\n" +
                    "Connection: close\r\n" +
                    "Max-Age: 0\r\n" +
                    "Expires: 0\r\n" +
                    "Cache-Control: no-cache, private\r\n" + 
                    "Pragma: no-cache\r\n" + 
                    "Content-Type: multipart/x-mixed-replace; " +
                    "boundary=--" + BOUNDARY +
                    "\r\n\r\n").getBytes());

            out.flush();
//            String line = null;
//            while ((line = in.readLine()) != null) {
//                Log.d(tag, line);
//                
//                out.write(line);
//                out.flush();
//                
//                Message msg = Message.obtain();
//                msg.obj = ("received: " + line);
//		    	progressHandler.sendMessage(msg);
//		    	
//            }
            
        } catch (Exception e) {
        	Message msg = Message.obtain();
        	msg.obj = ("ERROR: connection interrupted !");
	    	progressHandler.sendMessage(msg);
	    	e.printStackTrace();
	    	
            
        }


    

    }
    
}




