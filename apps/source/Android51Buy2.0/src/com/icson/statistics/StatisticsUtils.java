package com.icson.statistics;

import com.icson.util.IcsonApplication;
import com.icson.util.ToolUtil;
import java.util.Collections;
import java.util.List;
import java.net.InetAddress;
import java.net.NetworkInterface;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class StatisticsUtils 
{
	/**
	 * getAvailableInfo
	 * @param aContext
	 * @return
	 */
	public static NetworkInfo getAvailableInfo(Context aContext)
	{
		if ( null == aContext )
			return null;
		
		// Get the connectivity manager.
		ConnectivityManager pManager = (ConnectivityManager)aContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if ( null == pManager )
			return null;
		
		// Get current active network information.
		NetworkInfo pInfo = pManager.getActiveNetworkInfo();
		if ( null == pInfo )
		{
			// Get current active network information.
			NetworkInfo[] aAllInfo = pManager.getAllNetworkInfo();
			final int nSize = (null != aAllInfo ? aAllInfo.length : 0);
			for ( int nIdx = 0; nIdx < nSize; nIdx++ )
			{
				NetworkInfo pEntity = aAllInfo[nIdx];
				if ( (null != pEntity) && (pEntity.isAvailable() && (pEntity.isConnectedOrConnecting())) )
				{
					pInfo = pEntity;
					break;
				}
			}
			
			aAllInfo = null;
		}
		
		return pInfo;
	}
	
	/**
	 * isNetworkAvailable
	 * @return
	 */
	public static boolean isNetworkAvailable(Context aContext)
	{
		return null != StatisticsUtils.getAvailableInfo(aContext);
	}
	
	/**
	 * getDeviceId
	 * @param aContext
	 * @return
	 */
	public static String getDeviceUid(Context aContext)
	{
		// Get the device id.
		String strDeviceId = StatisticsUtils.getIMEI(aContext.getApplicationContext());

        String strMacAddr = StatisticsUtils.getMacAddr(aContext.getApplicationContext());
        final boolean bWithMac = !TextUtils.isEmpty(strMacAddr);
        if( bWithMac )
        {
        	strMacAddr = strMacAddr.replaceAll(":", "");
        }
        
        // Compose the result.
        String strUid = StatisticsUtils.encryptUid(strDeviceId + (bWithMac ? strMacAddr : ""));
        
        return strUid;
	}
	
	public static String getMacAddr(Context aContext)
	{
		if(aContext == null) {
			return null;
		}
		// Permission: <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
		// Get mac address.
		String strMacAddr = null;
		
		WifiManager pWifiMan = (WifiManager) aContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE); 
		if(pWifiMan != null) {
			WifiInfo pWifiInfo = pWifiMan.getConnectionInfo();
			if(pWifiInfo != null) {
				strMacAddr = pWifiInfo.getMacAddress();
			}
		}

		return strMacAddr;
	}
	
	public static String getIMEI(Context aContext)
	{
		// Get the device id.
		String strDeviceId = Secure.getString(aContext.getContentResolver(), Secure.ANDROID_ID);
		
		// Some devices can not get id by ANDROID_ID indicator.
		if ( TextUtils.isEmpty(strDeviceId) )
		{
			// Try another way to retrieve device id.
			TelephonyManager pManager = (TelephonyManager) aContext.getApplicationContext().
				getSystemService(Context.TELEPHONY_SERVICE);
			if(pManager != null) {
				
				strDeviceId = pManager.getDeviceId();
			}
		}
		
		return strDeviceId;
	}
	
	public static String getIMSI()
	{
		TelephonyManager telManager = (TelephonyManager) IcsonApplication.app.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = telManager.getSubscriberId();
		
		return imsi;
	}
	
	/**
     * Get IP address from first non-localhost interface
     * @param ipv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
	
	
	/**
	 * getTestId
	 * @return
	 */
	public static String getTestId(Context aContext)
	{
		if( null == aContext )
			return "";
		
		// Get application name.
		String strName = aContext.getPackageName();
		final int nPos = strName.indexOf(".");
		strName = strName.substring(nPos + 1);
		final String strOrigin = strName + "6.6260693";
		
		// Compose the buffer.
		String strDevId = StatisticsUtils.getDeviceUid(aContext);
		StringBuffer pBuffer = new StringBuffer(strDevId);
		pBuffer.append("&");
		final byte aArray[] = {67,104,66,105,67,105,66,105,67,105,69,102,74,97,69,102,67,100,70,100,71,97,67,105};
		final int nLength = strOrigin.length();
		for( int nIdx = 0; nIdx < nLength; nIdx++ )
		{
			byte nByte = aArray[nIdx];
			pBuffer.append((char) (strOrigin.charAt(nIdx) + StatisticsUtils.getChar(nByte, nIdx)));
		}
		
	//	md5(device_id + "&" + secret_key);
		String strOrg = pBuffer.toString();
		return ToolUtil.toMD5(strOrg);
	}
	
	/**
	 * getChar
	 * @param aByte
	 * @param nPos
	 * @return
	 */
	private static char getChar(byte aByte, int nPos)
	{
		return (char) (aByte - StatisticsUtils.getOffset((nPos & 0x01)));
	}
	
	/**
	 * getOffset
	 * @param bFlag
	 * @return
	 */
	private static byte getOffset(int nVal)
	{
		byte nByte = (byte) (0 == nVal ? 65 : 97);
		return nByte;
	}
	
	/**
	 * encryptUid
	 * @param strUid
	 * @return
	 */
	private static String encryptUid(String strUid)
	{
		if( TextUtils.isEmpty(strUid) )
			return "";
		
		StringBuilder pBuilder = new StringBuilder();
		final int nLength = strUid.length();
		for( int nIdx = 0; nIdx < nLength; nIdx++ )
		{
			final int nChar = strUid.charAt(nIdx);
			pBuilder.append(nChar + (nChar % nLength));
		}
		pBuilder.reverse();
		return pBuilder.toString();
	}
	
	/**
	 * default constructor of StatisticsUtils
	 */
	private StatisticsUtils() 
	{
	}
}
