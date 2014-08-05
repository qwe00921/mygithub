package com.icson.more;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.IPageCache;
import com.icson.statistics.StatisticsUtils;
import com.icson.util.AjaxUtil;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Parser;
import com.icson.util.ajax.Response;

public class AppInfoActivity extends BaseActivity implements OnSuccessListener<Object>
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.appinfo_activity);
		loadNavBar(R.id.appinfo_navbar);
		mListView = (ListView)findViewById(R.id.app_info_list);

		mListInfo = null;
		mParser = new AppInfoParser();
		
		// Initialize the instance.
		this.loadAppInfo();
	}
	
	@Override
	protected void onDestroy()
	{
		if( null != mListInfo )
		{
			mListInfo.cleanup();
			mListInfo = null;
		}
		
		if( null != mAdapter )
		{
			mAdapter.notifyDataSetChanged();
			mAdapter.cleanup();
			mAdapter = null;
		}
		
		super.onDestroy();
	}
	
	@Override
	public void onSuccess(Object aObject, Response aResponse) 
	{
		closeLoadingLayer();
		if( null == aObject )
			return ;
		
		mListInfo = (AppListInfo)aObject;
		if( null != mListInfo )
		{
			if( (0 == mListInfo.mErrCode) && (mListInfo.size() > 0) )
			{	
				this.updateUi(mListInfo);
			}
			else
			{
				// Show error message.
				String strErrMsg = TextUtils.isEmpty(mListInfo.mErrMsg) ? getString(R.string.network_error) : mListInfo.mErrMsg;
				onError(this.getAjax(0), aResponse, strErrMsg);
			}
		}
	}
	
	@Override
	public void onError(Ajax aAjax, Response aResponse)
	{
		closeLoadingLayer();
		super.onError(aAjax, aResponse);
	}
	
	/**
	 * updateUi
	 * @param aListInfo
	 */
	private void updateUi(final AppListInfo aListInfo)
	{
		if( null == aListInfo )
			return ;
					
		if( null == mAdapter )
		{
			// Initialize the header part
			LayoutInflater pInflater = LayoutInflater.from(this);
			final View pHeader = pInflater.inflate(R.layout.appinfo_header, null);
			mListView.addHeaderView(pHeader);
			
			// Initialize the information for adapter.
			mAdapter = new AppInfoAdapter(this, aListInfo);
			mListView.setAdapter(mAdapter);
			mListView.setOnItemClickListener(new OnItemClickListener() 
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
				{
					String strWapUrl = "";
					strWapUrl = aListInfo.mTargetUrl;
					position -= 1;

					if( position >= 0 )
					{
						// Get item.
						AppInfo pEntity = (AppInfo)mAdapter.getItem(position);
						if( null != pEntity ) 
						{
							strWapUrl = pEntity.mWapUrl;
						}
					}
					
					// Check target url.
					if( !TextUtils.isEmpty(strWapUrl) )
					{
						if( !strWapUrl.startsWith("http://") )
						{
							strWapUrl = "http://" + strWapUrl;
						}
						
						// Start the action view.
						Intent pIntent = new Intent(Intent.ACTION_VIEW);
						pIntent.setData(Uri.parse(strWapUrl));
						startActivity(pIntent);
					}
				}
			});
			
			if( !TextUtils.isEmpty(aListInfo.mAdvertiseUrl) )
			{
				AjaxUtil.getLocalImage(this, aListInfo.mAdvertiseUrl, new ImageLoadListener()
				{
					@Override
					public void onLoaded(Bitmap image, String url) 
					{
						((ImageView) pHeader.findViewById(R.id.appinfo_header)).setImageBitmap(image);
						pHeader.setVisibility(View.VISIBLE);
					}

					@Override
					public void onError(String strUrl) {
					}
				});
			}
		}
		else
		{
			// Update data.
			mAdapter.setAppInfo(aListInfo);
			mAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * loadAppInfo
	 */
	private void loadAppInfo()
	{
		final int nSize = (null != mListInfo ? mListInfo.size() : 0);
		if( nSize > 0 )
			return ;
		
		// Check the page cache first.
		if( !checkCache() )
		{
			// Send request for recommend application list.
			showLoadingLayer(true);
			
			// Send request for loading app list.
			Ajax pAjax = ServiceConfig.getAjax(Config.URL_RECOMMEND_LOADLIST);
			if( null == pAjax )
				return ;
			pAjax.setData("tokenid", StatisticsUtils.getDeviceUid(this));
			pAjax.setParser(mParser);
			pAjax.setOnSuccessListener(this);
			pAjax.setOnErrorListener(this);
			addAjax(pAjax);
			pAjax.send();
		}
	}
	
	private boolean checkCache()
	{
		IPageCache pPageCache = new IPageCache();
		String strJson = pPageCache.get(KEY_APPINFO_CACHE);
		pPageCache = null;
		if( (TextUtils.isEmpty(strJson)) || (null == mParser) )
			return false;
		
		// Parse the information.
		boolean bSuccess = true;
		try 
		{
			mListInfo = mParser.parse(strJson, false);
		}
		catch (Exception aException) 
		{
			if( null != mListInfo )
			{
				mListInfo.cleanup();
				mListInfo = null;
			}
			
			aException.printStackTrace();
			bSuccess = false;
		}
		
		if( bSuccess )
		{
			// Update Ui.
			this.updateUi(mListInfo);
		}
		
		return bSuccess;
	}
	
	// Load information.
	private AppListInfo     mListInfo;
	private AppInfoParser   mParser;
	private ListView        mListView;
	private AppInfoAdapter  mAdapter;

	// Constants for AppInfo
	static final String     KEY_APPINFO_CACHE = "APP_INFO_CACHE";
	static final long       CACHE_EXPIRE_TIME = 60 * 60; // Cache for 1 hours. 
	
	// Implementation for 3rd-party application connection.
	private class AppInfo
	{
		public String mLabel;
		public String mIconUrl;
		public String mDesc;
		public String mWapUrl;
	}
	
	private class AppInfoParser extends Parser<byte[], AppListInfo>
	{
		@Override
		public AppListInfo parse(byte[] aBytes, String strCharset) throws Exception 
		{
			JSONParser parser = new JSONParser();
			parser.parse(aBytes, strCharset);
			
			return this.parse(parser.getString(), true);
		}
		
		/**
		 * parse
		 * @param strJson
		 * @return
		 * @throws Exception
		 */
		public AppListInfo parse(String strJson, boolean bFromNetwork) throws Exception
		{
			if( TextUtils.isEmpty(strJson) )
				return null;
			
			JSONObject pObject = new JSONObject(strJson);
			
			// Create a new instance for AppListInfo
			AppListInfo pListInfo = new AppListInfo();
			
			// Get error code.
			final int nErrCode = pObject.optInt("errno");
			if( 0 != nErrCode )
			{
				// Retrieve the message.
				String strErrMsg = pObject.optString("message");
				
				// Save the error code and message.
				pListInfo.mErrCode = nErrCode;
				pListInfo.mErrMsg = strErrMsg;
			}
			else
			{
				// Parse the properties.
				JSONObject pData = pObject.getJSONObject("data");
				pListInfo.parse(pData);
				
				if( bFromNetwork )
				{
					// Save the content to local if content is correct.
					IPageCache pPageCache = new IPageCache();
					pPageCache.set(KEY_APPINFO_CACHE, strJson, CACHE_EXPIRE_TIME);
					pPageCache = null;
				}
			}
			
			mIsSuccess = true;
			
			return pListInfo;
		}
	}
	
	private class AppListInfo
	{
		/**
		 * size
		 * @return
		 */
		public int size()
		{
			return (null != mList ? mList.size() : 0);
		}
		
		/**
		 * getInfo
		 * @param nPos
		 * @return
		 */
		public AppInfo getInfo(int nPos)
		{
			return (null != mList ? mList.get(nPos) : null);
		}
		
		public void cleanup()
		{
			if( null != mList )
			{
				mList.clear();
				mList = null;
			}
		}
		
		/**
		 * parse
		 * @param aObject
		 * @throws JSONException 
		 */
		public boolean parse(JSONObject aObject) throws JSONException
		{
			if( null == aObject )
				return false;
			
			// Parse the object.
			mAdvertiseUrl = aObject.optString("advertise_url");
			mTargetUrl = aObject.optString("target_url");
			
			JSONArray aArray = aObject.optJSONArray("list");
			final int nSize = (null != aArray ? aArray.length() : 0);
			if( nSize > 0)
			{
				if( null == mList )
				{
					mList = new ArrayList<AppInfo>();
				}
				
				for( int nIdx = 0; nIdx < nSize; nIdx++ )
				{
					JSONObject pChild = aArray.getJSONObject(nIdx);
					if( null != pChild )
					{
						AppInfo pEntity = new AppInfo();
						pEntity.mLabel = pChild.optString("label");
						pEntity.mIconUrl = pChild.optString("icon");
						pEntity.mDesc = pChild.optString("desc");
						pEntity.mWapUrl = pChild.optString("url");
						
						// Save to array.
						mList.add(pEntity);
					}
				}
			}
			
			return true;
		}
		
		List<AppInfo>  mList = null;
		String         mAdvertiseUrl;
		String         mTargetUrl;
		String         mErrMsg;
		int            mErrCode;
	}
	
	private class AppInfoHolder
	{
		ImageView mIcon;
		TextView  mLabel;
		TextView  mDesc;
	}
	
	private final class AppInfoAdapter extends BaseAdapter implements ImageLoadListener
	{
		/**
		 * Default constructor of AppInfoAdapter
		 * @param aAppListInfo
		 */
		public AppInfoAdapter(Context aContext, AppListInfo aAppListInfo)
		{
			mInfoRef = new WeakReference<AppListInfo>(aAppListInfo);
			mInflater = LayoutInflater.from(aContext);
			mImageLoader = new ImageLoader(aContext, "app_icon_cache", false, true);
		}
		
		public void setAppInfo(AppListInfo aAppListInfo)
		{
			if( null != mInfoRef )
			{
				mInfoRef.clear();
				mInfoRef = null;
			}
			
			mInfoRef = new WeakReference<AppListInfo>(aAppListInfo);
		}
		
		/**
		 * cleanup
		 */
		public void cleanup()
		{
			if( null != mImageLoader )
			{
				mImageLoader.cleanup();
				mImageLoader = null;
			}
		}

		@Override
		public int getCount() 
		{
			AppListInfo pInfo = (null != mInfoRef ? mInfoRef.get() : null);
			return (null != pInfo ? pInfo.size() : 0);
		}

		@Override
		public Object getItem(int position)
		{
			AppListInfo pInfo = (null != mInfoRef ? mInfoRef.get() : null);
			return (null != pInfo ? pInfo.getInfo(position) : null);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			AppInfoHolder pHolder = null;
			if (convertView == null)
			{
				convertView = mInflater.inflate(R.layout.appinfo_item, null);
				pHolder = new AppInfoHolder();
				pHolder.mIcon = (ImageView) convertView.findViewById(R.id.app_icon);
				pHolder.mLabel = (TextView) convertView.findViewById(R.id.app_label);
				pHolder.mDesc = (TextView) convertView.findViewById(R.id.app_desc);
				
				convertView.setTag(pHolder);
			}
			else
			{
				pHolder = (AppInfoHolder) convertView.getTag();
			}
			
			// Get item.
			AppInfo pInfo = (AppInfo)this.getItem(position);
			
			Bitmap pBitmap = mImageLoader.get(pInfo.mIconUrl);
			if( null != pBitmap )
			{
				pHolder.mIcon.setImageBitmap(pBitmap);
			}
			else
			{
				pHolder.mIcon.setImageResource(mImageLoader.getLoadingId());
				mImageLoader.get(pInfo.mIconUrl, this);
			}
			pHolder.mLabel.setText(pInfo.mLabel);
			pHolder.mDesc.setText(pInfo.mDesc);
			
			return convertView;
		}
		
		@Override
		public void onLoaded(Bitmap aBitmap, String strUrl) 
		{
			notifyDataSetChanged();
		}

		@Override
		public void onError(String strUrl) 
		{
		}
		
		private WeakReference<AppListInfo> mInfoRef;
		private LayoutInflater             mInflater;
		private ImageLoader                mImageLoader;
	}
	
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_AppInfoActivity);
	}
}
