/**
 * Copyright (C) 2013 Tencent Inc. 
 * All rights reserved, for internal usage only.
 * 
 * Project: 51Buy2.0 
 * FileName: AddressRadioDialog.java
 * 
 * Description: 
 * Author: qingliang (qingliang@tencent.com) 
 * Created: 2012-3-8
 */
package com.icson.lib.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.icson.R;

public class AddressRadioDialog extends AppDialog implements android.widget.AdapterView.OnItemClickListener
{
	
	/**  
	* Create a new Instance AppRadioDialog.  
	*  
	* @param aContext
	* @param aListener  
	*/
	public AddressRadioDialog(Context aContext, AppDialog.OnClickListener aListener, AddressRadioAdapter aAdapter, boolean isShowArrow)
	{
		super(aContext, aListener, R.style.AddressDialog);
		mAdapter = (null == aAdapter ? new AddressRadioAdapter(aContext, isShowArrow) : aAdapter);
	}
	
	
	/**
	* Class Name:OnRadioSelectListener 
	* Class Description: 
	*
	 */
	public interface OnAddressRadioSelectListener
	{
		/**
		 * onDialogClick
		 * @param nButtonId
		 */
		public abstract void onRadioItemClick(int which);
	}
	
	/**
	 * 
	* method Name:setRadioSelectListener    
	* method Description:  
	* @param aListener
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public void setAddressRadioSelectListener(OnAddressRadioSelectListener aListener)
	{
		mRadioListener = aListener;
	}
	
	/**
	 * setSelection
	 * @param nPosition
	 */
	public void setSelection(final int nPosition)
	{
		final int nCount = (null != mListView ? mListView.getCount() : 0);
		if ( (nPosition >= 0) && (nPosition < nCount) )
		{
			mAdapter.setPickIdx(nPosition);
			mAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * setProperty
	 * @param strCaption
	 * @param strMessage
	 * @param nPostiveBtnTextId
	 * @param nNegativeBtnTextId
	 */
	public void setProperty(String strCaption)
	{
		(mCaption = getComponent(mCaption)).mText = strCaption;
	}
	
	/**
	 * setProperty
	 * @param nCaptionId
	 * @param nMessageId
	 * @param nPostiveBtnTextId
	 * @param nNegativeBtnTextId
	 */
	public void setProperty(int nCaptionId)
	{
		Context pContext = getContext();
		String strCaption = pContext.getString(nCaptionId);
		
		setProperty(strCaption);
		strCaption = null;
	}
	
	/**
	 * 
	* method Name:setList    
	* method Description:  
	* @param aList   
	* void  
	* @exception   
	* @since  1.0.0
	 */
	public void setList(List<String> aList, int nCheckedItem)
	{
		if( null != mAdapter )
			mAdapter.setList(aList, nCheckedItem);
	}
	
	public void setList(String[] aOptions, int nCheckedItem) {
		mSelectId = nCheckedItem;
		if( null != mAdapter ) {
			mAdapter.setList(aOptions, nCheckedItem);
		}
	}
	
	/**
	 * onCreate
	 */
	@Override
	protected void onCreate(Bundle aSavedInstanceState)
	{
		super.onCreate(aSavedInstanceState);
		
		// Load the default configuration.
		setContentView(R.layout.dialog_radio);
		
		(mCaption = getComponent(mCaption)).mView = (TextView)findViewById(R.id.radio_dialog_caption);
		mListView = (ListView)findViewById(R.id.radio_list);
		mListView.setOnItemClickListener(this);
		if(null == mAdapter)
			return;
		
		mListView.setAdapter(mAdapter);	
		mListView.setSelection(mSelectId);
	
		this.updateUi();
	}
	
	
	/**
	 * updateUi
	 * Update the UI configuration.
	 */
	protected void updateUi()
	{
		Component aComponents[] = {mCaption};//, mMessage, mPositive, mNegative};
		for ( int nIdx = 0; nIdx < aComponents.length; nIdx++ )
		{
			Component pComponent = aComponents[nIdx];
			pComponent.mView.setText(pComponent.mText);
		}
		
		super.setAttributes();
	}
	
	/*  
	 * Description:
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		//change view
		mAdapter.setPickIdx(pos);
		mAdapter.notifyDataSetChanged();
		//notify out
		mRadioListener.onRadioItemClick(pos);
//		this.dismiss();
	}
	
	// Member instances.
	private  ListView   mListView;
	private  int		mSelectId = 0;
	private  AddressRadioAdapter mAdapter;
	private	 OnAddressRadioSelectListener  mRadioListener;
	
	/**
	 * Implementation of RadioAdapter
	 * @author qingliang
	 */
	public static class AddressRadioAdapter extends BaseAdapter
	{
		protected List<String> mList;
		protected Context      mContext;
		protected int          mPickIdx;
		protected boolean	   mIsShowArrow;
			
		/**
		 * 
		* Create a new Instance ChapterAdapter.  
		*
		*/
		public AddressRadioAdapter(Context aContext, boolean isShowArrow)
		{
			mList = new ArrayList<String>();
			mContext = aContext;
			mPickIdx = 0;
			mIsShowArrow = isShowArrow;
		}
			
		/**
		 * 
		* method Name:setList    
		* method Description:  
		* @param aList   
		* void  
		* @exception   
		* @since  1.0.0
		 */
		public void setList(List<String> aList, int nCheckedItem)
		{
			mList.clear();
			if(null!=aList)
				mList.addAll(aList);
			
			mPickIdx = nCheckedItem;
		}
		
		public void setList(String[] aOptions, int nCheckedItem) {
			mList.clear();
			final int nLength = (null != aOptions ? aOptions.length : 0);
			for( int nIdx = 0; nIdx < nLength; nIdx++ ) {
				mList.add(aOptions[nIdx]);
			}
			
			mPickIdx = nCheckedItem;
		}
		
		/**
		 * 
		* method Name:setPickIdx    
		* method Description:  
		* @param aIndex   
		* void  
		* @exception   
		* @since  1.0.0
		 */
		public void setPickIdx(int aIndex)
		{
			mPickIdx = aIndex;
		}
		/*  
		 * Description:
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return (null==mList) ? 0 : mList.size();
		}

		/*  
		 * Description:
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return (null==mList) ? null : mList.get(position);
		}

		/*  
		 * Description:
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}

		/*  
		 * Description:
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemHolder holder = null;
			if (null == convertView)
			{
				convertView = View.inflate(mContext, R.layout.address_radio_item, null);
				holder = new ItemHolder();
				holder.mArrow = (ImageView) convertView.findViewById(R.id.address_radio_item_arrow);
				holder.mName = (TextView) convertView.findViewById(R.id.address_radio_item_name);
				holder.mLine = (View) convertView.findViewById(R.id.address_radio_item_line);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ItemHolder) convertView.getTag();
			}
			
			// set data
			String strName = mList.get(position);
			holder.mName.setText(strName);
			holder.mName.setTextColor(mContext.getResources().getColor(mPickIdx == position ? R.color.filter_item_checked : R.color.global_text_color ));
			holder.mArrow.setVisibility(mIsShowArrow ? View.VISIBLE : View.INVISIBLE);
			
			if( position == (mList.size() - 1 ) ) {
				holder.mLine.setVisibility(View.INVISIBLE);
			}else{
				holder.mLine.setVisibility(View.VISIBLE);
			}
			
			return convertView;
		}
		
		private class ItemHolder
		{
			ImageView mArrow;
			TextView  mName;
			View mLine;
		}
	}
}
