package com.tencent.djcity.more;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.json.JSONException;
import org.json.JSONObject;

public class RoleModel {

	private String country;
	private int flag;
	private int gender;
	private String id;
	private int level;
	private String name;

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param object
	 * @throws JSONException
	 */
	public void parse(JSONObject object) throws JSONException {
		if(object == null) {
			return;
		}
		setCountry(object.optString("country", ""));
		setFlag(object.optInt("flag", 0));
		setGender(object.optInt("gender", 0));
		setId(object.optString("id", ""));
		setLevel(object.optInt("lv", 0));
		
		String name = object.optString("name", "");
		if(name != null) {
			try {
				name = URLDecoder.decode(name, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setName(name);
		}
		
	}
}
//country: "8",
//flag: "0",
//gender: "1",
//id: "2234506473927734110",
//lv: "1",
//name: "%E6%AF%82%E4%B8%B6%E5%BD%A1%E9%80%9A"