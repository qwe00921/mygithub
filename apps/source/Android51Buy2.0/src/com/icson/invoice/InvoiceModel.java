package com.icson.invoice;

import java.io.Serializable;

import org.json.JSONObject;

import android.content.Context;

import com.icson.R;
import com.icson.lib.model.BaseModel;

@SuppressWarnings("serial")
public class InvoiceModel extends BaseModel implements Serializable{

	// 商业零售发票(个人)
	public static final int INVOICE_TYPE_PERSONAL = 1;

	// 增值税专用发票
	public static final int INVOICE_TYPE_VAD = 2;

	// 商业零售发票(单位)
	public static final int INVOICE_TYPE_COMPANY = 3;

	// 普通发票(广东站用)
	public static final int INVOICE_TYPE_NORMAL = 4;
	
	// Guandong: 2, 4: available, else, 1, 2, 3: available

	public int getIid() {
		return iid;
	}

	public void setIid(int iid) {
		this.iid = iid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTaxno() {
		return taxno;
	}

	public void setTaxno(String taxno) {
		this.taxno = taxno;
	}

	public String getBankno() {
		return bankno;
	}

	public void setBankno(String bankno) {
		this.bankno = bankno;
	}

	public String getBankname() {
		return bankname;
	}

	public void setBankname(String bankname) {
		this.bankname = bankname;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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

	public int getUid() {
		return uid;
	}

	public String getTypeName(Context context) {
		final int type = getType();

		int nResId = 0;
		switch (type) {
		case INVOICE_TYPE_PERSONAL:
			nResId = R.string.invoice_type_personal;
			break;
		case INVOICE_TYPE_COMPANY:
			nResId = R.string.invoice_type_company;
			break;
		case INVOICE_TYPE_VAD:
			nResId = R.string.invoice_type_vad;
			break;
		case INVOICE_TYPE_NORMAL:
			nResId = R.string.invoice_type_retail;
			break;
		}
		
		return context.getString(nResId);
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	private int iid;
	private int type;
	private String title;
	private String name;
	private String address;

	private String phone;
	private String taxno;
	private String bankno;

	private String bankname;

	private int status;

	private int sortfactor;
	private int updatetime;
	private int createtime;
	private int uid;
	
	private int contentOpt;//content option 发票内容
	
	private String content;
	
	public String getContent(){
		return content;
	}
	
	public void setContent(String content){
		this.content = content;
	}
	
	public void setContentOpt(int aOpt) {
		this.contentOpt = aOpt;
	}

	public int getContentOpt() {
		return contentOpt;
	}

	public void parse(JSONObject json) throws Exception {
		setIid(json.getInt("iid"));
		setType(json.getInt("type"));
		setTitle(json.getString("title"));
		setName(json.getString("name"));
		setAddress(json.getString("addr"));
		setPhone(json.getString("phone"));
		setTaxno(json.getString("taxno"));
		setBankno(json.getString("bankno"));
		setBankname(json.getString("bankname"));
		setStatus(json.getInt("status"));
		setSortfactor(json.getInt("sortfactor"));
		setUpdatetime(json.getInt("updatetime"));
		setCreatetime(json.getInt("createtime"));
		setUid(json.getInt("uid"));
	}

	
}
