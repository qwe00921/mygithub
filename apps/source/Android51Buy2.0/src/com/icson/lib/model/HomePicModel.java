package com.icson.lib.model;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class HomePicModel extends BaseModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int type;
	
	private String imageUrl;
	
	private String ext;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public void parse(JSONObject json) throws JSONException{
		type = json.getInt("type");
		imageUrl = json.getString("img");
		ext = json.getString("ext");
	}
}
