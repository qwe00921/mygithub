package com.icson.statistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.icson.util.Config;
import com.icson.util.ToolUtil;

public class StorageHelper
{
	/**
	 * Default constructor of StorageHelper
	 * @param aContext
	 */
	StorageHelper(Context aContext) 
	{
		initialize(aContext);
	}
	
	/**
	 * dumpEntities
	 * @return
	 */
	synchronized boolean dumpEntities(Vector<RecordEntity> aEntities, int nPriority, StatisticsConfig aConfig)
	{
		final int nSize = (null != aEntities ? aEntities.size() : 0);
		if( (0 >= nSize) || (null == aConfig) )
			return false;
		
		StringBuffer pBuffer = new StringBuffer();
		for( RecordEntity pEnitiy : aEntities )
		{
			pBuffer.append(pEnitiy.toBuffer());
			pBuffer.append("$\r\n");
		}
		
		// Save to file.
		return save2File(pBuffer, nPriority, aConfig);
	}
	
	/**
	 * getUploadFile
	 * @return
	 */
	UploadInfo getUploadInfo()
	{
		File pUploading = null;
		
		// 1. Clean up previous content.
		mUploadInfo.mFile = null;
		
		
		// 1. Check whether there is any read-only file.
		final int nMinimal = StatisticsConfig.PRIORITY_FATAL;
		final int nMaximal = StatisticsConfig.PRIORITY_DEBUG;
		for( int nPriority = nMinimal; nPriority <= nMaximal; nPriority++ )
		{
			pUploading = this.getUploadFile(nPriority);
			if( null != pUploading )
			{
				// Fond, stop the loop.
				mUploadInfo.mFile = pUploading;
				mUploadInfo.mPriority = nPriority;

				return mUploadInfo;
			}
			else
			{
				// Decrease the priority for next loop.
				mUploadInfo.mPriority = nPriority;
			}
		}
		
		return mUploadInfo;
	}
	
	/**
	 * save2File
	 * @param aBuffer
	 * @return
	 */
	private boolean save2File(StringBuffer aBuffer, int nPriority, StatisticsConfig aConfig)
	{
		if( (null == aBuffer) || (0 >= aBuffer.length()) || (TextUtils.isEmpty(mRoot)) )
			return false;
		
		// Save the buffer to local file.
		String strFolder = mRoot + StorageHelper.getFolderName(nPriority);
		final long nAppendSize = aBuffer.length();
		File pFile = this.pickup(strFolder, nAppendSize);
		if( null == pFile )
			return false;
		
		// Save the content to the file.
		boolean bRet = this.appendContent(pFile, aBuffer, nPriority, aConfig);
		
		// Clean up the file.
		pFile = null;
		
		return bRet;
	}
	
	/**
	 * appendContent
	 * @param pFile
	 * @param aBuffer
	 */
	private boolean appendContent(File pFile, StringBuffer aBuffer, int nPriority, StatisticsConfig aConfig)
	{
		if( (null == pFile) || (!pFile.canWrite()) )
			return false;
		
		// Get content length.
		final long nLength = pFile.length();
		if( 0 >= nLength )
		{
			// Empty file, need add general information.
			StringBuffer pHeader = aConfig.toBuffer(nPriority);
			pHeader.append("$\r\n");
			
			// Save to file header.
			this.saveBuffer(pFile, pHeader);
		}
		
		// Save the content.
		return this.saveBuffer(pFile, aBuffer);
	}
	
	/**
	 * saveBuffer
	 * @param pFile
	 * @param aBuffer
	 */
	private boolean saveBuffer(File pFile, StringBuffer aBuffer)
	{
		if( (null == pFile) || (!pFile.canWrite()) )
			return false;
		
		boolean bSuccess = true;
		FileWriter pWriter = null;
		try
		{
			pWriter = new FileWriter(pFile, true);
			pWriter.write(aBuffer.toString());
		}
		catch( IOException aException )
		{
			aException.printStackTrace();
			bSuccess = false;
		}
		finally
		{
			if( null != pWriter )
			{
				try 
				{
					pWriter.close();
				} 
				catch (IOException aException) 
				{
					aException.printStackTrace();
				}
				pWriter = null;
			}
		}
		
		return bSuccess;
	}
	
	/**
	 * pickup
	 * @param strFolder
	 * @return
	 */
	private File pickup(String strFolder, long nAppendSize)
	{
		if( TextUtils.isEmpty(strFolder) )
			return null;
		
		// Get current file list in the folder.
		File pFolder = new File(strFolder);
		File aList[] = pFolder.listFiles();
		pFolder = null;
		
		// Get the latest file.
		File pLatest = null;
		final int nLength = (null != aList ? aList.length : 0);
		if( 0 >= nLength )
		{
			// Create a new file, named by time-stamp.
			pLatest = this.createNewFile(strFolder);
		}
		else if ( 1 == nLength )
		{
			pLatest = aList[0];
		}
		else
		{
			ArrayList<File> aArray = new ArrayList<File>();
			for( File pChild : aList )
			{
				aArray.add(pChild);
			}
			
			// Get the latest file in list.
			Collections.sort(aArray, new Comparator<File>()
			{
				@Override
				public int compare(File lhs, File rhs) 
				{
					return (int)(rhs.lastModified() - lhs.lastModified());
				}
			});
			
			// Get the latest file.
			pLatest = aArray.get(0);
			
			// Clean up.
			aArray.clear();
			aArray = null;
		}
		
		if( null != pLatest )
		{
			// Check the file size.
			final long nBytes = pLatest.length();
			if( (nBytes + nAppendSize >= MAX_FILE_SIZE) || (!pLatest.canWrite()) )
			{
				// Dump the latest file, create a new instance.
				pLatest = this.createNewFile(strFolder);
			}			
		}
		
		return pLatest;
	}
	
	/**
	 * createNewFile
	 * @param strFolder
	 * @return
	 */
	private File createNewFile(String strFolder)
	{
		if( TextUtils.isEmpty(strFolder) )
			return null;
		
		File pFolder = new File(strFolder);
		if( (!pFolder.exists()) && (!pFolder.mkdir()) )
		{
			Log.e(StorageHelper.class.toString(), pFolder.getPath());
		}
		pFolder = null;
		
		String strFullPath = strFolder + "/" + System.currentTimeMillis();
		File pLatest = new File(strFullPath);
		if( !pLatest.exists() )
		{
			try
			{
				pLatest.createNewFile();
			}
			catch (IOException aException) 
			{
				aException.printStackTrace();
				pLatest = null;
			}
		}
		
		return pLatest;
	}
	
	/**
	 * initialize
	 * @param aContext
	 */
	private void initialize(Context aContext)
	{
		// Create upload info.
		mUploadInfo = new UploadInfo(null, StatisticsConfig.PRIORITY_FATAL);
				
		// Get root path depends on SD card existing or not.
		mRoot = (ToolUtil.isSDExists() ? Environment.getExternalStorageDirectory() : aContext.getCacheDir()) + "/" + Config.TMPDIRNAME + "/statistics/";
		
		if( TextUtils.isEmpty(mRoot) )
			return ;
		
		// Check whether the root path already exists.
		File pRoot = new File(mRoot);
		if( !pRoot.exists() )
		{
			// Create the root path.
			if( !pRoot.mkdir() )
			{
				// Error occurs.
				return ;
			}
		}
		
		// Clean up the root file.
		pRoot = null;
		
		// Check whether folder exists.
		final int nMinimal = StatisticsConfig.PRIORITY_FATAL;
		final int nMaximal = StatisticsConfig.PRIORITY_DEBUG;
		for( int nPriority = nMinimal; nPriority <= nMaximal; nPriority++ )
		{
			this.createFolder(nPriority);
		}
	}
	
	/**
	 * getReadOnlyFile
	 * @param nPriority
	 * @return
	 */
	private File getUploadFile(int nPriority)
	{
		if( TextUtils.isEmpty(mRoot) )
			return null;
		
		String strFullPath = mRoot + StorageHelper.getFolderName(nPriority);
		File pFolder = new File(strFullPath);
		File aList[] = pFolder.listFiles();
		final int nLength = (null != aList ? aList.length : 0);
		if( 0 >= nLength )
			return null;
		
		final long nPeriod = StatisticsConfig.getPeriod(nPriority);
		for( int nIdx = 0; nIdx < nLength; nIdx++ )
		{
			File pChild = aList[nIdx];
			
			// Check whether is read-only.
			if( !pChild.canWrite() )
				return pChild;
			
			// Check whether file is time-up.
			if( nPeriod > 0 )
			{
				String strName = pChild.getName();
				final long nTimestamp = Long.valueOf(strName);
				final long nCurrent = System.currentTimeMillis();
				if( nCurrent - nTimestamp >= nPeriod )
				{
					pChild.setReadOnly();
					return pChild;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * createFolder
	 * @param nPriority
	 */
	private boolean createFolder(int nPriority)
	{
		if( TextUtils.isEmpty(mRoot) )
			return false;
		
		String strFullPath = mRoot + StorageHelper.getFolderName(nPriority);
		File pFile = new File(strFullPath);
		if( !pFile.exists() )
		{
			// Create a new folder.
			if( !pFile.mkdir() )
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * getFolderName
	 * @param nPriority
	 * @return
	 */
	private static String getFolderName(int nPriority)
	{
		return "priority" + nPriority;
	}
	
	private String                 mRoot;
	private UploadInfo             mUploadInfo = null;
	private static final long      MAX_FILE_SIZE = 16 * 1024; // 16K in bytes.
	
	/**
	 * UploadInfo
	 * @author lorenchen
	 */
	final class UploadInfo
	{
		public UploadInfo(File aFile, int nPriority)
		{
			mFile = aFile;
			mPriority = nPriority;
		}
		
		// Member instance.
		public File  mFile;
		public int   mPriority;
	}
}
