package com.xben.game.pacpong.objects;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;

public class cBall {
	
	private class cTouchPath {

		private class p { public p(int ix, int iy) {
			x = ix; y =iy; }int x,y; }

		p[] mPath;
		int BUFFER_SIZE = 32;
		int mNbPoints;
		int mOffset = -2;

		public cTouchPath()
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
	
	private float mX = 100;
	private float mY = 100;
	private float mDX = 0;
	private float mDY = 0;
	private float mViscosity = 0;
	public int mBounceCount = 0;
	private boolean mArmed = false;
	
	private cTouchPath mTouchPath;
	
	private cFieldRect mFieldRect;
	
	private int RADIUS = 30;
	
	public cBall(int x, int y) {
		setX(x); setY(y);
		mTouchPath = new cTouchPath();
	}
	
	public void setX(float mX) {
		this.mX = mX;
	}

	public float getX() {
		return mX;
	}

	public void setY(float mY) {
		this.mY = mY;
	}

	public float getY() {
		return mY;
	}

	
	public void setFieldRect(cFieldRect iFieldRect){
		this.mFieldRect = iFieldRect;
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
		setX(getX() + mDX);
		setY(getY() + mDY);
		
		// slow down
		mDX = mDX * mViscosity;
		mDY = mDY * mViscosity;
		mViscosity *= 0.9995f;
		
		return true;
	}
	
	public boolean checkBounce() {

		boolean lBounced = false;

		// top left
		if(getX() <= mFieldRect.left + RADIUS + mFieldRect.EDGE_SIZE)
		{
			mDX *= -1;
			setX(mFieldRect.left + RADIUS + mFieldRect.EDGE_SIZE);
			lBounced = true;
		}
		else if(getX() >= mFieldRect.right - RADIUS - mFieldRect.EDGE_SIZE)
		{
			mDX *= -1;
			setX(mFieldRect.right - RADIUS - mFieldRect.EDGE_SIZE);
			lBounced = true;
		}
		// top bounce
		if(getY() <= mFieldRect.top + RADIUS + mFieldRect.EDGE_SIZE)
		{
			mDY *= -1;
			setY(mFieldRect.top + RADIUS + mFieldRect.EDGE_SIZE);
			lBounced = true;
		}
		// bottom bounce
		else if(getY() >= mFieldRect.bottom - RADIUS - mFieldRect.EDGE_SIZE)
		{
			mDY *= -1;
			setY(mFieldRect.bottom - RADIUS - mFieldRect.EDGE_SIZE);
			lBounced = true;
		}
		
		if (lBounced)
		{
			mBounceCount++;
		}
	
		return lBounced;

	}

	public void draw(Canvas iCanvas) {
		// draw ball
		// set the paint
		final Paint lPaint = new Paint();
		lPaint.setColor(Color.WHITE);
		lPaint.setAntiAlias(true);
		iCanvas.drawCircle(getX(), getY(), RADIUS, lPaint);		
	}

	public void arm(int iX, int iY) {
		// TODO Auto-generated method stub
		mTouchPath.reset();
		mTouchPath.add(iX, iY);
		mArmed = true;
	}

	public void addMotion(int iX, int iY) {
		// TODO Auto-generated method stub
		mTouchPath.add(iX, iY);
	}

	public void endMotion() {
		// TODO Auto-generated method stub
		if (mArmed) {
			launch(mTouchPath.getDx() / 3,
				   mTouchPath.getDy() / 3);
		mArmed = false;
		}
	}

}
