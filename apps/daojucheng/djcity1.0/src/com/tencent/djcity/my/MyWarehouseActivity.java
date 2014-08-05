package com.tencent.djcity.my;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.more.GameInfo;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ImageLoadListener;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class MyWarehouseActivity extends BaseActivity {

	private ListView mListView;
	private WarehouseParser mParser;
	private WarehouseAdapter mAdapter;
	
	private ImageView mGameIcon;
	private TextView mGameInfo;
	
	private ArrayList<WarehouseModel> mModels;
	private ImageLoader mImageLoader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_my_warehouse);
		
		initUI();
		
		this.loadNavBar(R.id.warehouse_navbar);
		
		
		mImageLoader = new ImageLoader(this, Config.CHANNEL_PIC_DIR, true);
		mModels = new ArrayList<WarehouseModel>();
		mParser = new WarehouseParser();
		mAdapter = new WarehouseAdapter(this, mModels);
		mListView.setAdapter(mAdapter);
		
		setGameInfo();
		requestData();
		
	}
	
	private void initUI(){
		mListView = (ListView) findViewById(R.id.warehouse_listview);
		
		mGameIcon = (ImageView) findViewById(R.id.game_icon);
		mGameInfo = (TextView) findViewById(R.id.game_name);

	}
	
	private void setGameInfo(){
		String strUrl = null;
		String strGameInfo = "";
		GameInfo info = GameInfo.getGameInfoFromPreference();
		if(info != null) {
			strUrl = info.getBizImg();
			strGameInfo = info.getDescription();
		}
		
		if(TextUtils.isEmpty(strUrl) && TextUtils.isEmpty(strGameInfo)) {
			findViewById(R.id.warehouse_gameinfo).setVisibility(View.GONE);
		}
		
		if(TextUtils.isEmpty(strUrl)) {
			mGameIcon.setVisibility(View.GONE);
		}else{
			final Bitmap data = mImageLoader.get(strUrl);
			if (data != null) {
				mGameIcon.setImageBitmap(data);
				return;
			}
			
			mGameIcon.setImageBitmap(mImageLoader.getLoadingBitmap(this));
			mImageLoader.get(strUrl, new ImageLoadListener() {
				
				@Override
				public void onLoaded(Bitmap aBitmap, String strUrl) {
					mGameIcon.setImageBitmap(aBitmap);
				}
				
				@Override
				public void onError(String strUrl) {
					
				}
			});
		}
		
		mGameInfo.setText(strGameInfo);
		
	}
	
	private void requestData() {
		showLoadingLayer();
		Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/queryWarehouse.php");
		
		if(ajax == null) {
			return;
		}
		
		ajax.setData("uin", ILogin.getLoginUin());
		
		ajax.setOnErrorListener(this);
		ajax.setOnSuccessListener(new OnSuccessListener<ArrayList<WarehouseModel>>() {
			public void onSuccess(ArrayList<WarehouseModel> models, Response response) {
				closeLoadingLayer();
				
				if(!mParser.isSuccess()) {
					if(null != mModels) {
						mModels.clear();
					}
					
					mAdapter.notifyDataSetChanged();
					return;
				}
				
				if(null == mModels) {
					mModels = new ArrayList<WarehouseModel>();
				}
				
				mModels.addAll(models);
				mAdapter.notifyDataSetChanged();
				
			};
		});
		
		ajax.setParser(mParser);
		
		ajax.send();
		addAjax(ajax);
	}
	
	@Override
	protected void onDestroy() {
		mParser = null;
		mAdapter = null;
		mListView = null;
		
		if(null != mModels) {
			mModels.clear();
			mModels = null;
		}
		
		super.onDestroy();
	}
	
}
