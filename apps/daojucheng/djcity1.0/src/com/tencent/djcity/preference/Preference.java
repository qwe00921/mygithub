/**
 * Copyright (C) 2012 Tencent Inc.
 * All rights reserved, for internal usage only.
 * 
 * Project: SosoClient
 * FileName: Preference.java
 * 
 * Description: 
 * Author: lorenchen (lorenchen@tencent.com)
 * Created: Oct 30, 2012
 */
package com.tencent.djcity.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.djcity.more.GameInfo;
import com.tencent.djcity.util.IcsonApplication;

public final class Preference 
{
	// Public
	public static final int MODE_NOIMAGE  = 0;
	public static final int MODE_HASIMAGE = 1;
	
	
	public static final int NEED_ACCESS = 0;
	public static final int ACCESSED	= 1;
	public static final String  DEFAULT_INTERVAL = "10";

	/**
	* method Name:getInstance    
	* method Description:  
	* @param aContext
	* @return   
	* Preference  
	* @exception   
	* @since  1.0.0
	 */
	public synchronized static Preference getInstance()
	{
		if( null == mSelf )
		{
			mSelf = new Preference();
			mSelf.setContext(IcsonApplication.app);
		}
		
		return mSelf;
	}
	
	/**
	 * getImageMode
	 * @return
	 */
	public int getImageMode()
	{
		return this.getValue(PREF_IMAGE_MODE);
	}
	
	public void setConfigTag(String strTag) {
		this.setValue(PREF_CONFIG_TIMETAG, strTag);
	}
	
	public String getConfigTag() {
		return this.getString(PREF_CONFIG_TIMETAG);
	}
	
	/**
	 * Check whether push message is enabled.
	 * @return
	 */
	public boolean pushMessageEnabled() {
		return 1 == this.getValue(PREF_PUSH_MESSAGE);
	}
	
	public void setPushMessageEnabled(boolean bEnable) {
		this.setValue(PREF_PUSH_MESSAGE, bEnable ? 1 : 0);
	}
	
	/**
	 * setImageMode
	 * @param nMode
	 */
	public void setAnnounceID(int announce_id)
	{
		this.setValue(PREF_ANNOUNCE_ID, announce_id);
	}
	
	public int getAnnounceID()
	{
		return this.getValue(PREF_ANNOUNCE_ID);
	}
	
	/**
	 * setImageMode
	 * @param nMode
	 */
	public void setImageMode(int nMode)
	{
		this.setValue(PREF_IMAGE_MODE, nMode);
	}
	
	public void setPushInterval(int nMinutes) {
		if( nMinutes > 0 ) {
			this.setValue(PREF_PUSH_INTERVAL, nMinutes);
		}
	}
	
	public int getPushInterval() {
		return this.getValue(PREF_PUSH_INTERVAL);
	}

	/**
	 * 
	* method Name:getPortalVersion    
	* method Description:  
	* @return   
	* int  
	* @exception   
	* @since  1.0.0
	 */
	public String getPortalInfo()
	{
		return getString(PREF_PORTAL_INFO);
	}
	
	
	public void setPortalInfo(String aInfo)
	{
		this.setValue(PREF_PORTAL_INFO, aInfo);
	}
	
	
	
	public void setShakeRange(float aRange)
	{
		setValue(PREF_SHAKE_RANGE, aRange);
	}
	public float getShakeRange()
	{
		return getFloat(PREF_SHAKE_RANGE);
	}
	
	/**
	 * 
	* method Name:setProjVersion    
	* method Description:  
	* @param aVersion   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public void setProjVersion(int aVersion)
	{
		this.setValue(PREF_PROJ_VERSION, aVersion);
	}
	
	public int getProjVersion()
	{
		return getValue(PREF_PROJ_VERSION);
	}
	
	public void setFirstSightVersion(int idx, int aVersion)
	{
		String []items = getString(PREF_FIRST_SIGHT).split(":");
		if(null == items || idx < 0 || items.length <= idx)
		{
			items = DEFAULT_FIRST_SIGHT.split(":");
		}
		
		items[idx] = ""+aVersion;
		String aTmp = "";
		for(int i = 0; i < items.length;i++)
		{
			aTmp = aTmp + items[i] + ":";
		}
		aTmp = aTmp.substring(0,aTmp.length()-1);
		this.setValue(PREF_FIRST_SIGHT, aTmp);
	}
	
	public int getFirstSightVersion(int idx)
	{
		String []items = getString(PREF_FIRST_SIGHT).split(":");
		if(null == items || idx < 0 || items.length <= idx)
			return 0;
		return Integer.valueOf(items[idx]);
		
	}
	
	/**
	 * 
	* method Name:setQQAccount,setYiXunAccount    
	* method Description:  
	* @param aVersion   
	* void  
	* @exception   
	* @since  2.0.2
	 */
	public void setQQAccount(String account)
	{
		this.setValue(PREF_QQ_ACCOUNT, account);
	}
	public String getQQAccount()
	{
		return getString(PREF_QQ_ACCOUNT);
	}
	public void setLastUID(String last_uid)
	{
		this.setValue(PREF_LAST_UID, last_uid);
	}
	public long getLastUID()
	{
		String lastuid = getString(PREF_LAST_UID);
		long uid;
		uid = lastuid.equals("")? 0 : Long.valueOf(lastuid);
		return uid;
	}
	
	public void setBarcodeAccess(int accessValue)
	{
		this.setValue(PREF_BARCODE_ACESS, accessValue);
	}
	public Boolean needToBarcodeAccess()
	{
		return NEED_ACCESS == getValue(PREF_BARCODE_ACESS)? true : false;
	}
	
	public void setContactAccess(int accessValue)
	{
		this.setValue(PREF_CONTACT_ACESS, accessValue);
	}
	public Boolean needToContactAccess()
	{
		return NEED_ACCESS == getValue(PREF_CONTACT_ACESS)? true : false;
	}
	
	
	
	public void setMapAccess(int accessValue)
	{
		this.setValue(PREF_MAP_ACESS, accessValue);
	}
	public Boolean needToMapAccess()
	{
		return NEED_ACCESS == getValue(PREF_MAP_ACESS)? true : false;
	}
	
	public void savePermission() {
		setValue(PREF_PERMISSION_NEEDASK,0);
	}
	
	public boolean permissionNeedAsk() {
		int a = getValue(PREF_PERMISSION_NEEDASK);
		return  (getValue(PREF_PERMISSION_NEEDASK) > 0);
	}	
	
	public void setCallAccess(int accessValue)
	{
		this.setValue(PREF_CALL_ACESS, accessValue);
	}
	public Boolean needCallAccess()
	{
		return NEED_ACCESS == getValue(PREF_CALL_ACESS)? true : false;
	}
	
	/**
	 * @param info
	 */
	public void setGameInfo(GameInfo info) {
		if(info == null) {
			return;
		}
		setValue(KEY_BIZ_IMG, info.getBizImg());
		setValue(KEY_ROLE_FLAG, info.getRoleFlag());
		setValue(KEY_AREA_ID, info.getAreaId());
		setValue(KEY_AREA_NAME, info.getAreaName());
		setValue(KEY_BIZ_CODE, info.getBizCode());
		setValue(KEY_BIZ_NAME, info.getBizName());
		setValue(KEY_ROLE_ID, info.getRoleId());
		setValue(KEY_ROLE_LEVEL, info.getRoleLevel());
		setValue(KEY_ROLE_NAME, info.getRoleName());
		setValue(KEY_SERVER_ID, info.getServerId());
		setValue(KEY_SERVER_NAME, info.getServerName());
		setValue(KEY_AREA_LEVEL, info.getAreaLevel());
	}
	
	public void clearGameInfo()
	{
		this.setValue(KEY_AREA_ID, 0);
		this.setValue(KEY_AREA_NAME, "");
		this.setValue(KEY_BIZ_CODE, "");
		this.setValue(KEY_BIZ_NAME, "");
		this.setValue(KEY_ROLE_ID, "");
		this.setValue(KEY_ROLE_LEVEL, 0);
		this.setValue(KEY_ROLE_NAME, "");
		this.setValue(KEY_SERVER_ID, 0);
		this.setValue(KEY_SERVER_NAME, "");
		
		this.setValue(KEY_ROLE_FLAG, 0);
		this.setValue(KEY_AREA_LEVEL, 0);
		this.setValue(KEY_BIZ_IMG, "");
	}
	
	public GameInfo getGameInfo() {
		int areaId = getValue(KEY_AREA_ID);
		String areaName = getString(KEY_AREA_NAME);
		String bizCode = getString(KEY_BIZ_CODE);
		String bizname = getString(KEY_BIZ_NAME);
		String roleId = getString(KEY_ROLE_ID);
		int roleLevel = getValue(KEY_ROLE_LEVEL);
		String roleName = getString(KEY_ROLE_NAME);
		int serverId = getValue(KEY_SERVER_ID);
		String serverName = getString(KEY_SERVER_NAME);
		int roleFlag = getValue(KEY_ROLE_FLAG);
		int areaLevel = getValue(KEY_AREA_LEVEL);
		String bizImg = getString(KEY_BIZ_IMG);
		
//		if("".equals(areaName) && areaId == 0 && "".equals(serverName) && serverId == 0
//				&& "".equals(roleName) && roleLevel == 0) {
//			return null;
//		}

		GameInfo info = new GameInfo();
		info.setAreaId(areaId);
		info.setAreaName(areaName);
		info.setBizCode(bizCode);
		info.setBizName(bizname);
		info.setRoleId(roleId);
		info.setRoleLevel(roleLevel);
		info.setRoleName(roleName);
		info.setServerId(serverId);
		info.setServerName(serverName);
		info.setRoleFlag(roleFlag);
		info.setAreaLevel(areaLevel);
		info.setBizImg(bizImg);
		
		return info;
	}
	
	/**
	* method Name:restoreDefault    
	* method Description:  
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public void restoreDefault()
	{
		if ( null == mValMap )
			return ;
		
		for(int nIndex = 0; nIndex < MUTABLE_SIZE; nIndex++)
		{
			mValMap.setValue(PROPERTIES[nIndex], DEFAULTS[nIndex]);
		}
	}
	
	/**
	 * savePreference
	 * Save preference
	 */
	public void savePreference()
	{
		if ( null != mPreferences )
		{
			// Save the items to local preference.
			final int nSize = (null != mValMap ? mValMap.size() : 0);
			if ( nSize > 0 )
			{
				SharedPreferences.Editor pEditor = mPreferences.edit();
				
				for ( int nIdx = 0; nIdx < nSize; nIdx++ )
				{
					ValueMap.Element pElement = mValMap.elementAt(nIdx);
					
					if( null != pElement )
					{
						// Save to local storage.
						pEditor.putString(pElement.getKey(), pElement.getValue());
					}
				}
				
				// Commit it.
				pEditor.commit();
			}
		}
	}
	
	private void setValue(String aKey, String strVal) {
		if( null == mValMap)
			return ;
		
		if(strVal == null) {
			strVal = "";
		}
		mValMap.setValue(aKey, strVal);
	}
	
	private String getString(String aKey) {
		if(aKey == null) {
			aKey = "";
		}
		ValueMap.Element pElement = (null != mValMap ? mValMap.getElement(aKey) : null);
		
		return null != pElement ? pElement.getValue() : "";
	}
	
	/**
	* method Name:setValue    
	* method Description:  
	* @param aKey
	* @param bValue   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void setValue(String aKey, int nValue)
	{
		if ( null == mValMap )
			return ;
		
		mValMap.setValue(aKey, "" + nValue);
	}
	private void setValue(String aKey, float nValue)
	{
		if ( null == mValMap )
			return ;
		
		mValMap.setValue(aKey, "" + nValue);
	}

	/**
	* method Name:getValue    
	* method Description:  
	* @param aKey
	* @return   
	* boolean  
	* @exception   
	* @since  1.0.0
	 */
	private int getValue(String aKey)
	{
		ValueMap.Element pElement = (null != mValMap ? mValMap.getElement(aKey) : null);
		if ( null == pElement )
			return 0;
		
		return Integer.valueOf(pElement.getValue());
	}
	private float getFloat(String aKey)
	{
		ValueMap.Element pElement = (null != mValMap ? mValMap.getElement(aKey) : null);
		if ( null == pElement )
			return 0.0f;
		
		return Float.valueOf(pElement.getValue());
	}

	/**
	* Create a new Instance Preference.  
	*
	 */
	private Preference()
	{
		initialize();
	}
	
	/**
	* method Name:initialize    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void initialize()
	{
		mPreferences = null;
		mValMap = null;
	}
	
	/**
	* method Name:setContext    
	* method Description:  
	* @param aContext   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void setContext(Context aContext)
	{
		if ( null != aContext )
		{	
			// Load the preference.
			mPreferences = null;
			mPreferences = aContext.getSharedPreferences(PREF_NAME, 0);
			loadPref();
		}
	}
	
	/**
	* method Name:loadPref    
	* method Description:     
	* void  
	* @exception   
	* @since  1.0.0
	 */
	private void loadPref()
	{
		if ( (null == mPreferences) || (null != mValMap) )
			return ;
		
		mValMap = new ValueMap();
		final int nCount = PROPERTIES.length;
		for ( int nIdx = 0; nIdx < nCount; nIdx++ )
		{
			// Get the value from local storage.
			String strVal = mPreferences.getString(PROPERTIES[nIdx], DEFAULTS[nIdx]);
			
			// Save to memory.
			mValMap.addValue(PROPERTIES[nIdx], strVal);
		}
	}
	
	// Member instance.
	private static Preference mSelf = null;
	private SharedPreferences mPreferences;
	private ValueMap          mValMap;
	
	public  static final String PREF_PERMISSION_NEEDASK   	 = "PERMISSION_NEEDASK";
	
	// Preference key.
	public  static final String PREF_NAME            = "ICSON_PREF";
	public  static final String PREF_IMAGE_MODE      = "IMAGE_MODE";
	public  static final String PREF_ANNOUNCE_ID     = "ANNOUNCE_ID";
	public  static final String PREF_PUSH_MESSAGE    = "PUSH_MESSAGE";
	public  static final String PREF_CONFIG_TIMETAG  = "CONFIG_TIMETAG";
	public  static final String PREF_PUSH_INTERVAL   = "PUSH_INTERVAL";
	public  static final String PREF_QQ_ACCOUNT  	 = "QQ_ACCOUNT";
	public  static final String PREF_LAST_UID  		 = "LAST_UID";
	public  static final String PREF_BARCODE_ACESS   = "BARCODE_ACESS";
	public  static final String PREF_CONTACT_ACESS   = "CONTACT_ACESS";
	public  static final String PREF_MAP_ACESS    	 = "MAP_ACESS";
	public  static final String PREF_CALL_ACESS    	 = "CALL_ACESS";
	
	// Game Info
	public static final String KEY_BIZ_IMG = GameInfo.KEY_BIZ_IMG;
	public static final String KEY_ROLE_COUNTRY = GameInfo.KEY_ROLE_COUNTRY;
	public static final String KEY_ROLE_FLAG = GameInfo.KEY_ROLE_FLAG;
	public static final String KEY_ROLE_GENDER = GameInfo.KEY_ROLE_GENDER;
	public static final String KEY_ROLE_ID = GameInfo.KEY_ROLE_ID;
	public static final String KEY_ROLE_LEVEL = GameInfo.KEY_ROLE_LEVEL;
	public static final String KEY_ROLE_NAME = GameInfo.KEY_ROLE_NAME;
	public static final String KEY_BIZ_NAME = GameInfo.KEY_BIZ_NAME;
	public static final String KEY_BIZ_CODE = GameInfo.KEY_BIZ_CODE;
	public static final String KEY_SERVER_ID = GameInfo.KEY_SERVER_ID;
	public static final String KEY_SERVER_NAME = GameInfo.KEY_SERVER_NAME;
	public static final String KEY_AREA_ID = GameInfo.KEY_AREA_ID;
	public static final String KEY_AREA_NAME = GameInfo.KEY_AREA_NAME;
	public static final String KEY_AREA_LEVEL = GameInfo.KEY_AREA_LEVEL;
	
	//Portal info
	public  static final String PREF_PORTAL_INFO  = "PORTAL_INFO";
	
	//Slotmachine
	public  static final String PREF_SHAKE_RANGE = "SHAKE_RANGE";
	
	//Project_version code
	public  static final String PREF_PROJ_VERSION  = "PROJ_VERSION";
	
	//first_sight
	public  static final String PREF_FIRST_SIGHT  = "FIRST_SIGHT";
	public  static final String DEFAULT_FIRST_SIGHT  = "0:0:0:0";
	public  static final int    FIRST_SIGHT_SLOT = 0;
	public  static final int    FIRST_SIGHT_QIANG = 1;
	public  static final int    FIRST_SIGHT_FILTER = 2;
	public  static final int    FIRST_SIGHT_HOTLIST = 3;
	
	
	private static final String[] PROPERTIES = {
												PREF_IMAGE_MODE	, 
												PREF_ANNOUNCE_ID,
												PREF_PUSH_MESSAGE,
												PREF_CONFIG_TIMETAG,
												PREF_PORTAL_INFO,
												
												PREF_PUSH_INTERVAL,
												PREF_PROJ_VERSION,
												PREF_SHAKE_RANGE,
												PREF_QQ_ACCOUNT,
												PREF_PERMISSION_NEEDASK,
												
												PREF_LAST_UID,
												PREF_BARCODE_ACESS,
												PREF_CONTACT_ACESS,
												PREF_MAP_ACESS,
												PREF_CALL_ACESS,
												
												KEY_ROLE_COUNTRY,
												KEY_ROLE_FLAG,
												KEY_ROLE_GENDER,
												KEY_ROLE_ID,
												KEY_ROLE_LEVEL,
												KEY_ROLE_NAME,
												KEY_BIZ_NAME,
												KEY_BIZ_CODE,
												KEY_SERVER_ID,
												KEY_SERVER_NAME,
												KEY_AREA_ID,
												KEY_AREA_NAME,
												KEY_AREA_LEVEL,
												KEY_BIZ_IMG
												};
	// Default values for the preference.
	private static final int      MUTABLE_SIZE = 0;

	private static final String[] DEFAULTS = {
		"1", "0", "1","","",
		DEFAULT_INTERVAL,"0","0", "", "1", 
		"", "0", "0", "0", "0", 
		"", "0", "", 
		"", "0", "", 
		"", "", "0", 
		"", "0", "",
		"1", ""};
}
