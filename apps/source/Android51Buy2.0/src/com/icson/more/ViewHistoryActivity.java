package com.icson.more;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.icson.R;
import com.icson.home.HomeActivity;
import com.icson.item.ItemActivity;
import com.icson.lib.IViewHistory;
import com.icson.lib.model.ViewHistoryProductModel;
import com.icson.lib.ui.AppDialog;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.util.Config;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class ViewHistoryActivity extends BaseActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_more_history);

		findViewById(R.id.more_history_button_redirect).setOnClickListener(this);
		loadNavBar(R.id.history_navbar);
		mNavBar.setRightInfo(R.string.clear_history, new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (IViewHistory.getList() != null) {
					UiUtils.showDialog(ViewHistoryActivity.this, R.string.caption_hint, R.string.message_delete_history_confirm, R.string.btn_ok, R.string.btn_cancel, new AppDialog.OnClickListener() {
						@Override
						public void onDialogClick(int nButtonId) {
							if(nButtonId == AppDialog.BUTTON_POSITIVE) {
								IViewHistory.clear();
								UiUtils.makeToast(ViewHistoryActivity.this, R.string.message_clear_history_success);
								initData();
							}
						}
					});
				} else {
					UiUtils.makeToast(ViewHistoryActivity.this, R.string.message_no_history);
				}
			}
		});

		initData();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.more_history_button_redirect:
			MainActivity.startActivity(this, MainActivity.TAB_HOME);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_ViewHistoryActivity), HomeActivity.class.getName(), getString(R.string.tag_Home), "02011");
			break;
		}
	}

	public void initData() {
		long[] ids = IViewHistory.getList();

		if (ids == null || ids.length == 0) {
			requestFinish(null);
			return;
		}

		String pids = "";
		for (long id : ids) {
			pids += (pids.equals("") ? "" : ",") + id;
		}

		Ajax ajax = ServiceConfig.getAjax(Config.URL_SEARCH_GETBYIDS);
		if( null == ajax )
			return ;
		showLoadingLayer();
		ajax.setData("ids", pids);
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
			@Override
			public void onSuccess(JSONObject v, Response response) {
				ArrayList<ViewHistoryProductModel> models = new ArrayList<ViewHistoryProductModel>();
				try {
					final int errno = v.getInt("errno");
					if (errno != 0) {
						UiUtils.makeToast(ViewHistoryActivity.this, v.optString("data", Config.NORMAL_ERROR));
						return;
					}

					JSONArray arrs = v.getJSONArray("data");
					for (int i = 0, len = arrs.length(); i < len; i++) {
						ViewHistoryProductModel model = new ViewHistoryProductModel();
						model.parse(arrs.getJSONObject(i));
						models.add(model);
					}
				} catch (Exception ex) {
					Log.e(LOG_TAG, ToolUtil.getStackTraceString(ex));
					UiUtils.makeToast(ViewHistoryActivity.this, R.string.message_system_busy);
				} finally {
					requestFinish(models);
				}
			}

		});
		ajax.setOnErrorListener(this);
		addAjax(ajax);
		ajax.send();
	}

	private void requestFinish(ArrayList<ViewHistoryProductModel> models) {
		closeLoadingLayer();
		boolean empty = (models == null || models.size() == 0);
		if (!empty) {
			final HistoryAdapter mHistoryAdapter = new HistoryAdapter(this, models);
			ListView listView = ((ListView) findViewById(R.id.more_history_listview));
			listView.setAdapter(mHistoryAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					ViewHistoryProductModel mProductModel = (ViewHistoryProductModel) (mHistoryAdapter.getItem(position));
					Bundle param = new Bundle();
					param.putLong(ItemActivity.REQUEST_PRODUCT_ID, mProductModel.getProductId());
					ToolUtil.startActivity(ViewHistoryActivity.this, ItemActivity.class, param);
					ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_ViewHistoryActivity), ViewHistoryActivity.class.getName(), getString(R.string.tag_ViewHistoryActivity), "02012", String.valueOf(mProductModel.getProductId()));
				}
			});
		}

		findViewById(R.id.more_history_relative_empty).setVisibility(empty ? View.VISIBLE : View.GONE);
		findViewById(R.id.more_history_listview).setVisibility(!empty ? View.VISIBLE : View.GONE);
		mNavBar.setRightVisibility( empty ? View.GONE : View.VISIBLE);
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_ViewHistoryActivity);
	}
}