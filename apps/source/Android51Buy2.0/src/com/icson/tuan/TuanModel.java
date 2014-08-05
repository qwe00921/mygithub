package com.icson.tuan;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.lib.model.ProductModel;
import com.icson.shoppingcart.PromoRuleModel;
import com.icson.util.ToolUtil;

public class TuanModel extends BaseModel {

	private long now;

	private long begin;

	private long end;

	private int mPageNum;
	private int mPageSize;
	private int mPageCount;
	private int mTotalSize;
	private int mBuyNum;		//参团人数

	private ArrayList<CateInfo> mCateInfos = new ArrayList<CateInfo>();

	public int getPageNum() {
		return mPageNum;
	}

	public int getPageSize() {
		return mPageSize;
	}

	public int getPageCount() {
		return mPageCount;
	}

	public int getTotalSize() {
		return mTotalSize;
	}
	
	public int getBuyNum() {
		return mBuyNum;
	}

	public long getBegin() {
		return begin;
	}

	public void setBegin(long begin) {
		this.begin = begin;
	}

	public long getNow() {
		return now;
	}

	public void setNow(long now) {
		this.now = now;
	}

	private ArrayList<TuanProductModel> mTuanProductModels;

	public ArrayList<TuanProductModel> getTuanProductModles() {
		return mTuanProductModels;
	}
	public ArrayList<CateInfo> getCateInfos() {
		return mCateInfos;
	}

	private void setEnd(long end) {
		this.end = end;
	}

	public long getEnd() {
		return end;
	}

	public void parse(JSONObject json) throws JSONException {
		setEnd(json.getLong("end"));
		setNow(json.getLong("now"));
		setBegin(json.getLong("begin"));
		mTuanProductModels = new ArrayList<TuanProductModel>();
		if (!ToolUtil.isEmptyList(json, "products")) {
			JSONArray arrs = json.getJSONArray("products");

			for (int i = 0, len = arrs.length(); i < len; i++) {
				TuanProductModel model = new TuanProductModel();
				model.parse(arrs.getJSONObject(i));
				mTuanProductModels.add(model);
			}
		}

		// Parse data for page configuration.
		if (!ToolUtil.isEmptyList(json, "page")) {
			JSONObject pPage = json.getJSONObject("page");

			// 1. Page number.
			mPageNum = pPage.optInt("page_current");

			// 2. Page size.
			mPageSize = pPage.optInt("page_size");

			// 3. Page count.
			mPageCount = pPage.optInt("page_count");

			// 4. Total size.
			mTotalSize = pPage.optInt("total");
			
			// 5. 参团人数
			mBuyNum = pPage.optInt("allSale", 0);

			if (!ToolUtil.isEmptyList(pPage, "cateInfo")) {
				JSONArray arrs = pPage.getJSONArray("cateInfo");

				for (int i = 0, len = arrs.length(); i < len; i++) {
					CateInfo model = new CateInfo();
					model.parse(arrs.getJSONObject(i));
					mCateInfos.add(model);
				}
			}
		}
	}

	public static class CateInfo extends BaseModel {
		public String name;
		public int count;
		public int cid;

		public void parse(JSONObject json) throws JSONException {
			name = json.optString("n");
			count = json.optInt("c");
			cid = json.optInt("cid");
		}
	}

	public static class TuanProductModel extends ProductModel {
		private static final long serialVersionUID = 1L;

		private long saleCount;

		private String promoName;

		// 促销规则
		private transient ArrayList<PromoRuleModel> mPromoRuleModels = new ArrayList<PromoRuleModel>();

		// 促销规则
		public ArrayList<PromoRuleModel> getPromoRuleModelList() {
			return mPromoRuleModels;
		}

		// 是否节能补贴
		public boolean isESProduct() {
			for (PromoRuleModel rule : mPromoRuleModels) {
				if (rule.getName().contains("节能补贴"))
					return true;
			}
			return false;
		}

		public PromoRuleModel getESPromoRuleModel() {
			for (PromoRuleModel rule : mPromoRuleModels) {
				if (rule.getName().contains("节能补贴"))
					return rule;
			}
			return null;
		}

		public String getPromoName() {
			return promoName;
		}

		public void setPromoName(String promoName) {
			this.promoName = promoName;
		}

		public long getSaleCount() {
			return saleCount;
		}

		public void setSaleCount(long saleCount) {
			this.saleCount = saleCount;
		}

		@Override
		public void parse(JSONObject json) throws JSONException {
			super.parse(json);
			setSaleCount(json.getLong("sale_count"));

			// 促销规则
			if (!ToolUtil.isEmptyList(json, "rules_detail")) {
				JSONArray rules = json.getJSONArray("rules_detail");

				for (int i = 0, len = rules.length(); i < len; i++) {
					JSONObject rule = rules.getJSONObject(i);
					PromoRuleModel model = new PromoRuleModel();
					model.parse(rule);
					mPromoRuleModels.add(model);
				}

			}
		}
	}
}
