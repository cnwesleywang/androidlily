package net.yebaihe.jumpball;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class JumpView extends View{

	private static final int WAIT_FIX_FROM = 0;
	private static final int WAIT_FIX_TO = 1;
	private JumpDataModel datamodel;
	private ArrayList<Bitmap> bitmaps=new ArrayList<Bitmap>();
	private int cellWidth;
	private int cellHeight;
	private int curState=WAIT_FIX_FROM;
	private int curFromX=-1;
	private int curFromY=-1;

	private int XDelta=0;
	private int YDelta=0;
	
	private boolean inAnimation=false;
	private String path="";
	private Bitmap cover;

	public JumpView(Context context) {
		super(context);
		for (int i=0;i<JumpballActivity.RES_NUM;i++){
			bitmaps.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.b01+i));
		}
		cover=BitmapFactory.decodeResource(context.getResources(), R.drawable.b00);
	}

	@Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld)
    {
		this.invalidate();
    }
	
	@Override 
	public boolean onTouchEvent(MotionEvent event) { 
		if (inAnimation) return false;
		
		Log.d("", "touchevent:"+event.getAction());
		if (event.getAction()!=MotionEvent.ACTION_UP) return true;
		
		int x=(int) (event.getX()/this.cellWidth);
		int y=(int) (event.getY()/this.cellHeight);
		Log.d("", "x:"+x+" y:"+y);
		if (y>datamodel.value.length-1) return false;
		if (x>datamodel.value[y].length-1) return false;
		
		if (curState==WAIT_FIX_FROM){
			if (datamodel.value[y][x]!=-1){
				curFromX=x;
				curFromY=y;
				Log.d("", "curfromx:"+x+" curfromy:"+y);
				redrawSquar(x,y);
				curState=WAIT_FIX_TO;
			}
		}
		else if (curState==WAIT_FIX_TO){
			if (datamodel.value[y][x]==-1){
				path=datamodel.move(curFromX,curFromY,x,y);
				if (path.length()>0){
					Log.d("", "can move");
					curFromX=-1;
					curFromY=-1;				
					showMoveAnimation(x,y);
				}
				else{
					Log.d("", "no way");
				}
			}
			else{
				int oldFromX=curFromX;
				int oldFromY=curFromY;
				curFromX=x;
				curFromY=y;				
				redrawSquar(x,y);
				redrawSquar(oldFromX,oldFromY);
			}
		}
		
		return true;
	}
	
	/**
	 * show animation one point move from positon from to to.
	 * @param curFromX2
	 * @param curFromY2
	 * @param x
	 * @param y
	 * @param path 
	 */
	private void showMoveAnimation(final int x, final int y) {
		if (path.length()<=0){
			inAnimation=false;
			curState=WAIT_FIX_FROM;
			if (!datamodel.checkAndRemoveLine()){
				datamodel.randomAddPoint(JumpballActivity.BALL_EACH_TIME);
				datamodel.checkAndRemoveLine();
				if (datamodel.done()){
					datamodel.init();
				}
			}
			this.invalidate();
			return;
		}
		inAnimation=true;
		int lastx=Integer.parseInt(path.substring(0,2));
		int lasty=Integer.parseInt(path.substring(2,4));
		int newx=x;
		int newy=y;
		path=path.substring(4);
		if (path.length()>=4){
			newx=Integer.parseInt(path.substring(0,2));
			newy=Integer.parseInt(path.substring(2,4));
		}
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				showMoveAnimation(x,y);
			}
		}, 100);
		datamodel.value[newy][newx]=datamodel.value[lasty][lastx];
		datamodel.value[lasty][lastx]=-1;
		redrawSquar(lastx,lasty);
		redrawSquar(newx,newy);
	}

	private void redrawSquar(int x, int y) {
		this.invalidate(new Rect(x*cellWidth+XDelta,y*cellHeight+YDelta,(x+1)*cellWidth+XDelta,(y+1)*cellHeight+YDelta));
	}

	@Override
	public void onDraw(Canvas canvas){  
		super.onDraw(canvas); 
		if (datamodel==null) return;
		int rowNum=datamodel.value.length;
		int width=this.getWidth()/datamodel.value[0].length;
		int height=this.getHeight()/rowNum;
		if (width>height){
			XDelta=(this.getWidth()-datamodel.value[0].length*height)/2;
		}
		else{
			YDelta=(this.getHeight()-rowNum*width)/2;
		}
		width=Math.min(width, height);
		height=width;
		this.cellWidth=width;
		this.cellHeight=height;
		for (int i=0;i<datamodel.value.length;i++){
			for (int j=0;j<datamodel.value[i].length;j++){
				Paint paint=new Paint();
				paint.setColor(0xFF000000);
				paint.setStyle(Style.STROKE);
				paint.setStrokeWidth(1);
				canvas.drawRect(new Rect(j*width+XDelta, i*height+YDelta, (j+1)*width+XDelta,(i+1)*height+YDelta), paint);
				int idx=datamodel.value[i][j];
				if ((idx>=0) && (idx<bitmaps.size())){
					Bitmap bmp=bitmaps.get(idx);
					canvas.drawBitmap(bmp, new Rect(0,0,bmp.getWidth(),bmp.getHeight()),
							new Rect(j*width+XDelta,i*height+YDelta,(j+1)*width+XDelta,(i+1)*height+YDelta), null);
					
					if ((j==curFromX) && (i==curFromY)){
						canvas.drawBitmap(cover, new Rect(0,0,cover.getWidth(),cover.getHeight()),
								new Rect(j*width+XDelta,i*height+YDelta,(j+1)*width+XDelta,(i+1)*height+YDelta), null);
						//paint=new Paint();
						//paint.setColor(0xFFFF0000);
						//paint.setStrokeWidth(3);
						//canvas.drawCircle(j*width+width/2, i*height+height/2, width/4, paint);
					}
				}
			}
		}
	}

	public void setDataModel(JumpDataModel model) {
		this.datamodel=model;
		this.invalidate();
	}
	
}
