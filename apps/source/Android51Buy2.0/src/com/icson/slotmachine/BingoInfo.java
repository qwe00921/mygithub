/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: icson
 * FileName: AwardInfo.java
 * 
 * Description: 
 * Author: xingyao (xingyao@tencent.com)
 * Created: 2013-6-17
 */
package com.icson.slotmachine;

import com.icson.lib.model.BaseModel;

/**
 * 
*   
* Class Name:BingoInfo 
* Class Description: 
* Author: xingyao 
* Modify: xingyao 
* Modify Date: 2013-6-19 上午11:04:51 
* Modify Remarks: 
* @version 1.0.0
*
 */
public class BingoInfo extends BaseModel {
	private String bingoName;
	private int    bingoType;
	private long    timestamp;
	private String bingoDetail;
	private String cdkey;
	private BingoInfo    expBingoInfo;
	
	private String shareTitle;
	private String shareContent;
	private String shareUrl;

	//product about
	private long    bingoId;
	private int    priceNormal;
	private int    priceIcson;
	private String productImgUrl;
	private String productChannelId;
	public String  proCharId;
	
	public static final int      BINGO_COUPON = 1; 
	public static final int      BINGO_CDKEY = 2;
	public static final int      BINGO_COIN = 3;
	public static final int      BINGO_OTHER = 4;
	public static final int      MAX_BINGO_TYPE = 4;
	public BingoInfo()
	{
		bingoId = 0;
		timestamp = 0;
		bingoDetail = "";
	}

	public void clear()
	{
		bingoName = "";
		bingoType = 0;
		timestamp = 0;
		bingoDetail = "";
		cdkey = "";
		expBingoInfo = null;
		
		shareTitle = "";
		shareContent = "";
		shareUrl = "";

		//product about
		bingoId = 0;
		priceNormal = 0;
		priceIcson = 0;
		productImgUrl = "";
	}
	
	public void setBingoId(long aId)
	{
		bingoId = aId;
	}
	public long getBingoId()
	{
		return bingoId;
	}
	
	
	public void setTimestamp(long aStamp)
	{
		timestamp = aStamp;
	}
	public long getTimestamp()
	{
		return timestamp;
	}
	
	public void setBingoDetail(String aDetail)
	{
		bingoDetail = aDetail;
	}
	public String getBingoDetail()
	{
		return bingoDetail;
	}
	
	
	public void setExpBingoInfo(BingoInfo  aInfo)
	{
		expBingoInfo = aInfo;
	}
	public BingoInfo getExpBingoInfo()
	{
		return expBingoInfo;
	}

	public void setBingoType(int aType) {
		this.bingoType = aType;
	}

	public int getBingoType() {
		return bingoType;
	}

	public void setCdkey(String akey) {
		this.cdkey = akey;
	}

	public String getCdkey() {
		return cdkey;
	}

	public void setBingoName(String aName) {
		bingoName = aName;
	}
	public String getBingoName()
	{
		return bingoName;
	}

	public void setShareContent(String aContent) {
		shareContent = aContent;
	}
	//Share About
	public String getShareContent()
	{
		return shareContent;
	}
	public void setShareUrl(String aUrl) {
		shareUrl = aUrl;
	}
	public String getShareUrl()
	{
		return shareUrl;
	}
	public void setShareTitle(String aTitle) {
		shareTitle = aTitle;
	}
	public String getShareTitle()
	{
		return shareTitle;
	}
	//end

	//product
	public void setPriceNormal(int aPrice) {
		priceNormal = aPrice;
	}
	public int getPriceNormal() {
		return priceNormal;
	}
	public void setPriceIcson(int aPrice) {
		priceIcson = aPrice;
	}
	public int getPriceIcson() {
		return priceIcson;
	}

	public void setProductImgUrl(String aUrl) {
		productImgUrl = aUrl;
	}
	public String getProductImgUrl()
	{
		return productImgUrl;
	}

	public String getProductChannelId() {
		return productChannelId;
	}

	public void setProductChannelId(String aChannelId) {
		productChannelId = aChannelId;
	}

	public void setProductCharid(String aId) {
		proCharId = aId;
		
	}

	
	
	
}
