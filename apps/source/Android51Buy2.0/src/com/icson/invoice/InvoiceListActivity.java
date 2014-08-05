package com.icson.invoice;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.ui.ListItemView;
import com.icson.lib.ui.UiUtils;
import com.icson.order.invoice.InvoiceParser;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class InvoiceListActivity extends BaseActivity implements OnSuccessListener<ArrayList<InvoiceModel>> ,OnItemClickListener {
	private static final int FLAG_REQUEST_INVOICE_MODIFY = 1;
	private static final int FLAG_REQUEST_INVOICE_ADD = 2;
	public static final int FLAG_RESULT_SAVE_OK = 3;
	public static final int FLAG_RESULT_DEL_OK = 4;
	
	private int mIid = 0;
	private ListView mListView;
	private  TextView mListEmptyView;
	private InvoiceListAdapter mAdapter;
	private ArrayList<InvoiceModel> mInvoiceModelList;
	ArrayList<String> invoiceContentOpts;
	ArrayList<Integer> invoiceContentSelectOpts;
	private InvoiceControl mInvoiceControl;
	private InvoiceParser mParser;
	private boolean isCanVAT = false;
	private boolean mEnableSelect = false;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.activity_list_invoice);
		
		final Intent intent = this.getIntent();
		if( null != intent ) {
			invoiceContentSelectOpts = intent.getIntegerArrayListExtra(InvoiceActivity.REQUEST_CONTENT_SELECT_OPT);
			invoiceContentOpts = intent.getStringArrayListExtra(InvoiceActivity.REQUEST_CONTENT_OPT);
			isCanVAT = intent.getBooleanExtra(InvoiceActivity.REQUEST_CAN_VAT, false);
			mIid = intent.getIntExtra(InvoiceActivity.REQUEST_INVOICE_IID, 0);
			mEnableSelect = intent.getBooleanExtra(InvoiceActivity.REQUEST_ENABLE_SELECT, false);
		}
		
		mInvoiceControl = new InvoiceControl(this);
		mParser = new InvoiceParser();
		mListView = (ListView) findViewById(R.id.invoice_listview);
		loadNavBar(R.id.invoicelist_navigation_bar);
		mNavBar.setOnDrawableRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				onAddItemClick();
			}
		});
		
		mListEmptyView = (TextView) findViewById(R.id.invoice_listview_empty);
		getInvoiceList();
	}
	
	public void getInvoiceList() {
		showLoadingLayer();
		mInvoiceModelList = new ArrayList<InvoiceModel>();
		mAdapter = new InvoiceListAdapter(this, mInvoiceModelList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mInvoiceControl.getInvoiceList(mParser, this, this);
	}
	
	@Override
	public void onDestroy() {
		mInvoiceControl.destroy();
		mInvoiceControl = null;
		mListView = null;
		super.onDestroy();
	}

	@Override
	public void onSuccess(ArrayList<InvoiceModel> v, Response response) {
		closeLoadingLayer();
		if( !mParser.isSuccess() ) {
			UiUtils.makeToast(InvoiceListActivity.this, TextUtils.isEmpty(mParser.getErrMsg()) ? Config.NORMAL_ERROR: mParser.getErrMsg());
			return;
		}
		
		// Moved to refreshList method, need to modify datasource in UI thread.
		Message msg = mUIHandler.obtainMessage(MSG_REFRESH_LIST, v);
		mUIHandler.sendMessage(msg);
//		boolean firstExc = false;
//		if(invoiceContentSelectOpts.size()<=0)
//			firstExc = true;
//		for(InvoiceModel m: v){
//			//如果不能开增值税，并且当前发票是增值税类型，那么不加入列表
//			if(!isCanVAT && m.getType() == InvoiceModel.INVOICE_TYPE_VAD)
//				continue ;
//			
//			mInvoiceModelList.add(m);
//			//all init 0  商品明细
//			if(firstExc)
//				invoiceContentSelectOpts.add(0);
//		}
//		mAdapter.notifyDataSetChanged();
		
	}
	
	private void refreshList(ArrayList<InvoiceModel> modelList) {
		
		if(mInvoiceModelList == null) {
			Log.d(TAG, "[refreshList]modelList is null");
			return;
		}
		
		if(invoiceContentSelectOpts == null) {
			Log.d(TAG, "[refreshList]invoiceContentSelectOpts is null");
			return;
		}
		
		boolean firstExc = false;
		if(invoiceContentSelectOpts.size()<=0)
			firstExc = true;
		for(InvoiceModel m: modelList){
			//如果不能开增值税，并且当前发票是增值税类型，那么不加入列表
			if(!isCanVAT && m.getType() == InvoiceModel.INVOICE_TYPE_VAD)
				continue ;
			
			mInvoiceModelList.add(m);
			//all init 0  商品明细
			if(firstExc)
				invoiceContentSelectOpts.add(0);
		}
		
		onRefreshView();
	}
	
	private void onRefreshView() {
		if(mAdapter == null) {
			Log.d(TAG, "[refreshList]mAdapter is null");
			return;
		}
		mAdapter.notifyDataSetChanged();
		
		if( null == mInvoiceModelList || 0 == mInvoiceModelList.size() ) {
			mListView.setVisibility(View.GONE);
			mListEmptyView.setVisibility(View.VISIBLE);
			return;
		}
		
		
		mListView.setVisibility(View.VISIBLE);
		mListEmptyView.setVisibility(View.GONE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if( mEnableSelect ) {
			final Intent intent = getIntent();
			InvoiceModel current = mInvoiceModelList.get(position);
			
			int opt = 0;
			if(position < invoiceContentOpts.size()) {
				opt = invoiceContentSelectOpts.get(position);
			}
			current.setContentOpt(opt);
			intent.putExtra(InvoiceActivity.RESPONSE_INVOICE_MODEL, current);
			setResult(FLAG_RESULT_SAVE_OK, intent);
			finish();
		} else {
			onEditItemClick(position);
		}
	}
	
	void onEditItemClick(int position) {
		Bundle bundle = new Bundle();
		bundle.putInt(InvoiceActivity.REQUEST_CONTENT_SELECT_OPT,invoiceContentSelectOpts.get(position));
		bundle.putSerializable(InvoiceActivity.REQUEST_CONTENT_OPT,invoiceContentOpts);
		bundle.putSerializable(InvoiceActivity.REQUEST_INVOICE_MODEL, mInvoiceModelList.get(position));
		bundle.putBoolean(InvoiceActivity.REQUEST_CAN_VAT,isCanVAT);
		ToolUtil.checkLoginOrRedirect(this, InvoiceActivity.class, bundle, FLAG_REQUEST_INVOICE_MODIFY);
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_InvoiceListActivity), InvoiceActivity.class.getName(), getString(R.string.tag_InvoiceActivity), "02012");
	}
	
	void onAddItemClick() {
		Bundle bundle = new Bundle();
		bundle.putSerializable(InvoiceActivity.REQUEST_CONTENT_OPT,invoiceContentOpts);
		bundle.putBoolean(InvoiceActivity.REQUEST_CAN_VAT,isCanVAT);
		ToolUtil.checkLoginOrRedirect(this, InvoiceActivity.class, bundle, FLAG_REQUEST_INVOICE_ADD);
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_InvoiceListActivity), InvoiceActivity.class.getName(), getString(R.string.tag_InvoiceActivity), "02011");
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case FLAG_REQUEST_INVOICE_MODIFY:
		case FLAG_REQUEST_INVOICE_ADD:
			InvoiceModel current = null != data ? (InvoiceModel)data.getSerializableExtra(InvoiceActivity.RESPONSE_INVOICE_MODEL) : null;
			if( null != current ) {
				if ( RESULT_OK == resultCode ) {
					if(FLAG_REQUEST_INVOICE_MODIFY == requestCode)
					{
						for(int idx = 0; idx < mInvoiceModelList.size();idx++)
						{
							InvoiceModel item = mInvoiceModelList.get(idx);
							if(item.getIid() == current.getIid())
							{
								invoiceContentSelectOpts.remove(idx);
							}
						}
					}
					invoiceContentSelectOpts.add(0, current.getContentOpt());
					onOperationDone(current);
					finish();
				} else if ( FLAG_RESULT_DEL_OK == resultCode ) {
					// Current model is deleted.
					int nSize = (null != mInvoiceModelList ? mInvoiceModelList.size() : 0);
					final int nDelIid = current.getIid();
					for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
						InvoiceModel entity = mInvoiceModelList.get(nIdx);
						if( (null != entity) && (entity.getIid() == nDelIid) ) {
							// Remove the item.
							mInvoiceModelList.remove(nIdx);
							invoiceContentSelectOpts.remove(nIdx);
							onRefreshView();
							nSize--;
							
							// Update current item.
							if( (nDelIid == mIid) && (nSize > 0) ) {
								entity = mInvoiceModelList.get(0);
								entity.setContentOpt(invoiceContentSelectOpts.get(0));
								onOperationDone(entity);
								mIid = entity.getIid();
							}else if((nDelIid == mIid) && (nSize == 0)) {
								onOperationDone(null);
								mIid = 0;
							}
							else
							{
								for(int idx = 0; idx <mInvoiceModelList.size();idx ++)
								{
									InvoiceModel item = mInvoiceModelList.get(idx);
									if(item.getIid() == mIid)
									{
										item.setContentOpt(invoiceContentSelectOpts.get(idx));
										onOperationDone(item);
										break;
									}
								}
							}
							
							break;
						}
					}
				}
			}
			break;
		}
	}
	
	/**
	 * 
	* method Name:onOperationDone    
	* method Description:  
	* @param nResultCode   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void onOperationDone(InvoiceModel mod) {
		final Intent intent = getIntent();
		intent.putExtra(InvoiceActivity.RESPONSE_INVOICE_MODEL, mod);
		intent.putIntegerArrayListExtra(InvoiceActivity.REQUEST_CONTENT_SELECT_OPT, invoiceContentSelectOpts);
		setResult(FLAG_RESULT_SAVE_OK, intent);
	}
	
	/**
	 * @author lorenchen
	 *
	 */
	final class InvoiceListAdapter extends BaseAdapter implements OnClickListener {
		
		public InvoiceListAdapter(Context context, List<InvoiceModel> list) {
			mContext = context;
			mInvoices = list;
		}
		
		@Override
		public int getCount() {
			return (null != mInvoices ? mInvoices.size() : 0);
		}

		@Override
		public Object getItem(int arg0) {
			return (null != mInvoices ? mInvoices.get(arg0) : null);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemHolder pHolder = null;
			if( null == convertView ) {
				if( null == mInflater ) {
					mInflater = LayoutInflater.from(mContext);
				}
				convertView = mInflater.inflate(R.layout.list_item_invoice, null);
				pHolder = new ItemHolder();
				pHolder.mView =(ListItemView) convertView.findViewById(R.id.invoice_item_content_layout);
				pHolder.mName = (TextView)convertView.findViewById(R.id.invoice_item_name);
				pHolder.mInfo = (TextView)convertView.findViewById(R.id.invoice_item_info);
				pHolder.mEdit = (ImageView)convertView.findViewById(R.id.invoice_item_edit);
				convertView.setTag(pHolder);
			} else {
				pHolder = (ItemHolder)convertView.getTag();
			}
			
			// Initialize the item.
			InvoiceModel item = (InvoiceModel)this.getItem(position);
			if( null != item ) {
				pHolder.mName.setText(item.getTitle());
				pHolder.mInfo.setText(item.getTypeName(mContext));
				final int nIid = item.getIid();
				if (mIid > 0 && mIid == nIid) {
					pHolder.mView.setSelected(true);
				}else{
					pHolder.mView.setSelected(false);
				}
				
				pHolder.mEdit.setOnClickListener(this);
				pHolder.mEdit.setTag(R.id.holder_pos, position);
			}
			
			return convertView;
		}
		
		@Override
		public void onClick(View v) {
			switch( v.getId() ) {
			case R.id.invoice_item_edit:
				onEditItemClick((Integer) v.getTag(R.id.holder_pos));
				break;
			}
		}
		
		private LayoutInflater     mInflater = null;
		private Context            mContext;
		private List<InvoiceModel> mInvoices;
	}
	
	final class ItemHolder {
		ListItemView mView = null;
		TextView   mName = null;
		TextView   mInfo = null;
		ImageView  mEdit = null;
	}
	
	private static final String TAG = "InvoiceListActivity";
	private static final int MSG_REFRESH_LIST = 1001;
	
	private Handler mUIHandler = new UIHandler(this);
	private static class UIHandler extends Handler {
		private final WeakReference<InvoiceListActivity> mRef;
		public UIHandler(InvoiceListActivity activity) {
			mRef = new WeakReference<InvoiceListActivity>(activity);
		}
		
		public void handleMessage(Message msg) {
			if (msg == null) {
				return;
			}
			InvoiceListActivity parent = mRef.get();
			if(parent == null) {
				return;
			}

			int msgCode = msg.what;
			switch (msgCode) {
			case MSG_REFRESH_LIST: {
				@SuppressWarnings("unchecked")
				ArrayList<InvoiceModel> modelList = (ArrayList<InvoiceModel>) msg.obj;
				parent.refreshList(modelList);
				break;
			}
			
			default:
				break;
			}
		};
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_InvoiceListActivity);
	}
}
