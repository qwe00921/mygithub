package com.yy.android.gamenews.util;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yy.android.gamenews.ui.common.ImageAdapter;
import com.yy.android.sportbrush.R;

public class DropDownHelper {

	public static void showDropDownList(Context context, BaseAdapter adapter,
			View anchor, final OnDropDownClickListener listener) {
		final PopupWindow popup = new PopupWindow(context);
		popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popup.setBackgroundDrawable(context.getResources().getDrawable(
				R.drawable.transparent_bg));
		popup.setOutsideTouchable(true);
		popup.setTouchable(true);
		popup.setFocusable(true);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final View contentView = inflater.inflate(R.layout.dropdown_list, null);
		popup.setContentView(contentView);
		contentView.measure(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		ListView listView = (ListView) contentView.findViewById(R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				popup.dismiss();
				if (listener != null) {
					DropDownItem item = (DropDownItem) parent.getAdapter()
							.getItem(position);
					listener.onClick(position, item.text);
				}
			}
		});
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.setAdapter(adapter);
		popup.showAsDropDown(anchor);
	}

	public static void showDropDownList(Context context, View anchor,
			final String[] textArray, final Object[] resArray, int selectIndex,
			OnDropDownClickListener listener) {
		if (textArray == null) {
			return;
		}

		ArrayList<DropDownItem> itemList = new ArrayList<DropDownItem>();
		for (int i = 0; i < textArray.length; i++) {
			DropDownItem item = new DropDownItem();
			item.icon = resArray[i];
			item.text = textArray[i];
			if (selectIndex == i) {
				item.selected = true;
			} else {
				item.selected = false;
			}
			itemList.add(item);
		}

		ImageAdapter<DropDownItem> adapter = new ImageAdapter<DropDownItem>(
				context) {

			@Override
			public int getViewTypeCount() {
				return getCount();
			}

			@Override
			public int getItemViewType(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder holder = null;
				if (convertView == null) {
					convertView = mInflater.inflate(
							R.layout.dropdown_list_item, null);
					holder = new ViewHolder();
					holder.image = (ImageView) convertView
							.findViewById(R.id.dropdown_list_item_img);
					holder.text = (TextView) convertView
							.findViewById(R.id.dropdown_list_item_text);
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				DropDownItem item = getItem(position);
				if (item != null) {

					Log.d("", "[getView] convertView = " + convertView
							+ ", position = " + position + ", icon = "
							+ item.icon);
					holder.text.setText(item.text);
					Object img = item.icon;
					if (img instanceof Integer) {
						holder.image.setImageResource((Integer) img);
					} else if (img instanceof String) {
						displayImage(String.valueOf(img), holder.image);
					}

					if (item.selected) {
						holder.text.setSelected(true);
						holder.image.setSelected(true);
					} else {

						holder.text.setSelected(false);
						holder.image.setSelected(false);
					}
				}
				return convertView;
			}
		};
		adapter.setDataSource(itemList);

		showDropDownList(context, adapter, anchor, listener);
	}

	private static class ViewHolder {
		ImageView image;
		TextView text;
	}

	private static class DropDownItem {
		String text;
		Object icon;
		boolean selected;
	}

	public interface OnDropDownClickListener {
		public void onClick(int position, String text);
	}
}
