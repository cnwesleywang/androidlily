package net.yebaihe.puzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

public class DragView extends ImageView {

    private final LayoutParams  mLayoutParams;

    public DragView(Context context, Bitmap bitmap) {
        super(context);

        mLayoutParams = new LayoutParams();

        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;

        mLayoutParams.height = LayoutParams.WRAP_CONTENT;
        mLayoutParams.width  = LayoutParams.WRAP_CONTENT;

        mLayoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE
                            | LayoutParams.FLAG_NOT_TOUCHABLE;

        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.windowAnimations = 0;

        mLayoutParams.alpha = 0.5f;

        setImageBitmap(bitmap);

        setLayoutParams(mLayoutParams);
    }

    public void move(int x, int y) {
        mLayoutParams.x = x;
        mLayoutParams.y = y;
    }
}
