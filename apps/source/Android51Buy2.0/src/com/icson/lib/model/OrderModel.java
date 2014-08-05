package com.icson.lib.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.icson.lib.pay.PayFactory;
import com.icson.util.ToolUtil;

public class OrderModel extends BaseModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int siteId;

	private String packageOrderId;
	private String orderCharId;

	private int flag;

	private long outTime;

	private String orderId;

	private int status;
	private String status_name;

	private long orderDate;

	private int payType;

	private String payTypeName;

	private boolean isPayed;
	private boolean products_collapse;

	private int shippingType;

	private String shippingTypeName;

	private String receiver;

	private String receiverAddress;

	private String couponCode;

	private double couponAmt;

	private int point;

	private double cash;

	private String rn;

	private boolean needPay;

	private boolean canCancel;

	private boolean canEvaluate;

	private double shippingCost;

	private double orderCost;

	private boolean payTypeIsOnline;

	private int buyNum;

	private long uid;

	private String receiverZip;

	private String receiverTel;

	private String receiverMobile;

	private String expect_dly_time;

	private String logistics;
	private String logiTime;

	private String telephone;

	private boolean hasLoc;
	private String strCouponSendRule;

	public boolean forceHide = false;
	
	public static final int NORMAL_ORDER = 0;
	public static final int LAST_PACKAGE_IN_ORDER = 1;
	
	
	public int   mPackageStatus = NORMAL_ORDER;
	public int   mPackageIdx;
	
	private String priceTips;
	
	public String getPriceTips() {
		return priceTips;
	}

	public void setPriceTips(String priceTips) {
		this.priceTips = priceTips;
	}

	public String getLogiTime() {
		return logiTime;
	}

	public String getLogistics() {
		return logistics;
	}

	public String getTelephone() {
		return telephone;
	}

	public boolean hasLoc() {
		return hasLoc;
	}

	public String getExpectDlyTime() {
		return expect_dly_time;
	}

	private String invoice_title = "";
	private String invoice_type = "";
	private String invoice_content = "";

	public String getInvoiceTitle() {
		return invoice_title;
	}

	public String getInvoiceType() {
		return invoice_type;
	}

	public String getInvoiceContent() {
		return invoice_content;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	private ArrayList<OrderProductModel> mOrderProductModelList = new ArrayList<OrderProductModel>();

	public int getBuyNum() {
		return buyNum;
	}
	public int getLeftNum(){
		if(mOrderProductModelList.size()<1)
			return 0;
		else
			return buyNum - mOrderProductModelList.get(0).getBuyCount();
	}

	public void setBuyNum(int buyNum) {
		this.buyNum = buyNum;
	}

	public ArrayList<OrderProductModel> getOrderProductModelList() {
		return mOrderProductModelList;
	}

	public void setmOrderProductModelList(
			ArrayList<OrderProductModel> mOrderProductModelList) {
		this.mOrderProductModelList = mOrderProductModelList;
	}

	public void setOrderCost(double orderCost) {
		this.orderCost = orderCost;
	}

	public double getOrderCost() {
		return orderCost;
	}

	public int getSiteId() {
		return siteId;
	}

	public void setSiteId(int siteId) {
		this.siteId = siteId;
	}

	public String getPackageOrderId()
	{
		return packageOrderId;
	}
	public void setPackageOrderId(String pCharId) {
		this.packageOrderId = pCharId;
	}
	public String getOrderCharId() {
		return orderCharId;
	}

	public void setOrderCharId(String orderCharId) {
		this.orderCharId = orderCharId;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public long getOutTime() {
		return outTime;
	}

	public void setOutTime(long outTime) {
		this.outTime = outTime;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getStatus_name() {
		return status_name;
	}

	public void setStatus_name(String status_name) {
		this.status_name = status_name;
	}

	public long getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(long orderDate) {
		this.orderDate = orderDate;
	}

	public int getPayType() {
		return payType;
	}

	public void setPayType(int payType) {
		this.payType = payType;
	}

	public String getPayTypeName() {
		return payTypeName;
	}

	public void setPayTypeName(String payTypeName) {
		this.payTypeName = payTypeName;
	}

	public boolean isPayed() {
		return isPayed;
	}
	public boolean isShowAll() {
		return !products_collapse;
	}

	public void setPayed(boolean isPayed) {
		this.isPayed = isPayed;
	}

	public int getShippingType() {
		return shippingType;
	}

	public void setShippingType(int shippingType) {
		this.shippingType = shippingType;
	}

	public String getShippingTypeName() {
		return shippingTypeName;
	}

	public void setShippingTypeName(String shippingTypeName) {
		this.shippingTypeName = shippingTypeName;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public double getCouponAmt() {
		return couponAmt;
	}

	public void setCouponAmt(double couponAmt) {
		this.couponAmt = couponAmt;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public double getCash() {
		return cash;
	}

	public void setCash(double cash) {
		this.cash = cash;
	}

	public String getRn() {
		return rn;
	}

	public void setRn(String rn) {
		this.rn = rn;
	}

	public boolean isNeedPay() {
		return needPay;
	}

	public void setNeedPay(boolean needPay) {
		this.needPay = needPay;
	}

	public boolean isCanCancel() {
		return canCancel;
	}

	public void setCanCancel(boolean canCancel) {
		this.canCancel = canCancel;
	}

	public boolean isCanEvaluate() {
		return canEvaluate;
	}

	public void setCanEvaluate(boolean canEvaluate) {
		this.canEvaluate = canEvaluate;
	}

	public double getShippingCost() {
		return shippingCost;
	}

	public void setShippingCost(double shippingCost) {
		this.shippingCost = shippingCost;
	}

	public String getReceiverZip() {
		return receiverZip;
	}

	public void setReceiverZip(String receiverZip) {
		this.receiverZip = receiverZip;
	}

	public String getReceiverTel() {
		return receiverTel;
	}

	public void setReceiverTel(String receiverTel) {
		this.receiverTel = receiverTel;
	}

	public String getReceiverMobile() {
		return receiverMobile;
	}

	public void setReceiverMobile(String receiverMobile) {
		this.receiverMobile = receiverMobile;
	}
	
	public void setCouponSendRule(String str) {
		this.strCouponSendRule = str;
	}
	
	public String getCouponSendRule() {
		return strCouponSendRule;
	}

	public void parse(JSONObject v) throws Exception {
		setSiteId(v.getInt("hw_id"));

		setOrderCharId(v.getString("order_char_id"));
		setPackageOrderId(v.optString("pOrderId"));
		
		setFlag(v.optInt("flag"));

		setOutTime(v.getLong("out_time"));

		setOrderId(v.getString("order_id"));

		setStatus(v.getInt("status"));

		setStatus_name(v.optString("status_name"));

		setOrderDate(v.getLong("order_date"));

		setPayType(v.getInt("pay_type"));

		setPayed(v.getInt("isPayed") == 1);

		setShippingType(v.getInt("shipping_type"));

		setShippingTypeName(v.optString("shipping_type_name", ""));

		setPayTypeName(v.optInt("pay_type") == PayFactory.PAY_ALI ? "支付宝" : v
				.optString("pay_type_name", ""));

		setReceiver(v.getString("receiver"));

		setShippingCost(v.getDouble("shipping_cost"));

		setOrderCost(v.getDouble("order_cost"));

		setCouponCode(v.optString("coupon_code"));

		setCouponAmt(v.optDouble("coupon_amt"));

		setPoint(v.optInt("point"));

		setCash(v.getDouble("cash"));

		setRn(v.optString("rn", ""));

		setNeedPay(v.getInt("need_pay") == 1);

		setCanCancel(v.getInt("can_cancel") == 1);

		setCanEvaluate(v.optBoolean("can_evaluate", false));

		setReceiverAddress(v.optString("receiver_addr", ""));

		setReceiverZip(v.optString("receiver_zip", ""));

		setReceiverTel(v.optString("receiver_tel", ""));

		setReceiverMobile(v.optString("receiver_mobile", ""));

		setPriceTips(v.optString("price_tips", ""));
		
		expect_dly_time = v.optString("expect_dly_date", "") + " "
				+ v.optString("expect_dly_time_span", "");

		// Added by Loren Chen, add new properties for tracking order.
		logistics = v.optString("logistics", "");
		logiTime = v.optString("logisticsTime", "");
		telephone = v.optString("tel", "");
		hasLoc = (1 == v.optInt("has_loc", 0));
		// End of addition.
		products_collapse = v.optBoolean("products_collapse", false);

		if (!ToolUtil.isEmptyList(v, "invoices")) {
			JSONArray invoices = v.optJSONArray("invoices");
			if (invoices != null && invoices.length() > 0) {
				JSONObject invoice = invoices.getJSONObject(0);
				invoice_title = invoice.optString("title", "");
				invoice_type = invoice.optString("type", "");
				invoice_content = invoice.optString("content", "");
			}
		}

		mOrderProductModelList.clear();
		if (!ToolUtil.isEmptyList(v, "items")) {
			final JSONObject products = v.optJSONObject("items");
			if (products != null) {
				int buyCount = 0;
				@SuppressWarnings("unchecked")
				Iterator<String> keys = products.keys();
				while (keys.hasNext()) {
					final String key = keys.next();
					final JSONObject product = products.optJSONObject(key);
					OrderProductModel model = new OrderProductModel();
					model.parse(product);
					mOrderProductModelList.add(model);
					buyCount += model.getBuyCount();
				}

				setBuyNum(buyCount);
			} else {
				final JSONArray arrs = v.optJSONArray("items");

				if (arrs != null) {
					for (int i = 0, len = arrs.length(); i < len; i++) {
						final JSONObject product = arrs.getJSONObject(i);
						OrderProductModel model = new OrderProductModel();
						model.parse(product);
						mOrderProductModelList.add(model);
					}

					setBuyNum(v.optInt("buy_total"));
				}
			}
		}
	}

	public String getReceiverAddress() {
		return receiverAddress;
	}

	public void setReceiverAddress(String receiverAddress) {
		this.receiverAddress = receiverAddress;
	}

	public boolean isPayTypeIsOnline() {
		return payTypeIsOnline;
	}

	public void setPayTypeIsOnline(boolean payTypeIsOnline) {
		this.payTypeIsOnline = payTypeIsOnline;
	}

	
	public void setLastPackageinOrder() {
		mPackageStatus |= LAST_PACKAGE_IN_ORDER;
	}

	/**  
	* method Name:isPackage    
	* method Description:  
	* @return   
	* boolean  
	* @exception   
	* @since  1.0.0  
	*//*
	public boolean isFirstPackage() {
		if(TextUtils.isEmpty(packageOrderId) || TextUtils.isEmpty(orderCharId))
			return true;
		
		int pid = Integer.valueOf(packageOrderId);
		int charid = Integer.valueOf(orderCharId);
		if(pid+1 == charid)
			return true;
		else
			return false;
	}
	*/
	public boolean isPackage() {
		if(TextUtils.isEmpty(packageOrderId) || TextUtils.isEmpty(orderCharId))
			return false;
		return !(packageOrderId.equals(orderCharId));
	}
	
	public boolean isLastPackage() {
		return (mPackageStatus & LAST_PACKAGE_IN_ORDER) >0;
	}
	
	/*
	public int getPackageIndex()
	{
		if(TextUtils.isEmpty(packageOrderId) || TextUtils.isEmpty(orderCharId))
			return -1;
		int pid = Integer.valueOf(packageOrderId);
		int charid = Integer.valueOf(orderCharId);
		
		
		return charid - pid;
	}*/
	
	public void setPackageIdxInOrder(int aIdx)
	{
		mPackageIdx = aIdx;
	}
}