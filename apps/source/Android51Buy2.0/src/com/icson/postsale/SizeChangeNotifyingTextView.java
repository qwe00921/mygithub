package com.icson.postsale;

import java.lang.ref.WeakReference;

import com.icson.util.Log;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;


public class SizeChangeNotifyingTextView extends TextView {

	public SizeChangeNotifyingTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public SizeChangeNotifyingTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public SizeChangeNotifyingTextView(Context context) {
		super(context);
	}

	//当 textview setText并绘制出来后，会onSizeChanged方法，该方法表明TextView.getLineCount()可以获得正确的值
	public interface OnSizeChangedListener {
		public void onSizeChanged(int w, int h, int oldw, int oldh);
	}
	
	private OnSizeChangedListener mListener;
    public void setOnSizeChangedListener(OnSizeChangedListener listener) {
    	mListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Message msg = mUIHandler.obtainMessage(MSG_NOTIFY_SIZE_CHANGED);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_WIDTH, w);
        bundle.putInt(KEY_HEIGHT, h);
        bundle.putInt(KEY_OLD_WIDTH, oldw);
        bundle.putInt(KEY_OLD_HEIGHT, oldh);
        mUIHandler.sendMessage(msg);
    }
    
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_OLD_WIDTH = "old_width";
    private static final String KEY_OLD_HEIGHT = "old_height";
    
	private UIHandler mUIHandler = new UIHandler(this);
	public static final int MSG_NOTIFY_SIZE_CHANGED = 2001;
	private static final String TAG = SizeChangeNotifyingTextView.class.getSimpleName();
	private static class UIHandler extends Handler {
		private WeakReference<SizeChangeNotifyingTextView> mRef;
		public UIHandler(SizeChangeNotifyingTextView parent) {
			mRef = new WeakReference<SizeChangeNotifyingTextView>(parent);
		}
		
		@Override
		public void handleMessage(Message msg) {
			if(msg == null) {
				Log.e(TAG, "[handleMessage] msg is null!");
				return;
			}
			
			SizeChangeNotifyingTextView activity = mRef.get();
			if(activity == null) {
				Log.w(TAG, "[handleMessage] activity is null when handle message, the activity should be destoryed already");
				return;
			}
			int what = msg.what;
			switch(what) {
				case MSG_NOTIFY_SIZE_CHANGED: {
			        if (activity.mListener != null) {
			        	Bundle bundle = msg.getData();
			        	activity.mListener.onSizeChanged(bundle.getInt(KEY_WIDTH), bundle.getInt(KEY_HEIGHT), bundle.getInt(KEY_OLD_WIDTH), bundle.getInt(KEY_OLD_WIDTH));
			        }
				}
				default: {
					// do nothing
					break;
				}
			}
		}
	}
}
