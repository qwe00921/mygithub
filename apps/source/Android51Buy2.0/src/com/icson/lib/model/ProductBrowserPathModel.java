package com.icson.lib.model;

import org.json.JSONObject;

public class ProductBrowserPathModel extends BaseModel{
	private int c1Id;
	
	private String c1Name;
	
	private int c2Id;
	
	private String c2Name;
	
	public int getC1Id() {
		return c1Id;
	}

	public void setC1Id(int c1Id) {
		this.c1Id = c1Id;
	}

	public String getC1Name() {
		return c1Name;
	}

	public void setC1Name(String c1Name) {
		this.c1Name = c1Name;
	}

	public int getC2Id() {
		return c2Id;
	}

	public void setC2Id(int c2Id) {
		this.c2Id = c2Id;
	}

	public String getC2Name() {
		return c2Name;
	}

	public void setC2Name(String c2Name) {
		this.c2Name = c2Name;
	}

	public int getC3Id() {
		return c3Id;
	}

	public void setC3Id(int c3Id) {
		this.c3Id = c3Id;
	}

	public String getC3Name() {
		return c3Name;
	}

	public void setC3Name(String c3Name) {
		this.c3Name = c3Name;
	}

	private int c3Id;
	
	private String c3Name;
	
	public void parse(JSONObject jsonObject) throws Exception{
		setC1Id( jsonObject.has("c1_id") ?  jsonObject.getInt("c1_id") : 0 );
		setC1Name( jsonObject.has("c1_name") ?  jsonObject.getString("c1_name") : "" );

		setC2Id( jsonObject.has("c2_id") ?  jsonObject.getInt("c2_id") : 0 );
		setC2Name( jsonObject.has("c2_name") ?  jsonObject.getString("c2_name") : "" );

		setC3Id( jsonObject.has("c3_id") ?  jsonObject.getInt("c3_id") : 0 );
		setC3Name( jsonObject.has("c3_name") ?  jsonObject.getString("c3_name") : "" );
	}
}
