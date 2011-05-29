package com.xben.game.pacpong;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Window;
import android.widget.TextView;

public class PacPong extends Activity {
	
	private PacPongView mPacPongView;
	private Vibrator mVibrator;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pacpong_layout);
        
        // get the main game view
        mPacPongView = (PacPongView) findViewById(R.id.pacpong);
        // set a text view for score
        mPacPongView.setScoreTextView((TextView) findViewById(R.id.scoretext));
        
        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        mPacPongView.setVibrator(mVibrator);
    }
}