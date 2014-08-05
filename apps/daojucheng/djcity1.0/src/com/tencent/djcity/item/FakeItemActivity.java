package com.tencent.djcity.item;

import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.pay.PayCore;
import com.tencent.djcity.lib.pay.PayFactory;
import com.tencent.djcity.lib.ui.RadioDialog;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.AppUtils;
import com.tencent.djcity.util.AppUtils.DescProvider;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class FakeItemActivity extends BaseActivity implements OnClickListener, OnSuccessListener<JSONObject>, DescProvider{
	private TextView infoTextV;
	private TextView buyText;
	private TextView shareText;
	
	private String pf;
	private String tokenUrl;
	private String offerid;
	private String payMethodNames[];
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fakeitem);
		infoTextV = (TextView) this.findViewById(R.id.fake_info);
		buyText = (TextView) this.findViewById(R.id.submit_btn);
		shareText = (TextView) this.findViewById(R.id.share_btn);
		shareText.setOnClickListener(this);
		buyText.setOnClickListener(this);
		
        payMethodNames = new String[2];
    	payMethodNames[0] = this.getString(R.string.paytype_midas);
    	payMethodNames[1] = this.getString(R.string.paytype_wxpay);
	}
	
	
	@Override
	protected void onResume() {
		Ajax ajx = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/djapp_buy.php?" +
				"_appname=cf&propid=1&buynum=1&_appcode=djapp&areaid=320");
		ajx.setParser(new JSONParser());
		ajx.setOnSuccessListener(this);
		ajx.send();
		
		super.onResume();
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}
	
	@Override
	public void onClick(View v) {
		int rid = v.getId();
		if(rid == R.id.submit_btn)
		{
			UiUtils.showListDialog(this, getString(R.string.orderconfirm_choose_paytype), payMethodNames, 0, new RadioDialog.OnRadioSelectListener() {
				
				@Override
				public void onRadioItemClick(int which) {
					PayCore core =  PayFactory.getInstance(FakeItemActivity.this, which+1, tokenUrl);
					if(null!=core)
					{
						core.submit();
					}
				}
			});
		}
		else if(rid == R.id.share_btn)
			{
				AppUtils.shareAppInfo(this, getString(R.string.share_content),
						"http://www.baidu.com", "", this);
				
			}
	}

	@Override
	public void onSuccess(JSONObject v, Response response) {
		
		
	}


	@Override
	public String getDesc(String strPackageName) {
		// TODO Auto-generated method stub
		return null;
	}
}
