package com.example.btcar;

import java.util.Timer;
import java.util.TimerTask;

import com.example.btcar.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements SensorEventListener{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    Timer timer = new Timer(true);

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    private ImageView[] imgView=new ImageView[4];
    private SensorManager aSensorManager;
    private Sensor aSensor;
    SensorEvent GSensor[]=new SensorEvent[9];
    String control="";
    int num=0;
    btcon bt;
    private int ChangeDirection(int dir)
    {
    	int i;
        imgView[0]=(ImageView) findViewById(R.id.upImg);
        imgView[1]=(ImageView) findViewById(R.id.downImg);
        imgView[2]=(ImageView) findViewById(R.id.leftImg);
        imgView[3]=(ImageView) findViewById(R.id.rightImg); 
	        for(i=0;i<4;i++)
	        {
	        	imgView[i].setVisibility(View.INVISIBLE);
	        }
	        imgView[dir].setVisibility(View.VISIBLE);
    	return dir;
    	
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);
        
        
        bt=new btcon(this);
 
        ChangeDirection(0);

        timer = new Timer(true);
        timer.schedule(task,1000, 500); 
        GSensorEnable();
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
 //       findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }


	TimerTask task = new TimerTask(){  
       public void run() {
    	   int i;
    	   short tmp=0;
    	   
    	   if(num<8)
    	   {
    		   tmp=0;
    		   if(GSensor[num]==null)
    			   tmp=0;
    		   else if(GSensor[num].values[0]>2)
    		   {
    			   tmp|=0x1;
    			   ChangeDirection(0);
    		   }
    		   else if(GSensor[num].values[0]<-2)
    		   {
    			   tmp|=0x1;
    			   ChangeDirection(1);
    		   }
    		   else if(GSensor[num].values[1]>2)
    		   {
    			   tmp|=0x1;
    			   ChangeDirection(2);
    		   }
    		   else if(GSensor[num].values[1]<-2)
    		   {
    			   tmp|=0x1;
    			   ChangeDirection(3);
    		   }
    		   control=control+tmp;		
    		   num++;
    	   }
		   else
		   {
			   short tag=0x1,len=0x2;
			   num=0;
			   control=len+control;
			   control=tag+control;   
			   //bt.SendCmd(control);
			   control="";
		   } 
       }  
	}; 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.optionmenu, menu);
        return true;
    }
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	
        return true;
           
    }
    public boolean onOptionsItemSelected(MenuItem item){

        super.onOptionsItemSelected(item);
        switch(item.getItemId())//得到被点击的item的itemId
        {
        	case R.id.item1://这里的Id就是布局文件中定义的Id，在用R.id.XXX的方法获取出来
        		 ChangeDirection(0);
            break;
        	case R.id.item2://这里的Id就是布局文件中定义的Id，在用R.id.XXX的方法获取出来
        		 ChangeDirection(1);
            break;
        	case R.id.item3://这里的Id就是布局文件中定义的Id，在用R.id.XXX的方法获取出来
        		 ChangeDirection(2);
            break;
        	case R.id.item4://这里的Id就是布局文件中定义的Id，在用R.id.XXX的方法获取出来
        		 ChangeDirection(3);
            break;
        }
    	 return true;

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
    	// TODO Auto-generated method stub
    	GSensor[num] = event;//第一個值(values[0])代表手機的水平旋轉
 //x 方向就是手机的水平方向，右为正

//y 方向就是手机的水平垂直方向，前为正

//z 方向就是手机的空间垂直方向，天空的方向为正，地球的方向为负
    	
    	
    	
    }
    public void GSensorEnable()
    {
    	aSensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
    	aSensor = aSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	aSensorManager.registerListener((SensorEventListener) this, aSensor, aSensorManager.SENSOR_DELAY_NORMAL);

    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
