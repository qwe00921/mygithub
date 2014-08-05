package com.icson.lib.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class MultiLineLayout extends LinearLayout {

	public MultiLineLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MultiLineLayout(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		mLineLayoutList = new ArrayList<LinearLayout>();
	}

	private static final int DEFAULT_LINE_SIZE = 3;
	private int mLineSize = DEFAULT_LINE_SIZE;
	
	public int getLineSize() {
		return mLineSize;
	}

	public void setLineSize(int lineSize) {
		this.mLineSize = lineSize;
	}

	private List<LinearLayout> mLineLayoutList;
	private LinearLayout mCurrentLine;
	
	private LinearLayout getCurrentLineForAdding() {
	
		int count = getCurrentLineItemCount();//mCurrentLine.getChildCount();
		if(mCurrentLine == null || count % mLineSize == 0) {

			mCurrentLine = newline();
		}
		
		return mCurrentLine;
	}
	
	private int getCurrentLineItemCount() {
		if(mCurrentLine == null) {
			return 0;
		}
		return mCurrentLine.getChildCount();
	}
	
	private LinearLayout newline() {
		LinearLayout newLine = new LinearLayout(getContext());
		
		LayoutParams lineParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		newLine.setLayoutParams(lineParams);
		newLine.setWeightSum(mLineSize);
		super.addView(newLine);
		
		mLineLayoutList.add(newLine);
		
		return newLine;
	}
	
	private int indexOfLine(LinearLayout layout) {
		return mLineLayoutList.indexOf(layout);
	}
	
	private boolean isFirstLine(LinearLayout layout) {
		return indexOfLine(layout) == 0;
	}
	
	private boolean isLastLine(LinearLayout layout) {
		return indexOfLine(layout) == mLineLayoutList.size() - 1;
	}
	
	private boolean isFirstItemInLine() {
		return mCurrentLine.getChildCount() == 0;
	}
	
	private boolean isLastItemInLine() {
		return mCurrentLine.getChildCount() == mLineSize - 1;
	}
	
	@Override
	public void addView(View child) {
		LinearLayout layout = getCurrentLineForAdding();
		
		int leftMargin = vertical;
		int rightMargin = vertical;
		int topMargin = horizontal;
		int bottomMargin = horizontal;
		
		if(isFirstItemInLine()) {
			leftMargin = 0;
		}
		if(isLastItemInLine()) {
			rightMargin = 0;
		}
		if(isFirstLine(mCurrentLine)) {
			topMargin = 0;
		}
		if(isLastLine(mCurrentLine)) {
			bottomMargin = 0;
		}
		
		LayoutParams params = (LayoutParams) child.getLayoutParams();
		if(params == null) {
			params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
			child.setLayoutParams(params);
		}
		params.weight = 1;
		params.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);

		layout.addView(child);
	}

	private int vertical;
	private int horizontal;
	public void setLineItemMargin(int v, int h) {
		vertical = v;
		horizontal = h;
	}
	
	@Override
	public void removeAllViews() {
		for(LinearLayout layout : mLineLayoutList) {
			layout.removeAllViews();
			removeView(layout);
		}
		
		mLineLayoutList.clear();
	}
	
	public View getViewAt(int pos) {
		int line = pos / mLineSize;
		int row = pos % mLineSize;
		View view = null;
		if(line < mLineLayoutList.size()) {
			LinearLayout lineLayout = mLineLayoutList.get(line);
			if(row < lineLayout.getChildCount()) {
				view = lineLayout.getChildAt(row);
			}
		}
		
		return view;
	}
	
//	@Override
//	public int getChildCount() {
//		int count = 0;
//		for(LinearLayout layout : mLineLayoutList) {
//			count += layout.getChildCount();
//		}
//		
//		return count;
//	}
}
