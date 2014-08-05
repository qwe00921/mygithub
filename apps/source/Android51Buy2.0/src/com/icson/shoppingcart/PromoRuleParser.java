/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: PromoRuleParser.java
 * 
 * Description: 
 * Author: qingliang (qingliang@tencent.com)
 * Created: Dec 20, 2013
 * 
 */

package com.icson.shoppingcart;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.lib.ILogin;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class PromoRuleParser extends Parser<byte[], ArrayList<PromoRuleModel>> {
	private JSONParser mPaser;
	private ArrayList<PromoRuleModel> mModels;

	public PromoRuleParser() {
		mPaser = new JSONParser();
	}

	@Override
	public ArrayList<PromoRuleModel> parse(byte[] bytes, String charset) throws Exception {
		clean();
		mIsSuccess = false;
//		String str = "{\"errno\":0, \"data\":{\"promoRule\":[{\"rule_id\":10837,\"benefit_times\":1,\"selectNum\":0,\"rule_type\":1,\"desc\":\"买手机满500元返500元优惠券（共4张，可用于购买易迅自营名表礼品）\",\"condition\":50000,\"productList\":\"\",\"rule_sum_value\":419900,\"name\":\"满500.00元立送优惠券\",\"benefit_type\":2,\"benefits\":10064,\"url\":\"\"},{\"rule_id\":15867,\"benefit_times\":1,\"selectNum\":1,\"rule_type\":1,\"desc\":\"百货买就送蓝月亮旅行装80g（特价商品不参加）\",\"condition\":100,\"productList\":[{\"selected\":2,\"price\":500,\"productCharId\":\"101-001-03714\",\"commodityId\":1447174,\"stockNum\":1,\"promotion_price\":0,\"name\":\"蓝月亮 风清白兰手洗专用洗衣液（旅行装）80g\"},{\"selected\":1,\"price\":500,\"productCharId\":\"101-000-15127\",\"commodityId\":1304846,\"stockNum\":0,\"promotion_price\":0,\"name\":\"蓝月亮 风清白兰手洗专用洗衣液（旅行装）80g\"}],\"rule_sum_value\":2990,\"name\":\"满1.00元立送商品\",\"benefit_type\":6,\"benefits\":0,\"url\":\" \"},{\"rule_id\":10847,\"benefit_times\":1,\"selectNum\":1,\"rule_type\":1,\"desc\":\"购物赠圣诞可口可乐（限易迅快递配送地区及部分商品）\",\"condition\":100,\"productList\":[{\"selected\":0,\"price\":500,\"productCharId\":\"101-000-84859\",\"commodityId\":1414426,\"stockNum\":1,\"promotion_price\":0,\"name\":\"限量版可口可乐圣诞礼花瓶（12.23至1.1下单满1元赠品）\"},{\"selected\":1,\"price\":500,\"productCharId\":\"101-001-04922\",\"commodityId\":1453469,\"stockNum\":0,\"promotion_price\":0,\"name\":\"限量版可口可乐圣诞礼花瓶（12.23至1.1下单满1元赠品）\"},{\"selected\":1,\"price\":500,\"productCharId\":\"101-001-04923\",\"commodityId\":1453471,\"stockNum\":0,\"promotion_price\":0,\"name\":\"限量版可口可乐圣诞礼花瓶（12.23至1.1下单满1元赠品）\"}],\"rule_sum_value\":422890,\"name\":\"满1.00元立送商品\",\"benefit_type\":6,\"benefits\":0,\"url\":\"\"}]}}";
		JSONObject v = mPaser.parse(bytes, charset);
//		v = new JSONObject(str);
		final int errno = v.optInt("errno", -1);
		// ?:??: W/?(?):
		// {"errCode":0,"errMsg":"","data":{"items":[{"uid":"30563557","product_id":6731,"buy_count":1,"main_product_id":6731,"type":0,"wh_id":"1","price_id":0,"OTag":"19901002013-19903403005-19908110001-0.2","package_id":0,"unique_id":"6731","matchNum":1,"restricted_trans_type":0,"name":"TP-LINK 普联 TL-SF1008+ 8口桌面式交换机(塑壳迷你型)","size":"","color":"aaaa","product_char_id":"06-154-032","pic_num":11,"weight":530,"flag":8266,"c3_ids":"88","market_price":8900,"psystock":1,"canAddToWireLessCart":true,"rushing_buy":false,"canVAT":true,"canUseCoupon":false,"cash_back":0,"price":6200,"isVirtual":0,"lowest_num":-1,"delay_days":0,"point":100,"num_limit":0,"stock_desc":"有货，可当日出库","stock_status":0,"vValue":0,"gift":[]}],"suiteInfo":[],"promoRule":[],"coupons":[],"conflict":[]}}

		if (errno == Config.NOT_LOGIN) {
			mErrMsg = v.optString("data", "您已退出登录");;
			ILogin.clearAccount();
			return null;
		}

		if (errno != 0) {
			mErrMsg = v.optString("data", "服务器端错误, 请稍候再试");
			return null;
		}
		
		JSONObject data = v.getJSONObject("data");
		if (!ToolUtil.isEmptyList(data, "promoRule")) {
			JSONArray arrs = data.getJSONArray("promoRule");

			ArrayList<PromoRuleModel> models = new ArrayList<PromoRuleModel>();

			for (int i = 0, len = arrs.length(); i < len; i++) {
				PromoRuleModel model = new PromoRuleModel();
				model.parse(arrs.getJSONObject(i));
				models.add(model);
			}
			
			mModels = models;
		}
		
		mIsSuccess = true;
		return mModels;
	}

}
