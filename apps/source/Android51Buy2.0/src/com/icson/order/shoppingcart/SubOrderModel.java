package com.icson.order.shoppingcart;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icson.lib.model.ShoppingCartProductModel;
import com.icson.order.shippingtype.ShippingTypeTimeModel;
import com.icson.util.ToolUtil;

public class SubOrderModel {
	//包裹id
	private String itemId;
	
	public long   sale_mode;
	public long  seller_id;
	public long  seller_stock_id;
	public double  totalAmtB4Promotion;
	public long   vender_deliver;
	public long   giftcard_type;
	private double totalAmt;
	private double totalcut;
	private double totalWeight;
	
	public String  shipTypeName;
	public double shippingPrice;
	public ArrayList<ShippingTypeTimeModel> mShippingTypeTimeModelList = new ArrayList<ShippingTypeTimeModel>();
	//以下数据需要在下单接口提交（与订单确认页拉取数值一样）
	private String    psystock; //仓库id 
	
	//包裹中商品信息列表
	private ArrayList<ShoppingCartProductModel> mProducts = new ArrayList<ShoppingCartProductModel>();
	//包裹中商品总价
	public double getPrice() {
		return totalAmt - totalcut;
	}

	public ArrayList<ShoppingCartProductModel> getProducts() {
		return mProducts;
	}

	public void setmProducts(ArrayList<ShoppingCartProductModel> mProducts) {
		this.mProducts = mProducts;
	}

	public double getTotalAmt() {
		return totalAmt;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public double getTotalPrice() {
		return totalAmt;
	}

	public void setTotalAmt(double totalAmt) {
		this.totalAmt = totalAmt;
	}

	public double getTotalcut() {
		return totalcut;
	}

	public void setTotalcut(double totalcut) {
		this.totalcut = totalcut;
	}

	public double getTotalWeight() {
		return totalWeight;
	}

	public void setTotalWeight(double totalWeight) {
		this.totalWeight = totalWeight;
	}


	public void setProducts(ArrayList<ShoppingCartProductModel> mProducts) {
		this.mProducts = mProducts;
	}
	
	public void setPsystock(String aPsystock) {
		this.psystock = aPsystock;
	}

	public String getPsystock() {
		return psystock;
	}
	
	public void parse(JSONObject json) throws Exception{
		setTotalAmt(json.optDouble("totalAmt"));
		setTotalcut(json.optDouble("totalCut"));
		setTotalWeight(json.optDouble("totalWeight"));
		setPsystock(json.optString("psystock"));
		sale_mode = json.optLong("sale_mode");
		seller_id = json.optLong("seller_id");
		seller_stock_id = json.optLong("seller_stock_id");
		this.totalAmtB4Promotion = json.optLong("totalAmtB4Promotion");
		this.vender_deliver = json.optLong("vender_deliver");
		this.giftcard_type = json.optLong("giftcart_type");
		
		if(!ToolUtil.isEmptyList(json, "items")){
			final JSONObject arrs = json.getJSONObject("items");
			@SuppressWarnings("unchecked")
			final Iterator<String> iterator = arrs.keys();
			while(iterator.hasNext()){
				String key = iterator.next();
				ShoppingCartProductModel model = new ShoppingCartProductModel();
				model.parse(arrs.getJSONObject(key));
				mProducts.add(model);
			}
		}
		
		final JSONObject shipinfo = json.optJSONArray("pkgshipinfo").getJSONObject(0);
		if(null != shipinfo)
		{
			shipTypeName = shipinfo.optString("ShipTypeName");
			shippingPrice = shipinfo.optDouble("shippingPrice");
			JSONObject shipitems = shipinfo.optJSONObject("subShippingItemNew");
			if(null!=shipitems)
			{
				JSONArray arrs = shipitems.optJSONArray("timeAvaiable");
				if(arrs!=null)
				{
					for (int i = 0, len = arrs.length(); i < len; i++) {
					JSONObject v = arrs.getJSONObject(i);
					ShippingTypeTimeModel model = new ShippingTypeTimeModel();
					model.parse(v);
					if(model.getState()== ShippingTypeTimeModel.STATUS_OK)
						mShippingTypeTimeModelList.add(model);
				}
				}
				//排序？
				//sortShippingTypeModels(mShippingTypeTimeModelList);
			}
		}
	}

	
}
