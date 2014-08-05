package com.icson.address;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;

public class AddressModel extends BaseModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
	private int defaultShipping;
	private int defaultPayType;
	private int lastUseTime;
	private int status;
	private int uid;

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

	public int getDefaultShipping() {
		return defaultShipping;
	}

	public void setDefaultShipping(int defaultShipping) {
		this.defaultShipping = defaultShipping;
	}

	public int getDefaultPayType() {
		return defaultPayType;
	}

	public void setDefaultPayType(int defaultPayType) {
		this.defaultPayType = defaultPayType;
	}

	public int getLastUseTime() {
		return lastUseTime;
	}

	public void setLastUseTime(int lastUseTime) {
		this.lastUseTime = lastUseTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public void parse(JSONObject json) throws JSONException {
		setAid(json.getInt("aid"));
		setIid(json.getInt("iid"));
		setName(json.getString("name"));
		setMobile(json.getString("mobile"));
		setPhone(json.getString("phone"));
		setFax(json.getString("fax"));
		setZipcode(json.getString("zipcode"));
		setDistrict(json.getInt("district"));
		setAddress(json.getString("address"));
		setWorkplace(json.getString("workplace"));
		setSortfactor(json.getInt("sortfactor"));
		setUpdatetime(json.getInt("updatetime"));
		setCreatetime(json.getInt("createtime"));
		setDefaultShipping(json.optInt("default_shipping"));
		setDefaultPayType(json.optInt("default_pay_type"));
		setLastUseTime(json.optInt("last_use_time"));
		setStatus(json.getInt("status"));
		setUid(json.getInt("uid"));
	}

}
