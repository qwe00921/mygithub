package com.tencent.djcity.my;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.djcity.lib.model.BaseModel;
import com.tencent.djcity.util.ToolUtil;

public class OrderDetailModel extends BaseModel {
	private String mBuyTime;
	private String mPayTime;
	private String mSerialNum;
	private long mPayPrice;
	private long mPrice;
	private long mDiscount;
	private ArrayList<PackageModel> mPackageModels;
	
	public void parse(JSONObject v) throws JSONException {
		JSONObject json = v.optJSONObject("data");
		
		mBuyTime = json.optString("dtBuyTime", "");
		mPayTime = json.optString("dtPayTime", "");
		mSerialNum = json.optString("sSerialNum", "");
		mPayPrice = json.optLong("iPayAmount", 0);
		mPrice = json.optLong("iPrice", 0);
		mDiscount = json.optLong("iDiscount", 0);
		
		JSONObject object = json.optJSONObject("sGoodsInfo");
		
		if(!ToolUtil.isEmptyList(object, "list")) {
			mPackageModels = new ArrayList<PackageModel>();
			
			JSONArray aArray = object.optJSONArray("list");
			final int nCount = (null != aArray ? aArray.length() : 0);
			for( int nPos = 0; nPos < nCount; nPos++ )
			{
				PackageModel packageModel = new PackageModel();
				packageModel.parse(aArray.getJSONObject(nPos));
				mPackageModels.add(packageModel);
			}
		}
		
	}
	
	public String getBuyTime(){
		return mBuyTime;
	}
	
	public String getPayTime(){
		return mPayTime;
	}
	
	public String getSerialNum(){
		return mSerialNum;
	}
	
	public long getPayPrice(){
		return mPayPrice;
	}
	public long getPrice(){
		return mPrice;
	}
	public long getDiscount(){
		return mDiscount;
	}
	public ArrayList<PackageModel> getPackageModels(){
		return mPackageModels;
	}
	
	
	
	public class PackageModel extends BaseModel{
		private String mPackageName;
		private String mPicUrl;
		private int mStatus;
		private ArrayList<GoodModel> mGoodModels;
		
		public void parse(JSONObject json) throws JSONException {
			mPackageName = json.optString("sPacketName", "");
			mPicUrl = json.optString("sPacketPic", "");
			mStatus = json.optInt("iStatus", 0);
			
			if(!ToolUtil.isEmptyList(json, "list")) {
				mGoodModels = new ArrayList<GoodModel>();
				
				JSONArray aArray = json.optJSONArray("list");
				final int nCount = (null != aArray ? aArray.length() : 0);
				for( int nPos = 0; nPos < nCount; nPos++ )
				{
					GoodModel goodModel = new GoodModel();
					goodModel.parse(aArray.getJSONObject(nPos));
					mGoodModels.add(goodModel);
				}
			}
			
		}
		
		public String getGoodUrl(){
			return mPicUrl;
		}
		
		public int getStatus(){
			return mStatus;
		}
		
		public String getGoodName(){
			return mPackageName;
		}
		
		public ArrayList<GoodModel> getGoodsModels(){
			return mGoodModels;
		}
		
		
		
		
		public class GoodModel extends BaseModel{
			private String mGoodName;
			private String mGoodUrl;
			private int mStatus;
			
			public void parse(JSONObject json) throws JSONException {
				mGoodName = json.optString("sGoodsName", "");
				mGoodUrl = json.optString("sGoodsPic", "");
				mStatus = json.optInt("iStatus", 0);
			}
			
			
			public String getGoodUrl(){
				return mGoodUrl;
			}
			
			public int getStatus(){
				return mStatus;
			}
			
			public String getGoodName(){
				return mGoodName;
			}
			
		}
		
	}


}
