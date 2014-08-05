package com.icson.shoppingcart;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.home.ProvinceModel;
import com.icson.lib.ILogin;
import com.icson.lib.model.ShoppingCartProductModel;
import com.icson.util.Config;
import com.icson.util.ToolUtil;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.Parser;

public class ShoppingCartListParser extends
		Parser<byte[], ShoppingCartListModel> {
	public ShoppingCartListParser() {
		new JSONParser();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ShoppingCartListModel parse(byte[] bytes, String charset)
			throws Exception {
		clean();

		JSONParser parser = new JSONParser();
		final JSONObject v = parser.parse(bytes, charset);
		final int errno = v.optInt("errno", -1);
		// ?:??: W/?(?):
		// {"errCode":0,"errMsg":"","data":{"items":[{"uid":"30563557","product_id":6731,"buy_count":1,"main_product_id":6731,"type":0,"wh_id":"1","price_id":0,"OTag":"19901002013-19903403005-19908110001-0.2","package_id":0,"unique_id":"6731","matchNum":1,"restricted_trans_type":0,"name":"TP-LINK 普联 TL-SF1008+ 8口桌面式交换机(塑壳迷你型)","size":"","color":"aaaa","product_char_id":"06-154-032","pic_num":11,"weight":530,"flag":8266,"c3_ids":"88","market_price":8900,"psystock":1,"canAddToWireLessCart":true,"rushing_buy":false,"canVAT":true,"canUseCoupon":false,"cash_back":0,"price":6200,"isVirtual":0,"lowest_num":-1,"delay_days":0,"point":100,"num_limit":0,"stock_desc":"有货，可当日出库","stock_status":0,"vValue":0,"gift":[]}],"suiteInfo":[],"promoRule":[],"coupons":[],"conflict":[]}}

		if (errno == Config.NOT_LOGIN) {
			mErrMsg = v.optString("data", "您已退出登录");
			ILogin.clearAccount();
			return null;
		}

		if (errno != 0) {
			mErrMsg = v.optString("data", "服务器端错误, 请稍候再试");
			return null;
		}

		ShoppingCartListModel result = new ShoppingCartListModel();
		
		//默认的三级分类
		if(!ToolUtil.isEmptyList(v, "fullDistrict")){
			JSONObject districtJson = v.getJSONObject("fullDistrict");
			ProvinceModel model = new ProvinceModel();
			
			Iterator<String> iter= districtJson.keys();
			if(iter.hasNext()) {
				String key = iter.next();
				model.parse(districtJson.getJSONObject(key));
			}
			
			result.setFullDistrictModel(model);
		}
		
		JSONObject data = v.getJSONObject("data");
		if (!ToolUtil.isEmptyList(data, "items")) {
			JSONArray arrs = data.getJSONArray("items");
			ArrayList<ShoppingCartProductModel> models = new ArrayList<ShoppingCartProductModel>();

			for (int i = 0, len = arrs.length(); i < len; i++) {
				ShoppingCartProductModel model = new ShoppingCartProductModel();
				model.parse(arrs.getJSONObject(i));
				models.add(model);
			}
			result.setShoppingCartProductModels(models);
		}
		
		if (!ToolUtil.isEmptyList(data, "errorItems")) {
			JSONArray arrs = data.getJSONArray("errorItems");
			ArrayList<ShoppingCartProductModel> models = new ArrayList<ShoppingCartProductModel>();

			for (int i = 0, len = arrs.length(); i < len; i++) {
				ShoppingCartProductModel model = new ShoppingCartProductModel();
				model.parse(arrs.getJSONObject(i));
				models.add(model);
			}
			result.setShoppingCartProduct_ErrorItemsModels(models);
		}
		
		if (!ToolUtil.isEmptyList(data, "promoRule")) {
			JSONArray arrs = data.getJSONArray("promoRule");

			ArrayList<PromoApplyRuleModel> models = new ArrayList<PromoApplyRuleModel>();
			ArrayList<PromoApplyRuleModel> pFreeGiftsModels = new ArrayList<PromoApplyRuleModel>();
			ArrayList<PromoApplyRuleModel> pLessPriceBuyModels = new ArrayList<PromoApplyRuleModel>();
			
			for (int i = 0, len = arrs.length(); i < len; i++) {
				PromoApplyRuleModel model = new PromoApplyRuleModel();
				model.parse(arrs.getJSONObject(i));
				//过滤掉非 1的rule_type
				if(model.getRuleType()!= PromoRuleModel.RULE_TYPE_CASH_AMT)
					continue ;
				
				int nBenefitType = model.getBenefitType();
				if(nBenefitType == PromoRuleModel.BENEFIT_TYPE_FREEGIFT ){
					pFreeGiftsModels.add(model);
				}else if(nBenefitType == PromoRuleModel.BENEFIT_TYPE_LESSPRICEBUY ){
					pLessPriceBuyModels.add(model);
				}
				//过滤掉非 1 非 2的 BenefitType
				if(nBenefitType != PromoRuleModel.BENEFIT_TYPE_CASH && nBenefitType != PromoRuleModel.BENEFIT_TYPE_COUPON){
					continue ;
				}
				
				if (model.getName() != null && !model.getName().contains("节能补贴")) {
					models.add(model);
				}
			}

			result.setFreeGiftsRulesModels(pFreeGiftsModels);
			result.setLessPriceBuyRulesModels(pLessPriceBuyModels);
			result.setPromoApplyRuleModels(models);
		}
		
		if (!ToolUtil.isEmptyList(data, "rules_buy_more")) {
			JSONArray arrs = data.getJSONArray("rules_buy_more");

			ArrayList<PromoBuyMoreRuleModel> models = new ArrayList<PromoBuyMoreRuleModel>();

			for (int i = 0, len = arrs.length(); i < len; i++) {
				PromoBuyMoreRuleModel model = new PromoBuyMoreRuleModel();
				model.parse(arrs.getJSONObject(i));
				models.add(model);
			}

			result.setPromoBuyMoreRuleModels(models);
		}
		//分单提示
		if (!ToolUtil.isEmptyList(data, "istips")) {
			result.setSpliTips(data.optInt("istips") > 0);
		}
	
		//单品赠券
		if(!ToolUtil.isEmptyList(data, "coupons")) {
			JSONArray pJSONArray = data.getJSONArray("coupons");
			JSONObject pJSONObject;
			ArrayList<ProductCouponGiftModel> models = new ArrayList<ProductCouponGiftModel>();
			int nLength = pJSONArray.length();
			Iterator<String> iter;
			for(int nId = 0; nId < nLength; nId ++ ) {
				pJSONObject = pJSONArray.getJSONObject(nId);
				iter = pJSONObject.keys();
				while(iter.hasNext()) {
					String key = iter.next();
					ProductCouponGiftModel model = new ProductCouponGiftModel();
					model.setProductId(Long.parseLong(key));
					model.parse(pJSONObject.getJSONObject(key));
					models.add(model);
				}
			}
			
			result.setProductCouponGiftModels(models);
		}
		
		mIsSuccess = true;

		return result;
	}

}
