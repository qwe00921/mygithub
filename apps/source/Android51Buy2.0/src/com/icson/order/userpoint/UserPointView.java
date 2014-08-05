package com.icson.order.userpoint;

import java.util.ArrayList;

import android.text.Html;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.order.OrderBaseView;
import com.icson.order.OrderConfirmActivity;
import com.icson.order.OrderPackage;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class UserPointView extends OrderBaseView<UserPointModel, ArrayList<UserPointModel>> {

	public static final int FLAG_REQUDST_USERPOINT = 12300;

	private UserPointModel mUserPointModel;

	public UserPointView(OrderConfirmActivity activity) {
		super(activity);
		mParser = new UserPointParser();
	}

	public UserPointModel getUserPointModel() {
		return mUserPointModel;
	}

	public void requestFinish() {
		mIsRequestDone = true;
		updateUserPoint();
		mActivity.ajaxFinish(OrderConfirmActivity.VIEW_FLAG_USERPOINT_VIEW);
	}

	public void getUserPoint(double amt) {
		final long uid = ILogin.getLoginUid();
		String strInfo = "" + uid+"&amt="+amt;
		Ajax ajax = ServiceConfig.getAjax(Config.URL_GET_USER_CAN_USE_POINT, strInfo);
		if( null == ajax )
			return ;
		
		mUserPointModel = null;
		mIsRequestDone = false;
		ajax.setParser(mParser);
		ajax.setOnErrorListener(this);
		ajax.setOnSuccessListener(new OnSuccessListener<UserPointModel>() {
			@Override
			public void onSuccess(UserPointModel v, Response response) {
				mUserPointModel = v;
				requestFinish();
			}
		});
		mActivity.addAjax(ajax);
		ajax.send();
		
		
	}

	private void updateUserPoint() {
//		((EditView) mActivity.findViewById(R.id.orderconfirm_point_value)).setText(mUserPointModel == null ? "" : String.valueOf(mUserPointModel.getInputPoint()));
		((TextView) mActivity.findViewById(R.id.orderconfirm_point_all)).setText(mUserPointModel == null ? "" : mActivity.getString(R.string.all_userPoint, mUserPointModel.getUserPoint(), (float)mUserPointModel.getUserPoint()/10));
	}
	
	public void setUserPoint(UserPointModel point) {
		mUserPointModel = point;
		updateUserPoint() ;
	}
	
	public void updatePointView(boolean isFocus, boolean isEmpty) {
		String strWarnings = "";
		if(null!=mUserPointModel)
		{
			if(isFocus && !isEmpty){
				strWarnings = mActivity.getString(R.string.orderconfirm_use_point, mUserPointModel.getInputPoint(), (float)mUserPointModel.getInputPoint()/10);
			}else{
				strWarnings = mActivity.getString(R.string.all_userPoint, mUserPointModel.getUserPoint(), (float)mUserPointModel.getUserPoint()/10);
			}
		}
//		((TextView) mActivity.findViewById(R.id.orderconfirm_point_value)).setText(mUserPointModel == null ? "" : String.valueOf(mUserPointModel.getInputPoint()));
		((TextView) mActivity.findViewById(R.id.orderconfirm_point_all)).setText(mUserPointModel == null ? "" : Html.fromHtml(strWarnings));
	}

//	public void showUserPointDialog(double amt,double shippingPrice) {
//		final Bundle params = new Bundle();
//		params.putSerializable(UserPointActivity.USERPOINT_MODEL, mUserPointModel);
//		params.putDouble(UserPointActivity.AMT, amt);
//		params.putDouble(UserPointActivity.SHIPPING_PRICE,shippingPrice);
//		ToolUtil.checkLoginOrRedirect(mActivity, UserPointActivity.class, params, FLAG_REQUDST_USERPOINT);
//	}

//	public void onUserPointConfirm(Intent intent) {
//		if (intent.getSerializableExtra(UserPointActivity.USERPOINT_MODEL) == null) {
//			Log.e(LOG_TAG, "onUserPointConfirm|UserPointModel is null.");
//			return;
//		}
//
//		mUserPointModel = (UserPointModel) intent.getSerializableExtra(UserPointActivity.USERPOINT_MODEL);
//		updateUserPoint();
//	}

	public boolean setUserPoint(OrderPackage pack) {
		if (mUserPointModel == null) {
			pack.put("point", "0");
		}else{
			pack.put("point", String.valueOf(mUserPointModel.getInputPoint()*10));
		}
		return true;
	}
	public void destroy() {
		mActivity = null;
	}

	@Override
	public void onSuccess(ArrayList<UserPointModel> v, Response response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(Ajax ajax, Response response) {
		// TODO Auto-generated method stub

	}
}