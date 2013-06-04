package org.tapsee.streamer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {

	private Camera mCamera;
	static AudioRecord audioRecorder;
	private SurfaceHolder mHolder;
	boolean mInitSuccesful;
	public static  TextView serverStatusView;
	private Thread audioThread;
	private Thread serviceThread;
	private static String tag = "IVAN";

	static ArrayList<DataOutputStream>  outputVideoStreamList = new  ArrayList<DataOutputStream>();
	static ArrayList<DataOutputStream>  outputAudioStreamList = new  ArrayList<DataOutputStream>();
	String BOUNDARY = "kjsdfkhsdkjfsdkjfsd";
	ByteArrayOutputStream previewBuffer = new ByteArrayOutputStream();
	int framesProcessed = 0;
	private static ServerSocket serverSocket;
	long secsStart;
	
	static int AUDIO_SRATE = 8000;
	static int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
	static int AUDIO_FORMAT =  AudioFormat.ENCODING_PCM_16BIT;
	static int AUDIO_BUFFSIZE = AudioRecord.getMinBufferSize(AUDIO_SRATE, AUDIO_CHANNEL_CONFIG, AUDIO_FORMAT);
    
	static boolean audioIsRecording;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    setContentView(R.layout.activity_main);
	
	
	    
	    // we shall take the video in landscape orientation
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        //                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
	    serverStatusView = (TextView) findViewById(R.id.status_message);

	    SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
	    mHolder = surfaceView.getHolder();
	    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    mHolder.addCallback(this);
	    

	    
	       
	}
	

    @Override
    public void onPreviewFrame(byte[] frameData, Camera camera) {

        java.util.Date date= new java.util.Date();
        long secsNow = date.getTime();
        long secsElapsed =  secsNow - secsStart;
        float fps = 1000*framesProcessed / (float) secsElapsed;
        framesProcessed++;
        //Log.i(tag, "num active streams: " + outputVideoStreamList.size() + ", fps: " + fps);
        
        int cameraFormat = mCamera.getParameters().getPreviewFormat();

        Size size = mCamera.getParameters().getPreviewSize();
        int width= size.width;
		int height = size.height;
        
		previewBuffer.reset();
	    try {
			switch (cameraFormat) {
	            case ImageFormat.JPEG:
	                // nothing to do, leave it that way
	            	previewBuffer.write(frameData);
	            	previewBuffer.flush();
	                break;
	            
	            case ImageFormat.NV21:
	            	// this is the case for ivan's phone
	            	Rect rect  = new Rect(0,0,width,height);	            	
	            	new YuvImage(frameData, cameraFormat, width, height, null).compressToJpeg(rect, 50, previewBuffer);
	            	previewBuffer.flush();
	                break;
	                
	            default:
	                throw new IOException("Error while encoding: unsupported image format");
	        }
	    } catch (IOException e) {
    		e.printStackTrace();
    	}
		

        for (int i=0; i<outputVideoStreamList.size(); i++) {
    	
        	DataOutputStream outputStream = outputVideoStreamList.get(i);

        	try {
        		outputStream.write(("Content-type: image/jpeg\r\n" +
                  "Content-Length: " + previewBuffer.size() + "\r\n" +
                  "\r\n").getBytes());
        		
        		previewBuffer.writeTo(outputStream);
        		
        		outputStream.write(("\r\n--" + BOUNDARY + "\r\n").getBytes());
		        outputStream.flush();
		        
        	} catch (SocketException e) {
        		// our client dosconnected, so we remove them from the array and continue
        		// to serve the other clients
        		Log.w(tag, "client died, removing");
        		outputVideoStreamList.remove(i);
        		continue;
        		
        	}  catch (IOException e) {
        		e.printStackTrace();
        	}
			

        }
        
        	
    }

    
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	// this must be here coz of this:
    	// http://stackoverflow.com/questions/7042377/camera-onpreviewframe-not-called
    	// "You must to call setPreviewCallback in surfaceChanged method, not only in surfaceCreated."

    	if(mInitSuccesful) {
    		Log.i(tag, "do surfaceChanged");
    		
	    	try {
	    	    mCamera.setPreviewCallback(this);
	    	    mCamera.setPreviewDisplay(mHolder);
	    	    mCamera.startPreview();
	
	    	} catch (Exception e){
	    	    Log.i(tag, "Error starting camera preview: " + e.getMessage());
	    	}
    	}
	}
    
	@Override
	public void surfaceCreated(SurfaceHolder holder) {

			if(!mInitSuccesful) {
				
				Log.i(tag, "creating surface..");

				mCamera = Camera.open();
				
		        try {
					mCamera.setPreviewDisplay(mHolder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
		        //Parameters parameters = mCamera.getParameters();
		        //parameters.setPreviewFrameRate(5);
		        //parameters.setPreviewFormat(ImageFormat.JPEG);
		        //mCamera.setParameters(parameters);
	    	    mCamera.startPreview();
	    	    
	    	    //AUDIO_BUFFSIZE = 8192;
	    	    audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
	    	    		AUDIO_SRATE, AUDIO_CHANNEL_CONFIG,
	    	    		AUDIO_FORMAT, AUDIO_BUFFSIZE);


	    	    
	    	    
	    	    OnRecordPositionUpdateListener logMe = new OnRecordPositionUpdateListener()
	            {
	                @Override
	                public void onPeriodicNotification(AudioRecord recorder)
	                {
	                	Date d = new Date();
	                    
	                    //recorder.read(audioData, 0, finalBufferSize);
	                    //byte[] bbarray = new byte[AUDIO_BUFFSIZE];
	                    //int numBytesRead = recorder.read(bbarray, 0, AUDIO_BUFFSIZE);
	                    Log.e(tag, "periodic: " + d.getTime()/(float)1000);// + "read: " + numBytesRead);
	                    
	                    //do something amazing with audio data
	                }

	                @Override
	                public void onMarkerReached(AudioRecord recorder)
	                {
	                    Log.e(tag, "marker reached");
	                }
	            };
	            Log.e(tag, "marker reachedxx");
	            audioIsRecording = true;
				
//	            audioRecorder.setNotificationMarkerPosition(4096);
//				audioRecorder.setPositionNotificationPeriod(1);
//				audioRecorder.setRecordPositionUpdateListener(logMe);
				audioRecorder.startRecording();

	    	    
				Date date= new java.util.Date();
		        secsStart = date.getTime();
		        
		        audioThread = new Thread(new AudioThread());
				audioThread.start();
				
				serviceThread = new Thread(new ServerThread());
				serviceThread.start();
		        
//		        try {
//					HTTPServer httpServer = new HTTPServer(8080, "/sdcard/streamer/");
//					
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
		        
		        

				mInitSuccesful = true;
				
			}

	}
	
	public static void addVideoOutputStream(DataOutputStream outputDataStream) {
		outputVideoStreamList.add(outputDataStream);
	}


	public static void addAudioOutputStream(DataOutputStream outputDataStream) {
		outputAudioStreamList.add(outputDataStream);
		
//		if (outputAudioStreamList.size()==1) {
//			Log.i(tag, "start audio thread");
//			Thread audioThread = new Thread(new AudioThread());
//			audioThread.start();
//			
//		}
	}
	
	public static void removeAudioOutputStream(int index) {
		outputAudioStreamList.remove(index);
	}
	
	public static ArrayList<DataOutputStream> getOutputAudioStreams() {
		
		return outputAudioStreamList;
	}

	public static AudioRecord getAudioRecorder() {	
		return audioRecorder;
	}
	
	public static TextView getStatusView() {
		return serverStatusView;
	}
	
//	public static ServerSocket getSocket() {
//		return serverSocket;
//	}
	public static  void setSocket(ServerSocket socket) {
		serverSocket = socket;
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	    shutdown();
	}


    @Override
    protected void onStop() {
        super.onStop();
        Log.i(tag, "activity onStop");
        shutdown();
        
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(tag, "activity onPause");
        shutdown();
        
    }

    
	private void shutdown() {

		Log.i(tag, "activity shutdown");

		// the socket and camera shutdowns are in a try because shutdown maybe called twice
		// in a row for pause and shutdown
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		if (mCamera != null) {
			try {
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		if (audioRecorder != null) {
			try {
				audioRecorder.stop();
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		
		audioRecorder.stop();
		audioIsRecording = false;
		audioThread.interrupt();
		serviceThread.interrupt();
		
		// set the init to false to force a restart if we come back
		mInitSuccesful = false;
		
	}

}