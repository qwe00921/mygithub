package com.icson.lib.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProductOptionModel {
/*	{
		id: "42914",
		list: [
		{
		name: "16G单卡",
		product_id: 842663,
		selected: "1"
		},
		{
		name: "16G双卡",
		product_id: 804744,
		selected: "-1"
		},
		{
		name: "32G单卡",
		product_id: 1288666,
		selected: "0"
		},
		{
		name: "32G双卡",
		product_id: 912529,
		selected: "-1"
		}
		],
		name: "存储容量"
		},
		*/
	private long mId;
	private String mName;
	private List<ProductOptionDetailModel> mProductOptionDetailModelList;

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public List<ProductOptionDetailModel> getProductOptionDetailModelList() {
		return mProductOptionDetailModelList;
	}

	public void setProductOptionDetailModelList(
			List<ProductOptionDetailModel> productOptionDetailModelList) {
		this.mProductOptionDetailModelList = productOptionDetailModelList;
	}

	public void parse(JSONObject json) throws JSONException{
		if(json == null) {
			return;
		}
		setId(json.optLong("id", 0));
		setName(json.optString("name", ""));
		
		JSONArray detailArray = json.getJSONArray("list");
		
		List<ProductOptionDetailModel> productOptionDetailModelList = new ArrayList<ProductOptionDetailModel>();
		for(int i = 0; i < detailArray.length(); i++) {
			JSONObject detail = detailArray.getJSONObject(i);
			ProductOptionDetailModel model = new ProductOptionDetailModel();
			model.parse(detail);
			productOptionDetailModelList.add(model);
		}
		
		setProductOptionDetailModelList(productOptionDetailModelList);
		
	}
	
}
