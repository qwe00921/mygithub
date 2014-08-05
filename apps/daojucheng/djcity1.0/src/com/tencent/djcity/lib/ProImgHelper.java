package com.tencent.djcity.lib;

import java.util.ArrayList;

import com.tencent.djcity.util.IcsonApplication;
import com.tencent.djcity.util.ToolUtil;

import android.text.TextUtils;

public class ProImgHelper {
private static final int[] PIC_WIDTH = {30, 60, 80, 120, 160, 200, 300, 400, 600, 800};
	
	/**
	 * http://img0.wgimg.com/qqbuy/554807484/item-0000000000000000000000652111B0BC.3.jpg/80?123445
	 * @param baseProImgUrl å¦?ä¸????ç¤ºç????¾ç?????å§?url
	 * @param size	??¾ç????????ç´?
	 * @param index	??¾ç????¨å?¾ç??ç´¢å????°ç??ä¸????ä½?ç½?
	 * @return	è¿??????¾ç?????å§?url??¨å????°ç??ä¿???¹ä??????????°ç??url
	 */
	public static String getPicUrl(String baseProImgUrl, int size, int index ) {
		if(TextUtils.isEmpty(baseProImgUrl))
			return "";
		
		//http://img0.wgimg.com/qqbuy/554807484/item-0000000000000000000000652111B0BC.3.jpg/80?123445
		int end = baseProImgUrl.lastIndexOf("?");
		String str_timemark = ""; 
		if(end > 0 )
		{
			str_timemark = baseProImgUrl.substring(end);  // /?123445
		}
		
		//http://img0.wgimg.com/qqbuy/554807484/item-0000000000000000000000652111B0BC.3.jpg/
		end = baseProImgUrl.lastIndexOf("/");
		String innerUrl = baseProImgUrl.substring(0, end);
		
		end = innerUrl.lastIndexOf("/");
		//http://img0.wgimg.com/qqbuy/554807484/
		String head = innerUrl.substring(0, end);
		//item-0000000000000000000000652111B0BC.3.jpg
		String mid =  innerUrl.substring(end);
	
		String items [] = mid.split("\\.");
		if(null == items || items.length != 3)
			return "";
		
		int flag = 0;
		for (int len = PIC_WIDTH.length, i = len - 1; i > -1; i--) {
			if (size >= PIC_WIDTH[i]) {
				flag = i;
				break;
			}
		}
		if(flag >= PIC_WIDTH.length )
			flag = PIC_WIDTH.length -1;

		int resolution =  PIC_WIDTH[flag] == 800 ? 0 : PIC_WIDTH[flag]; 

		String result =  head + items[0] + "." + index + "."+ items[2] + "/" + resolution + str_timemark;
		return result;
	}

	public static String getAdapterPicUrl(String baseProImgUrl, int dip, int index) {
		float px = ToolUtil.dip2px(IcsonApplication.app, dip);

		int width = PIC_WIDTH[0];
		for (int len = PIC_WIDTH.length, i = len - 1; i > -1; i--) {
			if (px >= PIC_WIDTH[i]) {
				width = PIC_WIDTH[i];
				break;
			}
		}

		return getPicUrl(baseProImgUrl, width, index);
	}
}
