package com.icson.lib.model;

import org.json.JSONException;
import org.json.JSONObject;

public class PageModel extends BaseModel {
	private int currentPage;
	private int pageSize;
	private int total;
	private int pageCount;

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public void parse(JSONObject page) throws JSONException {
		setCurrentPage(page.optInt("current_page"));
		setTotal(page.optInt("total"));
		setPageSize(page.optInt("page_size"));
		setPageCount(page.optInt("page_count"));
	}
}
