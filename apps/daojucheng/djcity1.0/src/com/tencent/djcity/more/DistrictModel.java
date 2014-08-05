package com.tencent.djcity.more;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DistrictModel {
	private int areaId;
	private String areaName;
	private List<ServerModel> serverModelList;

	public int getAreaId() {
		return areaId;
	}

	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public List<ServerModel> getServerModelList() {
		return serverModelList;
	}

	public void setServerModelList(List<ServerModel> serverModelList) {
		this.serverModelList = serverModelList;
	}

	public void parse(JSONObject object) throws JSONException {
		if(object == null) {
			return;
		}

		JSONObject area = object.optJSONObject("area");
		setAreaId(area.optInt("area_id", 0));
		setAreaName(area.optString("area_name", ""));
		
		JSONArray array = object.optJSONArray("server");
		
		if(array != null) {
			List<ServerModel> serverList = new ArrayList<ServerModel>();
			for(int i = 0; i < array.length(); i++) {
				JSONObject server = array.getJSONObject(i);
				ServerModel model = new ServerModel();
				model.parse(server);
				serverList.add(model);
			}
			setServerModelList(serverList);
		}
		
	}
}
