package net.yebaihe.puzzle;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.util.Log;
import android.view.View;

public class SubIndex extends Activity{
	private static final int RESBASE = R.drawable.b01;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subindex);
        GridView gridview=(GridView)this.findViewById(R.id.gridView);
        
        SharedPreferences settings = getSharedPreferences(Index.PREFS_NAME, 0);  
        int passedlevel = settings.getInt("passed",0); 
        int level=this.getIntent().getIntExtra("level", 0);
        
        ArrayList<HashMap<String, Object>> meumList = new ArrayList<HashMap<String, Object>>();
        for(int i = 0;i<9;i++) { 
            HashMap<String, Object> map = new HashMap<String, Object>(); 
            if (i+level*9<=passedlevel){
                map.put("ItemImage", R.drawable.big02); 
            }
            else{
                map.put("ItemImage", R.drawable.big01); 
            }
            meumList.add(map); 
        }
        
        SimpleAdapter saMenuItem = new SimpleAdapter(this, 
        		meumList, //数据源 
        		R.layout.index_item, //xml实现 
        		new String[]{"ItemImage",}, //对应map的Key 
        		new int[]{R.id.ItemImage,});  //对应R的Id 
        gridview.setAdapter(saMenuItem); 
        gridview.setOnItemClickListener(new OnItemClickListener() { 
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) { 
                int level=getIntent().getIntExtra("level", 0);
        		Intent i=new Intent(SubIndex.this,PuzzleActivity.class);
        		i.putExtra("level", level*9+arg2);
        		startActivity(i);
        	} 
        }); 
        
    }
}
