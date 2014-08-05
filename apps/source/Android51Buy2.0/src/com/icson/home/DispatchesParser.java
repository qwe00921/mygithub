package com.icson.home;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.lib.inc.DispatchFactory.DispatchItem;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class DispatchesParser extends Parser<byte[], ArrayList<DispatchItem>> 
{
	private String mStr;
	@Override
	public ArrayList<DispatchItem> parse(byte[] bytes, String charset) throws Exception {
		JSONParser parser = new JSONParser();
		parser.parse(bytes, charset);
		
		return parse(parser.getString());
	}

	public ArrayList<DispatchItem> parse(String str) throws Exception {
		clean();

		mStr = str;
		JSONObject json = new JSONObject(str);

		if (json.getInt("errno") != 0) {
			mErrMsg = json.optString("data", Config.NORMAL_ERROR);
			return null;
		}

		ArrayList<DispatchItem> results = new ArrayList<DispatchItem>();
		if (ToolUtil.isEmptyList(json, "data")) {
			return results;
		}

		JSONObject data = json.getJSONObject("data");
		JSONArray aDispatches = data.getJSONArray("dispatches");
		if( null != aDispatches )
		{
			final int nLength = aDispatches.length();
			String strRegEx = "省|市|自治区";
			Pattern pPattern = Pattern.compile(strRegEx);
			for( int nIdx = 0; nIdx < nLength; nIdx++ )
			{
				JSONObject pObject = aDispatches.getJSONObject(nIdx);
				final int nId = pObject.optInt("id");
				String strName = pObject.optString("name");
				Matcher pMatcher = pPattern.matcher(strName);
				strName = pMatcher.replaceFirst("");
				if( strName.length() >= 4 )
				{
					strName = strName.substring(0, 2);
				}
				
				final int nSiteId = pObject.optInt("siteId");
				final int district = pObject.optInt("district");
				final int nProvinceId = pObject.optInt("provinceid");
				final int nCityId = pObject.optInt("cityid");
				DispatchItem pItem = new DispatchItem(nId, strName, nSiteId,district, nProvinceId, nCityId);
				results.add(pItem);
			}
		}

		mIsSuccess = true;
		return results;
	}
	
	public String getString() {
		return mStr;
	}
}

