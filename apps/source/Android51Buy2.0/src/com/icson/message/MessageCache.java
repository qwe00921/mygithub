package com.icson.message;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.icson.push.MsgEntity;

public class MessageCache {
	/**
	 * Default constructor of MessageCache
	 */
	public MessageCache(Context aContext) {
		mCache = new Vector<MsgEntity>(MAX_CACHE);
		mContext = new WeakReference<Context>(aContext);
		
		// Try to load cache from local storage.
	//	this.loadCache();
		
		// Reset the flag to false.
		mUpdated = false;
	}
	
	/**
	 * append a new item.
	 * @param aEntity
	 */
	void append(MsgEntity aEntity) {
		if( null == aEntity )
			return ;
		//zhiliu 让修正不根据id排重 2013-9-10
		/*
		for( MsgEntity pEntity : mCache ) {
			if( (pEntity == aEntity) || (aEntity.mId == pEntity.mId) )
				return ;
		}
		*/
		mCache.add(aEntity);
	}
	
	/**
	 * addEntity
	 * @param aEntity
	 */
	boolean addFirst(MsgEntity aEntity) {
		if( null == aEntity )
			return false;
		/*
		for( MsgEntity pEntity : mCache ) {
			if( (pEntity == aEntity) || (aEntity.mId == pEntity.mId) )
				return false;
		}
		*/
		final int nSize = mCache.size();
		if( nSize >= MAX_CACHE ) {
			mCache.remove(nSize - 1);
		}
		
		mCache.insertElementAt(aEntity, 0);
		mUpdated = true;
		
		return mUpdated;
	}
	
	/**
	 * get first element time tag
	 * @return
	 */
	public long getFirstTag() {
		final int nSize = (null != mCache ? mCache.size() : 0);
		MsgEntity pFirst = (nSize > 0 ? mCache.elementAt(0) : null);
		
		return (null != pFirst ? pFirst.mTimetag : 0) / 1000;
	}
	
	/**
	 * get last element time tag
	 * @return
	 */
	public long getLastTag() {
		final int nSize = (null != mCache ? mCache.size() : 0);
		MsgEntity pLast = (nSize > 0 ? mCache.elementAt(nSize - 1) : null);
		
		return (null != pLast ? pLast.mTimetag : 0) / 1000;
	}
	
	/**
	 * getCount
	 * @return
	 */
	public int getCount() {
		return (null != mCache ? mCache.size() : 0);
	}
	
	/**
	 * clear message recoreds
	 */
	public void clear() {
		if( null != mCache ) {
			mCache.clear();
			
			mUpdated = true;
		}
	}
	
	/**
	 * getEntity
	 * @param nPos
	 * @return
	 */
	public MsgEntity getEntity(int nPos) {
		final int nSize = (null != mCache ? mCache.size() : 0);
		if( 0 > nPos || nPos >= nSize )
			return null;
		
		return mCache.elementAt(nPos);
	}
	
	/**
	 * loadCache
	 * @throws IOException 
	 */
	@SuppressWarnings("unused")
	private void loadCache() {
		Context pContext = mContext.get();
		if( null == pContext )
			return ;
		
		// Get content.
		String strContent = null;
		FileInputStream pInputStream = null;
		try {
			pInputStream = pContext.openFileInput(CACHE_FILE);
			
			byte aBytes[] = new byte[pInputStream.available()];
			pInputStream.read(aBytes);
			
			strContent = new String(aBytes);
			
			// Parse the json object.
			JSONObject pRoot = new JSONObject(strContent);
			JSONArray aArray = pRoot.optJSONArray(JSON_TAG);
			final int nLength = (null != aArray ? aArray.length() : 0);
			for( int nIdx = 0; nIdx < nLength; nIdx++ ) {
				JSONObject pObject = aArray.getJSONObject(nIdx);
				MsgEntity pEntity = new MsgEntity();
				pEntity.parse(pObject);
				
				// Save to array.
				this.append(pEntity);
			}
			
		} catch (FileNotFoundException aException) {
			aException.printStackTrace();
			strContent = null;
		} catch (IOException aException) {
			aException.printStackTrace();
			strContent = null;
		} catch (JSONException aException) {
			aException.printStackTrace();
		} finally {
			if( null != pInputStream ) {
				try {
					pInputStream.close();
				} catch (IOException aException) {
					aException.printStackTrace();
				}
				pInputStream = null;
			}
		}
	}
	
	/**
	 * saveCache
	 * Save cache to local storage.
	 */
	public void saveCache() {
		if( !mUpdated ) {
			// Do nothing when records are not updated.
			return ;
		}
			
		final int nSize = (null != mCache ? mCache.size() : 0);
		if( 0 >= nSize )
			return ;
		
		// Save cache to local storage.
		Context pContext = mContext.get();
		if( null == pContext )
			return ;
		
		FileOutputStream pOutputStream = null;
		try {
			JSONObject pRoot = new JSONObject();
			JSONArray aArray = new JSONArray();
			pRoot.put(JSON_TAG, aArray);
			
			// Save the content.
			for( MsgEntity entity : mCache ){
				aArray.put(entity.toJson());
			}
			
			pOutputStream = pContext.openFileOutput(CACHE_FILE, Context.MODE_PRIVATE);
			pOutputStream.write(pRoot.toString().getBytes());
			
		} catch (FileNotFoundException aException) {
			aException.printStackTrace();
		} catch (IOException aException) {
			aException.printStackTrace();
		} catch (JSONException aException) {
			aException.printStackTrace();
		} finally {
			if ( null != pOutputStream ) {
				try {
					pOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				pOutputStream = null;
			}
		}
	}

	// Member instances.
	private Vector<MsgEntity>      mCache;
	private WeakReference<Context> mContext;
	private boolean                mUpdated;
	
	// Max cache count.
	private static final int    MAX_CACHE  = 64;
	private static final String JSON_TAG   = "cache";
	private static final String CACHE_FILE = "icson_msg.cache";
}
