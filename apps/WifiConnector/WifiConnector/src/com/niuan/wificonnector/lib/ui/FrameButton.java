package com.niuan.wificonnector.lib.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.niuan.wificonnector.R;

public class FrameButton extends FrameLayout {
	public FrameButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public FrameButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FrameButton(Context context) {
		super(context);
		init(context);
	}

	/**
	 * @param context
	 */
	private void init(Context context) {
		setClickable(true);
		LayoutInflater.from(context).inflate(R.layout.frame_button, this);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		findViewById(R.id.btn).setOnClickListener(l);
		super.setOnClickListener(l);
	}

	public void setTitle(CharSequence title) {
		((TextView) findViewById(R.id.btn)).setText(title);
	}
}
