package com.tencent.djcity.more;

import android.app.Activity;
import android.content.Intent;

public class SelectHelper {

	public static final int REQUEST_PICK_AREA = 10001;
	public static void changeArea(Activity activity) {
		GameInfo info = GameInfo.getGameInfoFromPreference();
		changeArea(activity, info, false);
	}
	
	public static void changeArea(Activity activity, GameInfo info, boolean isPickMode) {
		String bizCode = null;
		if(info != null) {
			bizCode = info.getBizCode();
		}
		Intent intent = new Intent();
		if(bizCode == null) {
			intent.setClass(activity, SelectGameActivity.class);
		} else {
			
			intent.putExtra(SelectDistrictActivity.KEY_BIZ_CODE, info.getBizCode());
			intent.putExtra(SelectDistrictActivity.KEY_BIZ_NAME, info.getBizName());
			intent.putExtra(SelectDistrictActivity.KEY_ROLE_FLAG, info.getRoleFlag());
			intent.putExtra(SelectDistrictActivity.KEY_AREA_NAME, info.getAreaName());
			intent.putExtra(SelectDistrictActivity.KEY_SERVER_NAME, info.getServerName());
			intent.putExtra(SelectDistrictActivity.KEY_ROLE_NAME, info.getRoleName());
			
			intent.putExtra(SelectDistrictActivity.KEY_IS_PICK_MODE, isPickMode);
			intent.setClass(activity, SelectDistrictActivity.class);
		}
		activity.startActivityForResult(intent, REQUEST_PICK_AREA);
	}
}
