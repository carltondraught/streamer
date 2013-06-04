package org.tapsee.streamer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.Random;
import org.tapsee.streamer.NanoHTTPD;

//import teaonly.projects.droidipcam.NanoHTTPD.Response;
import android.os.Environment;
import android.util.Log;

public class HTTPServer extends NanoHTTPD{

	String tag = "IVAN";
	
	static File wwwRootFile;// = new File( Environment.getExternalStorageDirectory().getPath() + "/streamer/" ).getAbsoluteFile();
	static int wwwPort = 8080;
	String BOUNDARY = "kjsdfkhsdkjfsdkjfsd";
	//Socket mySocket;
	
	public HTTPServer(int port, String wwwRoot) throws IOException {
		
		super(port, new File(wwwRoot) );
		wwwRootFile =  new File(wwwRoot);
		
        
    }
	
	public Response serve( String uri, String method, Properties header, Properties parms, Properties files, Socket socket ) {
        Log.d(tag, method + " '" + uri + "' " );

        Log.d(tag, "server on port: " + socket.getPort());
        Log.d(tag, uri);
        
        if ( uri.equalsIgnoreCase("/video") ) {
        	
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	ByteArrayInputStream inputStream = new ByteArrayInputStream( outputStream.toByteArray());
        	DataInputStream inputStream2 = new DataInputStream(inputStream);
        	
        	//DataOutputStream out;
        	Response res = null;
			try {
//				out = new DataOutputStream(socket.getOutputStream());
//	            out.write(("HTTP/1.0 200 OK\r\n" +
//	                    "Server: Streamer\r\n" +
//	                    "Connection: close\r\n" +
//	                    "Max-Age: 0\r\n" +
//	                    "Expires: 0\r\n" +
//	                    "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
//	                    "Pragma: no-cache\r\n" + 
//	                    "Content-Type: multipart/x-mixed-replace; " +
//	                    "boundary=" + BOUNDARY + "\r\n" +
//	                    "\r\n" +
//	                    "--" + BOUNDARY + "\r\n").getBytes());
//
//				out.flush();
//				MainActivity.addOutputStream(out);
				
				
				res = new Response( HTTP_OK, "multipart/x-mixed-replace", "");
                res.addHeader( "Connection", "Keep-alive");
                res.data = inputStream2;
                res.isStreaming = true; 
                
                //MainActivity.addOutputStream(outputStream);

				
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return res;
            
//                InputStream ins;
//                ins = myRequestListen.onRequest();
//                if ( ins == null)
//                    return res;
//                
//                if ( streamingResponse == null ) {
//                    Random rnd = new Random();
//                    String etag = Integer.toHexString( rnd.nextInt() );
//
//                    res = new Response( HTTP_OK, "video/x-flv", ins);
//                    res.addHeader( "Connection", "Keep-alive");
//                    res.addHeader( "ETag", etag);
//                    res.isStreaming = true; 
//                    streamingResponse = res;
//                    Log.d("TEAONLY", "Starting streaming server");
//                }
//                return res;
//            }
        } else {
            return serveFile( uri, header, wwwRootFile, true ); 
        }
    }
	
}
