package com.icson.lib.model;
import java.io.Serializable;

public class SearchCategoryModel extends CategoryModel  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5448109229582936999L;
	private int mNum;
	private String mName;
	private String mPath;

	public int getNum() {
		return mNum;
	}

	public void setNum(int num) {
		this.mNum = num;
	}
	
	public String getName(){
		return this.mName;
	}
	
	public void setName(String name){
		this.mName = name;
	}
	
	public String getPath(){
		return this.mPath;
	}
	
	public void setPath(String path){
		this.mPath = path;
	}
	
}