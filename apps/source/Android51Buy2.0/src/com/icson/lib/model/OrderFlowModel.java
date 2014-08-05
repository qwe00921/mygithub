package com.icson.lib.model;

import java.util.ArrayList;

public class OrderFlowModel {

	private ArrayList<Item> items;

	private String total;

	private String thirdSysno;
	
	private int thirdType;
	private boolean has_loc;

	public boolean isShowMap() {
		return has_loc;
	}
	public void setShowMap(boolean show) {
		has_loc = show;
	}

	public int getThirdType() {
		return thirdType;
	}

	public void setThirdType(int thirdType) {
		this.thirdType = thirdType;
	}

	public ArrayList<Item> getItems() {
		return items;
	}

	public void setItems(ArrayList<Item> items) {
		this.items = items;
	}

	public void setItem(Item item) {
		if (items == null) {
			items = new ArrayList<OrderFlowModel.Item>();
		}

		items.add(item);
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getThirdSysno() {
		return thirdSysno;
	}

	public void setThirdSysno(String thirdSysno) {
		this.thirdSysno = thirdSysno;
	}

	public static class Item {
		private String time;

		private String content;

		public String getTime() {
			return time;
		}

		public void setTime(String time) {
			this.time = time;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}
}
