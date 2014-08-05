package com.icson.order.invoice;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;

import com.icson.R;
import com.icson.address.AddressModel;
import com.icson.invoice.InvoiceActivity;
import com.icson.invoice.InvoiceControl;
import com.icson.invoice.InvoiceListActivity;
import com.icson.invoice.InvoiceModel;
import com.icson.lib.IPageCache;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.ui.TextField;
import com.icson.lib.ui.UiUtils;
import com.icson.order.OrderBaseView;
import com.icson.order.OrderConfirmActivity;
import com.icson.order.OrderPackage;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.Response;

public class InvoiceView extends
		OrderBaseView<InvoiceModel, ArrayList<InvoiceModel>> {

	private static final String LOG_TAG = InvoiceView.class.getName();

	public static final int FLAG_REQUDST_INVOICE_CHECK = 1;

	private InvoiceModel mInvoiceModel;

	private InvoiceControl mInvoiceControl;
	
	private ArrayList<Integer> mInvoiceContentSelectOpts;

	public InvoiceView(OrderConfirmActivity activity) {
		super(activity);
		mInvoiceControl = new InvoiceControl(mActivity);
		mParser = new InvoiceParser();
		mInvoiceContentSelectOpts = new ArrayList<Integer>();
	}

	public void setInvoiceContentSelectOpts(ArrayList<Integer> aOpts)
	{
		if(null!=aOpts)
			mInvoiceContentSelectOpts = aOpts; 
	}
	
	public InvoiceModel getmInvoiceModel() {
		return mInvoiceModel;
	}

	public void requestFinish() {
		mIsRequestDone = true;
		renderInvoice();
		mActivity.ajaxFinish(OrderConfirmActivity.VIEW_FLAG_INVOICE_VIEW);
	}

	/*
	public void getInvoiceList() {

		mInvoiceModel = null;
		mIsRequestDone = false;

		if (mActivity.getOrderAddressView().getModel() == null) {
			requestFinish();
			return;
		}

		// 初始化iid
		final IPageCache cache = new IPageCache();
		String siid = cache.get(CacheKeyFactory.CACHE_ORDER_INVOICE_ID);

		int iid = siid == null ? 0 : Integer.valueOf(siid);

		if (iid == 0) {
			final AddressModel mAddressModel = mActivity.getOrderAddressView()
					.getModel();
			iid = mAddressModel == null ? 0 : mAddressModel.getIid();
		}

		final int initInvoiceId = iid;

		mInvoiceControl.getInvoiceList((InvoiceParser) mParser,
				new OnSuccessListener<ArrayList<InvoiceModel>>() {
					@Override
					public void onSuccess(ArrayList<InvoiceModel> v,
							Response response) {

						if (!mParser.isSuccess()) {
							UiUtils.makeToast(mActivity, mParser.getErrMsg());
							requestFinish();
							return;
						}

						InvoiceModel tmpModel = null;

						for (InvoiceModel model : v) {
							if (model.getIid() == initInvoiceId) {
								tmpModel = model;
								break;
							}
						}

						tmpModel = tmpModel == null ? (v.size() > 0 ? v.get(0)
								: null) : tmpModel;

						if (tmpModel != null) {
							mInvoiceModel = tmpModel;
							mInvoiceModel.setContent(mActivity
									.getShoppingCartView().getModel()
									.getInvoiceContentOpt().get(0));
						}
						// 如果默认发票是增值税，但是当前不支持开增值税发票，那么清空
						if (mInvoiceModel != null
								&& !mActivity.getShoppingCartView().getModel()
										.isCanVAT()
								&& mInvoiceModel.getType() == InvoiceModel.INVOICE_TYPE_VAD) {
							mInvoiceModel = null;
						}
						requestFinish();

					}
				}, new OnErrorListener() {
					@Override
					public void onError(Ajax ajax, Response response) {
						UiUtils.makeToast(mActivity, "加载发票信息失败");
						mInvoiceModel = null;
						requestFinish();
					}
				});
	}
*/
	private void renderInvoice() {
		TextField invoiceView = (TextField) mActivity
				.findViewById(R.id.orderconfirm_invoice);
		if (mInvoiceModel != null)
			invoiceView.setContent(mInvoiceModel.getTitle(),
					mInvoiceModel.getTypeName(mActivity));
		else
			invoiceView
					.setContent(mActivity.getString(R.string.select_default));
	}

	public void setInvoice(InvoiceModel invoiceModel) {
		mInvoiceModel = invoiceModel;
		// 如果木有地址，那么发票信息也是空
		if (mActivity.getOrderAddressView().getModel() == null) {
			mInvoiceModel = null;
		}
		renderInvoice();
	}

	public boolean setInvoicePackage(OrderPackage pack) {

		if (mInvoiceModel == null || mInvoiceModel.getIid() == 0) {
			UiUtils.makeToast(mActivity, "请填写发票信息");
			return false;
		}

		final InvoiceModel model = mInvoiceModel;
		// String title = model.getType() == InvoiceModel.INVOICE_TYPE_ZEN ?
		// model.getTitle() : model.getTitle();
		/*
		 * //当发票为个人时，发票名称为收货人 if (model.getType() ==
		 * InvoiceModel.INVOICE_TYPE_PERSONAL) { final AddressModel addressModel
		 * = mActivity.getOrderAddressView().getModel(); title =
		 * addressModel.getName(); }
		 */

		pack.put("invoiceId", model.getIid());
		pack.put("invoiceType", model.getType());
		pack.put("invoiceTitle", model.getTitle());

		pack.put("invoiceContent", model.getContent());

		// :todo
		// pack.put("invoiceContent",
		// productInfo.contentOpt[invoice.invoiceContentIdx] || '商品明细' );

		if (model.getType() == InvoiceModel.INVOICE_TYPE_VAD) {
			pack.put("invoiceCompanyName", model.getTitle());
			pack.put("invoiceCompanyAddr", model.getAddress());
			pack.put("invoiceCompanyTel", model.getPhone());
			pack.put("invoiceTaxno", model.getTaxno());
			pack.put("invoiceBankNo", model.getBankno());
			pack.put("invoiceBankName", model.getBankname());
		} else {
			pack.put("invoiceCompanyName", "");
			pack.put("invoiceCompanyAddr", "");
			pack.put("invoiceCompanyTel", "");
			pack.put("invoiceTaxno", "");
			pack.put("invoiceBankNo", "");
			pack.put("invoiceBankName", "");
		}

		return true;
	}

	//InvoiceView --> InvoiceList to select
	public void selectInvoice() {
		final AddressModel mAddressModel = mActivity.getOrderAddressView()
				.getModel();
		if (mAddressModel == null || mAddressModel.getName() == null
				|| mAddressModel.getName().trim().equals("")) {
			UiUtils.makeToast(mActivity, "请先填写收货人信息");
			return;
		}

		final Bundle params = new Bundle();
		// 在发票管理页，提交个人发票时需要发票抬头, 从本页带入
		if (mInvoiceModel == null) {
			mInvoiceModel = new InvoiceModel();
			mInvoiceModel.setTitle(mAddressModel.getName());
			mInvoiceModel.setType(InvoiceModel.INVOICE_TYPE_PERSONAL);
		}

		ArrayList<String> opts = mActivity.getShoppingCartView().getModel()
				.getInvoiceContentOpt();
		if (opts.indexOf(mInvoiceModel.getContent()) == -1) {
			mInvoiceModel.setContent(opts.get(0));
		}


		params.putSerializable(InvoiceActivity.REQUEST_CONTENT_SELECT_OPT, mInvoiceContentSelectOpts);

		params.putSerializable(InvoiceActivity.REQUEST_CONTENT_OPT,mActivity
				.getShoppingCartView().getModel().getInvoiceContentOpt());
		params.putBoolean(InvoiceActivity.REQUEST_CAN_VAT, mActivity
				.getShoppingCartView().getModel().isCanVAT());
		params.putInt(InvoiceActivity.REQUEST_INVOICE_IID,
				(null != mInvoiceModel ? mInvoiceModel.getIid() : 0));
		params.putBoolean(InvoiceActivity.REQUEST_ENABLE_SELECT, true);
		// params.putSerializable(InvoiceActivity.REQUEST_INVOICE_MODEL,
		// mInvoiceModel);

		ToolUtil.checkLoginOrRedirect(mActivity, InvoiceListActivity.class,
				params, FLAG_REQUDST_INVOICE_CHECK);
		ToolUtil.sendTrack( mActivity.getClass().getName(), mActivity.getString(R.string.tag_OrderConfirmActivity), 
				InvoiceListActivity.class.getName(), mActivity.getString(R.string.tag_InvoiceListActivity), "05012");
	}

	public void onInvoiceConfirm(Intent intent) {
		if (intent.getSerializableExtra(InvoiceActivity.RESPONSE_INVOICE_MODEL) == null) {
			setInvoice(null);
			Log.e(LOG_TAG, "onInvoiceConfirm|invoiceModel is null.");
			return;
		}

		mInvoiceModel = (InvoiceModel) intent
				.getSerializableExtra(InvoiceActivity.RESPONSE_INVOICE_MODEL);
		
		ArrayList<String> OptStrs = mActivity
			.getShoppingCartView().getModel().getInvoiceContentOpt();
		if( null != OptStrs) {
			mInvoiceModel.setContent(OptStrs.get(mInvoiceModel.getContentOpt()));
		}
		renderInvoice();

		// 保存用户最后选择的发票
		IPageCache cache = new IPageCache();
		cache.set(CacheKeyFactory.CACHE_ORDER_INVOICE_ID,
				String.valueOf(mInvoiceModel.getIid()), 0);
	}

	public void destroy() {
		mInvoiceControl.destroy();
		mInvoiceControl = null;
		mActivity = null;
	}

	@Override
	public void onSuccess(ArrayList<InvoiceModel> v, Response response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(Ajax ajax, Response response) {
		// TODO Auto-generated method stub

	}
}