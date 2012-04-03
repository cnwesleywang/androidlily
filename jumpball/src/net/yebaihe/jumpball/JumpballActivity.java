package net.yebaihe.jumpball;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class JumpballActivity extends Activity implements JumpballDelegate {
	private static final String PREFS_NAME = "jumpball";
	public static int RES_NUM = 5;
	public static int BALL_EACH_TIME=2;
	JumpDataModel model;
	JumpView view;
	TextView bestScore;
	TextView currScore;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        RES_NUM=this.getIntent().getIntExtra("ballnum", 5);
        BALL_EACH_TIME=this.getIntent().getIntExtra("eachtime", 3);
        
        model=new JumpDataModel();
        model.delegate=this;
        
        view=new JumpView(this);
        view.setDataModel(model);
        
        LinearLayout l=(LinearLayout)this.findViewById(R.id.viewcontainer);
        l.addView(view,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
     
        bestScore=(TextView)this.findViewById(R.id.beatscore);
        currScore=(TextView)this.findViewById(R.id.currscore);
        
        updateScore();
        
    }
	private void updateScore() {
		currScore.setText(String.format("当前得分:%d", model.score));
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);  
        int bestscore = settings.getInt("bestscore"+BALL_EACH_TIME,0);   
        bestScore.setText(String.format("最高得分:%d", bestscore));
	}
	@Override
	public void onScoreDataChange() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);  
        int bestscore = settings.getInt("bestscore"+BALL_EACH_TIME,0);   
		if (model.score>bestscore){
			SharedPreferences.Editor editor = settings.edit();  
			editor.putInt("bestscore"+BALL_EACH_TIME, model.score);
			editor.commit();
		}
		updateScore();
	}
}