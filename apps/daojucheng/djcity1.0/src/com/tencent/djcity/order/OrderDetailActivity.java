package com.tencent.djcity.order;

import org.json.JSONObject;

import com.tencent.djcity.R;
import com.tencent.djcity.home.recommend.ProductModel;
import com.tencent.djcity.item.ItemActivity;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.model.Account;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class OrderDetailActivity extends BaseActivity implements OnSuccessListener<JSONObject>{
    private String mOrderId;
    public static final String  ORDER_ID = "order_id";
    private TextView   mInfo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        if(null == intent || !intent.hasExtra(ORDER_ID))
        	finish();
        
        mOrderId = intent.getStringExtra(ORDER_ID);
        
        setContentView(R.layout.activity_my_orderdetail);
        mInfo = (TextView) this.findViewById(R.id.info);
        fetchOrderInfo();
        
    }

	private void fetchOrderInfo() {
		Account act = ILogin.getActiveAccount();
		if(null == act ||act.getUin() <=0)
		{
			UiUtils.makeToast(this, "Need login");
			return;
		}
		
		Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/api/daoju_app/orderDetail.php?uin=" + act.getUin() );
		
		if(ajax == null) {
			return;
		}
		
		ajax.setData("serial", mOrderId);
		ajax.setOnSuccessListener(this);
		ajax.setParser(new JSONParser());
		ajax.send();
		addAjax(ajax);
		
	}

	@Override
	public void onSuccess(JSONObject v, Response response) {
		mInfo.setText(v.toString());
	}
}