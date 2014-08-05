package com.tencent.djcity.more;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerModel {
	private int serverId;
	private String serverName;

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public void parse(JSONObject object) throws JSONException {
		if(object == null) {
			return;
		}
		
		setServerId(object.optInt("server_id", 0));
		setServerName(object.optString("server_name", ""));
	}
}
