package com.icson.event;

import com.icson.lib.model.BaseModel;

public class EventBaseModel extends BaseModel {
	private long eventId;
	private int templateId;
	private int    payType;
	
	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public int getTemplateId() {
		return templateId;
	}

	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}

	public void setPayType(int aType) {
		this.payType = aType;
		
	}
	public int getPayType() {
		return this.payType;
		
	}
	
}
