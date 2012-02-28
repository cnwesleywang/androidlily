package net.yebaihe.sdk;

import java.util.Random;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.telephony.TelephonyManager;

public class SdkUtils {
    public static int getRandomInt(int min,int max)
    {
        try {
            //Give the currentTimeMillis some time for the seed
            Thread.sleep(2);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        Random randomizer = new Random(System.currentTimeMillis());
        return randomizer.nextInt(max-min+1)+min;
    }
    
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    
    public static Bitmap toGrayscale(Bitmap bmpOriginal, boolean recycle) {
    	int width, height;
    	height = bmpOriginal.getHeight();
    	width = bmpOriginal.getWidth();    

    	Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
    	Canvas c = new Canvas(bmpGrayscale);
    	Paint paint = new Paint();
    	ColorMatrix cm = new ColorMatrix();
    	cm.setSaturation(0);
    	ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
    	paint.setColorFilter(f);
    	c.drawBitmap(bmpOriginal, 0, 0, paint);
    	if (recycle){
    		bmpOriginal.recycle();
    	}
    	return bmpGrayscale;
    }
    
	public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
                // ---get the package info---
                PackageManager pm = context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
                return ""+pi.versionName;
        } catch (Exception e) {
                //Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
	}	
	public static String getIMEI(Context context) {
    	TelephonyManager telephonyManager=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    	String imei=telephonyManager.getDeviceId();	  
    	return imei;
	}


    
}
