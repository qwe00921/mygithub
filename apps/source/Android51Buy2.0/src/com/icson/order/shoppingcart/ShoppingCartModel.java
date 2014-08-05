package com.icson.order.shoppingcart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.address.AddressModel;
import com.icson.invoice.InvoiceModel;
import com.icson.lib.model.BaseModel;
import com.icson.order.shippingtype.ShippingTypeModel;
import com.icson.order.userpoint.UserPointModel;
import com.icson.paytype.PayTypeModel;
import com.icson.shoppingcart.ProductCouponGiftModel;
import com.icson.shoppingcart.PromoRuleModel;
import com.icson.util.ToolUtil;

public class ShoppingCartModel extends BaseModel {
	//发票相关节点 invoice
	private boolean isCanVAT;
	private ArrayList<String> invoiceContentOpt;
	private ArrayList<String> invoiceContentOptID;
	
	//总计金额
	private double totalCut;
	private double totalAmt;
	private double totalWeight;
	
	private double totalShipPrice;
	
	//购物车商品包裹 packageList
	private ArrayList<SubOrderModel> subOrders = new ArrayList<SubOrderModel>();
	//配送方式shipList
//	public ArrayList<ShippingTypeModel> mShippingTypeModelList = new ArrayList<ShippingTypeModel>();
	//支付方式paytypeList
	public ArrayList<PayTypeModel> mPayTypeModelList = new ArrayList<PayTypeModel>();
	//默认地址default_address
	public AddressModel mAddressModel = new AddressModel();
	//默认发票default_invoice
	public InvoiceModel mInvoiceModel = new InvoiceModel();
	//积分可使用范围pointrange
	public UserPointModel mUserPointModel = new UserPointModel();
	
	//单品赠券
	private ArrayList<ProductCouponGiftModel> mCouponGiftModels = new ArrayList<ProductCouponGiftModel>();
	private ArrayList<PromotionProModel> mPromotionPros = new ArrayList<PromotionProModel>();

	
	private long prule_id;// 订单中的促销规则
	private int benefit_type;// 订单中的促销规则
	private long benefits;// 订单中的促销金额
	private long prule_condition;
	private String prule_desc;
	
	private String token_deal;
	private long token_dealtime;
	private String token_package;
	private String token_product;
	
	

	private String comment;

	private String priceTips;
	
	private boolean signByOther;

	public boolean isSignByOther() {
		return signByOther;
	}

	public void setSignByOther(boolean signByOther) {
		this.signByOther = signByOther;
	}

	public double getTotalShipPrice() {
		return totalShipPrice;
	}

	public void setTotalShipPrice(double totalShipPrice) {
		this.totalShipPrice = totalShipPrice;
	}

	public double getTotalCut() {
		return totalCut;
	}

	public void setTotalCut(double totalCut) {
		this.totalCut = totalCut;
	}

	public double getTotalAmt() {
		return totalAmt;
	}

	public double getBenefits() {
		return benefits;
	}

	public long getPruleID() {
		return prule_id;
	}
	
	public int getBenefitType() {
		return benefit_type;
	}

	public void setTotalAmt(double totalAmt) {
		this.totalAmt = totalAmt;
	}

	public double getTotalWeight() {
		return totalWeight;
	}

	public void setTotalWeight(double totalWeight) {
		this.totalWeight = totalWeight;
	}

	public ArrayList<SubOrderModel> getSubOrders() {
		return subOrders;
	}

	public void setSubOrders(ArrayList<SubOrderModel> subOrders) {
		this.subOrders = subOrders;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public ArrayList<ProductCouponGiftModel> getProductCouponGiftModels(){
		return this.mCouponGiftModels;
	}
	
	public String getPriceTips() {
		return priceTips;
	}

	public void setPriceTips(String priceTips) {
		this.priceTips = priceTips;
	}

	@SuppressWarnings("unchecked")
	public void parse(JSONObject json) throws Exception {
		setTotalCut(json.getDouble("totalCut"));
		setTotalAmt(json.getDouble("totalAmt"));
		setTotalWeight(json.getDouble("totalWeight"));
		setTotalShipPrice(json.getDouble("totalShipPrice"));

		if (!ToolUtil.isEmptyList(json, "packageList")) {
			final JSONObject whole = json.getJSONObject("packageList");
			final JSONObject arrs = whole.getJSONObject("default");
			
			final Iterator<String> iterator = arrs.keys();
			while (iterator.hasNext()) {
				String key = iterator.next();
				//包裹
				SubOrderModel model = new SubOrderModel();
				model.setItemId(key);
				model.parse(arrs.getJSONObject(key));
				subOrders.add(model);
			}
		}
		
		setPriceTips(json.optString("price_tips", ""));
		
		// 支付方式paytypeList
		if (!ToolUtil.isEmptyList(json, "paytypeList")) {
			JSONObject data = json.getJSONObject("paytypeList");
			final Iterator<String> iter = data.keys();
			while (iter.hasNext()) {
				String key = iter.next();
				PayTypeModel model = new PayTypeModel();
				model.parse(data.getJSONObject(key));
				mPayTypeModelList.add(model);
			}
			
			sortPayTypeModels(mPayTypeModelList);
		}

		// 默认地址default_address
		if (!ToolUtil.isEmptyList(json, "default_address")) {
			mAddressModel.parse(json.getJSONObject("default_address"));
		}else{
			mAddressModel = null;
		}
		// 默认发票default_invoice
		if (!ToolUtil.isEmptyList(json, "default_invoice")) {
			mInvoiceModel.parse(json.getJSONObject("default_invoice"));
		}else{
			mInvoiceModel = null;
		}
		// 积分限制pointrange
		if (!ToolUtil.isEmptyList(json, "pointrange")) {
			mUserPointModel.parse(json.getJSONObject("pointrange"));
		}
				
		//可开发票类型
		if (!ToolUtil.isEmptyList(json, "invoice")) {
			final JSONObject invoice = json.getJSONObject("invoice");

			isCanVAT = invoice.optBoolean("isCanVAT", false);
			if (!ToolUtil.isEmptyList(invoice, "contentOpt")) {
				invoiceContentOpt = new ArrayList<String>();
				JSONArray arrs = invoice.getJSONArray("contentOpt");
				for (int i = 0, len = arrs.length(); i < len; i++) {
					invoiceContentOpt.add(arrs.getString(i));
				}
			}
			if (!ToolUtil.isEmptyList(invoice, "contentOptID")) {
				invoiceContentOptID = new ArrayList<String>();
				JSONArray arrs = invoice.getJSONArray("contentOptID");
				for (int i = 0, len = arrs.length(); i < len; i++) {
					invoiceContentOptID.add(arrs.getString(i));
				}
			}
		}
		// 促销活动
		if (!ToolUtil.isEmptyList(json, "promotion")) {
			/*
			 {"is_restrict":0,"benefit_times":1,"rule_id":"13","desc":"放心购满500元返100元","rule_type":"1","pids":[412367],"condition":"50000",
			  	"benefit_type":"2","rule_sum_value":"129900","benefits":"5194,5193,5192",
			  	"url":"http:\/\/u.51buy.com\/d99878 ","account_type":"0"}
			 */
			JSONObject promotion = json.getJSONObject("promotion");
			benefit_type = promotion.getInt("benefit_type");
			benefits = promotion.getLong("benefits");
			prule_condition = promotion.getLong("condition");
			prule_desc = promotion.optString("desc");
			prule_id = promotion.getLong("rule_id");
		}
		
		if(!ToolUtil.isEmptyList(json, "promotion_products"))
		{
			mPromotionPros = new ArrayList<PromotionProModel>();
			JSONArray arrs = json.getJSONArray("promotion_products");
			for (int i = 0, len = arrs.length(); i < len; i++) {
				JSONObject obj = arrs.getJSONObject(i);
				PromotionProModel promotionpro = new PromotionProModel();
				promotionpro.promotion_id = obj.optLong("promotion_id");
				promotionpro.promotion_type = obj.optLong("type");
				promotionpro.product_id = obj.optLong("product_id");
				promotionpro.num = obj.optLong("num");
				promotionpro.price = obj.optDouble("price");
				
				mPromotionPros.add(promotionpro);
			}
		}
	

		// 配送信息shipList
		/*
		if (!ToolUtil.isEmptyList(json, "shipList")) {
			JSONObject data = json.getJSONObject("shipList");
			final Iterator<String> iter = data.keys();
			while (iter.hasNext()) {
				final String key = iter.next();
				ShippingTypeModel model = new ShippingTypeModel();
				model.parse(data.getJSONObject(key));

				
				if (model.getSubShippingTypeModelList() == null
						|| model.getSubShippingTypeModelList().size() == 0) {
					break;
				}

				// 不支持拆单操作
				//if (model.getSubShippingTypeModelList().size() > 1) {
				//	break;
				//}

				mShippingTypeModelList.add(model);
			}
			// 配送时间排序
			sortShippingTypeModels(mShippingTypeModelList);
		}
*///abort
		
		if(!ToolUtil.isEmptyList(json, "token")) 
		{	
			JSONObject promotion = json.getJSONObject("token");
			token_deal = promotion.optString("Deal");
			token_dealtime = promotion.optLong("DealTime");
			token_package = promotion.optString("Deal");
			token_product = promotion.optString("Product");
		}
		
		//单品赠券
		if(!ToolUtil.isEmptyList(json, "coupons")) {
			JSONArray pJSONArray = json.getJSONArray("coupons");
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
			
			mCouponGiftModels.addAll(models);
		}
		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void sortShippingTypeModels(ArrayList<ShippingTypeModel> models) {
		if (models == null || models.size() < 2) {
			return;
		}

		Collections.sort(models, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				ShippingTypeModel a = (ShippingTypeModel) o1;
				ShippingTypeModel b = (ShippingTypeModel) o2;

				return a.getOrderNumber() < b.getOrderNumber() ? 1 : -1;
			}
		});

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void sortPayTypeModels(ArrayList<PayTypeModel> models) {
		if (models == null || models.size() < 2)
			return;

		/*
		Collections.sort(models, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				PayTypeModel a = (PayTypeModel) o1;
				PayTypeModel b = (PayTypeModel) o2;
				return a.getPayType() > b.getPayType() ? 1 : -1;
			}
		});*/
		
		Collections.sort(models, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				PayTypeModel a = (PayTypeModel) o1;
				PayTypeModel b = (PayTypeModel) o2;
				return a.getSortId() > b.getSortId() ? 1 : -1;
			}
		});
	}

	public ArrayList<String> getInvoiceContentOpt() {
		if (invoiceContentOpt == null || invoiceContentOpt.size() == 0) {
			invoiceContentOpt = new ArrayList<String>();
			invoiceContentOpt.add("商品明细");
		}

		return invoiceContentOpt;
	}

	public boolean isCanVAT() {
		return isCanVAT;
	}

	public void setInvoiceContentOpt(ArrayList<String> invoiceContentOpt) {
		this.invoiceContentOpt = invoiceContentOpt;
	}

	
}
