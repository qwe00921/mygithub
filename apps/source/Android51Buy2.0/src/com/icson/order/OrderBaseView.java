package com.icson.order;

import com.icson.lib.BaseView;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Parser;
import com.icson.util.ajax.Response;

public abstract class OrderBaseView< MODEL, MODELS> extends BaseView implements OnSuccessListener<MODELS>, OnErrorListener {

	protected OrderConfirmActivity mActivity;

	protected boolean mIsRequestDone = false;

	protected MODELS mModels;

	protected MODEL mModel;

	protected int mErrCode;

	protected String mErrMsg;

	@SuppressWarnings("rawtypes")
	protected Parser mParser;

	public int getErrCode() {
		return mErrCode;
	}

	public String getErrMsg() {
		return mErrMsg;
	}

	public MODELS getModels() {
		return mModels;
	}

	public MODEL getModel() {
		return mModel;
	}

	public boolean isSuccess() {
		return mParser != null && mParser.isSuccess();
	}

	public OrderBaseView(OrderConfirmActivity mActivity) {
		this.mActivity = mActivity;
	}

	public boolean IsRequestDone() {
		return mIsRequestDone;
	}

	@Override
	public abstract void onSuccess(MODELS v, Response response);

	@Override
	public abstract void onError(Ajax ajax, Response response);

	public void destroy() {
		mActivity = null;
		mModels = null;
		mModel = null;
		mParser = null;

	}
}
