package com.tencent.djcity.my;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.ui.AppDialog;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.main.MainActivity;
import com.tencent.djcity.more.GameInfo;
import com.tencent.djcity.more.SelectGameActivity;
import com.tencent.djcity.msgcenter.MsgCenterActivity;
import com.tencent.djcity.msgcenter.MsgMgr;
import com.tencent.djcity.msgcenter.MsgMgr.MsgObserver;
import com.tencent.djcity.preference.Preference;
import com.tencent.djcity.setting.SettingActivity;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ImageLoadListener;
import com.tencent.djcity.util.ImageLoader;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.activity.BaseActivity;

public class MyCityActivity extends BaseActivity implements OnClickListener, MsgObserver{
	
	private ImageView mMsgImgView;
	private ImageView mGameIcon;
	private TextView mGameInfo;
	private View mMessageCenter;
	private TextView mSelectGameView;
	private TextView mMyRole;
	private TextView mMyWarehose;
	private TextView mMyOrder;
	private TextView mSettings;
	
	private View mMsgCenter;
	private TextView mMsgNum;
	private Button mLogoutButton;
	
	private ImageLoader mImageLoader;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_my_city);
		render();
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
	
	private void render(){
		mMessageCenter = findViewById(R.id.mycity_message);
		mSelectGameView = (TextView) findViewById(R.id.mycity_select_game);
		mMyRole = (TextView) findViewById(R.id.mycity_role);
		mMyWarehose = (TextView) findViewById(R.id.mycity_warehouse);
		mMyOrder = (TextView) findViewById(R.id.mycity_order);
		mSettings = (TextView) findViewById(R.id.mycity_settings);
		mLogoutButton = (Button) findViewById(R.id.button_logout);
		
		mMsgCenter = this.findViewById(R.id.message);
		mMsgCenter.setOnClickListener(this); 
		
		mMsgNum = (TextView) this.findViewById(R.id.message_num);
		
		mMsgImgView = (ImageView) this.findViewById(R.id.navigationbar_drawable_left);
		mMsgImgView.setOnClickListener(this); 
		
		mMessageCenter.setOnClickListener(this);
		mSelectGameView.setOnClickListener(this);
		mMyRole.setOnClickListener(this);
		mMyWarehose.setOnClickListener(this);
		mMyOrder.setOnClickListener(this);
		mSettings.setOnClickListener(this);
		mLogoutButton.setOnClickListener(this);
		
		mMsgCenter.setOnClickListener(this); 
		
		mGameIcon = (ImageView) findViewById(R.id.game_icon);
		mGameInfo = (TextView) findViewById(R.id.game_name);
		
		mImageLoader = new ImageLoader(this, Config.CHANNEL_PIC_DIR, true);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		setGameInfo();
		
		MsgMgr msgMgr = MsgMgr.getInstance();
		msgMgr.setMsgObserver(this);
		msgMgr.getMsgNum();
	}
	
	@Override
	public void onResult(int num)
	{
		if(num != 0)
		{
			mMsgImgView.setImageResource(R.drawable.ico_mail_new);
			mMsgNum.setText(this.getString(R.string.message_num, num));
			mMsgNum.setVisibility(View.VISIBLE);
		}
		else
		{
			mMsgImgView.setImageResource(R.drawable.ico_mail);
			mMsgNum.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		int rid = v.getId();
		switch(rid) {
		case R.id.mycity_message:
			ToolUtil.checkLoginOrRedirect(this, MsgCenterActivity.class);
			break;
			
//		case R.id.mycity_selectgame_button:
		case R.id.mycity_select_game:
			ToolUtil.checkLoginOrRedirect(this, SelectGameActivity.class);
			break;
			
		case R.id.mycity_role:
			ToolUtil.checkLoginOrRedirect(this, MyRoleActivity.class);
			break;
		case R.id.mycity_warehouse:
			ToolUtil.checkLoginOrRedirect(this, MyWarehouseActivity.class);
			break;
		case R.id.mycity_order:
			ToolUtil.checkLoginOrRedirect(this, MyOrderListActivity.class);
			break;	
			
		case R.id.mycity_settings:
			Intent aIntent = new Intent(this,SettingActivity.class);
			this.startActivity(aIntent);
			break;
			
		case R.id.button_logout:
			UiUtils.showDialog(this, R.string.caption_hint, R.string.message_logout, R.string.btn_ok, R.string.btn_cancel, new AppDialog.OnClickListener() {
				@Override
				public void onDialogClick(int nButtonId) {
					if (nButtonId == AppDialog.BUTTON_POSITIVE) {
						ILogin.clearAccount();
						
						Preference.getInstance().clearGameInfo();
						
//						// Clear QQ account information.
//						ReloginWatcher.getInstance(MoreActivity.this).clearAccountInfo();
						
						//Intent pIntent = new Intent(MyCityActivity.this,LoginActivity.class);
						//MyCityActivity.this.startActivity(pIntent);
						
						MainActivity.logout(MyCityActivity.this);
						//MainActivity.startActivity(MyCityActivity.this, MainActivity.TAB_MY);
					}
				}
			});

			break;
		case R.id.navigationbar_drawable_left:
		case R.id.message:
			Intent pIntent = new Intent(this,MsgCenterActivity.class);
			this.startActivity(pIntent);
			break;
		}
	}

}
