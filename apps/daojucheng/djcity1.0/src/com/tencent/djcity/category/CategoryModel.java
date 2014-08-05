package com.tencent.djcity.category;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.djcity.lib.model.BaseModel;
import com.tencent.djcity.util.ToolUtil;

public class CategoryModel extends BaseModel implements Serializable {

	private static final long serialVersionUID = -7992803656327928663L;

	private String name;
	private String id;

	private ArrayList<SubCategoryModel> subs = new ArrayList<SubCategoryModel>();

	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}

	public ArrayList<SubCategoryModel> getSubCategorys(){
		return subs;
	}
	public String getDesc() {
		StringBuilder sb = new StringBuilder();
		int size = subs.size();
		for (int i = 0; i < size; i++) {
			if (i > 2)
				break;
			sb.append(subs.get(i).name).append("  ");
		}
		return sb.toString();
	}
 
	@SuppressWarnings("serial")
	public static class SubCategoryModel extends BaseModel implements Serializable{
		private String name;
		private ArrayList<NodeCategoryModel> nodes = new ArrayList<NodeCategoryModel>();
		public boolean isSelected;
		public String getName() {
			return name;
		}
		public ArrayList<NodeCategoryModel> getNodes(){
			return nodes;
		}
		public String getDesc() {
			StringBuilder sb = new StringBuilder("(");
			int size = nodes.size();
			for (int i = 0; i < size; i++) {
				if (i > 2)
					break;
				sb.append(nodes.get(i).name).append("  ");
			}
			if(size > 0)
			{
				sb.delete(sb.length()-2, sb.length());
			}
			sb.append(")");
			return sb.toString();
		}

		public void parse(JSONObject sub) throws JSONException {
			name = sub.optString("name");
			JSONArray array = sub.optJSONArray("subs");
			final int size = (null != array ? array.length() : 0);
			for (int i = 0; i < size; i++) {
				NodeCategoryModel node = new NodeCategoryModel();
				node.parse(array.getJSONObject(i));
				nodes.add(node);
			}
		}

	}

	@SuppressWarnings("serial")
	public static class NodeCategoryModel extends BaseModel  implements Serializable{
		public String name;
		
		public String path;
		public String option;
		public String areacode;
		public String keyword;
		public String classId;
		public String sort;
		public String page;
		public String pageSize;
		public String price;

		public void parse(JSONObject json) throws JSONException {
			name = json.optString("name");
			JSONArray array = json.optJSONArray("condition");
			if( null != array ) {
				path = array.getString(0);
				option = array.getString(1);
				areacode = array.getString(2);
				keyword = array.getString(3);
				classId = array.getString(4);
				sort = array.getString(5);
				page = array.getString(6);
				pageSize = array.getString(7);
				price = array.getString(8);
			}
		}
	}

	public void parse(JSONObject v) throws JSONException {
		name = v.optString("name");
		id = v.optString("id");
		if (ToolUtil.isEmptyList(v, "subs")) {
			return ;
		}
		
		JSONArray array = v.optJSONArray("subs");
		final int size = (null != array ? array.length() : 0);
		for (int i = 0; i < size; i++) {
			SubCategoryModel subCategory = new SubCategoryModel();
			subCategory.parse(array.getJSONObject(i));
			subs.add(subCategory);
		}
	}
}
