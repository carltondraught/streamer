package org.tapsee.streamer;

import android.media.AudioRecord;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Queue;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class AudioThread implements Runnable {

	
	private static String tag = "IVAN";
	String BOUNDARY = "kjsdfkhsdkjfsdkjfsd";
	int iBytesRead;
	ArrayList<DataOutputStream> audioStreamsList;
	
	private static Handler progressHandler = new Handler() {

		TextView serverStatus = MainActivity.getStatusView();
		
		@Override
		public void handleMessage(Message msg) {
			serverStatus.setText((String) msg.obj);
		}
	};
	

    public void run() {
    	
    	
    	
    	AudioRecord audioRecorder = MainActivity.getAudioRecorder();
    	int buffsize = AudioRecord.getMinBufferSize(
		        			audioRecorder.getSampleRate(),
		        			audioRecorder.getChannelConfiguration(),
		        			audioRecorder.getAudioFormat()
	        		   );
    	
    	buffsize = MainActivity.AUDIO_BUFFSIZE;
    	
    	//ByteBuffer bData = ByteBuffer.allocate(buffsize);
    	//ByteBuffer bData = ByteBuffer.allocate((int) Math.pow(2, 20));// 1 megabyte
    	
    	ByteBuffer bData = ByteBuffer.allocate(buffsize*1000);// x buffers 
    	
    	//bData.order(ByteOrder.BIG_ENDIAN);
    	//byte[] bbarray = new byte[bData.remaining()];
    	
    	Log.i(tag, "initial remaining: " + bData.remaining());
    	Log.i(tag, "buff size: " + buffsize);
    	
        int numBytesRead;
        //iBytesRead = 0;
        //int ShortsReadPerCycle = 1024;
        
    	while (MainActivity.audioIsRecording) {
    		

            //bData.get(bbarray);
    		byte[] bbarray = new byte[buffsize];
    		//short[] bbarray = new short[buffsize];
    		//int result = audioRecorder.read(bbarray, 0, bbarray.length);
			numBytesRead = audioRecorder.read(bbarray, 0, buffsize);
        	
            // Android is reading less number of bytes than requested.
//            if(buffsize > numBytesRead) {
//            	numBytesRead = numBytesRead + 
//            			audioRecorder.read(bbarray, numBytesRead-1, buffsize-numBytesRead);
//            }   
    		
            Log.i(tag, "bytes read: " + numBytesRead);
            if (numBytesRead > 0) {
                //bData.put(bbarray, 0, numBytesRead);
                bData.put(bbarray);
                
                Log.i(tag, "position: " + bData.position());
                Log.i(tag, "remaining: " + bData.remaining());
                //bData = ByteBuffer.allocate(buffsize);
            }
            
            Log.i(tag, "first element in buffer: " + bData.get(0));
            
            if (bData.position() > buffsize) {
            	Log.w(tag, "RESET BUFFER");
            	bData.clear();
            	continue;
            	
            }

            
    		// do the audio device read
	    	//byte[] buffer = new byte[buffsize];
        	//numBytesRead = audioRecorder.read(buffer, 0, buffsize);
        	
            // Android is reading less number of bytes than requested.
//            if(buffsize > numBytesRead) {
//            	numBytesRead = numBytesRead + 
//            			audioRecorder.read(buffer, numBytesRead-1, buffsize-numBytesRead);
//            }   
//            
	    	audioStreamsList = MainActivity.getOutputAudioStreams();
//	    	
	    	if (audioStreamsList.size() == 0 ) {
        		Log.i(tag, "no audiostreams");
        		continue;
        	}
	    	

	    	
	    	//short[] bytesForStream = new short[buffsize];
	    	

	    	//Log.i(tag, "load stream with bytes: " + bytesForStream.length);
	    	bData.limit(bData.position());
	    	if (bData.position() >= buffsize) {
	    		Log.w(tag, "BIGGGG");
	    		bData.position(bData.position() - buffsize);
	    	} else {
	    		bData.position(0);
	    	}
	    	
	    	
	    	byte[] bytesForStream = new byte[bData.limit() - bData.position()];
	    	bData.get(bytesForStream, 0, bytesForStream.length);
	    	bData.clear();

//	    	Log.w(tag, "byte bbarray0: " + bbarray[0] + " stream0: " + bytesForStream[0]);
//	    	Log.w(tag, "byte bbarray1: " + bbarray[1] + " stream1: " + bytesForStream[1]);
//	    	Log.w(tag, "byte bbarray2: " + bbarray[2] + " stream2: " + bytesForStream[2]);
//	    	Log.w(tag, "byte bbarrayL: " + bbarray[bbarray.length-1] + " streamL: " + bytesForStream[bbarray.length-1]);
//	    	
	    	
        	for (int i=0; i<audioStreamsList.size(); i++) {
	        	
	        	DataOutputStream outputStream = audioStreamsList.get(i);
	        	
	        	
	        	try {
					outputStream.write(bytesForStream);
					//outputStream.write(bbarray);
					outputStream.flush();
				} catch (IOException e) {
					// our client dosconnected, so we remove them from the array and continue
	        		// to serve the other clients
	        		Log.w(tag, "client died, removing");
	        		e.printStackTrace();
	        		MainActivity.removeAudioOutputStream(i);
	        		i--;
	        		continue;

				}
	        	
	        	
        	
	        	
	        }
        	
        	
    	} 	
        	
//        	try {
//        		
//		        
//        	} catch (SocketException e) {
//        		// our client dosconnected, so we remove them from the array and continue
//        		// to serve the other clients
//        		Log.w(tag, "client died, removing");
//        		outputVideoStreamList.remove(i);
//        		continue;
//        		
//        	}  catch (IOException e) {
//        		e.printStackTrace();
//        	}

    }
    
}




