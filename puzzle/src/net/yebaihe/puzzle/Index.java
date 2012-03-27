package net.yebaihe.puzzle;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Index extends Activity implements OnClickListener{

    static final String PREFS_NAME = "PUZZLE";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);  
        int passedlevel = settings.getInt("passed",0); 
    	Button level1=(Button)this.findViewById(R.id.btnlevel1);
    	level1.setOnClickListener(this);
        
        if (passedlevel>=9){
        	Button level2=(Button)this.findViewById(R.id.btnlevel2);
        	level2.setEnabled(true);
        	level2.setOnClickListener(this);
        }
        if (passedlevel>=18){
        	Button level3=(Button)this.findViewById(R.id.btnlevel3);
        	level3.setEnabled(true);
        	level3.setOnClickListener(this);
        }
    }

	@Override
	public void onClick(View arg0) {
    	Button level1=(Button)this.findViewById(R.id.btnlevel1);
    	Button level2=(Button)this.findViewById(R.id.btnlevel2);
		Intent i=new Intent(this,SubIndex.class);
		if (arg0==level1){
			i.putExtra("level", 0);
		}
		else if (arg0==level2){
			i.putExtra("level", 1);			
		}
		else{
			i.putExtra("level", 2);			
		}
		this.startActivity(i);
	}
	
}
