package com.tencent.djcity.discover;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.model.Account;
import com.tencent.djcity.lib.model.GiftModel;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class GiftcenterActivity extends BaseActivity implements OnSuccessListener<JSONObject>{
	private Button mHintBtn;
	private TextView mHintText;
	private ListView mList;

	private ArrayList<GiftModel> mGifts;
	private GiftFetchAdapter mGiftAdapter;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_giftcenter);
		loadNavBar(R.id.discover_navbar);
		
	
		mHintText = (TextView)findViewById(R.id.info_text);
		mHintText.setText(R.string.text_view_history);
		mHintBtn = (Button)findViewById(R.id.info_btn);
		mHintBtn.setText(R.string.text_view_history);
		mHintBtn.setOnClickListener(this);
		
		mList = (ListView) this.findViewById(R.id.gift_list);
		mList.setClickable(false);
		mGiftAdapter = new GiftFetchAdapter(this);
		mList.setAdapter(mGiftAdapter);
	}
	
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		fetchInfo();
		
	}
	
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.info_btn)
		{
			UiUtils.makeToast(this, "not done");//go gift_history
			
		}
		
	}
	
	
	private void fetchInfo() {
		showLoadingLayer();
		Account act = ILogin.getActiveAccount();
		if(null == act)
		{
			UiUtils.makeToast(this, "Logout");
			return;
		}
		Ajax ajx = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/listGift.php");
		
		ajx.setData("uin", act.getUin());
		ajx.setParser(new JSONParser());
		ajx.setOnSuccessListener(this);
		ajx.send();
		
		this.addAjax(ajx);
	}




	@Override
	public void onSuccess(JSONObject v, Response response) {
		closeLoadingLayer();
		final int ret = v.optInt("ret",-1);
		if(ret!=0)
		{
			UiUtils.makeToast(this, v.optString("msg"));
			return;
		}
			
		JSONArray data =  v.optJSONArray("list");
		if(data == null || data.length()<=0)
			return;
		
		mGifts = new ArrayList<GiftModel>();
		try {
			for(int i = 0; null!=data && i< data.length() ; i++)
			{
				JSONObject item = data.getJSONObject(i);
				GiftModel amodle = new GiftModel();
				amodle.setName(item.optString("sGoodsName"));
				amodle.setPicUrl(item.optString("sGoodsPic"));
				amodle.setTime(item.optString("dtGetTime"));
				int status = item.optInt("iStatus");
				if(status > 0 )
					amodle.fetchedGift();
				mGifts.add(amodle);
				
			}
			
			this.mGiftAdapter.setData(mGifts);
			mGiftAdapter.notifyDataSetChanged();
			
		}catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
}
