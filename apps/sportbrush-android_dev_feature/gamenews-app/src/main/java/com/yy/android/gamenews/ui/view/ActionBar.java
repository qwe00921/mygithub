package com.yy.android.gamenews.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yy.android.sportbrush.R;

/**
 * 
 * @author carlosliu
 * 
 */
public class ActionBar extends LinearLayout {

	private static final String LOG_TAG = ActionBar.class.getSimpleName();
	private View mLinLayt;
	private ImageView mLeftImageView;
	private ImageView mRightImageView;
	private TextView mTitleView;
	private TextView mLeftTextView;
	private TextView mRightTextView;
	private Context mContext;
	private View mLeft, mRight;

	public ActionBar(Context context) {
		super(context);
	}

	public ActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		parseAttrs(context, attrs);
	}

	private int mRightImageBorderColor = -1;
	private int mLeftImageBorderColor = -1;
	private int mTitleColor = -1;
	private float mTitleSize = -1;
	private boolean mIsRightRound;
	private boolean mIsLeftRound;
	private CharSequence mTitle;
	private CharSequence mLeftText;
	private CharSequence mRightText;
	private int mDrawableRightRes;
	private int mDrawableLeftRes;
	private int mBackgroundColor = -1;

	private void parseAttrs(Context context, AttributeSet attrs) {
		mContext = context;
		TypedArray types = mContext.obtainStyledAttributes(attrs,
				R.styleable.gamenews);
		mDrawableLeftRes = types.getResourceId(
				R.styleable.gamenews_drawableLeft, 0);
		mDrawableRightRes = types.getResourceId(
				R.styleable.gamenews_drawableRight, 0);
		mBackgroundColor = types.getResourceId(R.styleable.gamenews_background,
				-1);
		mTitle = types.getText(R.styleable.gamenews_title);
		mLeftText = types.getText(R.styleable.gamenews_leftText);
		mRightText = types.getText(R.styleable.gamenews_rightText);
		mIsRightRound = types.getBoolean(
				R.styleable.gamenews_isRightImageRound, false);
		mIsLeftRound = types.getBoolean(R.styleable.gamenews_isLeftImageRound,
				false);

		mLeftImageBorderColor = types.getColor(
				R.styleable.gamenews_leftBorderColor, -1);
		mRightImageBorderColor = types.getColor(
				R.styleable.gamenews_rightBorderColor, -1);

		mTitleColor = types.getColor(R.styleable.gamenews_titleColor, -1);
		mTitleSize = types.getDimension(R.styleable.gamenews_titleSize, -1);

		types.recycle();
	}

	private void init() {
		View container = inflate(mContext, R.layout.actionbar, this);

		if (container == null) {
			Log.e(LOG_TAG, "[init] container is null when inflater res id:"
					+ R.layout.actionbar);
			return;
		}

		if (mBackgroundColor != -1) {
			mLinLayt = container.findViewById(R.id.actionbar_container);
			mLinLayt.setBackgroundResource(mBackgroundColor);
		}
		mLeft = container.findViewById(R.id.left_container);
		mRight = container.findViewById(R.id.right_container);
		mTitleView = (TextView) container.findViewById(R.id.actionbar_title);
		mLeftTextView = (TextView) container
				.findViewById(R.id.actionbar_left_text);
		mRightTextView = (TextView) container
				.findViewById(R.id.actionbar_right_text);

		if (mIsRightRound) {
			mRightImageView = (ImageView) container
					.findViewById(R.id.actionbar_right_round);
			if (mRightImageBorderColor != -1) {
				((RoundImageView) mRightImageView)
						.setBorderColor(mRightImageBorderColor);
			}
		} else {
			mRightImageView = (ImageView) container
					.findViewById(R.id.actionbar_right);
		}

		if (mIsLeftRound) {
			mLeftImageView = (ImageView) container
					.findViewById(R.id.actionbar_left_round);
			if (mLeftImageBorderColor != -1) {
				((RoundImageView) mLeftImageView)
						.setBorderColor(mLeftImageBorderColor);
			}
		} else {
			mLeftImageView = (ImageView) container
					.findViewById(R.id.actionbar_left);
		}

		if (mRightImageView != null) {
			mRightImageView.setImageResource(mDrawableRightRes);
			mRightImageView.setVisibility(View.VISIBLE);
		}
		if (mLeftImageView != null) {
			mLeftImageView.setImageResource(mDrawableLeftRes);
			mLeftImageView.setVisibility(View.VISIBLE);
		}
		if (mLeftText != null) {
			mLeftTextView.setVisibility(View.VISIBLE);
			mLeftTextView.setText(mLeftText);
		} else {
			mLeftTextView.setVisibility(View.GONE);
		}
		if (mRightText != null) {
			mRightTextView.setVisibility(View.VISIBLE);
			mRightTextView.setText(mRightText);
		} else {
			mRightTextView.setVisibility(View.GONE);
		}

		if (mTitleColor != -1) {
			mTitleView.setTextColor(mTitleColor);
		}
		if (mTitleSize != -1) {
			mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
		}
		Log.i("actionbar", String.format("setTextSize %f", mTitleSize));

		mTitleView.setText(mTitle);
	}

	@Override
	protected void onFinishInflate() {
		init();
		super.onFinishInflate();
	}

	public void setTitle(CharSequence title) {
		if (mTitleView != null) {
			mTitleView.setText(title);
		}
	}

	public void setTitle(String title) {
		if (mTitleView != null) {
			mTitleView.setText(title);
		}
	}

	public void setTitle(int res) {
		if (mTitleView != null) {
			mTitleView.setText(res);
		}
	}

	public void setOnLeftClickListener(OnClickListener leftListener) {
		mLeft.setOnClickListener(leftListener);
	}

	public void setLeftVisibility(int visibility) {
		if (mLeftImageView != null) {
			mLeftImageView.setVisibility(visibility);
		}
	}

	public void setLeftImageResource(int resource) {
		if (mLeftImageView != null) {
			mLeftImageView.setImageResource(resource);
		}
	}

	public void setOnRightClickListener(OnClickListener leftListener) {
		mRight.setOnClickListener(leftListener);
	}

	public void setRightVisibility(int visibility) {
		if (mRightImageView != null) {
			mRightImageView.setVisibility(visibility);
		}
	}

	public void setRightImageResource(int resource) {
		if (mRightImageView != null) {
			mRightImageView.setVisibility(View.VISIBLE);
			mRightImageView.setImageResource(resource);
		}
	}

	public void setRightTextVisibility(int visibility) {
		if (mRightTextView != null) {
			mRightTextView.setVisibility(visibility);
		}
	}

	public void setOnRightTextClickListener(OnClickListener leftListener) {
		if (mRightTextView != null) {
			mRightTextView.setOnClickListener(leftListener);
		}
	}

	public TextView getRightTextView() {
		return mRightTextView;
	}

	public void setRightTextResource(int resource) {
		if (mRightTextView != null) {
			mRightTextView.setVisibility(View.VISIBLE);
			mRightTextView.setText(resource);
			mRightImageView.setVisibility(View.INVISIBLE);
		}
	}

	public void showLeftImgBorder(boolean show) {
		if (mLeftImageView instanceof RoundImageView) {

			((RoundImageView) mLeftImageView).showOutsideBorder(show);
		}
	}

	public ImageView getRightImageView() {
		return mRightImageView;
	}

	public ImageView getLeftImageView() {
		return mLeftImageView;
	}

	public void setActionBarBgColor(int color) {
		if (mLinLayt != null) {
			mLinLayt.setBackgroundColor(color);
		}
	}
}
