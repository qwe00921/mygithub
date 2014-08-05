package com.tencent.djcity.msgcenter;

import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnErrorListener;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class MsgMgr implements OnSuccessListener<JSONObject>, OnErrorListener {
	
	

	public interface MsgObserver 
	{
		public void onResult(int num);
	}
	
	public void setMsgObserver(MsgObserver aMsgObserver)
	{
		this.mMsgObserver = aMsgObserver;
	}

	private static MsgMgr mgr = new MsgMgr();
	
	private MsgMgr()
	{
		
	}
	
	public static MsgMgr getInstance()
	{
		return mgr;
	}
	
	private JSONObject mJsonObj;
	private JSONParser mParser;

	public MsgObserver mMsgObserver;

	private int mNum = 0;

	public int getMsgNum()
	{
		int nNum = 0;
		String url = "http://apps.game.qq.com/daoju/v3/test_apps/pullMsg.php?type=count&uin=123124123&biz=cf";
		Ajax ajax = AjaxUtil.get(url);// ServiceConfig.getAjax(Config.URL_CATEGORY_NEW);

		if (null == ajax)
			return nNum;

		if (null == mParser)
		{
			mParser = new JSONParser();
		}
		ajax.setParser(mParser);
		ajax.setOnSuccessListener(this);
		ajax.setOnErrorListener(this);
		ajax.send();

		return nNum;
	}

	@Override
	public void onError(Ajax ajax, Response response)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuccess(JSONObject jsonObj, Response response)
	{
//		if (!mParser.isSuccess())
//		{
//			return;
//		}

		try
		{
			if(null != jsonObj)
			{
				mNum = jsonObj.getInt("total");
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		mParser = null;
		
		if(null != mMsgObserver)
		{
			mMsgObserver.onResult(mNum);
		}
	}
}
