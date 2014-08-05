/**
 * Copyright (C) 2013 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy
 * FileName: RecentCates.java
 * 
 * Description: 
 * Author: lorenchen (lorenchen@tencent.com)
 * Created: Jul 18, 2013
 */

package com.icson.hotlist;

import java.util.ArrayList;
import java.util.List;

import com.icson.lib.AppStorage;
import com.icson.util.IcsonApplication;

import android.text.TextUtils;

public class RecentCates {
	public RecentCates() {
		loadContent();
	}
	
	public static void addCate(String strCategory) {
		if( TextUtils.isEmpty(strCategory) )
			return ;
		
		RecentCates pSelf = RecentCates.getObject();
		
		if( null == pSelf.mCategories )
			pSelf.mCategories = new ArrayList<String>(MAX_SIZE);
		
		// 1. Check whether the category already exists.
		final int nSize = pSelf.mCategories.size();
		for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
			String strEntity = pSelf.mCategories.get(nIdx);
			if( (strEntity.equalsIgnoreCase(strCategory)) && (nIdx == 0) )
			{
				return;
			}
			else if( (strEntity.equalsIgnoreCase(strCategory)) && (nIdx > 0) ) {
				pSelf.mCategories.remove(nIdx);
				pSelf.mCategories.add(0, strCategory);
				
				return ;
			}
		}
		
		if( nSize >= MAX_SIZE )
			pSelf.mCategories.remove(MAX_SIZE - 1);
		
		pSelf.mCategories.add(0, strCategory);
	}
	
	public void saveContent() {
		String strContent = getString(MAX_SIZE, SPERATOR);
		if( !TextUtils.isEmpty(strContent) )
			AppStorage.setData("CATEGORY", "Recent", strContent, true);
	}
	
	public static String getString(int nNum) {
		return getString(nNum, SPERATOR);
	}
	
	public static String getString(int nNum, String strSeperator) {
		RecentCates pSelf = RecentCates.getObject();
		final int nSize = null != pSelf.mCategories ? pSelf.mCategories.size() : 0;
		nNum = Math.min(nNum, nSize);
		if( 0 >= nNum )
			return "";
		
		StringBuilder pBuilder = new StringBuilder();
		for( int nIdx = 0; nIdx < nNum; nIdx++ ) {
			pBuilder.append(pSelf.mCategories.get(nIdx));
			if( nIdx < nNum - 1 )
				pBuilder.append(strSeperator);
		}
		
		return pBuilder.toString();
	}
	
	private void loadContent() {
		mCategories = new ArrayList<String>(MAX_SIZE);
		
		String strContent = AppStorage.getData("CATEGORY", "Recent");
		if( !TextUtils.isEmpty(strContent) ) {
			String aValues[] = strContent.split(SPERATOR);
			final int nSize = aValues.length;
			for( int nIdx = 0; nIdx < nSize; nIdx++ ) {
				mCategories.add(aValues[nIdx]);
			}
		}
	}
	
	private static RecentCates getObject() {
		if( null == IcsonApplication.mRecentCates )
			IcsonApplication.mRecentCates = new RecentCates();
		
		return IcsonApplication.mRecentCates;
	}
	
	private List<String>  mCategories;
	
	private static final String SPERATOR = ",";
	private static final int MAX_SIZE = 5;
}
