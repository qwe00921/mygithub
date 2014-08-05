package com.icson.shoppingcart;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import com.icson.lib.AppStorage;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.lib.control.BaseControl;
import com.icson.lib.model.ShoppingCartProductModel;
import com.icson.order.shoppingcart.ShoppingCartModel;
import com.icson.order.shoppingcart.ShoppingCartParser;
import com.icson.util.AjaxUtil;
import com.icson.util.Config;
import com.icson.util.ServiceConfig;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Parser;

public class ShoppingCartControl extends BaseControl {
	
	final static String LOG_TAG = ShoppingCartControl.class.getName();

	public ShoppingCartControl(BaseActivity activity) {
		super(activity);
	}
	
	public Ajax getBuyImmediatelyList(ShoppingCartParser parser, HashMap<String, Object> data, long productId, int buyCount,int channel_id, OnSuccessListener<ShoppingCartModel> success, OnErrorListener error) {
		final long uid = ILogin.getLoginUid();
		Ajax ajax = AjaxUtil.post("http://mgray.yixun.com/cart/GetProductList?mod=cart");ServiceConfig.getAjax(Config.URL_ORDER_CONFIRM_NEW, uid);
		if( null == ajax )
			return null;
		ajax.setParser(parser);
		ajax.setData(data);
		
		//需要区分节能补贴和普通离线
		//ajax.setData("ism", 2);//节能补贴
		//ajax.setData("ism", 3);//普通离线
		//场景channel_id追加
		ajax.setData("items","[{\"product_id\":"+productId+",\"buy_count\":"+buyCount+",\"chid\":"+channel_id+",\"price_id\":0}]");
		ajax.setData("district", FullDistrictHelper.getDistrictId());
				
		String thirdsource = AppStorage.getData(AppStorage.SCOPE_DEFAULT, "thirdcallsource");
		if(null!=thirdsource && thirdsource.equals("alipayapp"))
		{
			ajax.setData("loginSource","alipaywallet");
		}
		ajax.setOnErrorListener(error);
		ajax.setOnSuccessListener(success);
		mActivity.addAjax(ajax);
		ajax.send();

		return ajax;
	}
	
	public Ajax getShoppingCartList(ShoppingCartListParser parser, OnSuccessListener<ShoppingCartListModel> success, OnErrorListener error) {

//		Ajax ajax = ServiceConfig.getAjax(Config.URL_CART_GET_PRODUCT_LIST);
		Ajax ajax = AjaxUtil.post("http://mgray.yixun.com/cart/GetProductList?mod=cart");
		if( null == ajax )
			return null;
		ajax.setData("district", FullDistrictHelper.getDistrictId());
		ajax.setData("whId", ILogin.getSiteId());
		ajax.setData("source", "3001");
		ajax.setData("cmd", "603");
		ajax.setData("ism", "0");
		ajax.setData("isPackage", "0");
		ajax.setData("uid", ILogin.getLoginUid());
		
		ajax.setParser(parser);
		ajax.setOnErrorListener(error);
		ajax.setOnSuccessListener(success);
		ajax.send();

		return ajax;
	}
	//items:{"324930":{"product_id":324930,"buy_count":1,"main_product_id":324930,"price_id":0,"type":0,"OTag":"31000038300000-100070021-0-386212010.3"}}
	public Ajax getESShoppingCartList(String items,ShoppingCartListParser parser, OnSuccessListener<ShoppingCartListModel> success, OnErrorListener error) {

		Ajax ajax = ServiceConfig.getAjax(Config.URL_CART_GET_PRODUCT_LIST);
		if( null == ajax )
			return null;
		ajax.setData("district", FullDistrictHelper.getDistrictId());
		ajax.setData("whId", ILogin.getSiteId());
		ajax.setData("source", "3001");
		ajax.setData("cmd", "603");
		ajax.setData("ism", "2");
		ajax.setData("items", items);
		ajax.setData("isPackage", "0");
		ajax.setData("uid", ILogin.getLoginUid());
		
		ajax.setParser(parser);
		ajax.setOnErrorListener(error);
		ajax.setOnSuccessListener(success);
		mActivity.addAjax(ajax);
		ajax.send();

		return ajax;
	}

	@SuppressWarnings("rawtypes")
	public Ajax getOrderShoppingCartList(Parser parser,HashMap<String, Object> data, OnSuccessListener<ShoppingCartModel> success, OnErrorListener error) {
		
		//Ajax ajax = ServiceConfig.getAjax(Config.URL_ORDER_CONFIRM_NEW, uid);
		Ajax ajax = ServiceConfig.getAjax(Config.URL_ORDER_CONFIRM_SERVICE);
		
		if( null == ajax )
			return null;

		data.put("uid", ILogin.getLoginUid());
		data.put("ism", 0);
		
		ajax.setData(data);
		String thirdsource = AppStorage.getData(AppStorage.SCOPE_DEFAULT, "thirdcallsource");
		if(null!=thirdsource && thirdsource.equals("alipayapp"))
		{
			ajax.setData("loginSource","alipaywallet");
		}
		
		ajax.setData("district", FullDistrictHelper.getDistrictId());
		ajax.setParser(parser);
		ajax.setOnErrorListener(error);
		ajax.setOnSuccessListener(success);
		mActivity.addAjax(ajax);
		ajax.send();

		return ajax;
	}
/*
	public Ajax submit(int id, ArrayList<ShoppingCartProductModel> models, OnSuccessListener<JSONObject> success, OnErrorListener error) {
		String str = "";
		for (ShoppingCartProductModel mShoppingCartProductModel : models) {
			str += (str.equals("") ? "" : ",") + mShoppingCartProductModel.getProductId() + "|" + mShoppingCartProductModel.getBuyCount() + "|" + mShoppingCartProductModel.getMainProductId();
		}

		final String url = Config.BUY_ICSON_COM + "/json.php?mod=shoppingcart&act=addproductfromapp&uid=" + ILogin.getLoginUid();
		Ajax ajax = AjaxUtil.postJSON(url);
		ajax.setId(id);
		ajax.setData("ids", str);
		ajax.setOnErrorListener(mActivity);
		ajax.setOnSuccessListener(success);
		mActivity.addAjax(ajax);
		ajax.send();

		return ajax;
	}
*/
//	public Ajax getShoppingCartList(int id, ArrayList<ShoppingCartProductModel> models, OnSuccessListener<JSONObject> success, OnErrorListener error) {
//		String str = "";
//		for (ShoppingCartProductModel mShoppingCartProductModel : models) {
//			str += (str.equals("") ? "" : ",") + mShoppingCartProductModel.getProductId() + "|" + mShoppingCartProductModel.getBuyCount() + "|" + mShoppingCartProductModel.getMainProductId();
//		}
//		Ajax ajax = ServiceConfig.getAjax(Config.URL_LIST_CART_NONMEMBER);
//		if( null == ajax )
//			return null;
//		ajax.setId(id);
//		ajax.setData("cart", str);
//		ajax.setOnErrorListener(error);
//		ajax.setOnSuccessListener(success);
//		mActivity.addAjax(ajax);
//		ajax.send();
//		return ajax;
//	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
}
