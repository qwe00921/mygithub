package com.icson.my.order.evaluate;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.icson.R;
import com.icson.item.ItemActivity;
import com.icson.lib.ILogin;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.model.OrderProductModel;
import com.icson.lib.ui.UiUtils;
import com.icson.statistics.StatisticsConfig;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class OrderEvaluateActivity extends BaseActivity  implements ImageLoadListener  {
	private final int 	AJAX_ID_REVIEW 				= 0x100;
	private final int 	AJAX_ID_VOTES  				= (AJAX_ID_REVIEW + 1);
	public static final String ORDER_PRODUCT_MODEL 	= "OrderProductModel";
	
	private VoteOptionParser parser;
	private OrderProductModel model;
	private EditText editText;
	private TextView mAdviseWordNum;
	private ImageLoader mAsyncImageLoader;
	
	int [] votes = {100540,100539,100538,100537,100536};
	
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Intent aintent = getIntent();
		if(null == aintent)
		{
			finish();
			return;
		}
		
		setContentView(R.layout.activity_my_order_product_evaluate);
		this.loadNavBar(R.id.my_order_navbar);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		model = (OrderProductModel) aintent.getSerializableExtra(ORDER_PRODUCT_MODEL);
		
		initVotes();
		
		mAsyncImageLoader = new ImageLoader(this, true);
		loadImage((ImageView) findViewById(R.id.order_imageview_pic), IcsonProImgHelper.getAdapterPicUrl(model.getProductCharId(), 80));
		((TextView) findViewById(R.id.order_textview_name)).setText(model.getNameNoHTML());
		((TextView) findViewById(R.id.order_textview_price)).setText(getString(R.string.rmb) + ToolUtil.toPrice(model.getShowPrice(), 2));
		findViewById(R.id.product_list_item).setOnClickListener(this);
		findViewById(R.id.evaluateBtn).setOnClickListener(this);
		
		mAdviseWordNum = (TextView)findViewById(R.id.worldNum_TextView);
		editText = (EditText) findViewById(R.id.evaluate_editText);
		editText.clearFocus();
		editText.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start,int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int count,int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				mAdviseWordNum.setText(s.length() + "/" + 1000);
			}	
		});
		
//		ToolUtil.sendTrack(OrderEvaluateActivity.class.getName(), OrderEvaluateActivity.class.getName(),getString(R.string.tag_OrderEvaluateActivity)+"01011");
	}
	
	private void initVotes() {
		//http://app.51buy.com/json.php?mod=aitem&act=getvotes&pid=311033
		Ajax ajax = ServiceConfig.getAjax(Config.URL_ITEM_GETVOTES, model.getProductId());
		if( null == ajax )
			return ;
		parser = new VoteOptionParser();
		ajax.setParser(parser);
		ajax.setId(AJAX_ID_VOTES);
		ajax.setOnErrorListener(this);
		ajax.setOnSuccessListener(new OnSuccessListener<ArrayList<VoteOptionModel>>() {
			@Override
			public void onSuccess(ArrayList<VoteOptionModel> v, Response response) {
				if( !parser.isSuccess() ) {
					UiUtils.makeToast(OrderEvaluateActivity.this, TextUtils.isEmpty(parser.getErrMsg()) ? Config.NORMAL_ERROR: parser.getErrMsg());
					return;
				}
				if(v!=null && v.size()>0){
					
					for(VoteOptionModel model:v){
						//满意
						if(model.order == 1 ){
							votes[model.group_id-1] = model.option_id;
						}
					}
				}
			}
			
		});
		addAjax(ajax);
		ajax.send();
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.product_list_item:

			Bundle param = new Bundle();
			param.putLong(ItemActivity.REQUEST_PRODUCT_ID, model.getProductId());
			ToolUtil.startActivity(OrderEvaluateActivity.this, ItemActivity.class, param);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_OrderEvaluateActivity), ItemActivity.class.getName(), getString(R.string.tag_ItemActivity), "03011", String.valueOf(model.getProductId()));
			break;

		case R.id.evaluateBtn:
			
			String content = editText.getText().toString().trim();
			if (content.length() < 5) {
				UiUtils.makeToast(this, "请输入5个字以上评论内容.");
				return;
			}
			RatingBar ratingBar = (RatingBar)findViewById(R.id.evaluate_ratingBar);
			int rate = (int) ratingBar.getRating();
			if (rate == 0) {
				UiUtils.makeToast(this, "请给商品评分(1~5)");
				return;
			}
			
			Ajax ajax = ServiceConfig.getAjax(Config.URL_ADD_COMMENT, ILogin.getLoginUid());
			if( null == ajax )
				return ;
			
			ajax.setId(AJAX_ID_REVIEW);
			ajax.setData("pid", model.getProductId());
			ajax.setData("content", content);
			ajax.setData("satisfaction", rate);
			//性价比,安全性,使用效果,材质工艺,外观设计
			StringBuilder sb = new StringBuilder();
			String div="";
			for(int vote:votes){
				sb.append(div).append(vote);
				div=",";
			}
			
			ajax.setData("votes", sb.toString());
			ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
				@Override
				public void onSuccess(JSONObject v, Response response) {
					closeProgressLayer();
					final int errno = v.optInt("errno", -1);

					StatisticsEngine.alert("review", StatisticsConfig.PRIORITY_WARN, errno, v.optString("data"), ""+model.getProductId(), ILogin.getLoginUid());
					if (errno != 0) {
						UiUtils.makeToast(OrderEvaluateActivity.this, v.optString("data", Config.NORMAL_ERROR));
						return;
					}

					UiUtils.makeToast(OrderEvaluateActivity.this, "谢谢您的反馈",true);
					Intent data = new Intent();
					data.putExtra(OrderEvaluateActivity.ORDER_PRODUCT_MODEL, model);
					setResult(RESULT_OK,data);
					finish();

				}
			});
			ajax.setOnErrorListener(this);
			addAjax(ajax);
			ajax.send();
			
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_OrderEvaluateActivity), OrderEvaluateActivity.class.getName(), getString(R.string.tag_OrderEvaluateActivity), "02011", String.valueOf(model.getProductId()));
			break;
		}

	}
	
	private void loadImage(ImageView view, String url) {
		final Bitmap data = mAsyncImageLoader.get(url);
		if (data != null) {
			view.setImageBitmap(data);
			return;
		}
		view.setImageResource(mAsyncImageLoader.getLoadingId());
		mAsyncImageLoader.get(url, this);
	}
	
	@Override
	public void onError(Ajax ajax, Response response)
	{
		super.onError(ajax, response);
		if( null != response && AJAX_ID_REVIEW == response.getId() )
		{
			StatisticsEngine.alert("review", StatisticsConfig.PRIORITY_WARN, response.getHttpStatus(), "", ""+model.getProductId(), ILogin.getLoginUid());
		}
	}
	
	@Override
	public void onLoaded(Bitmap image, String url) {
		if (image != null) {
			((ImageView) findViewById(R.id.order_imageview_pic)).setImageBitmap(image);
			return;
		}
	}
	
	@Override
	public void onError(String strUrl) {
	}
	
	@Override
	public void onDestroy()
	{
		if( null != mAsyncImageLoader )
		{
			mAsyncImageLoader.cleanup();
			mAsyncImageLoader = null;
		}
		
		super.onDestroy();
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_OrderEvaluateActivity);
	}
}
