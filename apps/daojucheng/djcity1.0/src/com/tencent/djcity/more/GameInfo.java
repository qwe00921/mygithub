package com.tencent.djcity.more;

import com.tencent.djcity.preference.Preference;

public class GameInfo {
	
	// Game Info
	public static final String KEY_BIZ_IMG = "biz_img";
	public static final String KEY_ROLE_COUNTRY = "role_country";
	public static final String KEY_ROLE_FLAG = "role_flag";
	public static final String KEY_ROLE_GENDER = "role_gender";
	public static final String KEY_ROLE_ID = "role_id";
	public static final String KEY_ROLE_LEVEL = "role_lv";
	public static final String KEY_ROLE_NAME = "role_name";
	public static final String KEY_BIZ_NAME = "biz_name";
	public static final String KEY_BIZ_CODE = "biz_code";
	public static final String KEY_SERVER_ID = "server_id";
	public static final String KEY_SERVER_NAME = "server_name";
	public static final String KEY_AREA_ID = "area_id";
	public static final String KEY_AREA_NAME = "area_name";
	public static final String KEY_AREA_LEVEL = "area_level";
	
	private String bizImg;
	private String bizName;
	private String bizCode;
	private int serverId;
	private String serverName;
	private String roleName;
	private int roleLevel;
	private String roleId;
	private int roleFlag;
	private int areaId;
	private String areaName;
	private int areaLevel; //区等级，1表示只有区，2表示还有服务器

	public String getBizName() {
		return bizName;
	}

	public void setBizName(String bizName) {
		this.bizName = bizName;
	}

	public String getBizImg() {
		return bizImg;
	}

	public void setBizImg(String bizImg) {
		this.bizImg = bizImg;
	}

	public int getRoleFlag() {
		return roleFlag;
	}

	public void setRoleFlag(int roleFlag) {
		this.roleFlag = roleFlag;
	}

	public String getBizCode() {
		return bizCode;
	}

	public void setBizCode(String bizCode) {
		this.bizCode = bizCode;
	}

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

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public int getRoleLevel() {
		return roleLevel;
	}

	public void setRoleLevel(int roleLevel) {
		this.roleLevel = roleLevel;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

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

	public int getAreaLevel() {
		return areaLevel;
	}

	public void setAreaLevel(int areaLevel) {
		this.areaLevel = areaLevel;
	}

	public static GameInfo getGameInfoFromPreference() {
		Preference pref = Preference.getInstance();
		
		return pref.getGameInfo();
	}

//	0-不需要，
//	1-需要，拉取角色ID（其实包含角色名，不过下单时需要传入角色ID），
//	2-需要，拉取角色名（没有角色ID，下单时传入角色名）
	public boolean needBindRole() {
		switch(roleFlag) {
			case 0: {
				return false;  //没有角色不需要绑定
			}
			case 1: {	//role flag为1，当没有roleId时，需要绑定
				return roleId == null || "".equals(roleId);
			}
			case 2: {
				return roleName == null || "".equals(roleName);
			}
		}
		return false;
	}
	
	// areaLevel:
	public boolean needBindAreaServer() {
		switch(areaLevel) {
			case 2: {
				if(serverName == null || "".equals(serverName)) {
					return true;
				}
			}
			case 1: {
				if(areaName == null || "".equals(areaName)) {
					return true;
				}
				break;
			}
			default: {
				return true;
			}
		}
		return false;
	}
	
	public String getDescription() {
		
		String ret = "";
		
		if(serverName != null) {
			ret += serverName;
		}
		
		ret += " ";
		
		if(roleName != null) {
			ret += roleName;
		}
		return ret;
	}
	
	//判断是否有角色需要绑定
	//判断是否有区服需要绑定
	public boolean needBind() {
		return needBindAreaServer() || needBindRole();
	}
}
