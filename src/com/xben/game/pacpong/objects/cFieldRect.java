package com.xben.game.pacpong.objects;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class cFieldRect extends RectF {
	
	public int EDGE_SIZE = 8;
	
	public cFieldRect(int x, int y, int width, int height) {
		super(x, y, x+width, y+height);
	}
	
	public void draw(Canvas iCanvas){	

		final Paint lPaint = new Paint();
		// background
		lPaint.setColor(Color.BLACK);
		lPaint.setStyle(Paint.Style.FILL);
		iCanvas.drawRect(this, lPaint);
		// border
		lPaint.setColor(Color.WHITE);
		lPaint.setStyle(Paint.Style.STROKE);
		lPaint.setStrokeWidth(EDGE_SIZE);
		iCanvas.drawRect(this, lPaint);
	}
}
