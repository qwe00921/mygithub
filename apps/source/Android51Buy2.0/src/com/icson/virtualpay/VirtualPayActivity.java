package com.icson.virtualpay;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icson.R;
import com.icson.home.HTML5LinkActivity;
import com.icson.lib.AppStorage;
import com.icson.lib.ILogin;
import com.icson.lib.pay.PayFactory;
import com.icson.lib.pay.cft.CFTPayActivity;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.DenomPanel;
import com.icson.lib.ui.DenomPanel.OnDenomSelectListener;
import com.icson.lib.ui.EditField;
import com.icson.lib.ui.RadioDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.login.LoginActivity;
import com.icson.my.orderlist.VPOrderListActivity;
import com.icson.preference.Preference;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class VirtualPayActivity extends BaseActivity implements OnSuccessListener<JSONObject> {
	private EditText   mPhoneNumber;
	private TextView   mTarget;
	private Button     mConfirm;
	private ImageView  mContacts;
	private DenomPanel mPanel;
	private TextView   mPrice;
	private EditField  mPayOptions;
	private View       mNumberLayout;
	private View       mPriceLayout;
	private View       mPayTypeLayout;
	private int        mPayType = PayFactory.PAY_WX;
	private String     mMobile = "";
	private String     mDenom; // 充值面额
	private String     mProvince;
	private String     mOperator;
	private String     mCardPay;
	
	private RelativeLayout mTipsLayout;
	private TextView  mTipsTV;
	private	String     tips_content;
	private String     tips_url;
	private boolean    bActionPickSucc;
	private Handler    mHandler = new Handler();
	
	ArrayList<String> phonesList = new ArrayList<String>();

	final int AJAX_GETPHONEINFO = 12241523;
	final int AJAX_PAY_INFO = AJAX_GETPHONEINFO + 1;
	final int AJAX_PAY_ORDER = AJAX_GETPHONEINFO + 2;
	final int AJAX_ACTION = AJAX_GETPHONEINFO + 3;

	public static final String PHONE_NUMBER = "phone_number";
	public static final String CARD_NUMBER = "card_number";
	
	public static final int GO_LOGIN = 1;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_recharge);
		init();
	}

	private Handler mUIHandler = new Handler();
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if(!bActionPickSucc)
		{
			UiUtils.showSoftInputDelayed(this, mPhoneNumber, mUIHandler);
		}
		bActionPickSucc = false;
	}
	
	private void init() {
		// Load navigation bar.
		this.loadNavBar(R.id.recharge_navbar);
		
		mTipsLayout = (RelativeLayout) this.findViewById(R.id.vp_news_ll);
		mTipsTV = (TextView)this.findViewById(R.id.vp_tips_tv);
		mTipsLayout.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(!TextUtils.isEmpty(tips_url))
				{
					Bundle gjp = new Bundle();
					gjp.putString(HTML5LinkActivity.LINK_URL, tips_url);
					gjp.putString(HTML5LinkActivity.ACTIVITY_TITLE, getString(R.string.app_name));
					UiUtils.startActivity(VirtualPayActivity.this, HTML5LinkActivity.class, gjp, true);
				}
			}});
		bActionPickSucc = false;
		fetchActionAlert();
		
		mPhoneNumber = (EditText) findViewById(R.id.recharge_num_input);
		mPhoneNumber.setInputType(InputType.TYPE_CLASS_PHONE);
		mPhoneNumber.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String strText = s.toString();
				if (checkPhoneNumber(strText)) {
					mPhoneNumber.setSelection(mPhoneNumber.getEditableText().length());
					getPhoneInfo(strText);
				} else {
					clearAll();
				}
			}
		});
		
		// Target number.
		mTarget = (TextView)findViewById(R.id.recharge_target_number);
		
		// 初始 提交按钮不可点击
		mConfirm = (Button)findViewById(R.id.recharge_confirm);
		mConfirm.setEnabled(false);
		mConfirm.setTextColor(getResources().getColor(
				R.color.global_button_submit_d));
		mConfirm.setOnClickListener(this);

		mContacts = (ImageView)findViewById(R.id.recharge_select_contact);
		mContacts.setOnClickListener(this);
		
		// Update panel configuration.
		mPanel = (DenomPanel)findViewById(R.id.recharge_denom_panel);
		mPanel.setOnDenomSelectListener(new OnDenomSelectListener(){
			@Override
			public void onDenomSelect(int nDenom) {
				mDenom = "" + nDenom;
				getPayInfo(false);
			}
		});
		
		mPrice = (TextView)findViewById(R.id.need_pay_price);
		mPayOptions = (EditField)findViewById(R.id.recharge_paytype);
		mPayOptions.setOnDrawableRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				selectPayType();
			}
		});
		
		String thirdsource = AppStorage.getData(AppStorage.SCOPE_DEFAULT, "thirdcallsource");
		if(null!=thirdsource && thirdsource.equals("alipayapp"))
		{
			mPayType = PayFactory.PAY_ALI;
		}
		else
		{
			String paytype =  AppStorage.getData(AppStorage.KEY_RECHARGE_PAYTYPE);
			if(!TextUtils.isEmpty(paytype))
				mPayType = Integer.valueOf(paytype);
		}
		
		if(mPayType == PayFactory.PAY_WX)
			mPayOptions.setContent(this.getString(R.string.paytype_wxpay));
		else if(mPayType == PayFactory.PAY_ALI)
			mPayOptions.setContent(this.getString(R.string.paytype_alipay));
		else if(mPayType == PayFactory.PAY_CFT)
			mPayOptions.setContent(this.getString(R.string.paytype_tenpay));
		
		mNumberLayout = findViewById(R.id.recharge_number_layout);
		mPriceLayout = findViewById(R.id.recharge_amount_layout);
		mPayTypeLayout = findViewById(R.id.recharge_paytype_layout);
		
		// Initialize data.
		Intent data = getIntent();
		mMobile = data.getStringExtra(PHONE_NUMBER);
		mDenom = data.hasExtra(CARD_NUMBER) ? data.getStringExtra(CARD_NUMBER) : "" + mPanel.getDenom();
		
		if(TextUtils.isEmpty(mMobile))
			mMobile = AppStorage.getData(AppStorage.KEY_RECHARGE_NUM);
	
		if( !TextUtils.isEmpty(mMobile) )
			mPhoneNumber.setText(mMobile);
		
		updateLayout();
	}
	
	private void updateLayout() {
		if( TextUtils.isEmpty(mMobile) || TextUtils.isEmpty(mProvince) || TextUtils.isEmpty(mOperator) ) {
			mNumberLayout.setVisibility(View.GONE);
			mPriceLayout.setVisibility(View.GONE);
			mPayTypeLayout.setVisibility(View.GONE);
		} else {
			mNumberLayout.setVisibility(View.VISIBLE);
			mPriceLayout.setVisibility(View.VISIBLE);
			mPayTypeLayout.setVisibility(View.VISIBLE);
		}
	}
	
	
	private void selectPayType() {
		String aPayTypes[] = null;
		int selectedIndex = 0 ; //default 第一个
		String thirdsource = AppStorage.getData(AppStorage.SCOPE_DEFAULT, "thirdcallsource");
		if(null!=thirdsource && thirdsource.equals("alipayapp"))
		{
			return;
		}
		else
		{
			aPayTypes = new String[3];
			aPayTypes[0] = this.getString(R.string.paytype_wxpay);
			aPayTypes[1] = this.getString(R.string.paytype_alipay);
			aPayTypes[2] = this.getString(R.string.paytype_tenpay);
			
			if(mPayOptions.getContent().equals(aPayTypes[2]))
				selectedIndex = 2;
			else if(mPayOptions.getContent().equals(aPayTypes[1]))
				selectedIndex = 1;
		}
		
		UiUtils.showListDialog(this, getString(R.string.orderconfirm_paytype_title), aPayTypes, selectedIndex, new RadioDialog.OnRadioSelectListener() {
			@Override
			public void onRadioItemClick(int which) {
				switch( which ) {
				case 0:
					mPayType = PayFactory.PAY_WX;
					mPayOptions.setContent(getString(R.string.paytype_wxpay));
					break;
					
				case 1:
					mPayType = PayFactory.PAY_ALI;
					mPayOptions.setContent(getString(R.string.paytype_alipay));
					break;
					
				case 2:
					mPayType = PayFactory.PAY_CFT;
					mPayOptions.setContent(getString(R.string.paytype_tenpay));
					break;
				}
			}
		});
		
	}

	protected void clearAll() {
		mMobile = "";
		mProvince = "";
		mOperator = "";
		mCardPay = "";
		
		//初始 提交按钮不可点击
		mConfirm.setEnabled(false);
		mConfirm.setTextColor(getResources().getColor(
				R.color.global_button_submit_d));
		updateLayout();
	}

	@Override
	public void onClick(View v) {
		final String pageId= getString(R.string.tag_VirtualPayActivity);
		switch (v.getId()) {
		case R.id.recharge_confirm:
			submitVPay();
			ToolUtil.sendTrack(this.getClass().getName(), pageId, this.getClass().getName(), pageId, "03011");
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21001");
			break;
			
		case R.id.recharge_select_contact:
		{
			if(Preference.getInstance().needToContactAccess())
			{
				UiUtils.showDialog(VirtualPayActivity.this,
					R.string.permission_title, R.string.permission_hint_contact,R.string.permission_agree, R.string.permission_disagree,
					new AppDialog.OnClickListener() {
					@Override
					public void onDialogClick(int nButtonId) {
						if (nButtonId == AppDialog.BUTTON_POSITIVE)
						{
							Preference.getInstance().setContactAccess(Preference.ACCESSED);
							gotoContact(pageId);
						}
					}
				});
			}
			else
			{
				gotoContact(pageId);
			}
			break;
		}
		}
	}
	
	protected void gotoContact(String pageId) {
		// TODO Auto-generated method stub
		try {
			Uri uri = Uri.parse("content://com.android.contacts/contacts");
			Intent it = new Intent(Intent.ACTION_PICK, uri);
			startActivityForResult(it, 100021);
		} catch (Exception e) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(e));
			UiUtils.makeToast(VirtualPayActivity.this, "您手机不支持此功能，请输入手机号");
		}
		ToolUtil.sendTrack(VirtualPayActivity.this.getClass().getName(), pageId, this.getClass().getName(), pageId, "01012");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		bActionPickSucc = true;
		if (requestCode == 100021 && resultCode == RESULT_OK) {
			Uri contactData = data.getData();
			Cursor phones = null;
			try {
				//cursor of managedQuery DON'T CALL CLOSE! Otherwise  crah.See it's API document, warning
				Cursor contactCurosor = managedQuery(contactData, null, null, null, null);
				if(contactCurosor == null){
					UiUtils.makeToast(this, "无法获取手机号，请输入手机号");
					return ;
				}
				contactCurosor.moveToFirst();
				String id = contactCurosor.getString(contactCurosor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

				String hasPhone = contactCurosor.getString(contactCurosor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				if (Integer.parseInt(hasPhone) > 0) {
					
					phones = getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID+ " = " + id,
							null, 
							null);
					
					if(phones == null){
						UiUtils.makeToast(this, "无法获取手机号，请输入手机号");
						return ;
					}
					phonesList.clear();
					while (phones.moveToNext()) {
						String phoneNumber = phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						phoneNumber = phoneNumber.replace("+86", "")
								.replace(" ", "").replace("-", "");

						phonesList.add(phoneNumber);
					}
					if (phonesList.size() > 1) {
						selectPhones();
					} else if (phonesList.size() == 1) {
						mPhoneNumber.setText(phonesList.get(0));
					}
				}
			} catch (Exception e) {
				Log.e(LOG_TAG, ToolUtil.getStackTraceString(e));
				UiUtils.makeToast(this, "无法获取手机号，请输入手机号");
			}finally
			{
				if(null!=phones)
					phones.close();
			}
		}else if(requestCode == CFTPayActivity.REQUEST_CFT_PAY){
			if(resultCode == RESULT_OK){
				UiUtils.makeToast(this, "支付成功, 订单状态稍有迟延，请稍等.",true);
			}else{
				UiUtils.makeToast(this, "支付失败！",true);
			}
			orderFinish();
		}
		else if(GO_LOGIN == requestCode)
		{
			//login succ
			if(ILogin.getLoginUid() != 0)
			{
				submitVPay();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 选择充值号码
	private void selectPhones() {

		final String[] phones = new String[phonesList.size()];

		int selectedIndex = 0;
		for (int i = 0, len = phones.length; i < len; i++) {
			phones[i] = phonesList.get(i);

		}
		
		UiUtils.showListDialog(this, getString(R.string.recharge_choose_phone), phones, selectedIndex, new RadioDialog.OnRadioSelectListener() {
			@Override
			public void onRadioItemClick(int which) {
				mPhoneNumber.setText(phones[which]);
			}
		}, true);

	}

	private void submitVPay() {
		if (ILogin.getLoginUid() == 0) {
			Bundle param = new Bundle();
			param.putString(PHONE_NUMBER, mMobile);
			param.putString(CARD_NUMBER, "" + mDenom);
			ToolUtil.startActivity(this, LoginActivity.class,
					null,GO_LOGIN);
			//ToolUtil.checkLoginOrRedirect(this, VirtualPayActivity.class,param);
			//finish();
			return;
		}
		
		Ajax ajax = ServiceConfig.getAjax(Config.URL_RECHARGE_MOBILE_PAYMENT, ILogin.getLoginUid());
		if( null == ajax )
			return ;

		ajax.setData("chargeMoney", mDenom);
		ajax.setData("area", mProvince);
		ajax.setData("operator", mOperator);
		ajax.setData("payType", mPayType);
		ajax.setData("type", "1");// 1代表手机充值
		ajax.setData("targetId", mMobile);
		ajax.setData("payMoney", mCardPay);
		ajax.setData("ls", "--android--");
		ajax.setData("visitkey", System.currentTimeMillis());

		ajax.setParser(new JSONParser());
		ajax.setId(AJAX_PAY_ORDER);
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(this);
		addAjax(ajax);
		showProgressLayer("正在提交, 请稍后...");
		ajax.send();

	}

	private void getPhoneInfo(String phoneID) {
		mMobile = phoneID;

		Ajax ajax = ServiceConfig.getAjax(Config.URL_RECHARGE_MOBILE_INFO, phoneID);
		if( null == ajax )
			return ;
		
		ajax.setParser(new JSONParser());
		ajax.setId(AJAX_GETPHONEINFO);
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(this);
		addAjax(ajax);
		showLoadingLayer();
		ajax.send();
		
		UiUtils.hideSoftInputDelayed(this, mPhoneNumber, mHandler);
	}

	private void getPayInfo(boolean bCheckNum) {
		UiUtils.hideSoftInputDelayed(this, mPhoneNumber, mHandler);
		
		if( TextUtils.isEmpty(mMobile) ) {
			if( bCheckNum )
				UiUtils.makeToast(this, R.string.vp_phone_number_empty);
			return;
		}

		Ajax ajax = ServiceConfig.getAjax(Config.URL_RECHARGE_MOBILE_MONEY);
		if( null == ajax )
			return ;
		ajax.setData("card", mDenom);
		ajax.setData("area", mProvince);
		ajax.setData("operator", mOperator);
		ajax.setParser(new JSONParser());
		ajax.setId(AJAX_PAY_INFO);
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(this);
		addAjax(ajax);
		showLoadingLayer();
		ajax.send();
	}

	/**
	 * 
	* method Name:fetchActionAlert    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void fetchActionAlert()
	{
		Ajax ajax = ServiceConfig.getAjax(Config.URL_RECHARGE_INFO);
		if( null == ajax )
			return ;
		
		ajax.setParser(new JSONParser());
		ajax.setId(AJAX_ACTION);
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){
			/** "errno": 0,
			 *  "data": {"content": "1. qq会员 2. 首次充值",
			 *  		 "bgein": "2013-05-17",
			 *           "end" : "2014-05-17",
			 *  		 "title": "话费充值满100，送100优惠券",
			 *  	     "linkUrl": "http://m.51buy.com/test.htm",
			 *           "active": 1,
			 */
			@Override
			public void onSuccess(JSONObject v, Response response) {
				final int errno = null != v ? v.optInt("errno", -1) : -1;
				if(errno!=0)
					return;
				
				JSONObject data = v.optJSONObject("data");
				/*
				if(null == data)
				{
				try {
					data = new JSONObject("{\"content\": \"1. qq会员 2. 首次充值\"," +
							"\"begin\": \"2013-05-17\",\"end\" : \"2014-05-17\"," +
							"\"title\": \"话费充值满100，送100优惠券\",\"linkUrl\":" +
							" \"http://m.51buy.com/test.htm\",\"active\": 1}");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				}
				*/
				if(null!=data)
				{
					tips_content = data.optString("title");
					tips_url = data.optString("linkUrl");
					int iActive = data.optInt("active");
					String tmp = data.optString("begin");
					
					try {
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
						Date beginDate = format.parse(tmp);
						
						tmp = data.optString("end");
						Date endDate = format.parse(tmp);
						
						long curTime = System.currentTimeMillis();
						if(iActive>=1 && beginDate.getTime()<=curTime && 
								endDate.getTime()>curTime )
						{
							mTipsTV.setText(tips_content);
							mTipsLayout.setVisibility(View.VISIBLE);	
						}
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
			}});
		addAjax(ajax);
		ajax.send();
	}
	@Override
	public void onSuccess(JSONObject v, Response response) {
		try {
			closeLoadingLayer();
			switch (response.getId()) {
			case AJAX_PAY_INFO:
				if (v.getInt("errno") == 0) {
					JSONObject j = v.getJSONObject("data");
					mCardPay = j.getString("price");
					boolean bSuccess = true;
					try {
						Float.valueOf(mCardPay);
					} catch( NumberFormatException aException ) {
						Log.e(LOG_TAG, ToolUtil.getStackTraceString(aException));
						bSuccess = false;
					}
					if( bSuccess ) {
						mPrice.setText(getString(R.string.rmb) + mCardPay + " 元");
						mConfirm.setEnabled(true);
						mConfirm.setTextColor(getResources().getColor(
								R.color.global_white));
						//mMessage.setVisibility(View.INVISIBLE);
					} else {
						//mMessage.setVisibility(View.VISIBLE);
						//mMessage.setText(mCardPay);
						mPrice.setText(this.getString(R.string.virtualpay_no_card));
						mConfirm.setEnabled(false);
						mConfirm.setTextColor(getResources().getColor(
								R.color.global_button_submit_d));
					}
					
					updateLayout();
				}else{
					String strErrMsg = TextUtils.isEmpty(v.optString("data", "")) ? getString(R.string.global_error_warning) : v.optString("data", "");
					UiUtils.makeToast(this, strErrMsg);
				}
				break;

			case AJAX_GETPHONEINFO:
				closeLoadingLayer();
				if (v.getInt("errno") == 0) {
					JSONObject data = v.getJSONObject("data");
					mProvince = data.getString("province");
					mOperator = data.getString("isp");
					if ("未知".equals(mProvince) || "未知".equals(mOperator)) {
						mMobile = "";
						UiUtils.makeToast(this, R.string.vp_phone_error);
					} else {
						String strInfo = mMobile + " (" + mProvince + mOperator.substring(2) + ")";
						mTarget.setText(strInfo);
						
						if( !TextUtils.isEmpty(mDenom) ) {
							getPayInfo(true);
						}
					}
					updateLayout();
				} else {
					String strErrMsg = v.optString("data", "");
					if(TextUtils.isEmpty(strErrMsg)) {
						showAlert(mMobile);
					}else{
						UiUtils.makeToast(this, strErrMsg);
					}
				}
				break;
			case AJAX_PAY_ORDER:
				// {"errno":0,"data":{"order_char_id":"1022223860","uid":"9958592","payType":"21","targetId":"18611712487"}}
				closeProgressLayer();
//				String str = "{\"errno\":6006,\"data\":{\"errMsg\":\"拉拉拉拉\",\"prices\":90}}";
//				v = new JSONObject(str);
				int errNo = v.getInt("errno");
				JSONObject data = v.getJSONObject("data");
				if ( 0 == errNo) {
					String order_char_id = data.optString("order_char_id");
					if (order_char_id != null && !"".equals(order_char_id)) {
						commitSuccess(data);
					} else {
						UiUtils.makeToast(this, "充值订单生成失败！");
					}
				} else {
					if( Config.NOT_LOGIN == errNo) {
						ILogin.clearAccount();
						UiUtils.makeToast(this, "您已退出登录，请登录后重试.");
					}else if( 6006 == errNo || 6007 == errNo || 6009 == errNo || 6010 == errNo ) {
						String errMsg = !TextUtils.isEmpty(data.optString("errMsg", "")) ? data.optString("errMsg") : "您确定下单吗？";
						final String strCardPay= data.getString("price");
						
						UiUtils.showDialog(this, getString(R.string.caption_hint), errMsg, R.string.btn_ok, R.string.btn_back, new AppDialog.OnClickListener() {
							@Override
							public void onDialogClick(int nButtonId) {
								if(nButtonId == AppDialog.BUTTON_POSITIVE) {
									mCardPay = strCardPay;
									submitVPay();
								} else if (nButtonId == AppDialog.BUTTON_NEGATIVE){
									updateLayout();
								}
							}
						});
					}else {
						String errMsg = !TextUtils.isEmpty( data.optString("errMsg", "")) ? data.optString("errMsg") : Config.NORMAL_ERROR;
						
						UiUtils.showDialog(this, getString(R.string.caption_hint), errMsg, R.string.btn_ok,  new AppDialog.OnClickListener() {
							@Override
							public void onDialogClick(int nButtonId) {
								if(nButtonId == AppDialog.BUTTON_POSITIVE) {
									
								} 
							}
						});
					}
				}

				break;
			default:
				break;
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(e));

		}
	}

	private void showAlert(final String _mobile) { 
		UiUtils.showDialog(this, R.string.caption_hint, R.string.vp_get_phone_msg_error, R.string.btn_refresh, R.string.btn_change_num, new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int which) {
				if (which == AppDialog.BUTTON_POSITIVE) {
					getPhoneInfo(_mobile);
					
				} else if (which == AppDialog.BUTTON_NEGATIVE) {
					mMobile = "";
					mPhoneNumber.setText(mMobile);
				}
			}
		});
		
	}

	private void commitSuccess(JSONObject data) {
		// 提交订单按钮
		mConfirm.setEnabled(false);
//		mConfirm.setText("订单已生成");
		mConfirm.setTextColor(getResources().getColor(
				R.color.global_button_submit_d));

		AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_RECHARGE_NUM, mMobile, true);
		AppStorage.setData(AppStorage.SCOPE_DEFAULT, AppStorage.KEY_RECHARGE_PAYTYPE, ""+mPayType, true);

		VPOrderSuccessView mOrderSuccessView = new VPOrderSuccessView(this,
				data);
		mOrderSuccessView.success();
	}

	public void orderFinish() {
		finish();                                                     
		ToolUtil.startActivity(this, VPOrderListActivity.class);
	}

	private boolean checkPhoneNumber(String str) {
		Pattern p_str = Pattern.compile("^([0-9]{11})$");
		Matcher m = p_str.matcher(str);
		return m.find() && m.group(0).equals(str);
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_VirtualPayActivity);
	}
}
