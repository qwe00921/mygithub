package com.tencent.djcity.item;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.tencent.djcity.R;
import com.tencent.djcity.home.recommend.ProductModel;
import com.tencent.djcity.lib.AppStorage;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.model.Account;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.list.ListActivity;
import com.tencent.djcity.list.ListAdapter;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.AppUtils;
import com.tencent.djcity.util.AppUtils.DescProvider;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;


public class PaySuccActivity extends BaseActivity implements DescProvider,
		OnSuccessListener<JSONObject>, OnItemClickListener{
	
	private String  mShareContent="";
	private  String mPicUrl = "";
	private  String mLinkUrl = "http://daoju.qq.com/";
	
	private List<ProductModel> mProductList = new ArrayList<ProductModel>();
	private ListView mListView;
	private ListAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_paysucc);
		loadNavBar(R.id.payinfo_navbar);
		
		findViewById(R.id.share_btn).setOnClickListener(this);
		
		mListView = (ListView)findViewById(R.id.list_listview);
		mListView.setOnItemClickListener(this);
		mAdapter = new ListAdapter(this,mProductList);
		mListView.setAdapter(mAdapter);
		
		String orderID  = AppStorage.getData("WXOrder");
		AppStorage.delData("WXOrder");
	
		fetchPayInfo();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		
	}
	
	@Override
	public void onClick(View v)
	{
		if(v.getId() == R.id.share_btn)
		{
			{
				AppUtils.shareAppInfo(this, getString(R.string.share_content),
						this.mLinkUrl, this.mPicUrl, this);
				
			}
		}
		else
			super.onClick(v);
	}
	
	
	
	private void fetchPayInfo() {
		showLoadingLayer();
		Account act = ILogin.getActiveAccount();
		if(null == act)
		{
			UiUtils.makeToast(this, "Logout");
			return;
		}
		Ajax ajx = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/recommend.php");
		ajx.setParser(new JSONParser());
		ajx.setOnSuccessListener(this);
		ajx.send();
		this.addAjax(ajx);
		
	}

	@Override
	public void onSuccess(JSONObject v, Response response) {
		final int ret = v.optInt("result",-1);
		if(ret!=0)
		{
			UiUtils.makeToast(this, v.optString("msg"));
			return;
		}
		
		JSONObject data =  v.optJSONObject("data");
		if(null == data)
			return;
		JSONObject share = data.optJSONObject("share");
		if(share!=null)
		{
			mPicUrl = share.optString("pic");
			mShareContent = share.optString("text");
		}
		
		mProductList.clear();
		JSONArray var = data.optJSONArray("recommends");
		for(int i = 0; null!=var && i<var.length(); i++)
		{
			JSONObject item  = var.optJSONObject(i);
			if(item != null) {
				ProductModel productModel = ProductModel.fromJson(item);
				mProductList.add(productModel);
			}
		}
		
		mAdapter.notifyDataSetChanged();
		
	}

	@Override
	public String getDesc(String strPackageName) {
		return mShareContent;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
		Bundle abundle = new Bundle();
		abundle.putString(ItemActivity.KEY_PROP_ID, String.valueOf(id));
		UiUtils.startActivity(this,ItemActivity.class,abundle, true);// TODO Auto-generated method stub
		
	}
	
	
	
}
