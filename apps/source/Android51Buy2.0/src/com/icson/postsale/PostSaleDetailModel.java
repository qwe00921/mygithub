package com.icson.postsale;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.icson.lib.model.BaseModel;
import com.icson.util.Log;

public class PostSaleDetailModel extends BaseModel {

	/**
	 * 
	 */
	private int mApplyId;
	private String mOrderCharId;
	private String mApplyTime;
	private int mCanUrgent;
	private String mStatus;
	private String mHandleType;
	private String mCustomerDesc;
	private List<String> mImagesUrlList;
	private List<String> mBigImagesUrlList;
	private HandleDetail mHandleDetail;
	private String mRevAddress;
	private PostSaleItemModel mItem;
	private List<PostSaleLogModel> mLogModelList;

	public int getApplyId() {
		return mApplyId;
	}

	public void setApplyId(int applyId) {
		this.mApplyId = applyId;
	}

	public String getOrderCharId() {
		return mOrderCharId;
	}

	public void setOrderCharId(String orderCharId) {
		this.mOrderCharId = orderCharId;
	}

	public String getApplyTime() {
		return mApplyTime;
	}

	public void setApplyTime(String applyTime) {
		this.mApplyTime = applyTime;
	}

	public int getCanUrgent() {
		return mCanUrgent;
	}

	public void setCanUrgent(int canUrgent) {
		this.mCanUrgent = canUrgent;
	}

	public String getStatus() {
		return mStatus;
	}

	public void setStatus(String status) {
		this.mStatus = status;
	}

	public String getHandleType() {
		return mHandleType;
	}

	public void setHandleType(String handleType) {
		this.mHandleType = handleType;
	}

	public String getCustomerDesc() {
		return mCustomerDesc;
	}

	public void setCustomerDesc(String customerDesc) {
		this.mCustomerDesc = customerDesc;
	}

	public List<String> getImagesUrlList() {
		return mImagesUrlList;
	}

	public void setImagesUrlList(List<String> imagesUrl) {
		this.mImagesUrlList = imagesUrl;
	}

	public HandleDetail getHandleDetail() {
		return mHandleDetail;
	}

	public void setHandleDetail(HandleDetail handleDetail) {
		this.mHandleDetail = handleDetail;
	}

	public String getRevAddress() {
		return mRevAddress;
	}

	public void setRevAddress(String revAddress) {
		this.mRevAddress = revAddress;
	}

	public PostSaleItemModel getItem() {
		return mItem;
	}

	public void setItem(PostSaleItemModel item) {
		this.mItem = item;
	}

	public List<PostSaleLogModel> getLogModelList() {
		return mLogModelList;
	}

	public void setLogModelList(List<PostSaleLogModel> logModelList) {
		this.mLogModelList = logModelList;
	}

	public List<String> getBigImagesUrlList() {
		return mBigImagesUrlList;
	}

	public void setBigImagesUrlList(List<String> bigImagesUrlList) {
		this.mBigImagesUrlList = bigImagesUrlList;
	}

	public class HandleDetail {
		private String mTitle;
		private String mMethod;
		private String mMethodDetail;
		private List<TwoColObject> mMethodFormList;
		private List<String> mTextAreaList;
		private List<TwoColObject> mFormList;

		public String getTitle() {
			return mTitle;
		}

		public void setTitle(String title) {
			this.mTitle = title;
		}

		public String getMethod() {
			return mMethod;
		}

		public void setMethod(String method) {
			this.mMethod = method;
		}

		public String getMethodDetail() {
			return mMethodDetail;
		}

		public void setMethodDetail(String methodDetail) {
			this.mMethodDetail = methodDetail;
		}

		public List<TwoColObject> getMethodFormList() {
			return mMethodFormList;
		}

		public void setMethodFormList(List<TwoColObject> methodForm) {
			this.mMethodFormList = methodForm;
		}
		
		public List<String> getTextAreaList() {
			return mTextAreaList;
		}

		public void setTextAreaList(List<String> textAreaList) {
			this.mTextAreaList = textAreaList;
		}

		public List<TwoColObject> getFormList() {
			return mFormList;
		}

		public void setFormList(List<TwoColObject> formList) {
			this.mFormList = formList;
		}

		public void parse(JSONObject json) throws JSONException {
			if(json == null) {
				Log.w(TAG, "[parse] json is null!");
				return;
			}
			
			setTitle(json.optString(Constants.KEY_HANDLE_DETAIL_TITLE, ""));
			setMethod(json.optString(Constants.KEY_HANDLE_DETAIL_METHOD, ""));
			setMethodDetail(json.optString(Constants.KEY_HANDLE_DETAIL_METHOD_DETAIL, ""));

			// For method form
			JSONArray methodFormArray = json.getJSONArray(Constants.KEY_HANDLE_DETAIL_METHOD_FORM);
			if(methodFormArray != null) {
				List<TwoColObject> methodFormList = new ArrayList<TwoColObject>();
				
				for(int i = 0; i < methodFormArray.length(); i++) {
					JSONArray methodFormJSONArray = methodFormArray.getJSONArray(i);
					if(methodFormJSONArray != null) {
						TwoColObject form  = new TwoColObject();
						form.setTitle(methodFormJSONArray.getString(0));
						form.setValue(methodFormJSONArray.getString(1));
						methodFormList.add(form);
					}
				}
				setMethodFormList(methodFormList);
			}
			
			// For text area
			JSONArray textareaArray = json.getJSONArray(Constants.KEY_HANDLE_DETAIL_TEXTAREA);
			if(textareaArray != null) {
				List<String> textAreaList = new ArrayList<String>();
				
				for(int i = 0; i < textareaArray.length(); i++) {
					String textArea = textareaArray.getString(i);
					if(textArea != null) {
						textAreaList.add(textArea);
					}
				}
				setTextAreaList(textAreaList);
			}
			
			JSONArray detailFormArray = json.getJSONArray(Constants.KEY_HANDLE_DETAIL_FORM);
			if(detailFormArray != null) {
				List<TwoColObject> detailFormList = new ArrayList<TwoColObject>();
				for(int i = 0; i < detailFormArray.length(); i++) {
					JSONArray formJSONArray = detailFormArray.getJSONArray(i);
					if(formJSONArray != null) {
						TwoColObject form  = new TwoColObject();
						form.setTitle(formJSONArray.getString(0));
						form.setValue(formJSONArray.getString(1));
						detailFormList.add(form);
					}
				}
				setFormList(detailFormList);
			}
		}

	}
	
	public class TwoColObject {
		private String title;
		private String value;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
		public void parse(JSONObject json) throws JSONException {
			if(json == null) {
				Log.w(TAG, "[parse] json is null!");
				return;
			}
			
			
		}
	}
	
	private static final String TAG = PostSaleDetailModel.class.getSimpleName();
	public void parse(JSONObject json) throws JSONException {
		if(json == null) {
			Log.w(TAG, "[parse] json is null!");
			return;
		}
//		json = new JSONObject(getTestJson());
		setApplyId(json.optInt(Constants.KEY_APPLY_ID, 0));
		setOrderCharId(json.optString(Constants.KEY_ORDER_CHAR_ID, ""));
		setApplyTime(json.optString(Constants.KEY_APPLY_TIME, ""));
		setCanUrgent(json.optInt(Constants.KEY_CAN_URGENT, 0));
		setStatus(json.optString(Constants.KEY_REQ_ORDER_STATUS, ""));
		setHandleType(json.optString(Constants.KEY_HANDLE_TYPE, ""));
		setCustomerDesc(json.optString(Constants.KEY_CUSTOMER_DESC, ""));
		
		// For Images URL
		JSONArray imgUrlJSONObj = json.optJSONArray(Constants.KEY_IMAGES_URL);
		if(imgUrlJSONObj != null) {
			List<String> imagesUrlList = new ArrayList<String>();
			
			for(int i = 0; i < imgUrlJSONObj.length(); i++) {
				String imageUrl = imgUrlJSONObj.getString(i);
				if(imageUrl != null) {
					imagesUrlList.add(imageUrl);
				}
			}
			setImagesUrlList(imagesUrlList);
		}
		
		// For Images URL
		JSONArray bigImgUrlJSONObj = json.optJSONArray(Constants.KEY_IMAGES_URL_BIG);
		if(bigImgUrlJSONObj != null) {
			List<String> bigImgUrlList = new ArrayList<String>();
			
			for(int i = 0; i < bigImgUrlJSONObj.length(); i++) {
				String bigImgUrl = bigImgUrlJSONObj.getString(i);
				if(bigImgUrl != null) {
					bigImgUrlList.add(bigImgUrl);
				}
			}
			setBigImagesUrlList(bigImgUrlList);
		}
		
		// For Handle detail
		JSONArray detailJSONArray = json.optJSONArray(Constants.KEY_HANDLE_DETAIL);
		if(detailJSONArray != null) {
			HandleDetail detail = new HandleDetail();
			detail.parse(detailJSONArray.getJSONObject(0));
			setHandleDetail(detail);
		}
		
		setRevAddress(json.optString(Constants.KEY_REVADDRESS, ""));
		
		// For Items
		JSONArray itemJSONArray = json.optJSONArray(Constants.KEY_ITEMS);
		if(itemJSONArray != null) {
			JSONObject itemJSON = itemJSONArray.getJSONObject(0);
			PostSaleItemModel item = new PostSaleItemModel();
			item.parse(itemJSON);
			setItem(item);
		}

		// For log list
		JSONArray logJSONArray = json.optJSONArray(Constants.KEY_LOGLIST);
		if(logJSONArray != null) {
			List<PostSaleLogModel> logList = new ArrayList<PostSaleLogModel>();
			for(int i = 0; i < logJSONArray.length(); i++) {
				JSONObject logJSONObj = logJSONArray.getJSONObject(i);
				PostSaleLogModel log = new PostSaleLogModel();
				log.parse(logJSONObj);
				logList.add(log);
			}
			setLogModelList(logList);
		}
	}

	@SuppressWarnings("unused")
	private String getTestJson() {
		String str = 
			"{"         +
				"\"customerDesc\":\"线坏掉了\"," +
				"\"applyID\":2580710," +
				"\"logList\":[" +
					"{" +
						"\"log\":\"您提交了订单，请等待客服审核\", " +
						"\"logTime\":\"2013-11-20 15:36:13\"" +
					"}," +
					"{" +
						"\"log\":\"您的订单审核通过\", " +
						"\"logTime\":\"2013-11-20 15:36:14\"" +
					"}," +	
					"{" +
						"\"log\":\"您好， 客服已在15点36分再次电话联系您提示您挂机，很抱歉，您的产品是1年保修，目前已过保，无法质保处理，已帮您申请取消，感谢您对易迅的支持\", " +
						"\"logTime\":\"2013-11-20 15:36:15\"" +
					"}" +
					
				
				
				"]," +
				"\"revAddress\":\"漕河泾开发区古美路\"," +
				"\"items\":["+
					"{"+
						"\"productName\":\"shengwei 胜为 USB2.0延长线 UC-2018 银白色 1.8米\","+
						"\"product_char_id\":\"19-233-043\","+
						"\"productID\":142617,"+
						"\"productNum\":1"+
					"}"+
				"],"+
				"\"handleDetail\":["+
					"{"+
						"\"method_detail\":\"邮寄给易迅网\","+
						"\"method\":\"报修\","+
						"\"title\":\"取件方式：\","+
						"\"textArea\":[\"textArea value1\", \"textArea value2\", \"textArea value3\"],"+
						"\"form\":["+
							"[\"取货地址：\",\"漕河泾开发区古美路 \"],"+
							"[\"电话：\",\"13800000000\"]"+
						"],"+
						"\"method_form\":["+
							"[method_form_title1, method_form_content1],"+
							"[method_form_title2, method_form_content2]"+
						"]"+
					"}"+
				"],"+
				"\"status\":\"审核未通过\","+
				"\"handleType\":\"报修\","+
				"\"can_urgent\":1,"+
				"\"imagesUrl\":[\"http:\\/\\/shp.qpic.cn\\/kfpic\\/0\\/528C44A7-034F93270000000000000000203C0628.1.jpg\\/0\""
				+ ", \"http:\\/\\/shp.qpic.cn\\/kfpic\\/0\\/528C44A7-034F93270000000000000000203C0628.1.jpg\\/0\""
				+ ", \"http:\\/\\/shp.qpic.cn\\/kfpic\\/0\\/528C44A7-034F93270000000000000000203C0628.1.jpg\\/0\""
				+ ", \"http:\\/\\/shp.qpic.cn\\/kfpic\\/0\\/528C44A7-034F93270000000000000000203C0628.1.jpg\\/0\""
				+ ", \"http:\\/\\/shp.qpic.cn\\/kfpic\\/0\\/528C44A7-034F93270000000000000000203C0628.1.jpg\\/0\""
				+ ", \"http:\\/\\/shp.qpic.cn\\/kfpic\\/0\\/528C44A7-034F93270000000000000000203C0628.1.jpg\\/0\"],"+
				"\"order_char_id\":\"1020186860\""+
			"}";
		return str;
	}
}
