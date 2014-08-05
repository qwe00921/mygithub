package com.tencent.djcity.home;

import org.json.JSONObject;

import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.ajax.Parser;

public class HomeParser extends Parser<byte[], HomeConfig> {
	@Override
	public HomeConfig parse(byte[] bytes, String charset) throws Exception {
		return parse(new String(bytes, 0, bytes.length, charset));
	}

	public HomeConfig parse(String str) throws Exception {
		clean();

		mStr = str;
		JSONObject json = new JSONObject(str);

//		if (json.getInt("errno") != 0) {
//			mErrMsg = json.optString("data", Config.NORMAL_ERROR);
//			return null;
//		}
		
		if (ToolUtil.isEmptyList(json, "data")) {
			return null;
		}

		JSONObject data = json.getJSONObject("data");
		
		HomeConfig pConfig = new HomeConfig();
		pConfig.parseConfig(data);
		
		
//		HomeConfig pConfig = new HomeConfig();
//		pConfig.parseConfig(json);
		
		mIsSuccess = true;
		
		return pConfig;
	}
	
	public String getString() {
		return mStr;
	}
	
	private String mStr;
}
