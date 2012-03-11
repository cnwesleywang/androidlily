package net.yebaihe.shelton;

import java.util.ArrayList;

import com.waps.AdView;
import com.waps.AppConnect;
import com.waps.UpdatePointsNotifier;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class SheltonActivity extends PreferenceActivity implements OnPreferenceChangeListener{
    protected static final String PREFS_NAME = "livepref";
    protected static int  FREE_PARTICLE_COUNT=1;
    protected int [][] DEFINES={
    		//{raw,type 1 continue 2 single,length msec}
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
					{
						Preference pref = (Preference) findPreference("unlocked");
						pref.setEnabled(false);
					}
		        	for (int i=FREE_PARTICLE_COUNT;i<TOTAL_PARTICLE_NUM;i++){
		                Preference pref = (Preference) findPreference("enabled"+(i+1));
		                pref.setEnabled(true);
		        	}
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

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppConnect.getInstance(this);
        getPreferenceManager().setSharedPreferencesName(
        		PREFS_NAME);
        addPreferencesFromResource(R.xml.settings);
        //AppConnect.getInstance(this).awardPoints(1000, null);
        //setTheme(android.R.style.the);
        setContentView(R.layout.main);
        Preference mPref = (Preference) findPreference("unlocked");
        mPref.setOnPreferenceChangeListener(this);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);  
    	for (int i=0;i<TOTAL_PARTICLE_NUM;i++){
            Preference pref = (Preference) findPreference("enabled"+(i+1));
            pref.setOnPreferenceChangeListener(this);
    	}
        if (settings.getBoolean("unlocked",false)){
        	mPref.setSummary("已解锁");
        	mPref.setEnabled(false);
        	for (int i=FREE_PARTICLE_COUNT;i<TOTAL_PARTICLE_NUM;i++){
                Preference pref = (Preference) findPreference("enabled"+(i+1));
                pref.setEnabled(true);
        	}
        }
        //LinearLayout container =(LinearLayout)findViewById(R.id.AdLinearLayout);
        //new AdView(this,container).DisplayAd(20);

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
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);  
        if (settings.getBoolean("unlocked",false)){
    		if (arg0.getKey().startsWith("enable")) updateSetting(arg0,arg1);
        	return true;
        }
        
        if (arg0.getKey().startsWith("enable")) return false;
        
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
		}).create().show();
		return false;
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
		playIdx=-1;
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);  
		for (int i=0;i<DEFINES.length;i++){
			if (settings.getBoolean("enabled"+(i+1), false)){
				playIdx=i;
				break;
			}
		}
		if (playIdx>=0){
			playing=true;
			playHandler.post(playRunnable);
		}
	}

	private void updateSetting(Preference currpref, Object currvalue) {
		
		Log.d("", ""+currpref+":"+currvalue);
		Boolean b=(Boolean)currvalue;
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);  
		SharedPreferences.Editor editor = settings.edit();  
		if (b){
	    	for (int i=0;i<TOTAL_PARTICLE_NUM;i++){
	    		String key="enabled"+(i+1);
	    		if (!key.equals(currpref.getKey())){
	    			editor.putBoolean(key, false);
	    			CheckBoxPreference box=(CheckBoxPreference)findPreference(key);
	    			box.setChecked(false);
	    		}
	    	}
		}
		else{
	    	for (int i=0;i<TOTAL_PARTICLE_NUM;i++){
	    		String key="enabled"+(i+1);
	    		boolean value=false;
	            if (i==0) value=true;
	            editor.putBoolean(key, value);
    			CheckBoxPreference box=(CheckBoxPreference)findPreference(key);
    			box.setChecked(value);
	    	}
		}
		editor.commit();
		
	}
}