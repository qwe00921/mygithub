package com.icson.home;

import com.icson.lib.model.BaseModel;

public class EventPortalModel extends BaseModel {
	public static final int EVENT_PORTAL_WEBLINK = 5;
	
	private String pciUrl;
	
	private long eventId;
	
	private int templateId;
	
	private int typeId;
	
	private String linkUrl;
	
	private String title;

	public int getTemplateId() {
		return templateId;
	}

	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}

	public String getPciUrl() {
		return pciUrl;
	}

	public void setPciUrl(String pciUrl) {
		this.pciUrl = pciUrl;
	}

	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}
	
	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	
	public String getLinkUrl() {
		return linkUrl;
	}

	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
