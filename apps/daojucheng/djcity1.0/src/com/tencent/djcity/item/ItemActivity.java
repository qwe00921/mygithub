package com.tencent.djcity.item;

import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.home.recommend.ProductModel;
import com.tencent.djcity.home.recommend.ProductModel.Validate;
import com.tencent.djcity.lib.pay.PayCore;
import com.tencent.djcity.lib.pay.PayFactory;
import com.tencent.djcity.lib.pay.PayFactory.PayResponseListener;
import com.tencent.djcity.lib.ui.AppDialog;
import com.tencent.djcity.lib.ui.FlowLayout;
import com.tencent.djcity.lib.ui.NavigationBar;
import com.tencent.djcity.lib.ui.NavigationBar.OnLeftButtonClickListener;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.more.GameInfo;
import com.tencent.djcity.more.SelectHelper;
import com.tencent.djcity.order.OrderDetailActivity;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.ImageLoadListener;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.Utils;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class ItemActivity extends BaseActivity implements PayResponseListener {

	public static final String KEY_PROP_ID = "prop_id";
	
	private String mPropId;
	
	private ImageView mPropImgView;
	private TextView mTvPriceOld;
	private TextView mTvPriceQQ;
	private TextView mTvPriceWechat;
	private TextView mTvPropTitle;
	private TextView mTvItemDesc;
	private FlowLayout mDateLayout;
	private FlowLayout mPayTypeLayout;
	
	private int mCurrentPayType;
	private Button mDownBtn;
	private EditText mBuyCountEt;
	private Button mUpBtn;
	private Button mBuyBtn;
	private GameInfo mGameInfo;
	private PayCore mPayCore ;
	private ImageLoader mAsyncImageLoader;
	
	private TextView mAreaInfoView;
	private TextView mChangeAreaBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_detail);
		
		Intent intent = getIntent();
		mPropId = intent.getStringExtra(KEY_PROP_ID);
		mGameInfo =  GameInfo.getGameInfoFromPreference();
		
		mAsyncImageLoader = new ImageLoader(this, true);
		initUI();
	}
	
	private int currentBuyCount = 1;
	private void initUI() {
		mTvPriceOld = (TextView) findViewById(R.id.list_textview_price_old);
		Paint pPaint = mTvPriceOld.getPaint();
		pPaint.setFlags(pPaint.getFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		mPayTypeLayout = (FlowLayout) findViewById(R.id.item_linear_paytypes);
		
		mTvPriceQQ = (TextView) findViewById(R.id.list_textview_price_qq);
		mTvPriceWechat = (TextView) findViewById(R.id.list_textview_price_wechat);
		mTvPropTitle = (TextView) findViewById(R.id.list_textview_name);
		mPropImgView = (ImageView) findViewById(R.id.list_image_pic);
		mTvItemDesc = (TextView) findViewById(R.id.item_detail_propdesc);
		mDateLayout = (FlowLayout) findViewById(R.id.item_linear_attrs);
		mDownBtn = (Button) findViewById(R.id.item_detail_downBtn);
		mDownBtn.setOnClickListener(this);
		mUpBtn = (Button) findViewById(R.id.item_detail_upBtn);
		mUpBtn.setOnClickListener(this);
		mAreaInfoView = (TextView) findViewById(R.id.item_detail_role_info);
		mBuyCountEt = (EditText) findViewById(R.id.item_detail_edittext_buy_count);
		
		mBuyCountEt.setText("" + currentBuyCount);
		mNavBar = (NavigationBar) findViewById(R.id.item_detail_navbar);
		
		mNavBar.setOnLeftButtonClickListener(new OnLeftButtonClickListener() {
			
			@Override
			public void onClick() {
				onBackPressed();
			}
		});
		
		mBuyCountEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				int buyCount = 0;
				
				try {
					buyCount = Integer.parseInt(s.toString());
				} catch (NumberFormatException e) {
				}
				
				if(isValidNumber(buyCount))	{
					currentBuyCount = buyCount;
				} else {
					if(isValidNumber(currentBuyCount)) {
						mBuyCountEt.setText(String.valueOf(currentBuyCount));
					}
				}
				
			}
			
			private boolean isValidNumber(int want) {
				List<Validate> valiList = mProduct.getValidateList();
				int todayLimit = mProduct.getTodayLimit();
				int totalLimit = mProduct.getTotalLimit();
				
				int todayBought = 0;
				int left = 0;
				int totalBought = 0;
				
				// 拿到当前商品valiDate
				Validate valiDate = (Validate) Utils.getObjectSafely(valiList, mCurrentValiDatePos);
				if(valiDate != null) {
					todayBought = valiDate.getTodayBought();
					left = valiDate.getLeft();
					totalBought = valiDate.getBought();
				}
				
				/*
				 *  如果设置了totalLimit, 则输入的数量必须满足：
				 *  1. 需求量小于等于剩余量
				 *  2. 需求量小于等于总限额减去已经购买的数量
				 */
				if(totalLimit != 0) {
					if(left >= 0) {
						if(want > left) {
							
							return false;
						}
					}
					if(want > totalLimit - totalBought) {
						return false;
					}
				}
				/*
				 *  如果设置了odayLimit, 则输入的数量必须满足：
				 *  1. 需求量小于等于剩余量
				 *  2. 需求量小于等于今日限额减去今日已经购买的数量
				 */
				if(todayLimit != 0) {
					if(left >= 0) {
						if(want > left) {
							return false;
						}
					}
					if(todayBought >= 0) {
						if(want > todayLimit - todayBought) {
							return false;
						}
					}
				}
				
				if(want <= 0) {
					return false;
				}

				
				return true;
			}
		});
		
		mChangeAreaBtn = (TextView) findViewById(R.id.item_detail_change_area_btn);
		mChangeAreaBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				SelectHelper.changeArea(ItemActivity.this, mGameInfo, true);
			}
		});
		
		mBuyBtn = (Button) findViewById(R.id.buy_now_btn);
		mBuyBtn.setOnClickListener(this);
		findViewById(R.id.send_present).setOnClickListener(this);
		
	}

	@Override
	protected void onResume() {
		if (!mGameInfo.needBind()) {
			mAreaInfoView.setVisibility(View.VISIBLE);
			mAreaInfoView.setText(mGameInfo.getDescription());
			
			android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) mChangeAreaBtn.getLayoutParams();
			
			if(params != null) {
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			}
			mChangeAreaBtn.setText(getString(R.string.item_detail_change_info));
		} else {
			mAreaInfoView.setVisibility(View.GONE);
			android.widget.RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) mChangeAreaBtn.getLayoutParams();
			if(params != null) {
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			}
			mChangeAreaBtn.setText(getString(R.string.item_detail_no_info));
		}
		requestData();
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch(requestCode) {
		case SelectHelper.REQUEST_PICK_AREA: {
			if(resultCode == RESULT_OK) {
				mGameInfo.setAreaName(data.getStringExtra(GameInfo.KEY_AREA_NAME));
				mGameInfo.setAreaId(data.getIntExtra(GameInfo.KEY_AREA_ID, 0));
				mGameInfo.setRoleName(data.getStringExtra(GameInfo.KEY_ROLE_NAME));
				mGameInfo.setRoleId(data.getStringExtra(GameInfo.KEY_ROLE_ID));
				mGameInfo.setServerName(data.getStringExtra(GameInfo.KEY_SERVER_NAME));
				mGameInfo.setServerId(data.getIntExtra(GameInfo.KEY_SERVER_ID, 0));
			}
			break;
		}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private ProductModel mProduct;
	private void requestData() {
		Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/api/daoju_app/Goods.php?plat=2");
		
		if(ajax == null) {
			return;
		}
		
		ajax.setData("propid", mPropId);
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				String msg = v.optString("msg");
				if(!"".equals(msg)) {
					UiUtils.makeToast(ItemActivity.this, msg);
					return;
				}
				
				JSONObject data = v.optJSONObject("data");
				mProduct = ProductModel.fromJson(data);
				requestFinish();
			}
		});
		ajax.setOnErrorListener(this);
		ajax.setParser(new JSONParser());
		ajax.send();
		addAjax(ajax);
	}
	
	private int mCurrentValiDatePos;
	
	private void refreshPriceInfo() {
		List<Validate> valiList = mProduct.getValidateList();
		Validate valiDate = (Validate) Utils.getObjectSafely(valiList, mCurrentValiDatePos);
		if(valiDate != null) {
			double oldPrice = Double.parseDouble(valiDate.getOldPrice());
			double priceQQ = Double.parseDouble(valiDate.getCurPrice());
			double priceWechat = Double.parseDouble(valiDate.getWechatPrice());
			
			mTvPriceOld.setText(getString(R.string.price_old) + getString(R.string.rmb) + ToolUtil.toPrice(oldPrice, 2));
			mTvPriceQQ.setText(ToolUtil.toPrice(priceQQ, 2) + getString(R.string.qb));
			mTvPriceWechat.setText(getString(R.string.rmb) + ToolUtil.toPrice(priceWechat, 2));
		}
		loadImage(mPropImgView, mProduct.getPropImg());
		mTvPropTitle.setText(mProduct.getPropName());
	}
	private void requestFinish() {
		if(mProduct == null) {
			return;
		}
		
		List<Validate> valiList = mProduct.getValidateList();
		// Render product price info
		refreshPriceInfo();

		// Render product detail
		mTvItemDesc.setText(mProduct.getPropDesc());
		
		mDateLayout.removeAllViews();
		// Render product date
		for(int i = 0; i < valiList.size(); i++) {
			
			Validate date = valiList.get(i);
			TextView value = (TextView) getLayoutInflater().inflate(R.layout.view_btn, null);

			value.setText(date.getDay());
			value.setSingleLine(true);
			value.setTag(i);
			
			if(i == mCurrentValiDatePos) {
				value.setBackgroundResource(R.drawable.button_red_round_frame_shape);
				value.setTextColor(getResources().getColor(R.color.red));
			} else {
				value.setBackgroundResource(R.drawable.button_gray_round_frame_shape);
				value.setTextColor(getResources().getColor(R.color.global_gray));
			}

			LayoutParams params = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(3, 0, 3, 0);
			value.setLayoutParams(params);
			value.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View textView) {
					TextView old = (TextView) mDateLayout.getChildAt(mCurrentValiDatePos);
					old.setBackgroundResource(R.drawable.button_gray_round_frame_shape);
					old.setTextColor(getResources().getColor(R.color.global_gray));
					
					TextView current = (TextView)textView;
					current.setBackgroundResource(R.drawable.button_red_round_frame_shape);
					old.setTextColor(getResources().getColor(R.color.red));
					
					mCurrentValiDatePos = (Integer)	textView.getTag();
					refreshPriceInfo();
				}
			});
			mDateLayout.addView(value);
		}
		
		// Render product payType
		mPayTypeLayout.removeAllViews();
		for(int i = 0; i < PayFactory.PAY_METHODS; i++) 
		{					
			TextView value = (TextView) getLayoutInflater().inflate(R.layout.view_btn, null);
			if(i == PayFactory.PAY_WX)
				value.setText(this.getString(R.string.paytype_wxpay));
			else
				value.setText(this.getString(R.string.paytype_midas));
			value.setSingleLine(true);
			value.setTag(i);
					
			if(i == mCurrentPayType) {
				value.setBackgroundResource(R.drawable.button_red_round_frame_shape);
				value.setTextColor(getResources().getColor(R.color.red));
			} else {
				value.setBackgroundResource(R.drawable.button_gray_round_frame_shape);
				value.setTextColor(getResources().getColor(R.color.global_gray));
				
			}
					

			LayoutParams params = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(3, 0, 3, 0);
			value.setLayoutParams(params);
			value.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mCurrentPayType = (Integer)	v.getTag();
					refreshPayType();
				}
			});
			mPayTypeLayout.addView(value);
		}
	}
	
	
	protected void refreshPayType() {
		for(int i = 0 ;null!= mPayTypeLayout && i< mPayTypeLayout.getChildCount(); i++)
		{
			TextView child = (TextView) mPayTypeLayout.getChildAt(i);
			int tg = (Integer) child.getTag();
			if(tg == mCurrentPayType)  {
				child.setBackgroundResource(R.drawable.button_red_round_frame_shape);
				child.setTextColor(getResources().getColor(R.color.red));
			}
			else {
				child.setBackgroundResource(R.drawable.button_gray_round_frame_shape);
				child.setTextColor(getResources().getColor(R.color.global_gray));
			}
		}
	}
	
	private void loadImage(final ImageView view, String url) {
		final Bitmap data = mAsyncImageLoader.get(url);
		if (data != null) {
			view.setImageBitmap(data);
			return;
		}
//		view.setImageResource(mAsyncImageLoader.getLoadingId());
		view.setImageBitmap(mAsyncImageLoader.getLoadingBitmap(this));
		mAsyncImageLoader.get(url, new ImageLoadListener() {
			
			@Override
			public void onLoaded(Bitmap aBitmap, String strUrl) {
				view.setImageBitmap(aBitmap);
			}
			
			@Override
			public void onError(String strUrl) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	
	
	@Override
	protected void onDestroy() {
		mAsyncImageLoader.cleanup();
		mAsyncImageLoader = null;
		super.onDestroy();
	}
	
	@Override
	public void onClick(View v)
	{
		if(v.getId() == R.id.buy_now_btn)
		{
			if(null == mGameInfo || mGameInfo.getAreaId() <=0)
				UiUtils.makeToast(this, "Need select game area");
			else
			{
				int areaid = mGameInfo.getServerId() > 0 ? mGameInfo.getServerId() : mGameInfo.getAreaId();
				final String ordurl = "_appname=" + mProduct.getBusId() +  
					"&propid=" + mProduct.getPropId() + 
					"&buynum=" + 1 + 
					"&_appcode=djapp&areaid=" +areaid;
				
				mPayCore =  PayFactory.getInstance(ItemActivity.this, this.mCurrentPayType, ordurl);
				mPayCore.setPayResponseListener(ItemActivity.this);
				if(null!=mPayCore)
					mPayCore.submit();
				/*
				if(null == payMethodNames )
				{
					payMethodNames = new String[2];
			    	payMethodNames[0] = this.getString(R.string.paytype_midas);
			    	payMethodNames[1] = this.getString(R.string.paytype_wxpay);
				}
				
				
					
				UiUtils.showListDialog(this, getString(R.string.orderconfirm_choose_paytype), payMethodNames, -1, 
						new RadioDialog.OnRadioSelectListener() {
					@Override
					public void onRadioItemClick(int which) {
						mPayCore =  PayFactory.getInstance(ItemActivity.this, which, ordurl);
						mPayCore.setPayResponseListener(ItemActivity.this);
						if(null!=mPayCore)
							mPayCore.submit();
				}
			});*/
			}
		} else if(v.getId() == R.id.item_detail_downBtn) {
			mBuyCountEt.setText("" + (currentBuyCount - 1));
		}else if(v.getId() == R.id.item_detail_upBtn) {

			mBuyCountEt.setText("" + (currentBuyCount + 1));
		}else if(v.getId() == R.id.send_present)
		{
			UiUtils.startActivity(this, PaySuccActivity.class , true);
			
		}
			
		super.onClick(v);
	}

	@Override
	public void onSuccess(String... message) {
		Bundle abd = new Bundle();
		abd.putString(OrderDetailActivity.ORDER_ID,mPayCore.getOrderId());
		
		UiUtils.startActivity(this, PaySuccActivity.class,abd, true);
	}

	@Override
	public void onError(String... message) {
		String str = ((message == null || message[0] == null) ? "未知错误" : message[0]);
		UiUtils.showDialog(this, getString(R.string.caption_pay_failed), str, R.string.btn_retry, R.string.btn_cancel, new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int nButtonId) {
				if (nButtonId == AppDialog.BUTTON_POSITIVE) {
					mPayCore.submit();
				}
			}
		});
	}
	
	
}
