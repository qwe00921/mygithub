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

public class PromotionProModel extends BaseModel {
	public long promotion_id;
	public long promotion_type;
	public long product_id;
	public double price;
	public long   num;
}
