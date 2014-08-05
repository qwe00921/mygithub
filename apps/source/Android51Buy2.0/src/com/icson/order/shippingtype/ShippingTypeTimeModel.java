package com.icson.order.shippingtype;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.util.Log;
import com.icson.util.ToolUtil;

public class ShippingTypeTimeModel extends BaseModel implements Comparator<ShippingTypeTimeModel>{
	protected final static String LOG_TAG = ShippingTypeTimeModel.class.getName();
	public static  final int STATUS_OK= 0;
	private String ship_date;

	private int wh_id;

	private int time_span;

	private String name;

	private int week_day;

	private int status;
	
	private HashMap<String,Integer> time_span_list;

	public String getShip_date() {
		return ship_date;
	}

	public void setShip_date(String ship_date) {
		this.ship_date = ship_date;
	}

	public int getWh_id() {
		return wh_id;
	}

	public void setWh_id(int wh_id) {
		this.wh_id = wh_id;
	}

	public int getTime_span() {
		return time_span;
	}

	public void setTime_span(int time_span) {
		this.time_span = time_span;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWeek_day() {
		return week_day;
	}

	public void setWeek_day(int week_day) {
		this.week_day = week_day;
	}

	public int getState() {
		return status;
	}

	public void setState(int ship_state) {
		this.status = ship_state;
	}

	public void parse(JSONObject json) throws Exception {
		setShip_date(json.getString("ship_date"));

		if (json.has("wh_id")) {
			setWh_id(json.getInt("wh_id"));
		}

		if (json.has("time_span")) {
			setTime_span(json.getInt("time_span"));
		}

		if(json.has("spanList"))
		{
			setSpanList(json.optJSONObject("spanList"));
		}
		
		if (json.has("name")) {
			setName(json.getString("name"));
		}

		if (json.has("week_day")) {
			setWeek_day(json.getInt("week_day"));
		}

		setState(json.optInt("status",-1));

	}

	/**  
	* method Name:setSpanList    
	* method Description:  
	* @param optJSONObject   
	* void  
	* @exception   
	* @since  1.0.0  
	*/
	private void setSpanList(JSONObject optJSONObject) {
		if(null==optJSONObject)
			return;
		if(null == time_span_list)
			time_span_list = new HashMap<String,Integer>();
		else
			time_span_list.clear();
		@SuppressWarnings("unchecked")
		Iterator<String> keys = optJSONObject.keys();
		while(null!=keys && keys.hasNext()){
			final String key = keys.next();
			time_span_list.put(key,optJSONObject.optInt(key) );
		}
	}

	@Override
	public int compare(ShippingTypeTimeModel lhs, ShippingTypeTimeModel rhs) {
		int result = 0;
		try{
			result =Integer.parseInt(lhs.getShip_date()) - Integer.parseInt(rhs.getShip_date());
		}catch(Exception e){
			Log.e(LOG_TAG, ToolUtil.getStackTraceString(e));
		}
		if(result != 0)
			return result;
		else{
			return lhs.getTime_span() -rhs.getTime_span();
		}
	}

	/**  
	* method Name:getTime_span_inlist    
	* method Description:  
	* @param itemId
	* @return   
	* String  
	* @exception   
	* @since  1.0.0  
	*/
	public String getTime_span_inlist(String itemId) {
		if(null!=time_span_list && time_span_list.containsKey(itemId))
		{
			return ""+time_span_list.get(itemId);
		}
		else
			return "";
	}
	
	public int getTime_span_inlist_size() {
		if(null!=time_span_list)
			return time_span_list.size();
		else
			return 0;
	}
}
