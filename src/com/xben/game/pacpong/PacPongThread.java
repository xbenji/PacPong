package com.xben.game.pacpong;

import com.xben.game.pacpong.objects.*;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

public class PacPongThread extends Thread {

	private class cTarget  {
		
		int mLevel;
		int mSize;
		//int mX;
		//int mY;
		
		RectF mRect;
		
		public cTarget(int x, int y, int iLevel, int iSize)
		{
			mLevel = iLevel;
			//mX = x; mY = y;
			mSize = iSize;
			mRect = new RectF(x, y, x+mSize, y+mSize);
		}
		
		public void draw(Canvas iCanvas)
		{
			final Paint lRectPaint = new Paint();
			// background
			lRectPaint.setARGB(-1, 10, 10, 10);
			lRectPaint.setStyle(Paint.Style.FILL);
			iCanvas.drawRect(mRect, lRectPaint);
			// border
			lRectPaint.setColor(Color.RED);
			lRectPaint.setStyle(Paint.Style.STROKE);
			lRectPaint.setStrokeWidth(2);
			iCanvas.drawRect(mRect, lRectPaint);
		}
		
		public RectF getRect()
		{
			return mRect;
		}
	}

	private cFieldRect mFieldRect;
	private cTarget mTarget;
	private cBall mBall;

	private static int EDGE_SIZE = 10;
	
	private Paint mPaint = null;
	private SurfaceHolder mSurfaceHolder;
	private boolean mRun = false;
	boolean mFirstTime = true;
	private Handler mHandler;
	
	private enum eGameState {
		BEGIN, MOVE, END
	}
	eGameState mGameState;
	


	public PacPongThread(SurfaceHolder iHolder, Context iContext, Handler iHandler) {
		mSurfaceHolder = iHolder;
		mPaint = new Paint();
		mHandler = iHandler;

		//mFieldRect = new cFieldRect(90, 250, 300, 300);
		mFieldRect = new cFieldRect(0, 80, 480, 720);
		mTarget = new cTarget(150, 150, 0, 150);
		mBall = new cBall(240, 650);
		mBall.setFieldRect(mFieldRect);
		
		mGameState = eGameState.BEGIN;
	}

	@Override
	public void run() {

		Canvas lCanvas = null;

		while (mRun) 
		{
			if(mFirstTime)
			{
				drawIntro();
				mFirstTime = false;
				continue;
			}

			try {
				lCanvas = mSurfaceHolder.lockCanvas(null);
				synchronized (mSurfaceHolder) 
				{   
					play();
					doDraw(lCanvas);

				}
			} 
			finally 
			{
				if (lCanvas != null)
				{
					this.mSurfaceHolder.unlockCanvasAndPost(lCanvas);
				}
			}
		}
	}

	private void play() {
		
		switch (mGameState){
		
		case BEGIN:
			break;
		case MOVE:
			// lets move
			if (mBall.move()){
				if (mBall.checkBounce()){
					buzz(50);
					setHitTextCount(mBall.mBounceCount);
				}
			}
			// ball stopped
			else {
				if (checkTarget())
				{
					buzz(200);
					//respawn target
					java.util.Random rand = new java.util.Random();
					int x = rand.nextInt(350);
					int y = rand.nextInt(590);
					mTarget = new cTarget(x, y, 0, 150);
				}
				Log.i("state", "STOPPED");
				mGameState = eGameState.END;
			}
			break;
		case END:
			//end display thing, or anim
			break;
		}
		
	}

	private void buzz(int iTime){
			// vibrate
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putLong("vibr", iTime);
			msg.setData(b);
			mHandler.sendMessage(msg);
	}
	
	private void setHitTextCount(int iBounceCount){
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putString("text", "Hits :" + iBounceCount);
			msg.setData(b);
			mHandler.sendMessage(msg);
	}
	
	private boolean checkTarget()
	{
		return (mTarget.getRect().contains(mBall.getX(), mBall.getY()));
		
	}
	
	private void doDraw(Canvas iCanvas) {
		//Clear the screen
		iCanvas.drawRGB(0, 0, 0);
		
		mFieldRect.draw(iCanvas);
		mTarget.draw(iCanvas);
		mBall.draw(iCanvas);
		
	}

	private void drawIntro() {
		
	}

	public void setRunning(boolean iBool) {
		mRun = iBool;
	}

	public boolean onTouch(View iView, MotionEvent iEv) 
	{	
		if (mFieldRect.contains(iEv.getX(), iEv.getY()))
		{		
			float lX = iEv.getX();
			float lY = iEv.getY();
			
			mBall.setX(lX); mBall.setY(lY);

			switch (iEv.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				mBall.arm((int)lX, (int)lY);						
				break;

			case MotionEvent.ACTION_MOVE:
				mBall.addMotion((int)lX, (int)lY);
				break;

			case MotionEvent.ACTION_UP:
				mBall.endMotion();				
				mGameState = eGameState.MOVE;
				break;
			}
		}
		else {
			if (iEv.getAction() == MotionEvent.ACTION_MOVE){
				mBall.endMotion();				
				mGameState = eGameState.MOVE;
			}
		}
		

		return true;
	}



}
