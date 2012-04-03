package net.yebaihe.jumpball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Index extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.index);
        Button b01=(Button)this.findViewById(R.id.btn01);
        b01.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent i=new Intent(Index.this,JumpballActivity.class);
				i.putExtra("eachtime", 2);
				i.putExtra("ballnum", 4);
				startActivity(i);
			}
        });
        Button b02=(Button)this.findViewById(R.id.btn02);
        b02.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent i=new Intent(Index.this,JumpballActivity.class);
				i.putExtra("eachtime", 3);
				i.putExtra("ballnum", 5);
				startActivity(i);
			}
        });
        Button b03=(Button)this.findViewById(R.id.btn03);
        b03.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent i=new Intent(Index.this,JumpballActivity.class);
				i.putExtra("eachtime", 3);
				i.putExtra("ballnum", 6);
				startActivity(i);
			}
        });
    }
}
