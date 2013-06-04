package org.tapsee.streamer;

import android.net.LocalServerSocket;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.TextView;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerThread implements Runnable {

	public ServerSocket serverSocket = null;
	public static final int SERVERPORT = 8080;
	private static String tag = "IVAN";
	String BOUNDARY = "kjsdfkhsdkjfsdkjfsd";
	
	private static Handler progressHandler = new Handler() {

		TextView serverStatus = MainActivity.getStatusView();
		
		@Override
		public void handleMessage(Message msg) {
			serverStatus.setText((String) msg.obj);
		}
	};
	

    public void run() {
    	
    	String ip_address = getLocalIpAddress();

    	Message msg = Message.obtain();
		msg.obj = ("Listening on IP: " + ip_address);
		msg.what = 0;
    	progressHandler.sendMessage(msg);
    	

        try {
			serverSocket = new ServerSocket(SERVERPORT);
			serverSocket.setSoTimeout(1000*60*60);// 1 hour timeout
			MainActivity.setSocket(serverSocket);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        while (true) {
            // LISTEN FOR INCOMING CLIENTS
            Socket clientSocket = null;
			try {
				Log.d(tag, "listening for connections..");
				clientSocket = serverSocket.accept();
				Log.d(tag, "accepted new connection, launch client thread");
				// the clientThread is in a new thread, and is passed the clientSocket and the 
				// handler that updats the UI. the handler must be got from here coz its only the
				// UI thread that can update the UI
				//Thread clientThread = new Thread(new ClientThread(clientSocket, progressHandler));
				//clientThread.start();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	            //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(theClient.getOutputStream()));
	            
				String inLine = in.readLine();
				Log.i(tag, inLine);
				
				String[] tokens = inLine.split(" ");
				String url = tokens[1];

				DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
				
				if (url.equalsIgnoreCase("/video")) {
				
				
		            
	
		            out.write(("HTTP/1.0 200 OK\r\n" +
	                        "Server: iRecon\r\n" +
	                        "Connection: close\r\n" +
	                        "Max-Age: 0\r\n" +
	                        "Expires: 0\r\n" +
	                        "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
	                        "Pragma: no-cache\r\n" + 
	                        "Content-Type: multipart/x-mixed-replace; " +
	                        "boundary=" + BOUNDARY + "\r\n" +
	                        "\r\n" +
	                        "--" + BOUNDARY + "\r\n").getBytes());
	
		            out.flush();
		            
		            MainActivity.addVideoOutputStream(out);
		            
				} else if (url.equalsIgnoreCase("/audio")) {
					
					out.write(("HTTP/1.0 200 OK\r\n" +
	                        "Server: iRecon\r\n" +
	                        "Connection: keep-alive\r\n" +
	                        "Max-Age: 0\r\n" +
	                        "Expires: 0\r\n" +
	                        "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
	                        "Pragma: no-cache\r\n" + 
	                        "Content-Type: audio/wav" +
	                        "\r\n\r\n").getBytes());
					
					
					short nChannels = 1;
					short bSamples = 16;
					
					
					
					out.writeBytes("RIFF");
					out.writeInt(0); // Final file size not known yet, write 0 
					out.writeBytes("WAVE");
					out.writeBytes("fmt ");
					out.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
					out.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
					out.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
					out.writeInt(Integer.reverseBytes(MainActivity.AUDIO_SRATE)); // Sample rate
                    out.writeInt(Integer.reverseBytes(MainActivity.AUDIO_SRATE*bSamples*nChannels/8)); // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
                    out.writeShort(Short.reverseBytes((short)(nChannels*bSamples/8))); // Block align, NumberOfChannels*BitsPerSample/8
                    out.writeShort(Short.reverseBytes(bSamples)); // Bits per sample
                    out.writeBytes("data");
                    out.writeInt(0);
                    
					out.flush();
					
					MainActivity.addAudioOutputStream(out);
					
				} else {
					serveFile(url, clientSocket);
				}



            } catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				break;
			}
			


        }

    }
    

    private void serveFile(String url, Socket socket) {
    	
    	Log.i(tag,"serve static file: " + url);
    	
    	DataOutputStream out;
		try {
			out = new DataOutputStream(socket.getOutputStream());
			out.write(("foobar serveFile").getBytes() );
			out.flush();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        
        		
    	
    	
    	//
    }
    
 // GETS THE IP ADDRESS OF YOUR PHONE'S NETWORK
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) { 
                    	return inetAddress.getHostAddress().toString(); 
                	}
                }
            }
        } catch (SocketException ex) {
            Log.e(tag, ex.toString());
        }
        return null;
    }
    
}




