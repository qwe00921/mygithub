package com.tencent.djcity.discover;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.util.activity.BaseActivity;

public class OptionAdapter extends BaseAdapter {

	private BaseActivity mActivity;
	private LayoutInflater mInflater;
	private int[]  optStr;
	private int[]  optRid;
	public OptionAdapter(BaseActivity aActivity)
	{
		mActivity = aActivity;
		mInflater = mActivity.getLayoutInflater();
	}
	
	@Override
	public int getCount() {
		return (null == optStr) ? 0 : optStr.length;
	}

	@Override
	public Object getItem(int position) {
		return (null == optStr) ? null : optStr[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		optionHolder holder = null;
		
		if (convertView == null) {
			convertView =  mInflater.inflate(R.layout.listitem_option, null);
			holder = new optionHolder();
			holder.tv = ((TextView) convertView.findViewById(R.id.opt_name));
			convertView.setTag(holder);	
		} else {
			holder = (optionHolder) convertView.getTag();
		}

		holder.tv.setText(optStr[position]);
		holder.tv.setCompoundDrawablesWithIntrinsicBounds(optRid[position], 0, 0, 0);

		return convertView;
	}

	private class optionHolder
	{
		TextView  tv;
	}
	
	public void setResdata(final int[] optrid, final int[] optstr) {
		optRid = optrid;
		optStr = optstr;
	}

}
