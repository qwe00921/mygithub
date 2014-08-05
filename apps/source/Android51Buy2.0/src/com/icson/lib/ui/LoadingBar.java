package com.icson.lib.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.icson.R;
import com.icson.util.ToolUtil;

public class LoadingBar extends RelativeLayout {

	private static final String SIZE_VALIDATE[] = new String[] { "large", "small" };

	private String mSize;

	public LoadingBar(Context context) {
		super(context);
		initUI(context);
	}

	private String getValidatSize(String size) {
		if (size == null)
			return SIZE_VALIDATE[0];

		for (String piece : SIZE_VALIDATE) {
			if (piece.equals(size)) {
				return size;
			}
		}

		return SIZE_VALIDATE[0];
	}

	public LoadingBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LoadingBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.loadingbar, defStyle, 0);
		mSize = getValidatSize(a.getString(R.styleable.loadingbar_size));
		a.recycle();
		initUI(context);
	}

	private void initUI(Context context) {

		boolean isLarge = mSize.equals("large");
		FrameLayout layout = new FrameLayout(context);
		layout.setBackgroundColor(getResources().getColor(R.color.global_loading_color_default));
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ToolUtil.dip2px(context, isLarge ? 22 : 12), ToolUtil.dip2px(context, isLarge ? 41 : 22));
//		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

		setLayoutParams(lp);
		View view = new View(context);
		FrameLayout.LayoutParams viewLp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
		view.setBackgroundColor(getResources().getColor(R.color.global_loading_color_tran)); 
		layout.addView(view, viewLp);

		ImageView imageView = new ImageView(context);
		FrameLayout.LayoutParams imageViewLp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
	//	imageView.setScaleType(ScaleType.FIT_XY);
		imageView.setImageResource(isLarge ? R.drawable.i_global_loading_bg : R.drawable.i_global_loading_bg_small);
		layout.addView(imageView, imageViewLp);

		Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.1f, Animation.RELATIVE_TO_PARENT, 1.0f);
		animation.setDuration(1500);
		animation.setInterpolator(new DecelerateInterpolator(2.0f));
		animation.setRepeatCount(Animation.INFINITE);
		animation.setRepeatMode(Animation.RESTART);
		view.startAnimation(animation);

		addView(layout, lp);
	}

}
