package com.tencent.djcity.more;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.ui.RadioDialog.OnRadioSelectListener;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.preference.Preference;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.Utils;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class SelectDistrictActivity extends BaseActivity {
	public final static String RESPONSE_SELECT_CITY 	= "city_name";
	public final static String SOURCE_SELECT_CITY 	= "from";
	public final static String SELECT_CITY_FROM_HOME 	= "home";
	public final static int REQUEST_SELECT_CITY 		= 1;
	
	public static final String KEY_AREA_NAME = "area_name";
	public static final String KEY_ROLE_NAME = "role_name";
	public static final String KEY_SERVER_NAME = "server_name";
	public final static String KEY_BIZ_CODE = "biz_code";
	public final static String KEY_BIZ_NAME = "biz_name";
	public final static String KEY_BIZ_IMG = "biz_img";
	public final static String KEY_ROLE_FLAG = "role_flag";
	
	private List<AreaModel> mAreaModelList = new ArrayList<AreaModel>();
	private List<RoleModel> mRoleModelList = new ArrayList<RoleModel>();
	
	private String mBizCode;
	private String mBizName;
	private String mBizImg;
	private int roleFlag;
	private String mAreaName;
	private String mServerName;
	private String mRoleName;
	
	private TextView mTextViewSelectArea;
	private TextView mTextViewSelectServer;
	private TextView mTextViewSelectRole;
	
//	private LinearLayout mAreaContainer;
	private LinearLayout mServerContainer;
	private LinearLayout mRoleContainer;
	
	private Button mConfirmButton;
	private TextView mGuangButton;
	private String mStrSelectArea;
	private String mStrSelectRole;
	private String mStrSelectServer;
	private int mLevel;
	
	public boolean mIsPickMode; //是否保存到本地，还是只是抓取数据
	public static final String KEY_IS_PICK_MODE = "is_pick_mode";
	
	/* (non-Javadoc)
	 * @see com.tencent.djcity.util.activity.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		
		setContentView(R.layout.activity_select_district);
		loadNavBar(R.id.navigation_bar);
		
		mStrSelectArea = getResources().getString(R.string.select_area);
		mStrSelectServer = getResources().getString(R.string.select_server);
		mStrSelectRole = getResources().getString(R.string.select_role);
		mNavBar.setRightVisibility(View.GONE);
		Intent mIntent = getIntent();
		mBizName = mIntent.getStringExtra(KEY_BIZ_NAME);
		mBizCode = mIntent.getStringExtra(KEY_BIZ_CODE);
		roleFlag = mIntent.getIntExtra(KEY_ROLE_FLAG, 0);
		mAreaName = mIntent.getStringExtra(KEY_AREA_NAME);
		mRoleName = mIntent.getStringExtra(KEY_ROLE_NAME);
		mBizImg = mIntent.getStringExtra(KEY_BIZ_IMG);
		mServerName = mIntent.getStringExtra(KEY_SERVER_NAME);
		mIsPickMode = mIntent.getBooleanExtra(KEY_IS_PICK_MODE, false);
		
//		mAreaContainer = (LinearLayout) findViewById(R.id.select_area_container);
		mServerContainer = (LinearLayout) findViewById(R.id.select_server_container);
		mRoleContainer = (LinearLayout) findViewById(R.id.select_role_container);
		
		mTextViewSelectArea = (TextView) findViewById(R.id.select_area);
		mTextViewSelectArea.setOnClickListener(mOnClickListener);
		if(mAreaName != null && !"".equals(mAreaName)) {
			mTextViewSelectArea.setText(mAreaName);
		}
		mTextViewSelectServer = (TextView) findViewById(R.id.select_server);
		mTextViewSelectServer.setOnClickListener(mOnClickListener);
		if(mServerName != null && !"".equals(mServerName)) {
			mTextViewSelectServer.setText(mServerName);
		}
		
		mTextViewSelectRole = (TextView) findViewById(R.id.select_role);
		mTextViewSelectRole.setOnClickListener(mOnClickListener);
		if(mRoleName != null && !"".equals(mRoleName)) {
			mTextViewSelectRole.setText(mRoleName);
		}
		
		mConfirmButton = (Button) findViewById(R.id.btn_submit);
		mConfirmButton.setOnClickListener(mOnClickListener);
		
		mGuangButton = (TextView) findViewById(R.id.btn_guangguang);
		mGuangButton.setOnClickListener(mOnClickListener);
		
		if(mIsPickMode) { 
			mGuangButton.setVisibility(View.GONE);
		}
		
		mNavBar.setText(mBizName);
		
		if(roleFlag == 0) { //Role flag为0时不显示角色区域
			mRoleContainer.setVisibility(View.GONE);
		}
		
		requestAreaData();
	}
	
	
	private int mCurrentAreaPos = -1;
	private int mCurrentServerPos = -1;
	private int mCurrentRolePos = -1;
	private OnClickListener mOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			switch(v.getId()) {
				case R.id.btn_guangguang :{
					onSelected(true);
					break;
				}
				case R.id.select_area : {
					
					String options[] = new String[mAreaModelList.size()];
					
					for(int i = 0; i < mAreaModelList.size(); i++) {
						AreaModel model = mAreaModelList.get(i);
						options[i] = model.getAreaName();
					}
					
					UiUtils.showListDialog(SelectDistrictActivity.this, options, mCurrentAreaPos, new OnRadioSelectListener() {
						
						@Override
						public void onRadioItemClick(int which) {
							if(mCurrentAreaPos == which) {
								return;
							}
							AreaModel model = mAreaModelList.get(which);
							String selectedName = model.getAreaName();
							mTextViewSelectArea.setText(selectedName);
							mTextViewSelectServer.setText(mStrSelectServer);
							mTextViewSelectRole.setText(mStrSelectRole);
							mCurrentServerPos = -1;
							mCurrentRolePos = -1;
							mCurrentAreaPos = which;
							
							if(model.getServerModelList().size() > 0) {
								mConfirmButton.setEnabled(false);
							} else {
								mConfirmButton.setEnabled(true);
							}
						}
					});
					break;
				}
				case R.id.select_role : {
					
					if(mRoleModelList.size() <= 0) {
						return;
					}
					String options[] = new String[mRoleModelList.size()];
					
					for(int i = 0; i < mRoleModelList.size(); i++) {
						RoleModel model = mRoleModelList.get(i);
						options[i] = model.getName();
					}
					
					UiUtils.showListDialog(SelectDistrictActivity.this, options, mCurrentRolePos, new OnRadioSelectListener() {
						
						@Override
						public void onRadioItemClick(int which) {
							if(mCurrentRolePos == which) {
								return;
							}
							String selectedName = mRoleModelList.get(which).getName();
							mTextViewSelectRole.setText(selectedName);
							mCurrentRolePos = which;
						}
					});
					mConfirmButton.setEnabled(true);
					break;
				}
				case R.id.select_server : {
					if(mCurrentAreaPos < 0 || mCurrentAreaPos >= mAreaModelList.size()) {
						return;
					}
					AreaModel model = mAreaModelList.get(mCurrentAreaPos);
					final List<ServerModel> serverList = model.getServerModelList();
					
					String options[] = new String[serverList.size()];
					
					for(int i = 0; i < serverList.size(); i++) {
						ServerModel server = serverList.get(i);
						options[i] = server.getServerName();
					}
					
					
					UiUtils.showListDialog(SelectDistrictActivity.this, options, mCurrentServerPos, new OnRadioSelectListener() {
						
						@Override
						public void onRadioItemClick(int which) {
							if(mCurrentServerPos == which) {
								return;
							}
							ServerModel server = serverList.get(which);
							mTextViewSelectServer.setText(server.getServerName());
							mCurrentServerPos = which;
							mTextViewSelectRole.setText(mStrSelectRole);
							mCurrentRolePos = -1;
							
							if(roleFlag == 0) {
								//不需要查询角色
								mConfirmButton.setEnabled(true);
							} else {
								requestRoleData();
							}
						}
					});
					break;
				}
				case R.id.btn_submit: {
					onSelected(false);
					break;
				}
			}
		}
	};

	private void saveToIntent(GameInfo info) {
		Intent intent = getIntent();
		
		intent.putExtra(GameInfo.KEY_ROLE_FLAG, info.getRoleFlag());
		intent.putExtra(GameInfo.KEY_AREA_ID, info.getAreaId());
		intent.putExtra(GameInfo.KEY_AREA_NAME, info.getAreaName());
		intent.putExtra(GameInfo.KEY_BIZ_CODE, info.getBizCode());
		intent.putExtra(GameInfo.KEY_BIZ_NAME, info.getBizName());
		intent.putExtra(GameInfo.KEY_ROLE_ID, info.getRoleId());
		intent.putExtra(GameInfo.KEY_ROLE_LEVEL, info.getRoleLevel());
		intent.putExtra(GameInfo.KEY_ROLE_NAME, info.getRoleName());
		intent.putExtra(GameInfo.KEY_SERVER_ID, info.getServerId());
		intent.putExtra(GameInfo.KEY_SERVER_NAME, info.getServerName());
		intent.putExtra(GameInfo.KEY_AREA_LEVEL, info.getAreaLevel());
		
	}
	
	private void saveToPreference(GameInfo info) {
		Preference pref = Preference.getInstance();
		pref.setGameInfo(info);
	}
	private void onSelected(boolean isGuang) {
		GameInfo info = new GameInfo();
		info.setBizCode(mBizCode);
		info.setBizName(mBizName);
		info.setRoleFlag(roleFlag);
		info.setAreaLevel(mLevel);
		info.setBizImg(mBizImg);
		
		int areaId = 0;
		String areaName = null;
		int serverId = 0;
		String serverName = null;
		String roleId = null;
		int roleLevel = 0;
		String roleName = null;
		if(!isGuang) { //如果是逛逛，不保存所选择的区服角色
			
			AreaModel area = (AreaModel) Utils.getObjectSafely(mAreaModelList, mCurrentAreaPos);
			if(area != null) {
				areaId = area.getAreaId();
				areaName = area.getAreaName();
				
				List<ServerModel> serverList = area.getServerModelList();
				ServerModel server = (ServerModel) Utils.getObjectSafely(serverList, mCurrentServerPos);
				if(server != null) {
					serverId = server.getServerId();
					serverName = server.getServerName();
				}
				
			}
			RoleModel role = (RoleModel) Utils.getObjectSafely(mRoleModelList, mCurrentRolePos);
			if(role != null) {
				roleId = role.getId();
				roleLevel = role.getLevel();
				roleName = role.getName();
			}
		}
		info.setAreaId(areaId);
		info.setAreaName(areaName);
		info.setServerId(serverId);
		info.setServerName(serverName);
		info.setRoleId(roleId);
		info.setRoleLevel(roleLevel);
		info.setRoleName(roleName);
		
		if(!mIsPickMode) {
			saveToPreference(info);
		}
		saveToIntent(info);
		setResult(RESULT_OK, getIntent());
		finish();
	}
	
	private void requestRoleData() {

		mRoleModelList.clear();
		Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/cgi-bin/daoju/v3/test_app/query_role.cgi?");
		
		ajax.setData("_appname", mBizCode);
		
		int iZone = 0;
		if(mCurrentAreaPos != -1) {
			AreaModel district = mAreaModelList.get(mCurrentAreaPos);
			if(mCurrentServerPos != -1) {
				ServerModel server = district.getServerModelList().get(mCurrentServerPos);
				iZone = server.getServerId();
			} else {
				iZone = district.getAreaId();
			}
		}
		ajax.setData("iZone", iZone);
		ajax.setData("_appcode", "djapp");
		ajax.setData("uin", ILogin.getLoginUin());
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				if(0 != v.optInt("result", 0)) {
					String msg = v.optString("msg");
					UiUtils.showDialog(SelectDistrictActivity.this, "提示", msg, getResources().getString(R.string.btn_reselect));
					mConfirmButton.setEnabled(false);
				} else {
					JSONArray data = v.optJSONArray("data");
					if(data != null) {
						for(int i = 0; i < data.length(); i++) {
							JSONObject object = data.optJSONObject(i);
							RoleModel role = new RoleModel();
							try {
								role.parse(object);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							mRoleModelList.add(role);
						}
					}

					
					if(mRoleModelList.size() == 0) {
						UiUtils.showDialog(SelectDistrictActivity.this, "提示", "您在该区服没有角色，请选择其它区服", getResources().getString(R.string.btn_reselect));
						mConfirmButton.setEnabled(false);
					} else if(mRoleModelList.size() == 1) {
						String selectedName = mRoleModelList.get(0).getName();
						mTextViewSelectRole.setText(selectedName);
						mCurrentRolePos = 0;
						mConfirmButton.setEnabled(true);
					} else {
						mConfirmButton.setEnabled(true);
					}
					
					restoreRoleData();
				}
			}
		});
		ajax.setOnErrorListener(this);
		ajax.setParser(new JSONParser());
		ajax.send();
	}
	
	
	private void requestAreaData() {
		Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/listArea.php");
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				
				try {
					JSONArray array = v.getJSONArray("data");
					mLevel = v.optInt("level");
					parseGameNames(array);
					
					if(mAreaModelList != null && mAreaModelList.size() > 0) {
						if(mLevel == 1) {
							mServerContainer.setVisibility(View.GONE);
						}
						restoreServerData();
						if(roleFlag == 0) {
							if(mCurrentAreaPos != -1 && mCurrentServerPos != -1) {
								mConfirmButton.setEnabled(true);
							}
						} else {
							if(mCurrentAreaPos != -1 && mCurrentServerPos != -1 && mCurrentRolePos != -1) {
								mConfirmButton.setEnabled(true);
							}
						}
					} else {
					}
					
					if(mServerName != null || mAreaName != null) {
						requestRoleData();
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
		});
		ajax.setOnErrorListener(this);
		ajax.setParser(new JSONParser());
		ajax.send();
	}
	
	private void restoreServerData() {
		if(mAreaName != null) {
			for(int i = 0; i < mAreaModelList.size(); i++) {
				AreaModel area = mAreaModelList.get(i);
				if(mAreaName.equals(area.getAreaName())) {
					mCurrentAreaPos = i;
					if(mServerName != null) {
						List<ServerModel> serverList = mAreaModelList.get(i).getServerModelList();
						if(serverList != null) {
							for(int j = 0; j < serverList.size(); j++) {
								ServerModel server = serverList.get(j);
								if(mServerName.equals(server.getServerName())) {
									mCurrentServerPos = j;
									break;
								}
							}
						}
					}
					break;
				}
			}
		}
	}
	
	private void restoreRoleData() {
		if(mRoleName != null) {
			for(int i = 0; i < mRoleModelList.size(); i++) {
				RoleModel model = mRoleModelList.get(i);
				if(mRoleName.equals(model.getName())) {
					mCurrentRolePos = i;
				}
			}
		}
	}
	
	private void parseGameNames(JSONArray array) throws JSONException {
		List<AreaModel> listForAdapter = mAreaModelList;
		for(int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);
			AreaModel model = new AreaModel();
			model.parse(object);
			listForAdapter.add(model);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
//		mItem = null;
		
//		IShippingArea.clean();
		super.onDestroy();
	}
}
