package com.icson.event;

import java.util.ArrayList;

import com.icson.R;
import com.icson.home.ModuleInfo;
import com.icson.lib.model.BaseModel;

public class TimeBuyModel extends BaseModel
{
	/**
	 * default constructor of TimeLimitModel
	 */
	public TimeBuyModel() 
	{
	}
	
	public long getCurrentTimetag()
	{
		return mCurrent;
	}
	
	public void setCurrentTimetag(long nCurrent)
	{
		mCurrent = nCurrent;
	}
	
	public long getStartTimetag()
	{
		return mStart;
	}
	
	public void setStartTimetag(long nStart)
	{
		mStart = nStart;
	}
	
	public long getFinishTimetag()
	{
		return mFinish;
	}
	
	public void setFinishTimetag(long nFinish)
	{
		mFinish = nFinish;
	}
	
	public void setPageNum(int nPageNum)
	{
		mPageNum = nPageNum;
	}
	
	public int getPageNum()
	{
		return mPageNum;
	}
	
	public void setPageSize(int nPageSize)
	{
		mPageSize = nPageSize;
	}
	
	public int getPageSize()
	{
		return mPageSize;
	}
	
	public void setPageCount(int nPageCount)
	{
		mPageCount = nPageCount;
	}
	
	public int getPageCount()
	{
		return mPageCount;
	}
	
	/**
	 * getBackground
	 * @return
	 */
	public int getBackground()
	{
		return mBackground;
	}
	
	/**
	 * setBackground
	 * @param nBackground
	 */
	public void setBackground(int nBackground)
	{
		mBackground = nBackground;
	}
	
	public void setType(int nType) {
		mType = nType;
	}
	
	public int getType() {
		return mType;
	}
	
	public void setPriceColor(int nColor) {
		mPriceColor = nColor;
	}
	
	public int getPriceColor() {
		return mPriceColor;
	}
	
	public void setStatus(String strStatus) {
		mStatus = strStatus;
	}
	
	public String getStatus() {
		return mStatus;
	}
	
	/**
	 * getAdvertiseUrl
	 * @return
	 */
	public String getAdvertiseUrl()
	{
		return mAdvertiseUrl;
	}
	
	/**
	 * setAdvertiseUrl
	 * @param strUrl
	 */
	public void setAdvertiseUrl(String strUrl)
	{
		mAdvertiseUrl = strUrl;
	}
	
	/**
	 * getListUrl
	 * @return
	 */
	public String getListUrl()
	{
		return mListUrl;
	}
	
	/**
	 * setListUrl
	 * @param strUrl
	 */
	public void setListUrl(String strUrl)
	{
		mListUrl = strUrl;
	}
	
	/**
	 * setProducts
	 * @param aProducts
	 */
	public void setProducts(ArrayList<TimeBuyEntity> aProducts)
	{
		mProducts = aProducts;
	}
	
	/**
	 * getProducts
	 * @return
	 */
	public ArrayList<TimeBuyEntity> getProducts()
	{
		return mProducts;
	}
	
	public static int getName(int nType) {
		switch( nType ) {
		case ModuleInfo.MODULE_ID_MORNING:
			return R.string.time_buy_morning;
		case ModuleInfo.MODULE_ID_BLACK:
			return R.string.time_buy_black;
		case ModuleInfo.MODULE_ID_WEEKEND:
			return R.string.time_buy_weekend;
		default:
			return 0;
		}
	}
	
	public static int getPageId(int nType) {
		switch( nType) {
		case ModuleInfo.MODULE_ID_MORNING:
			return R.string.tag_EventMorningActivity;
		case ModuleInfo.MODULE_ID_BLACK:
			return R.string.tag_EventThhActivity;
		case ModuleInfo.MODULE_ID_WEEKEND:
			return R.string.tag_EventWeekendActivity;
		default:
			return 0;
		}
	}
	
	private long   mCurrent;
	private long   mStart;
	private long   mFinish;
	private int    mBackground;
	private int    mPriceColor;
	private int    mPageNum;
	private int    mPageSize;
	private int    mPageCount;
	private int    mType;
	private String mAdvertiseUrl;
	private String mListUrl;
	private String mStatus;
	private ArrayList<TimeBuyEntity> mProducts;
	
	public static final String TIMEBUY_TYPE   = "type_timebuy";
}
