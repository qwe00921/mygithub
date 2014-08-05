package com.icson.shoppingcart;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.item.ItemProductModel;
import com.icson.lib.ILogin;
import com.icson.lib.model.ShoppingCartProductModel;
import com.icson.lib.ui.LinearListView;
import com.icson.lib.ui.UiUtils;
import com.icson.order.OrderConfirmActivity;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class ESShoppingCartActivity extends BaseActivity implements
		OnClickListener, OnSuccessListener<ItemProductModel>, ImageLoadListener {

	private static final String LOG_TAG = ESShoppingCartActivity.class.getName();
	private ShoppingCartControl mShoppingCartControl;
	private OnSuccessListener<ShoppingCartListModel> success;
	private ShoppingCartProductModel mShoppingCartProductModel;
	private ShoppingCartListModel mShoppingCartListModel;
	private ShoppingCartListParser mShoppingCartListParser;
	private LinearListView mRadioGroup;
	ArrayList<PromoApplyRuleModel> promoApplyRules;
	private int checkedId = -1;
	BaseAdapter mAdapter;
	private int buyCount;
	private long pid;
	private int payType;
	private EditText mEditNum;
	private Button mUpButton;
	private Button mDownButton;

	public static final String REQUEST_PAY_TYPE = "pay_type";

	private ImageLoader mImageLoader;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.activity_es_shoppingcart);
		this.loadNavBar(R.id.es_shoppingcart_navbar);
		
		final long uid = ILogin.getLoginUid();
		if (uid == 0) {
			Log.e(LOG_TAG, "uid is 0");
			finish();
		}

		Intent intent = getIntent();
		buyCount = 1;// intent.getIntExtra("BuyCount", 1);节能补贴只能一次一件
		pid = intent.getLongExtra("esProduct", 0);
		payType = intent.getIntExtra(OrderConfirmActivity.REQUEST_PAY_TYPE, 0);

		mImageLoader = new ImageLoader(this, Config.PIC_CACHE_DIR, true);
		mShoppingCartListParser = new ShoppingCartListParser();
		success = new OnSuccessListener<ShoppingCartListModel>() {
			@Override
			public void onSuccess(ShoppingCartListModel model, Response response) {
				closeLoadingLayer();
				
				if(!mShoppingCartListParser.isSuccess()){
					UiUtils.makeToast(ESShoppingCartActivity.this, TextUtils.isEmpty(mShoppingCartListParser.getErrMsg()) ? Config.NORMAL_ERROR: mShoppingCartListParser.getErrMsg(),true);
					finish();
					return;
				}
				
				mShoppingCartListModel = model;
				ArrayList<ShoppingCartProductModel> tmpModels = new ArrayList<ShoppingCartProductModel>();
				tmpModels = model.getShoppingCartProductModels();
				if (tmpModels != null && tmpModels.size() == 1) {
					mShoppingCartProductModel = tmpModels.get(0);
				}
				// 显示数据和促销规则
				init();
			}
		};
		// items:{"324930":{"product_id":324930,"buy_count":1,"main_product_id":324930,"price_id":0,"type":0,"OTag":"31000038300000-100070021-0-386212010.3"}}
		mShoppingCartControl = new ShoppingCartControl(this);
		showLoadingLayer();
		String json_items = "";
		try {

			JSONObject item = new JSONObject();
			item.put("product_id", pid);
			item.put("buy_count", buyCount);
			item.put("main_product_id", pid);
			item.put("price_id", 0);
			item.put("type", 0);

			JSONObject items = new JSONObject();
			items.put(pid + "", item);
			json_items = items.toString();
		} catch (JSONException e) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(e));
		}

		mShoppingCartControl.getESShoppingCartList(json_items,
				mShoppingCartListParser, success, this);
		
		StatisticsEngine.trackEvent(this, "go_esshopping_cart");
	}

	private void loadImage(ImageView view, String url) {
		final Bitmap data = mImageLoader.get(url);
		if (data != null) {
			view.setImageBitmap(data);
			return;
		}
		view.setImageResource(mImageLoader.getLoadingId());
		mImageLoader.get(url, this);
	}

	private void init() {
		TextView name = (TextView) findViewById(R.id.cart_textview_name);
		TextView promo = (TextView) findViewById(R.id.cart_textview_promo);
		TextView price = (TextView) findViewById(R.id.cart_textview_price);
		ImageView image = (ImageView) findViewById(R.id.cart_imageview);
		
		// 价格
		price.setText(getString(R.string.rmb) + mShoppingCartProductModel.getShowPriceStr());
		// 商品名称
		name.setText(Html.fromHtml(mShoppingCartProductModel.getName()));
		//商品促销语和赠品
		promo.setText(mShoppingCartProductModel.getPromotionWord());
		Drawable pGiftIcon = mShoppingCartProductModel.getGiftCount() > 0 ? getResources().getDrawable(R.drawable.i_list_activity_gift) : null;
		if (pGiftIcon != null) {
			pGiftIcon.setBounds(0, 0, pGiftIcon.getMinimumWidth(), pGiftIcon.getMinimumHeight());
		}
		promo.setCompoundDrawables(null, null, pGiftIcon, null);

		loadImage(image, mShoppingCartProductModel.getAdapterProductUrl(80));

		((TextView) findViewById(R.id.cart_textview_price_amt)).setText(getString(R.string.rmb)
				+ ToolUtil.toPrice(mShoppingCartProductModel.getShowPrice()
						* mShoppingCartProductModel.getBuyCount(), 2));

		((TextView) findViewById(R.id.es_info_textview)).setText(Html
				.fromHtml(getString(R.string.es_info)));
		findViewById(R.id.cart_confirm).setOnClickListener(this);
		
		mRadioGroup = (LinearListView) findViewById(R.id.list_apply_rule);
		
		mEditNum = (EditText) findViewById(R.id.cart_editext);
		mEditNum.setText(String.valueOf(buyCount));
		mEditNum.setFocusable(false);
		
		mUpButton = (Button) findViewById(R.id.upBtn);
		mUpButton.setEnabled(false);
		
		mDownButton = (Button) findViewById(R.id.downBtn);
		mDownButton.setEnabled(false);
		
		showPromoRule();
	}

	private void showPromoRule() {
		final LayoutInflater inflater = LayoutInflater.from(this);
		View applyListView = findViewById(R.id.promo_apply_rule);

		promoApplyRules = mShoppingCartListModel.getPromoApplyRuleModels();
		// 显示 可以选择的促销优惠活动
		if (promoApplyRules == null || promoApplyRules.isEmpty()) {
			applyListView.setVisibility(View.GONE);
		} else {
			applyListView.setVisibility(View.VISIBLE);
			mRadioGroup.removeAllViews();

			mAdapter = new BaseAdapter() {
				@Override
				public View getView(final int position, View convertView, ViewGroup parent) {
					final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.prule_item, null);
					
					ImageView radio = (ImageView) layout.getChildAt(0);
					radio.setVisibility(View.VISIBLE);
					if(position == checkedId){
						radio.setImageResource(R.drawable.choose_radio_on);
					}else{
						radio.setImageResource(R.drawable.choose_radio_off);
					}
					
					final TextView textview = (TextView) layout.getChildAt(1);
					PromoApplyRuleModel ruleDecs = (PromoApplyRuleModel)getItem(position);
					textview.setText(Html.fromHtml(ruleDecs.getName()));
					
					return layout;
				}

				@Override
				public long getItemId(int position) {
					return 0;
				}

				@Override
				public Object getItem(int position) {
					return promoApplyRules.get(position);
				}

				@Override
				public int getCount() {
					if(promoApplyRules == null || promoApplyRules.isEmpty())
						return 0;
					
					return promoApplyRules.size();
				}
			};

			mRadioGroup.setAdapter(mAdapter);
			mRadioGroup.setOnItemClickListener( new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if( checkedId == position){
						checkedId = -1;
					}else{
						checkedId = position;
					}
					
					mAdapter.notifyDataSetChanged();
					reSetPrice();
				}
			});

		}
		// 显示 可以参与的促销优惠活动
		final ArrayList<PromoBuyMoreRuleModel> promoBuyMoreRules = mShoppingCartListModel
				.getPromoBuyMoreRuleModels();
		LinearListView mLinearLayout = (LinearListView) findViewById(R.id.cart_list_promo_rule);
		mLinearLayout.setVisibility(View.VISIBLE);
		mLinearLayout.removeAllViews();
		if (promoBuyMoreRules != null && !promoBuyMoreRules.isEmpty()) {
			BaseAdapter mAdapter = new BaseAdapter() {
				@Override
				public View getView(final int position, View convertView, ViewGroup parent) {
					final ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.prule_item, null);
					final TextView textview = (TextView) layout.getChildAt(1);
					
					PromoBuyMoreRuleModel ruleDecs = (PromoBuyMoreRuleModel)getItem(position);
					String rule = "参加 <font color=\"red\">" + Html.fromHtml(ruleDecs.getName()) + "</font>活动，\n仅需再消费<font color=\"red\">¥" + ToolUtil.toPrice(ruleDecs.getBuyMore(), 2) + "</font>";
					textview.setText(Html.fromHtml(rule));
					return layout;
				}
	
				@Override
				public long getItemId(int position) {
					return 0;
				}
	
				@Override
				public Object getItem(int position) {
					return promoBuyMoreRules.get(position);
				}
	
				@Override
				public int getCount() {
					return promoBuyMoreRules.size();
				}
			};

			mLinearLayout.setAdapter(mAdapter);
		}

	}

	protected void reSetPrice() {
		double promoBenifit = 0;
		double priceAmt = mShoppingCartProductModel.getShowPrice()
				* mShoppingCartProductModel.getBuyCount();
		if (checkedId > -1) {
			// 满送券
			if (promoApplyRules.get(checkedId).getBenefitType() == PromoRuleModel.BENEFIT_TYPE_COUPON) {
				promoBenifit = 0;
			}// 满减
			else if (promoApplyRules.get(checkedId).getBenefitType() == PromoRuleModel.BENEFIT_TYPE_CASH) {
				promoBenifit = promoApplyRules.get(checkedId).getBenefits();
			}
		}
		priceAmt -= promoBenifit;
		// 总金额
		((TextView) findViewById(R.id.cart_textview_price_amt)).setText(getString(R.string.rmb)
				+ ToolUtil.toPrice(priceAmt, 2));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.cart_confirm:
			submitESinfo();
			break;
		default:
			break;
		}

	}

	private void submitESinfo() {
		Bundle param = new Bundle();
		param.putLong(OrderConfirmActivity.REQUEST_PRODUCT_ID, pid);
		param.putInt(OrderConfirmActivity.REQUEST_PRODUCT_BUYNUM, 1);
		param.putInt(OrderConfirmActivity.REQUEST_PAY_TYPE, payType);
		String name = ((EditText) findViewById(R.id.es_name_textview))
				.getEditableText().toString();
		String id_card = ((EditText) findViewById(R.id.es_idCard_textview))
				.getEditableText().toString();

		if (!checkChinese(name)) {
			UiUtils.makeToast(this, "请输入您的真实中文姓名");
			return;
		}
		;
		if (!checkIDCard(id_card)) {
			UiUtils.makeToast(this, "请输入您的15位或者18位身份证号");
			return;
		}
		/*
		 * CheckBox check = (CheckBox) findViewById(R.id.mCheckBox); if
		 * (!check.isChecked()) { UiUtils.makeToast(this, "请勾选 同意以上规则，才能参加节能补贴"); return;
		 * }
		 */
		
		if(checkedId > -1){
			param.putLong(OrderConfirmActivity.REQUEST_PRULE_ID,promoApplyRules.get(checkedId).getRuleId());
			if(promoApplyRules.get(checkedId).getBenefitType() == PromoRuleModel.BENEFIT_TYPE_CASH)
				param.putLong(OrderConfirmActivity.REQUEST_PRULE_BENEFITS, promoApplyRules.get(checkedId).getBenefits());
		}

		OrderConfirmActivity.ESInfo esInfo = new OrderConfirmActivity.ESInfo();
		esInfo.es_name = name;
		esInfo.es_idcard = id_card;
		esInfo.es_benefit = mShoppingCartProductModel.getEnergySaveDiscount();// 节能补贴优惠金额
		param.putSerializable(OrderConfirmActivity.REQUEST_ES_INFO, esInfo);

		ToolUtil.checkLoginOrRedirect(this, OrderConfirmActivity.class, param,
				-1);
		
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_ESShoppingCartActivity), 
				OrderConfirmActivity.class.getName(), getString(R.string.tag_OrderConfirmActivity), "02010");
		finish();
	}

	@Override
	public void onSuccess(ItemProductModel v, Response response) {

	}

	@Override
	public void onDestroy() {
		if (null != mImageLoader) {
			mImageLoader.cleanup();
			mImageLoader = null;
		}
		super.onDestroy();
	}

	@Override
	public void onLoaded(Bitmap data, String url) {
		if (data != null) {
			((ImageView) findViewById(R.id.cart_imageview))
					.setImageBitmap(data);
			return;
		}
	}

	@Override
	public void onError(String strUrl) {
	}

	private boolean checkChinese(String str) {
		Pattern p_str = Pattern.compile("[\\u4e00-\\u9fa5]{2,9}");
		Matcher m = p_str.matcher(str);
		return m.find() && m.group(0).equals(str);
	}

	private boolean checkIDCard(String str) {
		Pattern p_str = Pattern.compile("^([0-9]{15}|[0-9]{17}[0-9a-zA-Z])$");
		Matcher m = p_str.matcher(str);
		return m.find() && m.group(0).equals(str);
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_ESShoppingCartActivity);
	}
}
