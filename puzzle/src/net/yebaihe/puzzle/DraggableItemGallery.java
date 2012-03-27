package net.yebaihe.puzzle;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Gallery;

public class DraggableItemGallery extends Gallery {

    private boolean mDragging=false;
    private DragView mDragView;
    private DraggableView mDragViewOwner;

    private WindowManager mWindowManager;

    private boolean mScrollStarted;

    public DraggableItemGallery(Context context) {
        super(context);
        initialize();
    }

    public DraggableItemGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public DraggableItemGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public void initialize() {
        mWindowManager = (WindowManager)
            getContext().getSystemService("window");
    }

    private void startDraggingItem(DraggableView view, int x, int y) {
    	Log.d("", "startDraggingItem");
        mDragging      = true;
        mDragViewOwner = view;
        mDragView      = view.createDragView();

        mDragView.move(getLeft()+ x-mDragView.getWidth()/2, getTop()+y-mDragView.getHeight()/2);

        mWindowManager.addView(mDragView, mDragView.getLayoutParams());
    }

    private void continueDraggingItem(int x, int y) {
        DragView dragView = getDragView();

        dragView.move(getLeft()+ x-dragView.getWidth()/2, getTop()+y-dragView.getHeight()/2);
        mWindowManager.updateViewLayout(dragView, dragView.getLayoutParams());
    }

    private void stopDraggingItem(MotionEvent event) {
        mDragging = false;
    	mWindowManager.removeView(mDragView);
    	Log.d("", "ondrop:"+event.getX()+" "+event.getY());
        mDragViewOwner.afterDrop(getLeft()+event.getX()-mDragView.getWidth()/2,getTop()+event.getY()-mDragView.getHeight()/2);

        mDragView      = null;
        mDragViewOwner = null;
    }

    private DraggableView getDraggedItem() {
        return mDragViewOwner;
    }

    private DragView getDragView() {
        return mDragView;
    }

    private boolean isDraggingItem() {
        return (mDragging);
    }

    private void setScrolling(boolean scrolling) {
        mScrollStarted = scrolling;
        System.out.println("Scrolling " + scrolling);
    }

    private boolean isScrolling() {
        return mScrollStarted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

    	if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
        	Log.d("", "Action up");
    		setScrolling(false);

    		if (isDraggingItem()){
            	Log.d("", "isDraggingItem");
    			stopDraggingItem(event);
    		}
    	}

    	return super.onTouchEvent(event);
    }


    final Rect onScroll_tempRect = new Rect();

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    	Log.d("", "onScroll");
        if (isScrolling()) {
        	Log.d("", "isScrolling");
            if (isDraggingItem()) {
            	Log.d("", "isDraggingItem");
                int x = (int) e2.getX(),
                    y = (int) e2.getY();

                System.out.println("Moving to " + x + " " + y);

                continueDraggingItem(x, y);
                return true;

            } else {
            	Log.d("", "not isDraggingItem");
                /* Not dragging, let the Gallery handle the event */
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

        } else {
        	Log.d("", "not onScroll");
            setScrolling(true);
            boolean isVertical = (Math.abs(distanceY) > Math.abs(distanceX));

            if (isVertical) {
            	Log.d("", "isVertical");
                int x = (int) e1.getX(),
                    y = (int) e1.getY();

                View hitChild = null;

                // A tiny optimization, declared above this method
                final Rect hitRect = onScroll_tempRect;

                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.getHitRect(hitRect);

                    if (hitRect.contains(x, y)) {
                        hitChild = child;
                        break;
                    }
                }

                if (hitChild instanceof DraggableView) {
                    startDraggingItem((DraggableView) hitChild, x, y);
                    return true;
                }
                else{
                	Log.d("", "not DraggableView");
                }
            }

            /* Either the scroll is not vertical, or the point
             * of origin is not above a DraggableView. Again,
             * we let the Gallery handle the event.
             */
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

    }
}
