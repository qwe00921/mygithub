package com.tencent.djcity.discover;

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
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class GiftHistroyActivity extends BaseActivity implements OnSuccessListener<JSONObject>{
	private ListView mList;
	private GiftHistoryItemAdapter mGiftAdapter;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		loadNavBar(R.id.navigation_bar);
		
		mList = (ListView) this.findViewById(R.id.history_list);
		mGiftAdapter = new GiftHistoryItemAdapter(this);
		mList.setAdapter(mGiftAdapter);
	}
	
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		fetchHistory();
		
	}
	
	
	private void fetchHistory() {
		showLoadingLayer();
		Account act = ILogin.getActiveAccount();
		if(null == act)
		{
			UiUtils.makeToast(this, "Logout");
			return;
		}
		Ajax ajx = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/getLimit.php?type=shake&biz=" +
				"&uin=" + act.getUin());
		ajx.setParser(new JSONParser());
		ajx.setOnSuccessListener(this);
		ajx.send();
		
		this.addAjax(ajx);
	}




	@Override
	public void onSuccess(JSONObject v, Response response) {
		this.closeLoadingLayer();
		
	}
}
