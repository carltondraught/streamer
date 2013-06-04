package org.tapsee.streamer;

import java.io.IOException;
import java.net.InetAddress;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.rtsp.RtspServer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ToggleButton;

public class MainActivity_rtsp extends Activity implements SurfaceHolder.Callback {
	
	private MediaRecorder mMediaRecorder;
	private Camera mCamera;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mHolder;
	private View mToggleButton;
	private boolean mInitSuccesful;
	
	@Override
 
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    setContentView(R.layout.activity_main_rtsp);
	    
	
	    
	    // we shall take the video in landscape orientation
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        //                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
	    mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
	    mHolder = mSurfaceView.getHolder();
	    mHolder.addCallback(this);
	    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	
	    Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
	    editor.putString(RtspServer.KEY_PORT, String.valueOf(1234));
	    editor.commit();
	        	
	    
	    //mToggleButton = (ToggleButton) findViewById(R.id.toggleRecordingButton);
	       
	}
	
	/* Init the MediaRecorder, the order the methods are called is vital to
	 * its correct functioning */
	/*private void initRecorder(Surface surface) throws IOException {
	    // It is very important to unlock the camera before doing setCamera
	    // or it will results in a black preview
	    if(mCamera == null) {
	        mCamera = Camera.open();
	        mCamera.unlock();
	    }
	
	    
	    
	    if(mMediaRecorder == null)  mMediaRecorder = new MediaRecorder();
	    mMediaRecorder.setPreviewDisplay(surface);
	    mMediaRecorder.setCamera(mCamera);
	
	    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
	   //       mMediaRecorder.setOutputFormat(8);
	    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
	    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
	    mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
	    mMediaRecorder.setVideoFrameRate(30);
	    mMediaRecorder.setVideoSize(640, 480);
	    mMediaRecorder.setOutputFile(VIDEO_PATH_NAME);
	
	    try {
	        mMediaRecorder.prepare();
	    } catch (IllegalStateException e) {
	        // This is thrown if the previous calls are not called with the 
	        // proper order
	        e.printStackTrace();
	    }
	
	    mInitSuccesful = true;
	}
	*/
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {

			if(!mInitSuccesful) {
				mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
			    //mHolder = mSurfaceView.getHolder();
			    //mHolder.addCallback(this);
			    //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			    
				//					Session session = SessionBuilder.getInstance()
				//					        .setSurfaceHolder(mSurfaceView.getHolder())
				//					        .setContext(getApplicationContext())
				//					        .setAudioEncoder(SessionBuilder.AUDIO_AMRNB)
				//					        .setVideoEncoder(SessionBuilder.VIDEO_H264)
				//					        .build();
				//	
				

				SessionBuilder.getInstance()    
		            .setSurfaceHolder(mSurfaceView.getHolder())
		            .setContext(getApplicationContext())
		            .setAudioEncoder(SessionBuilder.AUDIO_AMRNB)
		            .setVideoEncoder(SessionBuilder.VIDEO_H264);
				
				Context context = getApplicationContext();
				
				context.startService(new Intent(this,RtspServer.class));
				
				//InetAddress foo = InetAddress.getByName("224.0.0.1");
				//InetAddress foo = InetAddress.getByName("192.168.1.35");
				//InetAddress foo = InetAddress.getByName("192.168.1.36");
				
				//session.setDestination(foo);
				
				Log.i("IVAN" , "HERE1");
				//String sdp = session.getSessionDescription();
				//Log.i("IVAN", sdp);
				//session.start();
				Log.i("IVAN" , "HERE");
				

				
				
			}
	            //initRecorder(mHolder.getSurface());
	        	
	        	

	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	    shutdown();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
	
	private void shutdown() {
		
		Context context = getApplicationContext();
		context.startService(new Intent(this,RtspServer.class));
		
		
	    // Release MediaRecorder and especially the Camera as it's a shared
	    // object that can be used by other applications
	    //mMediaRecorder.reset();
	    //mMediaRecorder.release();
	    //mCamera.release();
	
	    // once the objects have been released they can't be reused
	    //mMediaRecorder = null;
	    //mCamera = null;
	}

}