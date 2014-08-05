package com.icson.event;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class TimeBuyParser extends Parser<byte[], TimeBuyModel>
{
	@Override
	public TimeBuyModel parse(byte[] aInput, String strCharset) throws Exception
	{
		
		JSONParser parser = new JSONParser();
		JSONObject json = parser.parse(aInput, strCharset);
		
		return this.parse(json);
	}
	
	/**
	 * parse
	 * @param aObject
	 * @return
	 * @throws Exception
	 */
	private TimeBuyModel parse(JSONObject aObject) throws Exception
	{
		if( null == aObject )
			return null;
		
		clean();
		
		int errno = aObject.getInt("errno");
		if (errno != 0) {
			mErrCode = errno;
			mErrMsg = aObject.optString("data", Config.NORMAL_ERROR);
			throw new Exception("errno not is no 0.");
		}

		TimeBuyModel pResult = new TimeBuyModel();

		if (ToolUtil.isEmptyList(aObject, "data")) {
			return pResult;
		}

		JSONObject data = aObject.getJSONObject("data");

		pResult.setAdvertiseUrl(data.optString("advertise_url", "").trim());
		pResult.setListUrl(data.optString("list_url", "").trim());
		pResult.setBackground(data.optInt("background", 0xffe4f2ff));
		pResult.setType(data.optInt("type"));
		pResult.setPriceColor(data.optInt("price_color", 0xff8c8c8c));
		pResult.setStatus(data.optString("status"));
		
		pResult.setCurrentTimetag(data.optLong("now"));
		pResult.setStartTimetag(data.optLong("begin"));
		pResult.setFinishTimetag(data.optLong("end"));
		
		// Products
		ArrayList<TimeBuyEntity> aProducts = new ArrayList<TimeBuyEntity>();
		
		// Get product list.
		if (!ToolUtil.isEmptyList(data, "products")) {
			JSONArray aArray = data.getJSONArray("products");
			final int nCount = (null != aArray ? aArray.length() : 0);
			for( int nPos = 0; nPos < nCount; nPos++ )
			{
				TimeBuyEntity pEntity = new TimeBuyEntity();
				pEntity.parse(aArray.getJSONObject(nPos));
				aProducts.add(pEntity);
			}
		}
		// Copy the product model entities.
		pResult.setProducts(aProducts);
		
		// Parse data for page configuration.
		if( !ToolUtil.isEmptyList(data, "page") )
		{
			JSONObject pPage = data.getJSONObject("page");
			
			// 1. Page number.
			pResult.setPageNum(pPage.optInt("page_current"));
			
			// 2. Page size.
			pResult.setPageSize(pPage.optInt("page_size"));
			
			// 3. Page count.
			pResult.setPageCount(pPage.optInt("page_count"));
		}
		
		mIsSuccess = true;

		return pResult;
	}
}
