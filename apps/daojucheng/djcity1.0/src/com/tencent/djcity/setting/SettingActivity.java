package com.tencent.djcity.setting;

import android.os.Bundle;
import android.view.View;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.IPageCache;
import com.tencent.djcity.lib.IVersion;
import com.tencent.djcity.lib.control.VersionControl;
import com.tencent.djcity.lib.inc.CacheKeyFactory;
import com.tencent.djcity.lib.model.VersionModel;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.Config;
import com.tencent.djcity.util.ToolUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.OnErrorListener;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;
import com.tencent.djcity.util.cache.InnerCache;
import com.tencent.djcity.util.cache.SDCache;

public class SettingActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		mBackView = this.findViewById(R.id.back);
		mBackView.setOnClickListener(this);
		
		mClearCacheView = this.findViewById(R.id.clear_cache);
		mClearCacheView.setOnClickListener(this);
		
		mCheckVersionView = this.findViewById(R.id.check_newversion);
		mCheckVersionView.setOnClickListener(this);
	}

	private void checkVersion()
	{
		showProgressLayer("正在检查新版本, 请稍候...");
		VersionControl control = new VersionControl();
		control.getlatestVersionInfo(true, new OnSuccessListener<VersionModel>() {
			@Override
			public void onSuccess(VersionModel v, Response response)
			{
				closeProgressLayer();
				if (v.getVersion() <= IVersion.getVersionCode())
				{
					UiUtils.makeToast(SettingActivity.this, R.string.message_latest_version);
					return;
				}

				IVersion.notify(SettingActivity.this, v);
			}
		}, new OnErrorListener() {
			@Override
			public void onError(Ajax ajax, Response response)
			{
				showProgressLayer("正在检查新版本, 请稍候...");
				UiUtils.makeToast(SettingActivity.this, "检测失败");
				closeProgressLayer();
			}
		});
	}

	// 清除缓存
	private void cleanCache()
	{
		showProgressLayer("正在清理, 请稍候...");
		// 避免系统数据被删除，使用白名单策略
		IPageCache cache = new IPageCache();

		// 商品分类列表
		cache.remove(CacheKeyFactory.CACHE_BLOCK_CATEGORY);
		// 确认订单：发票ID
		cache.remove(CacheKeyFactory.CACHE_ORDER_INVOICE_ID);
		// 确认订单：地址ID
		cache.remove(CacheKeyFactory.CACHE_ORDER_ADDRESS_ID);
		// 确认订单：配送方式ID
		cache.remove(CacheKeyFactory.CACHE_ORDER_SHIPPING_TYPE_ID);
		// 确认订单：支付方式ID
		cache.remove(CacheKeyFactory.CACHE_ORDER_PAY_TYPE_ID);
		// 首页运营錧
		cache.remove(CacheKeyFactory.HOME_CHANNEL_INFO);
		// 各运营馆数据
		cache.removeLeftLike(CacheKeyFactory.CACHE_EVENT);
		// Remove dispatches information
		cache.remove(CacheKeyFactory.CACHE_DISPATCHES_INFO);
		// Remove search history words
		cache.remove(CacheKeyFactory.CACHE_SEARCH_HISTORY_WORDS);

		// Remove image cache.
		this.removeImaegCache();

		closeProgressLayer();
		UiUtils.makeToast(this, "缓存已清除");
	}

	/**
	 * removeImageCache
	 */
	private void removeImaegCache()
	{
		String aDirs[] = { Config.PIC_CACHE_DIR, Config.CHANNEL_PIC_DIR, Config.MY_FAVORITY_DIR,
				Config.QIANG_PIC_DIR, Config.TUAN_PIC_DIR };
		if (ToolUtil.isSDExists())
		{
			SDCache storage = new SDCache();
			for (String folder : aDirs)
			{
				storage.removeFolder(folder);
			}
		}

		InnerCache storage = new InnerCache(this);
		for (String folder : aDirs)
		{
			storage.removeFolder(folder);
		}

	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.back:
			this.onBackPressed();
			break;
		case R.id.clear_cache:
			this.cleanCache();
			break;
		case R.id.check_newversion:
			this.checkVersion();
			break;
			
		}
		super.onClick(v);
	}

	private View mBackView;
	private View mClearCacheView;
	private View mCheckVersionView;
	

}
