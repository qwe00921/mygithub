package com.tencent.djcity.my;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.more.GameInfo;
import com.tencent.djcity.my.OrderDetailModel.PackageModel;
import com.tencent.djcity.my.OrderDetailModel.PackageModel.GoodModel;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.AppUtils;
import com.tencent.djcity.util.AppUtils.DescProvider;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ImageLoadListener;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class OrderDetailActivity extends BaseActivity {
	public final static String ORDER_SERIAL = "order_serial";

	private ListView mListView;
	private OrderDetailParser mParser;
	private OrderDetailAdapter mAdapter;
	
	private ImageView mGameIcon;
	private TextView mGameInfo;
	private TextView mOrderStatus;
	private TextView mOrderNum;
	private TextView mOrderTime;
	private TextView mOrderPayPrice;
	private TextView mOrderPrice;
	private TextView mOrderDiscount;
	
	private ImageView mPackagePic;
	private TextView mPackageName;
	private Button mShareButton;
	
	private ImageLoader mImageLoader;
	private ArrayList<GoodModel> mModels;
	private PackageModel mPackageModel;
	private OrderDetailModel mOrderDetailModel;
	
	private String mSerialNum;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_order_detail);
		
		initUI();
		
		this.loadNavBar(R.id.orderdetail_navbar);
		
		mSerialNum = getIntent().getStringExtra(ORDER_SERIAL);
		mImageLoader = new ImageLoader(this, Config.CHANNEL_PIC_DIR, true);
		
		mModels = new ArrayList<GoodModel>();
		mParser = new OrderDetailParser();
		mAdapter = new OrderDetailAdapter(this, mModels);
		mListView.setAdapter(mAdapter);
		
		setGameInfo();
		requestData();
		
	}
	
	private void initUI(){
		mListView = (ListView) findViewById(R.id.orderdetail_good);
		
		View headerView = this.getLayoutInflater().inflate(R.layout.order_detail_header, null);
		View footerView = this.getLayoutInflater().inflate(R.layout.order_detail_footer, null);
		mListView.addHeaderView(headerView);
		mListView.addFooterView(footerView);
		
		mGameIcon = (ImageView) findViewById(R.id.game_icon);
		mGameInfo = (TextView) findViewById(R.id.game_name);
		mOrderStatus = (TextView) findViewById(R.id.orderdetail_status);
		mOrderNum = (TextView) findViewById(R.id.orderdetail_num);
		mOrderTime = (TextView) findViewById(R.id.orderdetail_time);
		mOrderPayPrice = (TextView) findViewById(R.id.orderdetail_payprice);
		mOrderPrice = (TextView) findViewById(R.id.orderdetail_price);
		mOrderDiscount = (TextView) findViewById(R.id.orderdetail_discount);
		
		mPackageName = (TextView) findViewById(R.id.orderdetail_pg_name);
		mPackagePic = (ImageView) findViewById(R.id.orderdetail_pic);;
		mShareButton = (Button) findViewById(R.id.button_share);
		mShareButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				AppUtils.shareAppInfo(OrderDetailActivity.this, getString(R.string.share_content),
						"http://www.baidu.com", "", new DescProvider(){
							@Override
							public String getDesc(String strPackageName) {
								
								return null;
							}
				});
			}
		});
	}
	
	private void setGameInfo(){
		String strUrl = null;
		String strGameInfo = "";
		GameInfo info = GameInfo.getGameInfoFromPreference();
		if(info != null) {
			strUrl = info.getBizImg();
			strGameInfo = info.getDescription();
		}
		
		if(TextUtils.isEmpty(strUrl) && TextUtils.isEmpty(strGameInfo)) {
			findViewById(R.id.orderlist_gameinfo).setVisibility(View.GONE);
		}
		
		if(TextUtils.isEmpty(strUrl)) {
			mGameIcon.setVisibility(View.GONE);
		}else{
			final Bitmap data = mImageLoader.get(strUrl);
			if (data != null) {
				mGameIcon.setImageBitmap(data);
				return;
			}
			
			mGameIcon.setImageBitmap(mImageLoader.getLoadingBitmap(this));
			mImageLoader.get(strUrl, new ImageLoadListener() {
				
				@Override
				public void onLoaded(Bitmap aBitmap, String strUrl) {
					mGameIcon.setImageBitmap(aBitmap);
				}
				
				@Override
				public void onError(String strUrl) {
					
				}
			});
		}
		
		mGameInfo.setText(strGameInfo);
		
	}
	
	private void requestData() {
		showLoadingLayer();
		Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/orderDetail.php");
		
		if(ajax == null) {
			return;
		}
		
		ajax.setData("serial", mSerialNum);
		ajax.setData("uin", ILogin.getLoginUin());
		
		ajax.setOnErrorListener(this);
		ajax.setOnSuccessListener(new OnSuccessListener<OrderDetailModel>() {
			public void onSuccess(OrderDetailModel orderModel, Response response) {
				closeLoadingLayer();
				
				if(!mParser.isSuccess()) {
					if(null != mModels) {
						mModels.clear();
					}
					
					mAdapter.notifyDataSetChanged();
					return;
				}
				
				mOrderDetailModel = orderModel;
				if(null != mOrderDetailModel && null != mOrderDetailModel.getPackageModels() && 0 != mOrderDetailModel.getPackageModels().size()) {
					mPackageModel = mOrderDetailModel.getPackageModels().get(0);
					if(null != mPackageModel) {
						mModels.addAll(mPackageModel.getGoodsModels());
						mAdapter.notifyDataSetChanged();
					}
				}
				
				handleSuccess();

			};
		});
		
		ajax.setParser(mParser);
		
		ajax.send();
		addAjax(ajax);
	}
	
	private void handleSuccess() {
		if(null == mOrderDetailModel || null == mPackageModel) {
			return;
		}
		
		
		setStatus(mOrderStatus, mPackageModel.getStatus());
		mOrderNum.setText("订 单 号: " + mOrderDetailModel.getSerialNum());
		mOrderTime.setText("订单时间: " + mOrderDetailModel.getBuyTime());
		
		mPackageName.setText(mPackageModel.getGoodName());
		loadImage(mPackagePic, mPackageModel.getGoodUrl());
		
		mOrderPayPrice.setText(ToolUtil.getPriceStr(mOrderDetailModel.getPayPrice()) + "元");
		mOrderPrice.setText(ToolUtil.getPriceStr(mOrderDetailModel.getPrice()) + "元");
		mOrderDiscount.setText(ToolUtil.getPriceStr(mOrderDetailModel.getDiscount()) + "元");
	}
	
	private void loadImage(final ImageView view, String url) {
		Bitmap data = mImageLoader.get(url);
		if (data != null) {
			view.setImageBitmap(data);
			return;
		}
		
		view.setImageBitmap(mImageLoader.getLoadingBitmap(this));
		mImageLoader.get(url, new ImageLoadListener(){
			@Override
			public void onLoaded(Bitmap aBitmap, String strUrl) {
				view.setImageBitmap(aBitmap);
			}

			@Override
			public void onError(String strUrl) {
				
			}
		});
	}
	
	private void setStatus(TextView view, int nStatus) {
		if(null == view) {
			return;
		}
		
		String strStatus = "";
		if(0 == nStatus) {
			strStatus = "未付款";
		}else if(1 == nStatus) {
			strStatus = "发货中";
		}else if(3 == nStatus) {
			strStatus = "已发货";
		}else if(-1 == nStatus) {
			strStatus = "用户取消";
		}else if(-2 == nStatus) {
			strStatus = "系统取消";
		}
		
		view.setText("订单状态: " + strStatus);
	}
	
	@Override
	protected void onDestroy() {
		
		mParser = null;
		mAdapter = null;
		mListView = null;
		mPackageModel = null;
		mOrderDetailModel = null;
		
		if(null != mModels) {
			mModels.clear();
			mModels = null;
		}
		
		super.onDestroy();
	}
}
