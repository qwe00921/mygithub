package com.icson.shoppingcart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.icson.R;
import com.icson.home.HomeActivity;
import com.icson.lib.ILogin;
import com.icson.lib.IShippingArea;
import com.icson.lib.model.ShoppingCartProductModel;
import com.icson.lib.ui.UiUtils;
import com.icson.login.LoginActivity;
import com.icson.main.MainActivity;
import com.icson.order.OrderConfirmActivity;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.Response;

public class ShoppingCartActivity extends BaseActivity {
	public static final int FLAG_REQUEST_ACCOUNT_LOGIN 	= 1;
	public static final int FLAG_REQUEST_FAVOR_LOGIN 	= 2;
	
	private ShoppingCartView 	mShoppingCartView;
	private long 				uid;
	private boolean             mBackable;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_shoppingcart);

		loadNavBar(R.id.shoppingcart_navbar);
		findViewById(R.id.cart_list_button_index).setOnClickListener(this);
		
		// Update backable status.
		Intent pIntent = getIntent();
		mBackable = (null != pIntent ? pIntent.getBooleanExtra("backable", false) : false);
		mNavBar.setLeftVisibility(mBackable ? View.VISIBLE : View.GONE);
		mNavBar.setOnDrawableRightClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				mShoppingCartView.setEditView();
				ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_ShoppingCartActivity), ShoppingCartActivity.class.getName(), getString(R.string.tag_ShoppingCartActivity), "01011");
			}
		});
		
		StatisticsEngine.trackEvent(this, "go_shopping_cart");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		this.reload();
	}
	
	private void reload() {
		uid = ILogin.getLoginUid();
		if(uid == 0){
			initNotLoginShoppingCart();
		}else{
			getShoppingCartListFromServer();
		}
	}
	
	private void getShoppingCartListFromServer(){
		if (mShoppingCartView == null) {
			mShoppingCartView = new ShoppingCartView(this);
		}
		showLoadingLayer();
		mShoppingCartView.setIsEditView(true);
		mShoppingCartView.refreshFullDistrictItem();
		mShoppingCartView.getShoppingCartList();
	}
	
	public ShoppingCartView getShoppingCartView(){
		if(null==mShoppingCartView)
			mShoppingCartView = new ShoppingCartView(this);
		return mShoppingCartView;
	}
	
	public static void loadShoppingCart(Activity aParent, boolean bBackable, boolean bClearTop) {
		if( null == aParent )
			return ;
		
		Bundle pBundle = new Bundle();
		pBundle.putBoolean("backable", bBackable);
		
		UiUtils.startActivity(aParent, ShoppingCartActivity.class, pBundle, bClearTop);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.cart_confirm:
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21001");
			if(null==mShoppingCartView)
				mShoppingCartView = new ShoppingCartView(this);
			mShoppingCartView.submit();

			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_ShoppingCartActivity), OrderConfirmActivity.class.getName(), getString(R.string.tag_OrderConfirmActivity), "02011");
			break;
		case R.id.cart_list_button_index:
			MainActivity.startActivity(this, MainActivity.TAB_HOME);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_ShoppingCartActivity), HomeActivity.class.getName(), getString(R.string.tag_Home), "02015");
			break;
		}

	}

	private void initNotLoginShoppingCart() {
		if (mShoppingCartView == null) {
			mShoppingCartView = new ShoppingCartView(this);
		}

		mShoppingCartView.getList();
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		initNotLoginShoppingCart();
	}

	//从购物车中删除商品
	public void deleteProduct(final long pid, boolean isLowProduct){
		if(null==mShoppingCartView)
			mShoppingCartView = new ShoppingCartView(this);
		mShoppingCartView.deleteConfirm(pid, isLowProduct);
	}
	
	//收藏和取消商品
	public void collectProduct(final long pid) {
		if(null==mShoppingCartView)
			mShoppingCartView = new ShoppingCartView(this);
		ShoppingCartProductModel product = mShoppingCartView.getShoppingCartProductModel(pid);
		if(product == null)
			return ;
		mShoppingCartView.favorProduct(pid);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
//		case FLAG_REQUEST_ACCOUNT_LOGIN:
//			//if (resultCode == LoginActivity.FLAG_RESULT_LOGIN_SUCCESS) {
//				mShoppingCartView.getShoppingCartList();
//			//}
//			break;
		case FLAG_REQUEST_FAVOR_LOGIN:
			if (resultCode == LoginActivity.FLAG_RESULT_LOGIN_SUCCESS) {
				if(null == mShoppingCartView) {
					mShoppingCartView = new ShoppingCartView(this);
				}
				
				mShoppingCartView.addFavorite();
			}
		}
	}

	@Override
	protected void onDestroy() {
		if(null != mShoppingCartView ) 
		{
			mShoppingCartView.cleanUp();
		}
		
		IShippingArea.clean();
		super.onDestroy();
	}
	
	@Override
	protected void onPause()
	{
		super.cleanAllAjaxs();
		super.onPause();
	}
	
	@Override
	public void onError(Ajax ajax, final Response response) {
		super.onError(ajax, response);
		ajax = null;
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_ShoppingCartActivity);
	}
}
