package com.tencent.djcity.home.recommend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.model.BaseModel;

public class ProductModel extends BaseModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProductModel() {

	}

	public static ProductModel fromJson(JSONObject aObject) {
		if (null == aObject)
			return null;

		ProductModel info = new ProductModel();
		info.appId = aObject.optString("appId");
		info.appName = aObject.optString("appName");
		info.appShort = aObject.optString("appShort");
		info.busId = aObject.optString("busId");
		info.limitPerOrder = aObject.optString("limitPerOrder");
		info.propDesc = aObject.optString("propDesc");
		info.propId = aObject.optString("propId");
		info.propImg = aObject.optString("propImg");
		info.propName = aObject.optString("propName");
		info.type = aObject.optString("type");
		info.todayLimit = aObject.optInt("todayLimit");
		info.totalLimit = aObject.optInt("totalLimit");
		info.selectArea = aObject.optInt("selectArea");
		info.selectRole = aObject.optInt("selectRole");
		info.setWaterMark(aObject.optInt("waterMark"));
//		private int todayLimit;
//		private int totalLimit;
//		private int selectArea;
//		private int selectRole;
		JSONArray validateArray = aObject.optJSONArray("valiDate");
		
		if(validateArray != null) {
			List<Validate> valiList = new ArrayList<ProductModel.Validate>();
			for(int i = 0; i < validateArray.length(); i++) {
				JSONObject validate = validateArray.optJSONObject(i);
				Validate vali = new Validate();
				vali.day = validate.optString("day");
				vali.code = validate.optString("code");
				vali.oldPrice = validate.optString("oldPrice");
				vali.curPrice = validate.optString("curPrice");
				vali.wechatPrice = validate.optString("wechatPrice");
				vali.left = validate.optInt("left", 0);
				vali.bought = validate.optInt("bought");
				vali.todayBought = validate.optInt("todayBought", 0);
				valiList.add(vali);
			}
			info.setValidateList(valiList);
		}

		
//		
//		day: 0,
//		code: "468",
//		oldPrice: "600",
//		curPrice: "200",
//		wechatPrice: "10",
//		left: 0,
//		bought: "0",
//		todayBought: 0
//		}

		return info;
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof ProductModel) {
			ProductModel target = (ProductModel) object;
			if (target.type.equals(this.type)
					&& target.propId.equals(this.propId)
					&& target.propName.equals(this.propName)
					&& target.busId.equals(this.busId)
					&& target.appName.equals(this.appName)
					&& target.appShort.equals(this.appShort)
					&& target.propDesc.equals(this.propDesc)
					&& target.propImg.equals(this.propImg)
					&& target.limitPerOrder.equals(this.limitPerOrder)
					&& target.appId.equals(this.appId)
					&& target.todayLimit == todayLimit
					&& target.totalLimit == totalLimit
					&& target.selectArea == selectArea
					&& target.selectRole == selectRole
					&& target.waterMark == waterMark
					)
				return true;
		}

		return false;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPropId() {
		return propId;
	}

	public void setPropId(String propId) {
		this.propId = propId;
	}

	public String getPropName() {
		return propName;
	}

	public void setPropName(String propName) {
		this.propName = propName;
	}

	public String getBusId() {
		return busId;
	}

	public void setBusId(String busId) {
		this.busId = busId;
	}

	public List<Validate> getValidateList() {
		return validateList;
	}

	public void setValidateList(List<Validate> validateList) {
		this.validateList = validateList;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppShort() {
		return appShort;
	}

	public void setAppShort(String appShort) {
		this.appShort = appShort;
	}

	public String getPropDesc() {
		return propDesc;
	}

	public void setPropDesc(String propDesc) {
		this.propDesc = propDesc;
	}

	public String getPropImg() {
		return propImg;
	}

	public void setPropImg(String propImg) {
		this.propImg = propImg;
	}

	public String getLimitPerOrder() {
		return limitPerOrder;
	}

	public void setLimitPerOrder(String limitPerOrder) {
		this.limitPerOrder = limitPerOrder;
	}

	public int getTodayLimit() {
		return todayLimit;
	}

	public void setTodayLimit(int todayLimit) {
		this.todayLimit = todayLimit;
	}

	public int getTotalLimit() {
		return totalLimit;
	}

	public void setTotalLimit(int totalLimit) {
		this.totalLimit = totalLimit;
	}

	public int getSelectArea() {
		return selectArea;
	}

	public void setSelectArea(int selectArea) {
		this.selectArea = selectArea;
	}

	public int getSelectRole() {
		return selectRole;
	}

	public void setSelectRole(int selectRole) {
		this.selectRole = selectRole;
	}

	public int getWaterMark() {
		return waterMark;
	}

	public void setWaterMark(int waterMark) {
		this.waterMark = waterMark;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result += 37 * result + getStringHashCode(type);
		result += 37 * result + getStringHashCode(propId);
		result += 37 * result + getStringHashCode(propName);
		result += 37 * result + getStringHashCode(busId);
		result += 37 * result + getStringHashCode(appId);
		result += 37 * result + getStringHashCode(appName);
		result += 37 * result + getStringHashCode(appShort);
		result += 37 * result + getStringHashCode(propDesc);
		result += 37 * result + getStringHashCode(propImg);
		result += 37 * result + getStringHashCode(limitPerOrder);

		return result;
	}

	public int getStringHashCode(String str) {
		return null != str ? str.hashCode() : 0;

	}

	private String type;
	private String propId;
	private String propName;
	private String busId;
	private String appId;
	private String appName;
	private String appShort;
	private String propDesc;
	private String propImg;
	private String limitPerOrder;
	private int waterMark;
	private int todayLimit;
	private int totalLimit;
	private int selectArea;
	private int selectRole;
	private List<Validate> validateList;

	public static final int WATER_MARK_GENERAL = 0;
	public static final int WATER_MARK_PRESALE = 1;
	public static final int WATER_MARK_HOT = 2;
	public static final int WATER_MARK_LIMIT = 3;
	public static final int WATER_MARK_NEW = 4;
	public static final int WATER_MARK_ONLY = 5;
	public static final int WATER_MARK_DISCOUNT = 6;
	public static final int WATER_MARK_CHOOSEN = 7;
	
	public int getWaterMarkResource() {
		switch(waterMark) {
			case WATER_MARK_DISCOUNT:{
				return R.drawable.label_discount;
			}
			case WATER_MARK_HOT: {
				return R.drawable.label_hot;
			}
			case WATER_MARK_NEW: {
				return R.drawable.label_new;
			}
		}
		
		return 0;
	}
	
	public static class PropDetail {
		
	}
	
	public static class Validate {
		private String day;
		private String code;
		private String oldPrice;
		private String curPrice;
		private String wechatPrice;
		private int left;
		private int bought;
		private int todayBought;

		public String getDay() {
			return day;
		}

		public void setDay(String day) {
			this.day = day;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getOldPrice() {
			return oldPrice;
		}

		public void setOldPrice(String oldPrice) {
			this.oldPrice = oldPrice;
		}

		public String getCurPrice() {
			return curPrice;
		}

		public void setCurPrice(String curPrice) {
			this.curPrice = curPrice;
		}

		public String getWechatPrice() {
			return wechatPrice;
		}

		public void setWechatPrice(String wechatPrice) {
			this.wechatPrice = wechatPrice;
		}

		public int getLeft() {
			return left;
		}

		public void setLeft(int left) {
			this.left = left;
		}

		public int getBought() {
			return bought;
		}

		public void setBought(int bought) {
			this.bought = bought;
		}

		public int getTodayBought() {
			return todayBought;
		}

		public void setTodayBought(int todayBought) {
			this.todayBought = todayBought;
		}
	}
}
