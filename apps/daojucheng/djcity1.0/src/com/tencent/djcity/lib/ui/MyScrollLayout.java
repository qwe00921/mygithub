package com.tencent.djcity.lib.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * æ­¤ç±»ä¸»è?????æ¨????æ»???¨å??å¹????ç±?
 * 
 * @author kunjiang
 * @Date 2012-03-01
 * 
 */
public class MyScrollLayout extends ViewGroup {
	Scroller mScroller;// æ»???¨æ?§å??
	VelocityTracker velocity;// ???åº???§å??
	int mCurScreen;// å½????å±?å¹?ä½?ç½?
	float mLastX;//???è¿????ä¸????X??????
	public static final int SNAP_VELOCITY = 600;
	
	public MyScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);// ???å§??????????
	}

	public MyScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);// ???å§??????????
	}

	public MyScrollLayout(Context context) {
		super(context);
		init(context);// ???å§??????????
	}

	public void init(Context context) {
		mScroller = new Scroller(context);// ???å»ºæ????¨æ?§å?¶ç??å¯¹è±¡
		mCurScreen = 0;// å½????å±?å¹?ä¸?0
	}

	/**
	 * å¯¹å?????å®¹å¤§å°?è¿?è¡?å®?ä¹?
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int childCount = getChildCount();// å¾???°å??ç»?ä»¶ç????°é??
		int width = MeasureSpec.getSize(widthMeasureSpec);
		for (int i = 0; i < childCount; i++) {
			View childView = getChildAt(i);// å¾???°å??ç»?ä»?
			childView.measure(widthMeasureSpec, heightMeasureSpec);// è°????measureä¸ºå??ç»?ä»¶å¤§å°?èµ????
		}

		
		scrollTo(mCurScreen * width, 0);// ???å§????æ»???¨ä??ç½?ï¼?ä½¿å?¶æ????¨å?°ç??ä¸?ä¸???????
	}

	/**
	 * å¯¹å??å®¹ç??å¸?å±?è¿?è¡?å®?ä¹?
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if(changed){
			int childCount = getChildCount();//å¾???°å??ç»?ä»¶ç????°é??
			int childLeft = 0;
			for(int i = 0; i < childCount; i ++){
				View childView = getChildAt(i);//å¾???°å??ç»?ä»?
				int width = childView.getMeasuredWidth();//å¾???°å??ç»?ä»¶ç??å®½åº¦
				childView.layout(childLeft, 0, childLeft + width, childView.getMeasuredHeight());
				
				childLeft += width;
			}
		}
	}
	
	@Override
	public void computeScroll() {//å½???¶ç??ä»¶è??æ±?å­?ç»?ä»¶æ????¨å??è°???¨æ?¤æ?¹æ??
		if(mScroller.computeScrollOffset()){//å½???¨ç?»æ²¡??????æ­¢æ??
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());//å¦??????¨ç?»æ²¡??????æ­?  ??£ä??ä¸???´æ?´æ?°å??View??????
			postInvalidate();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float curX = event.getX();
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN://???ä¸????ä½????
			if(velocity == null ){
				velocity = VelocityTracker.obtain();//???å§???????åº???§å??
				velocity.addMovement(event);//å°?è§?å±?äº?ä»¶äº¤ç»????åº???§å??
			}
			if(!mScroller.isFinished()){//è¿?æ²¡å?????æ»???¨å?¨ç??  ä½????å·²ç?°å?°å??å¹?
				mScroller.abortAnimation();//???æ­¢å?¨ç??
			}
			mLastX = curX;
			break;

		case MotionEvent.ACTION_MOVE://ç§»å?¨æ??ä½????
			int distance_x = (int)(mLastX - curX);
			if(IsCanMove(distance_x)){//??¤æ??????????½ç§»???
				if(velocity != null ){
					velocity.addMovement(event);//å°?è§?å±?äº?ä»¶äº¤ç»????åº???§å??
				}
				mLastX = curX;
				scrollBy(distance_x, 0);
			}
			break;
		case MotionEvent.ACTION_UP://???èµ·æ??ä½????
			int velocityX = 0;
            if (velocity != null)
            {
            	velocity.addMovement(event); 
            	velocity.computeCurrentVelocity(1000);  
            	velocityX = (int) velocity.getXVelocity();
            }
                    
                
            if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {       
                // Fling enough to move left       
                snapToScreen(mCurScreen - 1);       
            } else if (velocityX < -SNAP_VELOCITY       
                    && mCurScreen < getChildCount() - 1) {       
                // Fling enough to move right       
                snapToScreen(mCurScreen + 1);       
            } else {       
                snapToDestination();       
            }      
            
           
            
            if (velocity != null) {       
            	velocity.recycle();       
            	velocity = null;       
            }       
            
            break;   
		}
		return true;//??????true  ???è¯´æ??   å·²å????????ä½?  ä¸???¨å????©å?????ä½?äº?
	}

	 public void snapToDestination() {    
	        final int screenWidth = getWidth();    

	        final int destScreen = (getScrollX()+ screenWidth/2)/screenWidth;    
	        snapToScreen(destScreen);    
	 }  
	
	 public void snapToScreen(int whichScreen) {    
	
	        // get the valid layout page    
	        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount()-1));    
	        if (getScrollX() != (whichScreen*getWidth())) {    
	                
	            final int delta = whichScreen*getWidth()-getScrollX();    
	        
	            mScroller.startScroll(getScrollX(), 0,     
	                    delta, 0, Math.abs(delta)*2);

	            
	            mCurScreen = whichScreen;    
	            invalidate();       // Redraw the layout    
	            
	        }    
	    }    

	/**
	 * @param distance_x ç§»å?¨è??ç¦?
	 * @return ????????½å??ç§»å??
	 */
	public boolean IsCanMove(int distance_x){
		//æ»???¨å????³æ?????å¦????distance_xå°?äº?0 å¹¶ä?? ???ç§»é??å°?äº?0äº? ???ä¸???½æ????¨ä??
		if(distance_x < 0 && getScrollX() < 0){
			return false;
		}
		//æ»???¨å????³æ?????å¦????æ»???¨å¤§äº?ç»?ä»?0 å¹¶ä?? ???ç§»é??å¤§ä??äº???????ç»?ä»¶ç??å®½åº¦?????? ??£ä?????è¿????false
		if(getScrollX() > (getChildCount() - 1) * getWidth() && distance_x > 0){
			return false;
		}
		return true;
	}
}
