package net.yebaihe.puzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import net.yebaihe.puzzle.MultiTouchController.PointInfo;
import net.yebaihe.puzzle.PhotoSorterView.Img;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PuzzleActivity extends Activity implements PhotoSorterDelegate {
	
	class MyDragableView extends ImageView implements DraggableView{
		
		private Bitmap bitmap;

		public MyDragableView(Bitmap bitmap) {
			super(PuzzleActivity.this);
			this.setImageBitmap(bitmap);
			this.setScaleType(ImageView.ScaleType.FIT_XY);
			this.bitmap=bitmap;
		}

		@Override
		public void beforeDrag() {
		}

		@Override
		public DragView createDragView() {
			return new DragView(PuzzleActivity.this,bitmap);
		}

		@Override
		public Object getDraggedInfo() {
			return null;
		}

		@Override
		public void afterDrop(float x, float y) {
			x=x-p.getLeft();
			y=y-p.getTop();
			
			if (new Rect(0,0,p.getWidth(),p.getHeight()).contains((int)x+DeltaWidth/2, (int)y+DeltaHeight/2)){

				if (x<0) x=0;
				if (y<0) y=0;
				if (x>p.getWidth()-DeltaWidth) x=p.getWidth()-DeltaWidth;
				if (y>p.getHeight()-DeltaHeight) y=p.getHeight()-DeltaHeight;
				
				
				float dstx=x+DeltaWidth/2;
				float dsty=y+DeltaHeight/2;
				
				p.addImage(bitmap,PuzzleActivity.this.getResources(),dstx,dsty);
				adapt.bitmaps.remove(bitmap);
				int sel=g.getSelectedItemPosition();
				g.setAdapter(adapt);
				if (sel<g.getAdapter().getCount()){
					g.setSelection(sel);
				}
				else{
					if (adapt.getCount()>0){
						g.setSelection(g.getAdapter().getCount()/2);
					}
				}
				checkLevelPassed();
			}
		}
		
	}
	public  int DeltaWidth = 0;
	public  int DeltaHeight = 0;
	
	class ImageAdapter extends BaseAdapter{
		public ArrayList<Bitmap> bitmaps=new ArrayList<Bitmap>();
		private HashMap<Bitmap,Integer>bmphash=new HashMap<Bitmap,Integer>();
		private Context mContext;  
		
		public int getBitmapHash(Bitmap b){
			return bmphash.get(b);
		}
		
		public ImageAdapter(Context c) {
			mContext = c;  
			Bitmap b=BitmapFactory.decodeResource(getResources(), R.drawable.b01+currLevel);
			int w=b.getWidth()/pieceCount;
			int h=b.getHeight()/pieceCount;
			for (int i=0;i<pieceCount*pieceCount;i++){
				Bitmap bmp=Bitmap.createBitmap(w, h, Config.ARGB_8888);
				Canvas cv = new Canvas(bmp); 
				cv.drawBitmap(b, new Rect((i%pieceCount)*w,(i/pieceCount)*h,(i%pieceCount+1)*w,(i/pieceCount+1)*h), new Rect(0,0,w,h), null);
				Bitmap piece=Bitmap.createScaledBitmap(bmp, DeltaWidth, DeltaHeight, true);
				bitmaps.add(piece);
				bmphash.put(piece, i);
			}
			Collections.shuffle(bitmaps);
		}

		@Override
		public int getCount() {
			return bitmaps.size(); 
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {
			MyDragableView i = new MyDragableView(bitmaps.get(position)); 
			i.setLayoutParams(new Gallery.LayoutParams(galleryHeight, galleryHeight));
			i.setScaleType(ImageView.ScaleType.FIT_XY);
			return i;  
		}

	}
	
	@Override
	protected
	void onResume(){
		super.onResume();
		p.loadImages(this);
	}
	@Override
	protected
	void onStop(){
		super.onStop();
		p.unloadImages();
	}
	
	PhotoSorterView p;
	DraggableItemGallery g;
	ImageAdapter adapt;
	private boolean inited=false;
	
	class LinearLayoutChange extends LinearLayout{
		public LinearLayoutChange(Context context) {
			super(context);
		}
		
		@Override
	    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld)
	    {
			layoutSizeChange();
	    }
	}
	
	LinearLayoutChange ll;
	private boolean initedmargin=false;
	LinearLayout toplevel;
	LinearLayout cnt;
	private int currLevel=0;
	private boolean levelPassed=false;
	private int pieceCount=3;
	private String bywho="UnknownByWho";
	private String bywhen="2003-08-08 17:12:12";
	int galleryHeight=150;
	private int minAutoPlaceDelta=15;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        putAdInfo();
        
		currLevel=getIntent().getIntExtra("level", 0);
		pieceCount=3+currLevel/9;
		
        Display display = getWindowManager().getDefaultDisplay();
		DeltaWidth=display.getWidth()/pieceCount;
		DeltaHeight=DeltaWidth;//display.getHeight()/3;
		galleryHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
		minAutoPlaceDelta=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
		
        p=new PhotoSorterView(this);
        p.delegate=this;
        
        g = new DraggableItemGallery(this);
        g.setSpacing(3);
        
        cnt=(LinearLayout)this.findViewById(R.id.cnt);
        ll=new LinearLayoutChange(this);
        
        ll.setBackgroundResource(R.drawable.frame);
        
        
        cnt.addView(ll,new LinearLayoutChange.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
        toplevel=(LinearLayout)this.findViewById(R.id.toplevel);
        
        LinearLayout lad=(LinearLayout)this.findViewById(R.id.ad);
        int adheight=72;
        if (display.getWidth()>=800){
        	adheight=120;
        }
        else if (display.getWidth()>=600){
        	adheight=96;
        }
        toplevel.updateViewLayout(lad, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,adheight));
        toplevel.addView(g,1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,galleryHeight));  

    }
    
	public String getIMEI(Context context) {
    	TelephonyManager telephonyManager=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    	String imei=telephonyManager.getDeviceId();	  
    	return imei;
	}

	
	
	private void putAdInfo() {
		String data="";
		data+="package="+getPackageName();
		data+="&bywho="+bywho;
		data+="&bywhen="+bywhen;
		data+="&ad="+getDomodPid();
		data+="&imei="+getIMEI(this);
		new HttpConnection(this, new Handler()).post("http://lily.newim.net/appstore/adinfo.php", data);
	}
	private String getDomodPid() {
		ApplicationInfo ai;
		try {
			ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
			Bundle aBundle=ai.metaData;
			String aValue=aBundle.getString("DOMOB_PID");
			return aValue;
		} catch (NameNotFoundException e) {
		}
		return "";
	}
	
	public void layoutSizeChange() {
		if (initedmargin) return;
		initedmargin=true;
		
		Bitmap frame=BitmapFactory.decodeResource(getResources(), R.drawable.frame);
		int widthframe=ll.getWidth()*33/frame.getWidth();
		int heightframe=ll.getHeight()*49/frame.getHeight();
		
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT);
        lp.setMargins(widthframe, heightframe, widthframe, heightframe);
        ll.addView(p,lp);
		
	}
	@Override
	public void imagePositionChange(Img img, float x, float y,PointInfo touchPoint) {
		
		
		int pos=adapt.getBitmapHash(img.bitmap);
		int goodx=(pos % pieceCount) * DeltaWidth;
		int goody=(pos / pieceCount) * DeltaHeight;
		int curx=(int) (x-DeltaWidth/2);
		int cury=(int) (y-DeltaHeight/2);
		int delta=Math.max(Math.abs(goodx-curx),Math.abs(goody-cury));
		
		//Log.d("", "curx "+curx+" cury:"+cury);
		
		if (delta<minAutoPlaceDelta){			
			int setx=goodx+DeltaWidth/2;
			int sety=goody+DeltaHeight/2;
			p.setImgPosition(img,setx,sety,true);
		}
		else{
			if ((curx<0) || (curx>p.getWidth()-DeltaWidth) || (cury<0) || (cury>p.getHeight()-DeltaHeight)){
				if (curx<0) curx=0;
				if (cury<0) cury=0;
				if (curx>p.getWidth()-DeltaWidth) curx=p.getWidth()-DeltaWidth;
				if (cury>p.getHeight()-DeltaHeight) cury=p.getHeight()-DeltaHeight;
				
				Log.d("", "set curx "+curx+" cury:"+cury);
				
				int setx=curx+DeltaWidth/2;
				int sety=cury+DeltaHeight/2;
				p.setImgPosition(img,setx,sety,false);
			}
		}

		checkLevelPassed();
	}
	
	private void checkLevelPassed() {
		if (levelPassed) return;
		if ((adapt.bitmaps.size()==0) && p.allFixed()){
			levelPassed=true;
	        SharedPreferences settings = getSharedPreferences(Index.PREFS_NAME, 0);  
	        int passedlevel = settings.getInt("passed",0); 
			if (currLevel<=passedlevel){
				Editor edit = settings.edit();
				edit.putInt("passed", currLevel+1);
				edit.commit();
			}
			currLevel+=1;
			if (currLevel%9==0){
				new AlertDialog.Builder(this).setTitle("过关")
				.setMessage("您已经过完本级别所有的关卡，真厉害!")
				.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						setResult(RESULT_OK);
						finish();
						return;
					}
				})
				.create().show();
			}
			else{
				new AlertDialog.Builder(this).setTitle("过关")
				.setMessage("好棒呀，要继续吗？")
				.setPositiveButton("下一关", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (currLevel%9==0){
							setResult(RESULT_OK);
							finish();
							return;
						}
						levelPassed=false;
						p.clearAllImage();
						inited=false;
						sizeChanged();
					}
				})
				.setNegativeButton("关闭", null)
				.create().show();
			}
		}
	}
	@Override
	public void onBackPressed(){
		this.setResult(RESULT_CANCELED);
		finish();
	}
	
	@Override
	public void sizeChanged() {
		if (!initedmargin) return;
		if (inited) return;
		inited=true;
		
		DeltaWidth=p.getWidth()/pieceCount;
		DeltaHeight=p.getHeight()/pieceCount;
		
		toplevel.updateViewLayout(cnt, new LinearLayout.LayoutParams(cnt.getWidth(),cnt.getHeight()));
		
		new Handler().post(new Runnable(){
			@Override
			public void run() {
		        adapt=new ImageAdapter(PuzzleActivity.this);
				g.setAdapter(adapt);
		        if (adapt.getCount()>0) g.setSelection(adapt.getCount()/2);
			}
		});
		
	}
}