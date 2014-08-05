package com.icson.my.main;

import org.json.JSONObject;

import android.text.Html;
import android.text.Spanned;

import com.icson.lib.model.BaseModel;

public class MyPointsModel extends BaseModel {
	private String title;
	private String points;
	private String msg;
	private String time;

	public String getTitle() {
		return title;
	}

	public Spanned getPointsStr() {
		if(Double.parseDouble(points) < 0){
			return Html.fromHtml("<font color=\"#333333\">"+points+"</font>");
		}else{
			return Html.fromHtml("<font color=\"#5fb840\">+"+points+"</font>");
		}
	}

	public String getMsg() {
		return msg;
	}

	public String getTime() {
		return time;
	}

	public void parse(JSONObject v) throws Exception {
		title = v.optString("type", "");
		msg = v.optString("msg", "");
		time = v.optString("time", "");
		points = v.optString("points", "0");
	}

}
