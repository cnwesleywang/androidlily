package net.yebaihe.dxzmx;

import com.waps.AdInfo;
import com.waps.AppConnect;
import com.waps.UpdatePointsNotifier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class DxzmxActivity extends Activity implements OnClickListener, UpdatePointsNotifier {
    private static final String PREFS_NAME = "DXZMX";
	private boolean adfree=false;
	private AdInfo adInfo;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);  
        adfree = settings.getBoolean("showad",false);       

        if (!adfree){
	        AppConnect.getInstance("b7d670c71fc06b19d8973cbd21bbbd84","gfan",this);
	        AppConnect.getInstance(this).setAdViewClassName("net.yebaihe.dxzmx.MyAdFetch");		
			AppConnect.getInstance(this).getPoints(this);
	        AppConnect.getInstance(this).initAdInfo();
        }
        
        setContentView(R.layout.main);
        
        Button b=(Button)this.findViewById(R.id.idshowme);
        b.setOnClickListener(this);
                
    }

	@Override
	public void onClick(View arg0) {
        EditText t=(EditText) this.findViewById(R.id.idsrc);
        
        double d=Double.parseDouble(t.getText().toString());
        if (d>1000){
        	if (!adfree){
        		boolean networkok=true;
        		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);  
                NetworkInfo networkinfo = manager.getActiveNetworkInfo();  
                if (networkinfo == null || !networkinfo.isAvailable()) { 
                	networkok=false;
                }
                if (networkok){
                	showAd();
                	return;
                }
        	}
        }
        
        showResult();
	}

	private void showResult() {
        EditText t=(EditText) this.findViewById(R.id.idsrc);
        TextView l=(TextView) this.findViewById(R.id.iddest);
        String value=toUppercase(t.getText().toString());
        value=value.replace("\uFFFC", "");//I don't know why this junk char come from,I just ignore it.
        l.setText(value);
	}

	private void showAd() {
		View v=prepareAdView();
		if (v!=null){
			new AlertDialog.Builder(this).setTitle("广告")
			.setView(v)
			.setPositiveButton("安装此软件", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					AppConnect.getInstance(DxzmxActivity.this).downloadAd(adInfo.getAdId());
					showResult();
				}
			})
			.setNeutralButton("换一个看看", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					showAd();
				}
			})
			.setNegativeButton("直接看结果", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showResult();
				}
			})
			.create().show();
		}
		else{
			showResult();
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		AppConnect.getInstance(this).getPoints(this);
	}

	private View prepareAdView() {
		adInfo=null;
		adInfo = AppConnect.getInstance(this).getAdInfo();
		if (adInfo==null) return null;

		View v=ViewGroup.inflate(this, R.layout.adview, null);
		
		ImageView img=(ImageView)v.findViewById(R.id.idadicon);
		TextView title=(TextView)v.findViewById(R.id.idadtitle);
		TextView text=(TextView)v.findViewById(R.id.idadtext);
		
		img.setImageBitmap(adInfo.getAdIcon());
		title.setText(adInfo.getAdName()+" "+adInfo.getFilesize()+"M");
		text.setText(adInfo.getAdText());
		
		return v;
	}

	private String toUppercase(String value) {
		
		//移除数字前面的0
		int total=value.length();
		for (int i=0;i<total;i++){
			if (value.charAt(0)!='0') break;
			value=value.substring(1);
		}
		
		
		String retvalue="";
		int pointpos=value.indexOf(".");
		if (pointpos<0)pointpos=value.length();
		
		if (pointpos>12){
			return "现在最大只支持到数字:999999999999";
		}
		
		for (int i=0;i<pointpos;i++){
			retvalue+=getPosDesc(value,i,pointpos);
		}
		if (((int)Double.parseDouble(value))>0/*避免0元*/){
			retvalue+="元";
		}
		for (int i=pointpos+1;i<value.length();i++){
			retvalue+=getPosDesc(value,i,pointpos);
		}
		
		if (pointpos==value.length()) retvalue+="整";

		return retvalue;
	}

	private String getPosDesc(String value, int idx, int pointpos) {
		String ret="";
		int delta=pointpos-idx;
		char c = value.charAt(idx);
		switch (c){
		case '1':
			ret+="壹";
			break;
		case '2':
			ret+="贰";
			break;
		case '3':
			ret+="叁";
			break;
		case '4':
			ret+="肆";
			break;
		case '5':
			ret+="伍";
			break;
		case '6':
			ret+="陆";
			break;
		case '7':
			ret+="柒";
			break;
		case '8':
			ret=ret.concat("捌");
			break;
		case '9':
			ret+="玖";
			break;
		}
		
		if (c=='0'){
			if (idx<pointpos){
				if (value.length()>idx+1){
					if ((value.charAt(idx+1)!='0') && (value.charAt(idx+1)!='.')){
						ret+="零";
					}
				}
			}
			else{
				ret+="零";
			}
		}
		
		
		if ((delta>0) && (c!='0')){
			switch (delta % 4){
			case 2:
				ret+="拾";
				break;
			case 3:
				ret+="佰";
				break;
			case 0:
				ret+="仟";
				break;
			}
		}
		
		
		if (delta==5){
			int v=(int)Double.parseDouble(value);
			int vv=(v % 100000000)/10000;
			if (vv>0/*避免出现0万*/) ret+="万";
		}
		
		
		if (delta==9) ret+="亿";

		if (delta==-1) ret+="角";
		if (delta==-2) ret+="分";
		return ret;
	}
	
	@Override
	public void onBackPressed(){
		AppConnect.getInstance(this).finalize();
		super.onBackPressed();
	}

	@Override
	public void getUpdatePoints(String arg0, int arg1) {
		if (arg1>0){
			adfree=true;
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);  
			SharedPreferences.Editor editor = settings.edit();  
			editor.putBoolean("adfree", adfree);  
			editor.commit(); 
		}
	}

	@Override
	public void getUpdatePointsFailed(String arg0) {
		//nothing to do
	}
	
}