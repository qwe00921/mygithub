/**
 * 
 */
package com.icson.amap.mapUtils;

import java.text.DecimalFormat;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
//import com.amap.api.search.core.LatLonPoint;

/*******
 * @project AMapV2Demos
 * @email chuan.yu@autonavi.com
 * @time 2013-3-26下午7:03:47
 *******/
public class AMapUtil {
	/**
	 * 对AMap对象判断是否为null
	 */
	public static boolean checkReady(Context context, AMap aMap) {
		if (aMap == null) {
			//ToastUtil.show(context, R.string.map_not_ready);
			return false;
		}
		return true;
	}

	public static Spanned stringToSpan(String src) {
		return src == null ? null : Html.fromHtml(src.replace("\n", "<br />"));
	}

	public static String colorFont(String src, String color) {
		StringBuffer strBuf = new StringBuffer();

		strBuf.append("<font color=").append(color).append(">").append(src)
				.append("</font>");
		return strBuf.toString();
	}

	public static String makeHtmlNewLine() {
		return "<br />";
	}

	public static String makeHtmlSpace(int number) {
		final String space = "&nbsp;";
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < number; i++) {
			result.append(space);
		}
		return result.toString();
	}

	public static String getCustomLength(int lenMeter){
		if (lenMeter > 10000) // 10 km
		{
			int dis = lenMeter / 1000;
			return dis + AMapChString.Kilometer;
		}
		
		if (lenMeter > com.icson.amap.CargoMapActivity.HINT_DISTANCE) 
		{
			float dis = (float) lenMeter / 1000;
			DecimalFormat fnum = new DecimalFormat("##0.0");
			String dstr = fnum.format(dis);
			return dstr + AMapChString.Kilometer;
		}
		
		int dis = lenMeter;
		/*
		int dis = lenMeter / 10 * 10;
		if (dis == 0) {
			dis = 10;
		}
*/
		return dis + AMapChString.Meter;
		
	}
	public static String getFriendlyLength(int lenMeter) {
		if (lenMeter > 10000) // 10 km
		{
			int dis = lenMeter / 1000;
			return dis + AMapChString.Kilometer;
		}

		if (lenMeter > 1000) {
			float dis = (float) lenMeter / 1000;
			DecimalFormat fnum = new DecimalFormat("##0.0");
			String dstr = fnum.format(dis);
			return dstr + AMapChString.Kilometer;
		}

		if (lenMeter > 100) {
			int dis = lenMeter / 50 * 50;
			return dis + AMapChString.Meter;
		}

		int dis = lenMeter / 10 * 10;
		if (dis == 0) {
			dis = 10;
		}

		return dis + AMapChString.Meter;
	}
	public static boolean IsEmptyOrNullString(String s) {
		return (s == null) || (s.trim().length() == 0);
	}
	
	//public static LatLonPoint convertToLatLonPoint(LatLng latlon){
	//	return new LatLonPoint(latlon.latitude,latlon.longitude);
	//}

	
	public static int getDistance2Poi(final LatLng aLL1, final LatLng aLL2)
	{
		
		final double dValue = 0.0174532925199433D;
	
		final double nLatRadians1 = aLL1.latitude * dValue;
		final double nLatRadians2 = aLL2.latitude * dValue;
	
		final double lngRadians = (aLL1.longitude - aLL2.longitude) * dValue;
		final double latRadians = nLatRadians1 - nLatRadians2;

		final double dRet = 2.0D * Math.asin(Math.sqrt(Math.pow(Math.sin(latRadians / 2.0D), 2.0D) 
													   + Math.cos(nLatRadians1) * Math.cos(nLatRadians2) * 
													     Math.pow(Math.sin(lngRadians / 2.0D),2.0D)
													   )
											);
		
		int nDistance = (int)(dRet * 6378137.0D);
		
		return nDistance;
	}
	
	public static double getDeltaLatWithSameLng(final LatLng aLL1, double aDistance)
	{
		//longitude1 = longitude2
		return aDistance/111000.0D;
	}
	
	public static double getDeltaLngWithSameLat(final LatLng aLL1, double aDistance)
	{
		//latitude1 = latitude2
		return aDistance/111000.0D/Math.cos(aLL1.latitude*Math.PI/180);
	}
		
	
	public static final String HtmlBlack = "#000000";
	public static final String HtmlGray = "#808080";
}
