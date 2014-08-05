package com.icson.lib.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.util.ToolUtil;

public class SearchFilterAttributeModel extends BaseModel  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id;

	private String name;

	private ArrayList<SearchFilterOptionModel> mSearchFilterOptionModels;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<SearchFilterOptionModel> getSearchFilterOptionModels() {
		return mSearchFilterOptionModels;
	}

	public void setSearchFilterOptionModels(ArrayList<SearchFilterOptionModel> mSearchFilterOptionModels) {
		this.mSearchFilterOptionModels = mSearchFilterOptionModels;
	}
	
	public void parse(JSONObject v) throws JSONException{
		setId( v.getInt("attrId") );
		setName( v.getString("attrName") );
		
		mSearchFilterOptionModels = new ArrayList<SearchFilterOptionModel>();

		if( !ToolUtil.isEmptyList(v, "AttrValue") ){
			JSONArray attrs = v.getJSONArray("AttrValue");	
			int nSize = attrs.length();
			for(int i = 0; i < nSize ; i++){
				SearchFilterOptionModel model = new SearchFilterOptionModel();
				model.parse( attrs.getJSONObject(i) );
				mSearchFilterOptionModels.add(model);
				
			}
		}
		
		setSearchFilterOptionModels( mSearchFilterOptionModels);
	}
}
