package com.icson.lib.model;

import org.json.JSONObject;

public class UserAddressModel extends BaseModel{
	
	private int uid;
	
	private int aid;
	
	private int iid;
	
	private String name;
	
	private String mobile;
	
	private String phone;
	
	private String fax;
	
	private String zipcode;
	
	private int district;
	
	private String address;
	
	private String workplace;
	
	private int sortfactor;
	
	private int updatetime;
	
	private int createtime;
	
	private int default_shipping;
	
	private int default_pay_type;
	
	private int last_use_time;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getAid() {
		return aid;
	}

	public void setAid(int aid) {
		this.aid = aid;
	}

	public int getIid() {
		return iid;
	}

	public void setIid(int iid) {
		this.iid = iid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public int getDistrict() {
		return district;
	}

	public void setDistrict(int district) {
		this.district = district;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getWorkplace() {
		return workplace;
	}

	public void setWorkplace(String workplace) {
		this.workplace = workplace;
	}

	public int getSortfactor() {
		return sortfactor;
	}

	public void setSortfactor(int sortfactor) {
		this.sortfactor = sortfactor;
	}

	public int getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(int updatetime) {
		this.updatetime = updatetime;
	}

	public int getCreatetime() {
		return createtime;
	}

	public void setCreatetime(int createtime) {
		this.createtime = createtime;
	}

	public int getDefault_shipping() {
		return default_shipping;
	}

	public void setDefault_shipping(int default_shipping) {
		this.default_shipping = default_shipping;
	}

	public int getDefault_pay_type() {
		return default_pay_type;
	}

	public void setDefault_pay_type(int default_pay_type) {
		this.default_pay_type = default_pay_type;
	}

	public int getLast_use_time() {
		return last_use_time;
	}

	public void setLast_use_time(int last_use_time) {
		this.last_use_time = last_use_time;
	}
	
	@Override
	public String toString(){
		return "";
	} 
	
	public void parse(JSONObject json) throws Exception{
		if( json.has("uid") ){
			setUid( json.getInt("uid") );
		}

		if( json.has("aid") ){
			setAid( json.getInt("aid") );
		}

		if( json.has("iid") ){
			setIid( json.getInt("iid") );
		}
		
		if( json.has("name") ){
			setName( json.getString("name") );
		}
		
		if( json.has("mobile") ){
			setMobile( json.getString("mobile") );
		}
		
		if( json.has("phone") ){
			setPhone( json.getString("phone") );
		}
		
		if( json.has("fax") ){
			setFax( json.getString("fax") );
		}						
		
		if( json.has("zipcode") ){
			setZipcode( json.getString("zipcode") );
		}
		
		if( json.has("district") ){
			setDistrict( json.getInt("district") );
		}					
		if( json.has("address") ){
			setAddress( json.getString("address") );
		}					

		if( json.has("workplace") ){
			setWorkplace( json.getString("workplace") );
		}					

		if( json.has("sortfactor") ){
			setSortfactor( json.getInt("sortfactor") );
		}	
		
		if( json.has("updatetime") ){
			setUpdatetime( json.getInt("updatetime") );
		}					

		if( json.has("createtime") ){
			setCreatetime( json.getInt("createtime") );
		}					

		if( json.has("default_shipping") ){
			setDefault_shipping( json.getInt("default_shipping") );
		}					

		if( json.has("default_pay_type") ){
			setDefault_pay_type( json.getInt("default_pay_type") );
		}					

		if( json.has("last_use_time") ){
			setLast_use_time( json.getInt("last_use_time") );
		}					
	}
}
