package com.tencent.djcity.discover;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.tencent.djcity.R;

public class GuaGuaKaView extends TextView {

	private Canvas mCanvas = null;
    private Path mPath = null;
    private Paint mPaint = null;
    private Bitmap bitmap = null;
    private Bitmap mCover;
    
    private int mWidth;
    private int mHeight;
    /**
     * get the center rect of this view；split it into 16 small rect
     * ---------------------------------
     * |      ｜       |       ｜        |
     * |--------------------------------|
     * |      ｜       |       ｜        |
     * |--------------------------------|
     * |      ｜       |       ｜        |
     * |--------------------------------|
     * |      ｜       |       ｜        |
     * |--------------------------------|
     *
     * Path must get through 3/4 of them
     */
    private int mCot = 0;
    private int   mCorerectCount[] = {0,0,0,0, 
    		0,0,0,0,
    		0,0,0,0,
    		0,0,0,0};
    
    private int[] Xps = {0,0,0,0,0};
    private int[] Yps = {0,0,0,0,0};
    
    public GuaGuaKaView(Context context) {
        super(context);
        init(context);
    }
	public GuaGuaKaView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        init(context);
	}
	public GuaGuaKaView(Context context, AttributeSet attrs) {
		super(context, attrs);
        init(context);
	}
	
	public void reset() {
		mHasScratched = false;
		if(mCanvas != null) {
			mCanvas.drawBitmap(Bitmap.createScaledBitmap(mCover, mWidth, mHeight, true), 0, 0, new Paint());
		}
	}
	
	public boolean hasScratched() {
		return mHasScratched;
	}
    
    private void init(Context context) {
    	
		ViewTreeObserver vto = getViewTreeObserver();
		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			public boolean onPreDraw() {
				getViewTreeObserver().removeOnPreDrawListener(this);
				mHeight = getMeasuredHeight();
				mWidth = getMeasuredWidth();
				
//		        setBackgroundColor(0x00000000);
		        mPath = new Path();
		        bitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
		        mPaint = new Paint();
		        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		        mPaint.setAntiAlias(true);
		        mPaint.setDither(true);
		        mPaint.setStyle(Style.STROKE);
		        mPaint.setStrokeWidth(30);
		        mPaint.setStrokeCap(Cap.ROUND);
		        mPaint.setStrokeJoin(Join.ROUND);
		        mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		        mPaint.setAlpha(0);
		        
		        mCanvas = new Canvas(bitmap);
		        
		        Options options = new Options();
		        
		        /* 计算得到图片的高度 */
		        /* 这里需要主意，如果你需要更高的精度来保证图片不变形的话，需要自己进行一下数学运算 */
		        options.outWidth = mWidth;
		        options.outHeight = mHeight;
		        Xps[0] = mWidth/4;
		        Xps[1] = mWidth*3/8;
		        Xps[2] = mWidth/2;
		        Xps[3] = mWidth*5/8;
		        Xps[4] = mWidth*3/4;
		        
		        Yps[0] = mHeight/4;
		        Yps[1] = mHeight*3/8;
		        Yps[2] = mHeight/2;
		        Yps[3] = mHeight*5/8;
		        Yps[4] = mHeight*3/4;
		        
		        /* 这样才能真正的返回一个Bitmap给你 */
		        
		        options.inJustDecodeBounds = false;
		        
		        mCover = BitmapFactory.decodeResource(getResources(), R.drawable.bg_card_box, options);
		        //draw the cover
		        mCanvas.drawBitmap(Bitmap.createScaledBitmap(mCover, mWidth, mHeight, true), 0, 0, new Paint());
				return true;
			}
		});
    }
    
    private void setBackground() {
    }

    private boolean mHasScratched; //是否有刮开
    @Override
    protected void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
        mCanvas.drawPath(mPath, mPaint);
        if(mHasScratched) {
        	
        	mCanvas.drawPaint(mPaint);
        }
        
        canvas.drawBitmap(bitmap, 0, 0, null);
   }    
    
    int x = 0;
    int y = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
    	if(mHasScratched) {
    		return false;
    	}
    	
        int action = event.getAction();
        int currX = (int) event.getX();
        int currY = (int) event.getY();
        switch(action){
        	case MotionEvent.ACTION_DOWN:{
                mPath.reset();
                x = currX;
                y = currY;
                mPath.moveTo(x, y);
                break;
            }
            case MotionEvent.ACTION_MOVE:{
            	Log.e("move", ""+x+","+y + "-->" + currX+","+currY);
            	Log.e("rects", ""+ mCorerectCount[0] + "," + mCorerectCount[1] + "," + mCorerectCount[2] + "," + +mCorerectCount[3]);
            	Log.e("rects", ""+ mCorerectCount[4] + "," + mCorerectCount[5] + "," + mCorerectCount[6] + "," + +mCorerectCount[7]);
            	Log.e("rects", ""+ mCorerectCount[8] + "," + mCorerectCount[9] + "," + mCorerectCount[10] + "," + +mCorerectCount[11]);
            	Log.e("rects", ""+ mCorerectCount[12] + "," + mCorerectCount[13] + "," + mCorerectCount[14] + "," + +mCorerectCount[15]);
            	
            	
            	mPath.quadTo(x, y, currX, currY);
                x = currX;
                y = currY;
                checkInrects(x,y,currX,currY);
                postInvalidate();
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:{
                mPath.reset();
                mHasScratched = true;
            	if(mOnFinishScratchListener != null) {
            		mOnFinishScratchListener.onFinish();
            	}
            	
            	postInvalidate();
                break;
            }
        }
        return true;
    }
    
    
	/**
     * 
     * @param x2
     * @param y2
     * @param currX
     * @param currY
     */
	private void checkInrects(int x, int y, int currX, int currY) {
		int midx = (x + currX)/2;
		int midy = (y + currY)/2;
		
		for(int i=0 ; i<4; i++)
		{
			if(midx > Xps[i] && midx<=Xps[i+1])
			{
				for(int j=0 ; j<4; j++)
				{
					if(midy > Yps[j] && midy<=Yps[j+1])
					{
						if(mCorerectCount[i*4 + j] <=0)
							mCot++;
						mCorerectCount[i*4 + j] = 1;
						
					}
				}
			}
		}
	}
	
	public boolean isAllShow()
	{
		return mCot>=12;
	}

    private OnFinishScratchListener mOnFinishScratchListener;
    public void setOnFinishScratchListener(OnFinishScratchListener listener) {
    	mOnFinishScratchListener = listener;
    }
    public interface OnFinishScratchListener {
    	public void onFinish();
    }}
