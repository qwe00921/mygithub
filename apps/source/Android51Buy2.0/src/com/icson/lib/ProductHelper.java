package com.icson.lib;

import com.icson.util.IcsonApplication;
import com.icson.util.ToolUtil;

public class ProductHelper {

	private static final int[] PIC_WIDTH = { 50 /*38*/ ,80/*60*/,  120/*90*/, 160/*120*/,200/*150*/,300/*225*/,640/*480*/ };

	private static final String[] PIC_LABEL = { "ss",  "small",    "middle",  "pic160", "pic200",   "mm", "mpic" };

	public static String getPicUrl(String productCharId, int size, int index) {
		/*去掉productCharId的10位限制
		 * if (productCharId.length() > 10) {
			productCharId = productCharId.substring(0, 10);
		}*/
		//如果是二手商品，12-324-435R2 需要去掉R后的字符
		int R_index = productCharId.indexOf("R");
		if(R_index > 0)
			productCharId = productCharId.substring(0, R_index);

		int flag = 0;
		for (int len = PIC_WIDTH.length, i = len - 1; i > -1; i--) {
			if (size >= PIC_WIDTH[i]) {
				flag = i;
				break;
			}
		}
		if(flag >= PIC_WIDTH.length )
			flag = PIC_WIDTH.length -1;

		return "http://img2.icson.com/product/" + PIC_LABEL[flag] + "/" + productCharId.replaceAll("^(\\d+)-(\\d+).*$", "$1/$2") + "/" +  productCharId + (index == 0 ? "" : ("-" + (index < 10 ? ("0" + String.valueOf(index)) : index))) + ".jpg";
	}

	public static String getPicUrl(String productCharId, int size) {
		return getPicUrl(productCharId, size, 0);
	}

	public static String getAdapterPicUrl(String productCharId, int dip, int index) {
		float px = ToolUtil.dip2px(IcsonApplication.app, dip);

		int width = PIC_WIDTH[0];
		for (int len = PIC_WIDTH.length, i = len - 1; i > -1; i--) {
			if (px >= PIC_WIDTH[i]) {
				width = PIC_WIDTH[i];
				break;
			}
		}

		return getPicUrl(productCharId, width, index);
	}

	public static String getAdapterPicUrl(String productCharId, int dip) {
		return getAdapterPicUrl(productCharId, dip, 0 );
	}
	
	
}
