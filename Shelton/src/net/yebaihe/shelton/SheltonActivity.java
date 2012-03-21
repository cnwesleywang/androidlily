package net.yebaihe.shelton;

import java.util.ArrayList;

import com.waps.AppConnect;
import com.waps.UpdatePointsNotifier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

public class SheltonActivity extends Activity implements  OnTabChangeListener{
    protected static final String PREFS_NAME = "livepref";
    protected static int  FREE_PARTICLE_COUNT=1;
    protected int [][] DEFINES={
    		{R.raw.whip,1,2000},
    		{R.raw.gun,1,2000},
    		{R.raw.submachinegun,2,710},
    };
    protected  int  TOTAL_PARTICLE_NUM=DEFINES.length;
    protected ArrayList<Integer> reses=new ArrayList<Integer>();
    
	private UpdatePointsNotifier unlockCallback=new UpdatePointsNotifier(){
		@Override
		public void getUpdatePoints(String arg0, int arg1) {
			SheltonActivity.this.runOnUiThread(new Runnable(){
				@Override
				public void run() {
			        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);  
					SharedPreferences.Editor editor = settings.edit();  
					editor.putBoolean("unlocked", true);  
					editor.commit();
					Toast.makeText(SheltonActivity.this, "已解锁", Toast.LENGTH_SHORT).show();
				}
			});
		}

		@Override
		public void getUpdatePointsFailed(String arg0) {
			final String reason=arg0;
			SheltonActivity.this.runOnUiThread(new Runnable(){
				@Override
				public void run() {
					Toast.makeText(SheltonActivity.this, "获取积分失败，原因:"+reason, Toast.LENGTH_SHORT).show();
				}
			});
		}
	};
	private SoundPool snd;
	private int playIdx=-1;
	private boolean playing=false;
	private Handler playHandler=new Handler();
	private Runnable playRunnable=new Runnable(){
		@Override
		public void run() {
			if ((playIdx<=1) && (!inShaking)) return;
			if ((playIdx>1) && (inShaking)){
				inShaking=false;
				return;
			}
			if (playing){
				snd.play(reses.get(playIdx),1, 1, 0, 0, 1);
				if ((DEFINES[playIdx][1]==2) && (!inShaking)){
					playHandler.postDelayed(playRunnable, DEFINES[playIdx][2]);
				}
			}
			inShaking=false;
		}
	};
	private net.yebaihe.shelton.ShakeListener mShake;
	protected boolean inShaking=false;

	
	class GetCSDNLogoTask extends AsyncTask<String,Integer,Bitmap> {//继承AsyncTask  
		  
        @Override  
        protected Bitmap doInBackground(String... params) {//处理后台执行的任务，在后台线程执行  
            return null;  
        }            
          
    }  
	
	
	TabHost tabHost;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConnect.getInstance(this);
        setContentView(R.layout.main);

        View btnNearBy=LayoutInflater.from(this).inflate(R.layout.btn01, null);
        View btnHots=LayoutInflater.from(this).inflate(R.layout.btn02, null);
        View btnDiscount=LayoutInflater.from(this).inflate(R.layout.btn03, null);
        
        tabHost=(TabHost) findViewById(R.id.mytabhost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("b01").setContent(R.id.tab1).setIndicator(btnNearBy));
        tabHost.addTab(tabHost.newTabSpec("b02").setContent(R.id.tab2).setIndicator(btnHots));
        tabHost.addTab(tabHost.newTabSpec("b03").setContent(R.id.tab3).setIndicator(btnDiscount));
        tabHost.setOnTabChangedListener(this);
        
        snd = new SoundPool(2, AudioManager.STREAM_SYSTEM,0);
        for (int i=0;i<DEFINES.length;i++){
        	reses.add(snd.load(this, DEFINES[i][0], 0));
        }
        
    	mShake=new ShakeListener(this);
    	mShake.setOnShakeListener(new ShakeListener.OnShakeListener() {
			@Override
			public void onShake() {
				inShaking=true;
				startPlayTone();
			}
    	});

        
    }
    
	@Override
    public void onBackPressed(){
    	AppConnect.getInstance(this).finalize();
    	super.onBackPressed();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK){
			return super.onKeyUp(keyCode, event);
		}
		if (playing) return true;
		if ((keyCode==KeyEvent.KEYCODE_VOLUME_UP)||(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN)){
			startPlayTone();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK){
			mShake.pause();
			finish();
			return true;
		}
		
		Log.d("", ""+keyCode);
		if ((keyCode==KeyEvent.KEYCODE_VOLUME_UP)||(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN)){
			stopPlayTone();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private void stopPlayTone() {
		playing=false;
	}

	private void startPlayTone() {
		playIdx=tabHost.getCurrentTab();
		if (playIdx>=0){
			playing=true;
			playHandler.post(playRunnable);
		}
	}


	@Override
	public void onTabChanged(String arg0) {
        if (arg0.equals("b01")) return;
        
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);  
        if (settings.getBoolean("unlocked",false)){
        	return;
        }
        
        new AlertDialog.Builder(SheltonActivity.this).setTitle("需要积分")
		.setMessage("解锁全部效果只需要50个积分，而积分的获得是完全免费的！如果您已经获得了积分请直接点解锁!")
		.setPositiveButton("免费获取", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				AppConnect.getInstance(SheltonActivity.this).showOffers(SheltonActivity.this);
			}
		})
		.setNegativeButton("解锁", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog,
					int which) {
				AppConnect.getInstance(SheltonActivity.this).spendPoints(50,unlockCallback);
			}
		})
		.create().show();
        
        tabHost.setCurrentTab(0);
        
	}
}