package com.xben.game.pacpong;

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

	private class TouchPath {

		private class p { public p(int ix, int iy) {
			x = ix; y =iy; }int x,y; }

		p[] mPath;
		int BUFFER_SIZE = 32;
		int mNbPoints;
		int mOffset = -2;

		public TouchPath()
		{
			mPath = new p[BUFFER_SIZE];
			reset();
		}

		public void reset()
		{
			mNbPoints = 0;
			mOffset = -2;
		}

		public void add(int ix, int iy)
		{
			// I'm so proud of this modulo thing	 
			mPath[mNbPoints % BUFFER_SIZE] = new p(ix, iy);
			mNbPoints++;
		}

		public int getDx()
		{
			if (mNbPoints < 3)
				return 0;
		
			if (mNbPoints == 2)
				mOffset = -1;
			
			return mPath[(mNbPoints-1) % BUFFER_SIZE].x -
			mPath[(mNbPoints+mOffset-1) % BUFFER_SIZE].x;
		}

		public int getDy()
		{
			if (mNbPoints < 3)
				return 0;
			
			if (mNbPoints == 2)
				mOffset = -1;

			return mPath[(mNbPoints-1) % BUFFER_SIZE].y - 
			mPath[(mNbPoints+mOffset-1) % BUFFER_SIZE].y;

		}

	}
	
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
	
	private class cBall {
		
		private float mX = 100;
		private float mY = 100;
		private float mDX = 0;
		private float mDY = 0;
		private float mViscosity = 0;
		public int mBounceCount = 0;
		
		private int RADIUS = 30;
		
		public cBall(int x, int y) {
			mX = x; mY = y;
		}

		public void launch(float iDx, float iDy){
			mViscosity = 0.9999f;
			mBounceCount = 0;
			
			mDX = iDx;
			mDY = iDy;
		}
		
		public boolean move(){
			
			if (Math.abs(mDX) < 0.001 && Math.abs(mDY) < 0.001)
			{
				return false;
			}
			
			// move
			mX += mDX;
			mY += mDY;
			
			// slow down
			mDX = mDX * mViscosity;
			mDY = mDY * mViscosity;
			mViscosity *= 0.9994f;
			
			return true;
		}
		
		private void checkBounce() {

			boolean lBounced = false;

			// top and right bounce
			if(mX <= RADIUS + EDGE_SIZE)
			{
				mDX *= -1;
				mX = RADIUS + EDGE_SIZE;
				lBounced = true;
			}
			else if(mX >= mPadWidth - RADIUS - EDGE_SIZE)
			{
				mDX *= -1;
				mX = mPadWidth - RADIUS - EDGE_SIZE;
				lBounced = true;
			}
			// left bounce
			if(mY <= RADIUS + EDGE_SIZE)
			{
				mDY *= -1;
				mY = RADIUS + EDGE_SIZE;
				lBounced = true;
			}
			// bottom bounce
			else if(mY >= mPadHeight - RADIUS - EDGE_SIZE)
			{
				mDY *= -1;
				mY = mPadHeight - RADIUS - EDGE_SIZE;
				lBounced = true;
			}

			if (lBounced)
			{
				mBounceCount++;
				Message msg = mHandler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("text", "Hits :" + mBounceCount);
				b.putLong("vibr", 100);
				msg.setData(b);
				mHandler.sendMessage(msg);
			}
		}

		public void draw(Canvas iCanvas) {
			// draw ball
			// set the paint
			final Paint lPaint = mPaint;
			lPaint.setColor(Color.WHITE);
			lPaint.setAntiAlias(true);
			iCanvas.drawCircle(mX, mY, RADIUS, lPaint);		
		}
	}	

	private int mPadWidth = 480;
	private int mPadHeight = 720;

	private TouchPath mTouchPath;
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

		mTouchPath = new TouchPath();
		mTarget = new cTarget(250, 400, 0, 150);
		mBall = new cBall(120, 120);
		
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
				mBall.checkBounce();				
			}
			// ball stopped
			else {
				if (checkTarget())
				{
					buzz();
					//respawn target
					java.util.Random rand = new java.util.Random();
					int x = rand.nextInt(350);
					int y = rand.nextInt(590);
					mTarget = new cTarget(x, y, 0, 120);
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

	private void buzz(){
			// vibrate
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putLong("vibr", 200);
			msg.setData(b);
			mHandler.sendMessage(msg);
	}
	
	private boolean checkTarget()
	{
		return (mTarget.getRect().contains(mBall.mX, mBall.mY));
		
	}
	
	private void doDraw(Canvas iCanvas) {
		//Clear the screen
		iCanvas.drawRGB(50, 50, 50);

		

		// draw borders
		RectF lBorderRect = new RectF(0, 0, mPadWidth, mPadHeight);
		
		final Paint lRectPaint = new Paint();
		// background
		lRectPaint.setColor(Color.BLACK);
		lRectPaint.setStyle(Paint.Style.FILL);
		iCanvas.drawRect(lBorderRect, lRectPaint);
		// border
		lRectPaint.setColor(Color.WHITE);
		lRectPaint.setStyle(Paint.Style.STROKE);
		lRectPaint.setStrokeWidth(8);
		iCanvas.drawRect(lBorderRect, lRectPaint);

		// draw target
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
		if (iEv.getY() < mPadHeight)
		{		
			float lX = iEv.getX();
			float lY = iEv.getY();
			
			mBall.mX = lX; mBall.mY = lY;

			switch (iEv.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				mTouchPath.reset();
				mTouchPath.add((int)lX, (int)lY);			
				break;

			case MotionEvent.ACTION_MOVE:
				mTouchPath.add((int)lX, (int)lY);
				break;

			case MotionEvent.ACTION_UP:
				mBall.launch(mTouchPath.getDx() / 3,
							 mTouchPath.getDy() / 3);
				
				mGameState = eGameState.MOVE;
				break;
			}
		}

		return true;
	}



}
