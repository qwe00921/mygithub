package com.icson.invoice;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.ILogin;
import com.icson.lib.inc.DispatchFactory;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.EditField;
import com.icson.lib.ui.RadioDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class InvoiceActivity extends BaseActivity {
	public static final String REQUEST_INVOICE_MODEL = "content_model";
	public static final String REQUEST_CONTENT_OPT = "content_opt";
	public static final String REQUEST_CONTENT_SELECT_OPT = "content_select_opt";
	public static final String RESPONSE_INVOICE_MODEL = "invoice_model";
	public static final String RESPONSE_USER_NAME = "invoice_name";
	public static final String RESPONSE_CONTENT_OPT = "content_opt";
	public static final String REQUEST_CAN_VAT = "is_can_vat";
	public static final String REQUEST_INVOICE_IID = "invoice_iid";
	public static final String REQUEST_ENABLE_SELECT = "enable_select"; // Check whether selection for order confirm.

	private EditField mType;
	private EditField mContent;
	private EditField mTitle;
	private TextView mOkay;
	private TextView mDelete;
	private View mVadLayout = null;
	private EditField mCompanyName;
	private EditField mCompanyAddr;
	private EditField mCompanyTel;
	private EditField mTaxNum;
	private EditField mBankAccount;
	private EditField mBankBranch;
	private AppDialog pDialog;
	
	
	private boolean mCanVAT;
	private ArrayList<String> mContentOpts;
	private InvoiceModel mInvoiceModel;
	private InvoiceControl mInvoiceControl;
	private int     mSelectOpt;
	private final class TypeItem {
		public int mResId;
		public int mTypeId;
	}
	
	private ArrayList<TypeItem> mTypes = null;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		Intent intent = getIntent();
		if (intent.getStringArrayListExtra(InvoiceActivity.REQUEST_CONTENT_OPT) == null) {
			UiUtils.makeToast(this, R.string.params_error,true);
			finish();
			return;
		}
		
		mSelectOpt = intent.getIntExtra(InvoiceActivity.REQUEST_CONTENT_SELECT_OPT, 0);
		
		// 发票内容
		mContentOpts = intent.getStringArrayListExtra(InvoiceActivity.REQUEST_CONTENT_OPT);
		mCanVAT = intent.getBooleanExtra(REQUEST_CAN_VAT, false);
		
		// Unify the layout.
		setContentView(R.layout.activity_invoice);
		
		// Initialize title.
		loadNavBar(R.id.invoice_navigation_bar);
		mType = (EditField)findViewById(R.id.invoice_type);
		mType.setOnDrawableRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				showTypeList();
				
			}
		});
		mContent = (EditField)findViewById(R.id.invoice_content);
		mContent.setOnDrawableRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				showContentList();
			}
		});
		mTitle = (EditField)findViewById(R.id.invoice_title);
		mVadLayout = findViewById(R.id.invoice_vad_layout);
		
		// Information for vad field.
		mCompanyName = (EditField)findViewById(R.id.invoice_company_name);
		mCompanyAddr = (EditField)findViewById(R.id.invoice_company_addr);
		mCompanyTel = (EditField)findViewById(R.id.invoice_company_tel);
		mCompanyTel.setEditInputType(InputType.TYPE_CLASS_PHONE);
		
		mTaxNum = (EditField)findViewById(R.id.invoice_tax_num);
		mBankAccount = (EditField)findViewById(R.id.invoice_bank_account);
		mBankBranch = (EditField)findViewById(R.id.invoice_bank_branch);
		
		mOkay = (TextView)findViewById(R.id.invoice_btn_okay);
		mOkay.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				saveInvoice();
			}
		});
		mDelete = (TextView)findViewById(R.id.invoice_btn_delete);
		mDelete.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(pDialog == null)
				{
					pDialog = UiUtils.showDialog(InvoiceActivity.this, R.string.caption_hint, 
						R.string.is_sure_del_invoice, R.string.btn_delete,R.string.btn_cancel,
						new AppDialog.OnClickListener(){

							@Override
							public void onDialogClick(int nButtonId) {
								if(AppDialog.BUTTON_POSITIVE == nButtonId)
									deleteInvoice();
								//else if(null!=pDialog)
								//	pDialog.hide();
							}});
				}
				else
					pDialog.show();
			}
		});
		
		// Load content.
		mInvoiceModel = (InvoiceModel) intent.getSerializableExtra(InvoiceActivity.REQUEST_INVOICE_MODEL);
		if( null != mInvoiceModel ) {
			setNavBarText(R.string.edit_invoice);
			mDelete.setVisibility(View.VISIBLE);
		} else {
			mInvoiceModel = new InvoiceModel();
			mInvoiceModel.setType( ( ILogin.getSiteId() == DispatchFactory.SITE_SZ ) ? InvoiceModel.INVOICE_TYPE_NORMAL : InvoiceModel.INVOICE_TYPE_PERSONAL);
			setNavBarText(R.string.add_invoice);
			mDelete.setVisibility(View.GONE);
		}
		
		// Build the type information.
		loadTypeItems();
		
		// Initialize content.
		loadContent(mInvoiceModel, mCanVAT);
	}
	
	
	@Override
	public void onDestroy() {
		if(null!=pDialog)
		{
			pDialog.dismiss();
			pDialog = null;
		}
		super.onDestroy();
	}
	
	private void loadContent(InvoiceModel model, boolean canVAT) {
		if( null == model )
			return ;
		
		mType.setContent(model.getTypeName(this));
		
		String strContent = null;
		
		if(isVADType()) {
			strContent = getString(R.string.invoice_default_vad_content);
			mInvoiceModel.setContentOpt(0);
			mInvoiceModel.setContent(strContent);
		} else {
			strContent = model.getContent();
			if( TextUtils.isEmpty(strContent) && null != mContentOpts && mContentOpts.size() > 0 ) {
				strContent = mContentOpts.get(mSelectOpt);
				mInvoiceModel.setContentOpt(mSelectOpt);
				mInvoiceModel.setContent(mContentOpts.get(mSelectOpt));
			}
		}
		mContent.setContent(strContent);
		mTitle.setContent(model.getTitle());
		
		//VAD 增值税发票特殊处理 
		final int type = model.getType();
		if( InvoiceModel.INVOICE_TYPE_VAD == type ) {
			mTitle.setVisibility(View.GONE);
			mVadLayout.setVisibility(View.VISIBLE);
			
			mCompanyName.setContent(model.getName());
			mCompanyAddr.setContent(model.getAddress());
			mCompanyTel.setContent(model.getPhone());
			mTaxNum.setContent(model.getTaxno());
			mBankAccount.setContent(model.getBankno());
			mBankBranch.setContent(model.getBankname());
			
		} else {
			mTitle.setVisibility(View.VISIBLE);
			mVadLayout.setVisibility(View.GONE);
		}
	}
	
	private void loadTypeItems() {
		if( null == mTypes )
			mTypes = new ArrayList<TypeItem>();
		else
			mTypes.clear();
		
		if( ILogin.getSiteId() == DispatchFactory.SITE_SZ ) {
			addTypeItem(R.string.invoice_type_retail, InvoiceModel.INVOICE_TYPE_NORMAL);
		} else {
			addTypeItem(R.string.invoice_type_personal, InvoiceModel.INVOICE_TYPE_PERSONAL);
			addTypeItem(R.string.invoice_type_company, InvoiceModel.INVOICE_TYPE_COMPANY);
		}
		
		if( mCanVAT ) {
			addTypeItem(R.string.invoice_type_vad, InvoiceModel.INVOICE_TYPE_VAD);
		}
	}
	
	private boolean isVADType() {
		
		if(null != mType) {
			String strType = mType.getContent();
			if(strType != null && strType.equals(getString(R.string.invoice_type_vad))) {
				return true;
			}
		}
		return false;
	}
	
	private void addTypeItem(int nResId, int nTypeId) {
		if( null == mTypes )
			return ;

		TypeItem item = new TypeItem();
		item.mResId = nResId;
		item.mTypeId = nTypeId;
		mTypes.add(item);
	}
	
	
	
	private void showTypeList() {
		final int nTypes = (null != mTypes ? mTypes.size() : 0);
		if( 0 >= nTypes )
			return ;
		
		final int currentType = null != mInvoiceModel ? mInvoiceModel.getType() : -1;
		
		String[] options = new String[nTypes];
		int checkedItem = -1;
		for( int nIdx = 0; nIdx < nTypes; nIdx++ ) {
			TypeItem item = mTypes.get(nIdx);
			options[nIdx] = getString(item.mResId);
			if( currentType == item.mTypeId )
				checkedItem = nIdx;
		}
		
		// Show dialog.
		UiUtils.showListDialog(this, getString(R.string.invoice_type), options, checkedItem, new RadioDialog.OnRadioSelectListener(){
			@Override
			public void onRadioItemClick(int which) {
				TypeItem item = mTypes.get(which);
				mType.setContent(getString(item.mResId));
				mInvoiceModel.setType(item.mTypeId);
				
				// Update layout
				loadContent(mInvoiceModel, mCanVAT);
			}
		});
	}
	
	private void showContentList() {
		String[] options = null;
		int checkedItem = -1;
		
		if(isVADType()) {
			options = new String[] { getString(R.string.invoice_default_vad_content) };
			checkedItem = 0;
		} else {
			options = new String[mContentOpts.size()];
			mContentOpts.toArray(options);
			
			String currentOption = (null != mInvoiceModel && !TextUtils.isEmpty(mInvoiceModel.getContent()) ? mInvoiceModel.getContent() : mContent.getContent());
			checkedItem =  TextUtils.isEmpty(currentOption) ? -1 : mContentOpts.indexOf(currentOption); 
		}
		
		// Show dialog.
		UiUtils.showListDialog(this, getString(R.string.invoice_content), options, checkedItem, new RadioDialog.OnRadioSelectListener(){
			@Override
			public void onRadioItemClick(int which) {
				String strContent = mContentOpts.get(which);
				mContent.setContent(strContent);
				mInvoiceModel.setContent(strContent);
				mInvoiceModel.setContentOpt(which);
			}
		});
	}
	
	private void deleteInvoice() {
		if( null == mInvoiceControl ) {
			mInvoiceControl = new InvoiceControl(this);
		}
		
		showProgressLayer();
		mInvoiceControl.delete(mInvoiceModel, new OnSuccessListener<JSONObject>(){
			@Override
			public void onSuccess(JSONObject v, Response response) {
				postSuccess(v, response, true);
			}
		}, this);
	}

	private void saveInvoice() {
		if (mInvoiceModel.getType() == InvoiceModel.INVOICE_TYPE_VAD) {
			String name = mCompanyName.getContent();
			if (TextUtils.isEmpty(name)) {
				UiUtils.makeToast(this, R.string.invoice_company_name_empty);
				return;
			}
			String address = mCompanyAddr.getContent();
			if (TextUtils.isEmpty(address)) {
				UiUtils.makeToast(this, R.string.invoice_company_addr_empty);
				return;
			}
			String phone = mCompanyTel.getContent();
			if (TextUtils.isEmpty(phone)) {
				UiUtils.makeToast(this, R.string.invoice_company_tel_empty);
				return;
			}
			String taxNO = mTaxNum.getContent();
			if (TextUtils.isEmpty(taxNO)) {
				UiUtils.makeToast(this, R.string.invoice_tax_num_empty);
				return;
			}
			String bankNO = mBankAccount.getContent();
			if ("".equals(bankNO)) {
				UiUtils.makeToast(this, R.string.invoice_bank_account_empty);
				return;
			}
			String bank = mBankBranch.getContent();
			if (TextUtils.isEmpty(bank)) {
				UiUtils.makeToast(this, R.string.invoice_bank_branch_empty);
				return;
			}

			mInvoiceModel.setTitle(name);
			mInvoiceModel.setName(name);
			mInvoiceModel.setAddress(address);
			mInvoiceModel.setPhone(phone);
			mInvoiceModel.setTaxno(taxNO);
			mInvoiceModel.setBankno(bankNO);
			mInvoiceModel.setBankname(bank);
		} else {
			String strTitle = mTitle.getContent();
			if (TextUtils.isEmpty(strTitle)) {
				UiUtils.makeToast(this, R.string.hint_invoice_fill_title);
				return;
			}

			mInvoiceModel.setTitle(strTitle);
		}
		
		if( null == mInvoiceControl )
			mInvoiceControl = new InvoiceControl(this);

		showProgressLayer();
		mInvoiceControl.set(mInvoiceModel, new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				postSuccess(v, response, false);
			}
		}, this);
	}
	
	private void postSuccess(JSONObject v, Response response, boolean isDelete) {
		closeProgressLayer();
		try {
			final int errno = v.getInt("errno");

			if (errno != 0) {
				String data = v.has("data") ? v.getString("data") : "";
				UiUtils.makeToast(this, (TextUtils.isEmpty(data) ? getString(R.string.invoice_info_error) : data));
				return;
			}

			if( isDelete ) {
				UiUtils.makeToast(this, R.string.invoice_delete_success);
				onOperationDone(InvoiceListActivity.FLAG_RESULT_DEL_OK);
			} else {
				if (mInvoiceModel.getIid() == 0) {
					mInvoiceModel.setIid(v.getInt("data"));
				}
				onOperationDone(RESULT_OK);
			}
		} catch (Exception ex) {
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
			UiUtils.makeToast(this, R.string.server_error);
		}
	}
	
	private void onOperationDone(int nResultCode) {
		final Intent intent = getIntent();
		intent.putExtra(InvoiceActivity.RESPONSE_INVOICE_MODEL, mInvoiceModel);
		setResult(nResultCode, intent);
		finish();
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_InvoiceActivity);
	}
}
