package com.tencent.djcity.lib.ui;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AutoHeightImageView extends ImageView {

	public HashMap<String, String>	mCustomInfo = new HashMap<String, String>();
	
	public AutoHeightImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public AutoHeightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    } 
  
    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
        int width = MeasureSpec.getSize(widthMeasureSpec);   
        this.setMeasuredDimension(width, width);  
    }  

}
