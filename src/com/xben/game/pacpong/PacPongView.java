package com.xben.game.pacpong;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class PacPongView extends SurfaceView 
						 implements SurfaceHolder.Callback, OnTouchListener{

	private PacPongThread mGameThread;
	private TextView mScoreTextView;
	private Vibrator mVibrator;
	
	public PacPongView(Context iContext, AttributeSet iAttrs) {
		super(iContext, iAttrs);
		
		//So we can listen for events...
        SurfaceHolder lHolder = getHolder();
        lHolder.addCallback(this);        
        
        // instantiate the thread
        mGameThread = new PacPongThread(lHolder, iContext, new Handler(){
        		@Override
        		public void handleMessage(Message m) {
        			if (m.getData().containsKey("text")){
        				mScoreTextView.setText(m.getData().getString("text"));
        				mScoreTextView.setVisibility(VISIBLE);
        			}
        			if (m.getData().containsKey("vibr")){
        				long lTime = m.getData().getLong("vibr");
        				mVibrator.vibrate(lTime);
        			}
        			
        		}
        });
        
        setOnTouchListener(this);
        setFocusable(true);
	}
	
	public void setScoreTextView(TextView iTextView) {
		mScoreTextView = iTextView;
    }

	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceCreated(SurfaceHolder iHolder) {
		mGameThread.setRunning(true);
		mGameThread.start();		
	}


	public void surfaceDestroyed(SurfaceHolder iHolder) {
		mGameThread.setRunning(false);
		mGameThread.stop();		
	}

	public boolean onTouch(View iView, MotionEvent iMotionEvent) {
		// TODO Auto-generated method stub
		return mGameThread.onTouch(iView, iMotionEvent);
	}

	public void setVibrator(Vibrator iVibrator) {
		mVibrator = iVibrator;		
	}

}
