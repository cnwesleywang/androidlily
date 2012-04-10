package net.yebaihe.jumpball;

import java.util.ArrayList;

import android.graphics.Point;
import android.util.Log;

public class JumpDataModel {

	JumpballDelegate delegate;
	public static final int COLNUM=5;
	private static final int POINTS_DEL_NUM = 4;
	public int[][] value={
			{-1,0,0,0,1},
			{0,0,0,0,1},
			{0,2,0,-1,1},
			{0,-1,0,0,4},
			{0,0,3,0,1},
			{0,2,0,-1,1},
			{0,2,0,-1,1},
	};
	
	private int[][]lineBase=new int[800][101];
	
	//private ArrayList<String> lineBase=new ArrayList<String>();
	private int lastCheck=0;
	public int score=0;
	private int lineBaseLen=-1;
	
	JumpDataModel(){
		init();
	}
	
	public void randomAddPoint(int total) {
		for (int i=0;i<total;i++){
			addRandomPoint();
		}
	}

	private void addRandomPoint() {
		ArrayList<String> emptypoints=new ArrayList<String>();
		for (int i=0;i<value.length;i++){
			for (int j=0;j<value[i].length;j++){
				if (value[i][j]==-1){
					emptypoints.add(String.format("%02d%02d", i,j));
				}
			}
		}
		
		if (emptypoints.size()>0){
			int idx=(int)(Math.random()*emptypoints.size());
			int x=Integer.parseInt(emptypoints.get(idx).substring(0,2));
			int y=Integer.parseInt(emptypoints.get(idx).substring(2,4));
			value[x][y]=(int)(Math.random()*JumpballActivity.RES_NUM);
		}
	}

	/**
	 * Move One Point to Another Position if possible.
	 * @param curFromX 
	 * @param curFromY
	 * @param x
	 * @param y
	 * @return succ true,otherwise false
	 */
	public int[] move(int curFromX, int curFromY, int x, int y) {
		//lineBase.clear();
		for (int i=0;i<lineBase.length;i++){
			for (int j=0;j<lineBase[i].length;j++)
				lineBase[i][j]=-1;
			lineBase[i][0]=0;
		}
		lineBase[0][0]=1;//curFromX;
		lineBase[0][1]=curFromX;
		lineBase[0][2]=curFromY;
		lastCheck=0;
		lineBaseLen=1;
		int[] path=findPath(x,y);
		return path;
	}
	
	private int[] findPath(int x, int y) {
		
		//Log.d("", "curr line base is:");
		//for (int i=0;i<lineBase.size();i++){
		//	Log.d("", lineBase.get(i));
		//}
		
		int currlen=lineBaseLen;
		if (currlen>=800) return new int[0];//just for safe
		for (int i=lastCheck;i<currlen;i++){
			int lastx=lineBase[i][(lineBase[i][0]-1)*2+1];// Integer.parseInt(lastpos.substring(0, 2));
			int lasty=lineBase[i][(lineBase[i][0]-1)*2+2];// Integer.parseInt(lastpos.substring(2));
			int lastlastx=-1;
			int lastlasty=-1;
			if (lineBase[i][0]>=2){
				lastlastx= lineBase[i][(lineBase[i][0]-2)*2+1];
				lastlasty= lineBase[i][(lineBase[i][0]-2)*2+2];
			}
			if (lasty>0){
				if (checkPos(lastx,lasty-1,lastlastx,lastlasty,x,y,i)){
					return lineBase[i];
				}
			}
			if (lasty<value.length-1){
				if (checkPos(lastx,lasty+1,lastlastx,lastlasty,x,y,i)){
					return lineBase[i];
				}
			}
			if (lastx>0){
				if (checkPos(lastx-1,lasty,lastlastx,lastlasty,x,y,i)){
					return lineBase[i];
				}
			}
			if (lastx<value[lasty].length-1){
				if (checkPos(lastx+1,lasty,lastlastx,lastlasty,x,y,i)){
					return lineBase[i];
				}
			}
		}
		if (currlen==lineBaseLen) return new int[0];
		lastCheck=currlen;
		return findPath(x,y);
	}

	private boolean checkPos(int newx, int newy, int lastlastx, int lastlasty,
			int x, int y,int idx) {
		if ((newx==x)&& (newy==y)) return true;
		if ((newy!=lastlasty)||(newx!=lastlastx)){
			if (value[newy][newx]==-1){
				if (!inPath(newx,newy,idx)){
					if (!newPathExist(newx,newy,idx)){
						for (int i=1;i<lineBase[idx][0]*2+1;i++){
							lineBase[lineBaseLen][i]=lineBase[idx][i];
						}
						lineBase[lineBaseLen][0]=lineBase[idx][0]+1;
						lineBase[lineBaseLen][lineBase[idx][0]*2+1]=newx;
						lineBase[lineBaseLen][lineBase[idx][0]*2+2]=newy;
						lineBaseLen+=1;
					}
				}
			}
		}
		return false;
	}

	private boolean newPathExist(int newx, int newy, int idx) {
		for (int i=0;i<lineBaseLen;i++){
			if (i==idx) continue;
			boolean same=true;
			for (int j=1;j<lineBase[i][0]*2+1;j++){
				if (lineBase[i][j]!=lineBase[idx][j]){
					same=false;
					break;
				}
			}
			if (same){
				if (lineBase[i][0]==lineBase[idx][0]+1){
					if (lineBase[i][lineBase[idx][0]*2]==newx){
						if (lineBase[i][lineBase[idx][0]*2+1]==newy) return true;
					}
				}
			}
		}
		return false;
	}

	private boolean inPath(int x,int y, int idx) {
		for (int i=0;i<lineBase[idx][0];i++){
			if ((lineBase[idx][i*2+1]==x) && (lineBase[idx][i*2+2]==y)) return true;
		}
		return false;
	}

	/**
	 * check if any line a five or more in one line and need be reduced and remove it at once.
	 * @return more than one line reduces,return true
	 */
	public boolean checkAndRemoveLine() {
		ArrayList<Point> ps=new ArrayList<Point>();
		for (int i=0;i<value.length;i++){
			for (int j=0;j<value[i].length;j++){
				if (canErase(i,j)){
					ps.add(new Point(i,j));
				}
			}
		}
		
		for (int i=0;i<ps.size();i++){
			value[ps.get(i).x][ps.get(i).y]=-1;
		}
		
		score+=ps.size();
		if (delegate!=null) delegate.onScoreDataChange();
		
		return ps.size()>0;
	}

	private boolean canErase(int i, int j) {
		if (value[i][j]==-1) return false;
		
		int xl=0;
		for (int ii=i-1;ii>=0;ii--){
			if (value[ii][j]==value[i][j]) xl+=1;
			else break;
		}
		xl+=1;
		for (int ii=i+1;ii<value.length;ii++){
			if (value[ii][j]==value[i][j]) xl+=1;
			else break;
		}
		if (xl>=POINTS_DEL_NUM) return true;
		xl=0;
		for (int jj=j-1;jj>=0;jj--){
			if (value[i][jj]==value[i][j]) xl+=1;
			else break;
		}
		xl+=1;
		for (int jj=j+1;jj<value[i].length;jj++){
			if (value[i][jj]==value[i][j]) xl+=1;
			else break;
		}
		if (xl>=POINTS_DEL_NUM) return true;
		
		
		return false;
	}

	public boolean done() {
		for (int i=0;i<value.length;i++){
			for (int j=0;j<value[i].length;j++){
				if (value[i][j]==-1){
					return false;
				}
			}
		}
		return true;
	}

	public void init() {
		score=0;
		for (int i=0;i<value.length;i++){
			for (int j=0;j<value[i].length;j++){
				value[i][j]=-1;
			}
		}
		randomAddPoint(JumpballActivity.BALL_EACH_TIME);
	}
	
}
