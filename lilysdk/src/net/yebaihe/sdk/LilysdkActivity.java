package net.yebaihe.sdk;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LilysdkActivity extends Activity {
    private WebView lwebview;
	private Button lbtn;
	private Handler changeToMainHandler=new Handler();
	private Runnable changeToMainRunnable=new Runnable(){
		public void run() {
			try {
				ComponentName cn = new ComponentName(LilysdkActivity.this, LilysdkActivity.class);
				ActivityInfo ai;
				try {
					ai = LilysdkActivity.this.getPackageManager().getActivityInfo(cn, PackageManager.GET_META_DATA);
					String classname = ai.metaData.getString("changeto"); 
					startActivity(new Intent (LilysdkActivity.this, Class.forName(classname)));
				} catch (Exception e) {
					Log.d("myown", ""+e);
					startActivity(new Intent (LilysdkActivity.this, Class.forName(getString(R.string.LilySdkTurnTo))));
				}      
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			finish();
		}
    };
	protected Node curNode;
	private ProgressBar lprogress;
	private String apkName="unknown";
	private ImageView ilogo;
	private FrameLayout lflayout;
	private View lnew;
	private LinearLayout lapplayout;


	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lilysdkmain);
        
        lbtn=(Button)this.findViewById(R.id.lbutton);
        lnew=this.findViewById(R.id.inewimage);
        lflayout=(FrameLayout)this.findViewById(R.id.flayout);
        lapplayout=(LinearLayout)this.findViewById(R.id.idapplayout);
        lwebview=(WebView)this.findViewById(R.id.lwebview);
        
        lwebview.getSettings().setJavaScriptEnabled(true);
        
        lwebview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        lprogress=(ProgressBar) this.findViewById(R.id.lprogressBar);
        ilogo=(ImageView) this.findViewById(R.id.ilogo);
        
        TextView appname = (TextView)this.findViewById(R.id.idappname);
        appname.setText(this.getString(R.string.app_name));
        TextView appver = (TextView)this.findViewById(R.id.idappversion);
        appver.setText(SdkUtils.getAppVersionName(this));
        
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		anim.setDuration(3000);
		ilogo.startAnimation(anim);
        
        lbtn.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				changeToMainHandler.removeCallbacks(changeToMainRunnable);
				lbtn.setVisibility(View.GONE);
				lprogress.setVisibility(View.VISIBLE);
				startDownload();
				
			}
        });
        
        new Handler().postDelayed(new Runnable(){
			public void run() {
				login();
			}
        }, 100);        
		changeToMainAfter(5000);
    }

    private String getOutputPath() {
		String basedir=Environment.getExternalStorageDirectory()+"/lilydownload/apks/";
		File f=new File(basedir);
		if (!f.exists()) f.mkdirs();
		return basedir+apkName+".apk";
	}
    
	private void startDownload() {
        new HttpConnection(this,new Handler(){
        	public void handleMessage(Message message) {
        		switch (message.what) {
        		case HttpConnection.DID_START:
        			Toast.makeText(LilysdkActivity.this, "开始下载 "+lbtn.getTag(), Toast.LENGTH_LONG).show();
        			break;
        		case HttpConnection.DID_SUCCEED: {
        			Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File
                    (getOutputPath())), "application/vnd.android.package-archive");
                    startActivity(intent); 
                    finish();
                    break;
        		}
        		case HttpConnection.DID_PROGRESS:{
        			int percent=(Integer) message.obj;
        			lprogress.setProgress(percent);
        			break;
        		}
        		}
        	}
        }).file(lbtn.getTag().toString(), getOutputPath());
	}

	protected void login() {
        new HttpConnection(this,new Handler(){
        	public void handleMessage(Message message) {

        		switch (message.what) {
        		case HttpConnection.DID_START: {
        			break;
        		} 
        		case HttpConnection.DID_SUCCEED: {
        			String body=(String) message.obj;
        			Log.d("myown", "response:"+body);
        			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        			DocumentBuilder db;
					try {
						db = dbf.newDocumentBuilder();
	        			Document doc = db.parse(new InputSource(new StringReader(body)));
	        			doc.getDocumentElement().normalize();
	        			ConnectionManager.session=doc.getDocumentElement().getAttribute("session");
	        			Log.d("myown", "session:"+ConnectionManager.session);
	        			NodeList nodeList = doc.getElementsByTagName("AutoUpdate");
	        			if (nodeList.getLength()>0){
	        				curNode=nodeList.item(0);
	        				String apk=curNode.getAttributes().getNamedItem("apk").getNodeValue();
	        				curNode.getAttributes().getNamedItem("ver").getNodeValue();
	        				
	        				apkName=getPackageName();
	        				showAutoUp(apk,curNode.getAttributes().getNamedItem("hint").getNodeValue(),
	        						curNode.getAttributes().getNamedItem("force").getNodeValue());
	        			}
	        			nodeList = doc.getElementsByTagName("Message");
	        			if (nodeList.getLength()>0){
	        				String url=nodeList.item(0).getAttributes().getNamedItem("url").getNodeValue();
	        				showMessage(url);
	        			}
	        			nodeList = doc.getElementsByTagName("Soft");
	        			if (nodeList.getLength()>0){
	        				curNode=nodeList.item(0);
	        				String url=curNode.getAttributes().getNamedItem("apk").getNodeValue();
	        				String hint=curNode.getAttributes().getNamedItem("hint").getNodeValue();
	        				String packagename=curNode.getAttributes().getNamedItem("package").getNodeValue();
	        				apkName=curNode.getAttributes().getNamedItem("name").getNodeValue();
	        				PackageManager packageManager = getPackageManager();
	        				try {
	        					packageManager.getPackageInfo(packagename, 0);
	        				} catch (NameNotFoundException e) {
		        				showApkTuijian(url,hint);
	        				}
	        			}	        			
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
        			break;
        		}
        		case HttpConnection.DID_ERROR:{
        			break;
        		}
        		}
        	}
        }).get("http://lily.newim.net/appstore/login.php?key="+getKey());

	}

	protected void showAutoUp(String apk, String hint, String force) {
		if (force.equals("1")){
			changeToMainHandler.removeCallbacks(changeToMainRunnable);
		}
		lflayout.setVisibility(View.VISIBLE);
		lapplayout.setVisibility(View.GONE);
		lwebview.setVisibility(View.VISIBLE);
		lbtn.setVisibility(View.VISIBLE); 
		lwebview.loadUrl(hint);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		lwebview.startAnimation(anim);
		lbtn.startAnimation(anim);
		lbtn.setTag(apk);
	}

	protected void showApkTuijian(String url, String hint) {
		lflayout.setVisibility(View.VISIBLE);
		lapplayout.setVisibility(View.GONE);
		lnew.setVisibility(View.VISIBLE);
		changeToMainHandler.removeCallbacks(changeToMainRunnable);
		changeToMainAfter(5000);
		lwebview.setVisibility(View.VISIBLE);
		lbtn.setVisibility(View.VISIBLE);
		lwebview.loadUrl(hint);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		lwebview.startAnimation(anim);
		lbtn.startAnimation(anim);
		lbtn.setTag(url);
	}

	protected void showMessage(String url) {
		lflayout.setVisibility(View.VISIBLE);
		lapplayout.setVisibility(View.GONE);
		changeToMainHandler.removeCallbacks(changeToMainRunnable);
		changeToMainAfter(5000);
		lwebview.setVisibility(View.VISIBLE);
		lwebview.loadUrl(url);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		lwebview.startAnimation(anim);
	}

	private void changeToMainAfter(int delay) {
        changeToMainHandler.postDelayed(changeToMainRunnable,delay);
	}

	private String getKey() {
		try {
			//key解密以后为用|分隔的字符串，第一部分固定为lilysdk,第二部分为package名，第三部分为版本号，第四部分为IMEI
			String info="lilysdk|"+getPackageName()+"|"+getAppVersionName(this)+"|"+SdkUtils.getIMEI(this);
			//Log.d("myown", Des2.encode("lilysdk@",info));
			return URLEncoder.encode(Des2.encode("lilysdk@",info));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
                // ---get the package info---
                PackageManager pm = context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
                return ""+pi.versionCode;
        } catch (Exception e) {
                //Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
	}	
	
	
}