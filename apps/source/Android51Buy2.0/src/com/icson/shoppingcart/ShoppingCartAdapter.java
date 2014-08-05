package com.icson.shoppingcart;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.lib.FullDistrictHelper;
import com.icson.lib.ILogin;
import com.icson.lib.model.ShoppingCartProductModel;
import com.icson.lib.ui.UiUtils;
import com.icson.util.AjaxUtil;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity.DestroyListener;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class ShoppingCartAdapter extends BaseAdapter implements DestroyListener, ImageLoadListener {
	private ArrayList<ShoppingCartProductModel> mShoppingCartProductModels;
	private ShoppingCartActivity 				mActivity;
	private ImageLoader 						mImageLoader;
	private LayoutInflater 						mInflater;
	
	private boolean 							mEditView 	= false;
	private boolean 							mProductIsErrorItems = false;

	

	public ShoppingCartAdapter(ShoppingCartActivity activity, ArrayList<ShoppingCartProductModel> mShoppingCartProductModels) {
		mActivity = activity;
		this.mShoppingCartProductModels = mShoppingCartProductModels;
		mInflater = LayoutInflater.from(mActivity);
		mActivity.addDestroyListener(this);
	}

	public void setEditView(boolean mEditView) {
		if (this.mEditView != mEditView) {
			this.mEditView = mEditView;
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return (null == mShoppingCartProductModels ? 0 : mShoppingCartProductModels.size());
	}

	@Override
	public Object getItem(int position) {
		return (null == mShoppingCartProductModels ? null : mShoppingCartProductModels.get(position));
	}

	@Override
	public long getItemId(int position) {
		if(mShoppingCartProductModels == null || mShoppingCartProductModels.get(position) == null) {
			return 0;
		}
		return mShoppingCartProductModels.get(position).getProductId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.shoppingcart_activity_item, null);
			holder = new ItemHolder();
			holder.name = ((TextView) convertView.findViewById(R.id.cart_textview_name));
			holder.price = ((TextView) convertView.findViewById(R.id.cart_textview_price));
			holder.promo = ((TextView) convertView.findViewById(R.id.cart_textview_promo));
			holder.edit = ((EditText) convertView.findViewById(R.id.cart_editext));
			holder.image = (ImageView) convertView.findViewById(R.id.cart_imageview);
			holder.deleteBtn = (ImageView)convertView.findViewById(R.id.deleteBtn);
			holder.collectBtn = (TextView)convertView.findViewById(R.id.collectBtn);
			holder.upBtn = (Button)convertView.findViewById(R.id.upBtn);
			holder.downBtn = (Button)convertView.findViewById(R.id.downBtn);
			holder.proStatus = (TextView)convertView.findViewById(R.id.statusView);
			holder.productTag = (ImageView) convertView.findViewById(R.id.product_tag);
			holder.textViewNum = (TextView) convertView.findViewById(R.id.cart_textview_num);
			convertView.setTag(holder);
		} else {
			holder = (ItemHolder) convertView.getTag();
		}

		final ShoppingCartProductModel model = mShoppingCartProductModels.get(position);
		// 价格
		holder.price.setText(mActivity.getString(R.string.rmb) + model.getShowPriceStr());
		
		// 商品名称
		holder.name.setText(Html.fromHtml(model.getName()));
		//商品促销语和赠品，单品赠券也显示赠
		holder.promo.setText(model.getPromotionWord());
		boolean isCouponGiftProduct = false;
		ProductCouponGiftModel couponModel = model.getCouponGiftModel() ;
		if(null != couponModel) {
			isCouponGiftProduct = true;
		}
		Drawable pGiftIcon = (model.getGiftCount() > 0 || isCouponGiftProduct ) ? mActivity.getResources().getDrawable(R.drawable.i_list_activity_gift) : null;
		if (pGiftIcon != null) {
			pGiftIcon.setBounds(0, 0, pGiftIcon.getMinimumWidth(), pGiftIcon.getMinimumHeight());
		}
		holder.promo.setCompoundDrawables(null, null, pGiftIcon, null);
		//删除按钮
		holder.deleteBtn.setVisibility(mEditView ? View.GONE : View.VISIBLE);
		holder.deleteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToolUtil.reportStatisticsClick(mActivity.getActivityPageId(), "22003");
				mActivity.deleteProduct(model.getProductId(), false);
				ToolUtil.sendTrack(ShoppingCartActivity.class.getClass().getName(), mActivity.getString(R.string.tag_ShoppingCartActivity), "delete.mShoppingCartView", mActivity.getString(R.string.tag_ShoppingCartActivity), "05011", String.valueOf(model.getProductId()));
			}
		});
		
	
		 
		int promoType = model.getPromoType();
		//满赠商品 加价购商品 单独处理
		if(ShoppingCartProductModel.PRODUCT_BENEFIT_FREE_GFIT == promoType || ShoppingCartProductModel.PRODUCT_BENEFIT_LESS_PRICE_BUY == promoType) {
			holder.upBtn.setVisibility(View.GONE);
			holder.edit.setVisibility(View.GONE);
			holder.downBtn.setVisibility(View.GONE);
			holder.collectBtn.setVisibility(View.GONE);
			holder.textViewNum.setVisibility(View.VISIBLE);
			
			holder.textViewNum.setText(model.getBuyCount() + "件");
			
			//商品角标
			Drawable tag = null;
			if(ShoppingCartProductModel.PRODUCT_BENEFIT_FREE_GFIT == promoType) {
				//满赠商品
				tag = mActivity.getResources().getDrawable(R.drawable.freegifts_product_tag);
			}else if(ShoppingCartProductModel.PRODUCT_BENEFIT_LESS_PRICE_BUY == promoType) {
				//加价购商品
				tag = mActivity.getResources().getDrawable(R.drawable.lessprice_product_tag);
			}
			
			if(null != tag) {
				holder.productTag.setVisibility(View.VISIBLE);
				holder.productTag.setImageDrawable(tag);
			}else{
				holder.productTag.setVisibility(View.GONE);
			}
		}else{
			//普通商品
			holder.upBtn.setVisibility(View.VISIBLE);
			holder.edit.setVisibility(View.VISIBLE);
			holder.downBtn.setVisibility(View.VISIBLE);
			holder.textViewNum.setVisibility(View.GONE);
			
			final int pBuyCount = model.getBuyCount();
			final int pLowCount = model.getLowestNum();
			final int pLimitCount = model.getNumLimit();
			int proStatus = model.getProductConflictState();
			mProductIsErrorItems = proStatus > 0? true : false;
			
			if( mProductIsErrorItems)
				holder.price.setTextColor(mActivity.getResources().getColor(R.color.global_text_info_color));
			else
				holder.price.setTextColor(mActivity.getResources().getColor(R.color.global_price));
				
			//数量
			holder.edit.setVisibility(mEditView && !mProductIsErrorItems ? View.VISIBLE : View.GONE);
			holder.edit.setText(String.valueOf(pBuyCount));
			holder.edit.addTextChangedListener(new TextWatcher(){
				@Override
				public void beforeTextChanged(CharSequence s, int start,int count, int after) {
					
				}
	
				@Override
				public void onTextChanged(CharSequence s, int start, int count,int after) {
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					String numStr = s.toString();
					
					//同步到在线购物车
					if(!"".equals(numStr)){
						int qingliang;
						setBuyCountToOnlineShoppingCart(model,Integer.valueOf(numStr));
					}
				}	
			});
			
			
			//收藏按钮
			holder.collectBtn.setVisibility( ( mEditView || mProductIsErrorItems || model.IsWangGou() ) ? View.GONE : View.VISIBLE);
	//		if((View.VISIBLE  == holder.collectBtn.getVisibility()) && model.getIsCollected()) {
	//			holder.collectBtn.setImageResource(R.drawable.collect_item_press);
	//		}else if((View.VISIBLE  == holder.collectBtn.getVisibility()) && !model.getIsCollected()) {
	//			holder.collectBtn.setImageResource(R.drawable.collect_item_normal);
	//		}
			
			holder.collectBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mActivity.collectProduct(model.getProductId());
					ToolUtil.sendTrack(ShoppingCartActivity.class.getClass().getName(), mActivity.getString(R.string.tag_ShoppingCartActivity), "collect.mShoppingCartView", mActivity.getString(R.string.tag_ShoppingCartActivity), "05012", String.valueOf(model.getProductId()));
				}
	
			});
			
			
			//增加数量按钮
			holder.upBtn.setVisibility(mEditView && !mProductIsErrorItems ? View.VISIBLE : View.GONE);
			holder.upBtn.setEnabled(pBuyCount < pLimitCount);
			holder.upBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int num = model.getBuyCount() + 1;
					setBuyCountToOnlineShoppingCart(model, num);
					ToolUtil.sendTrack(ShoppingCartActivity.class.getClass().getName(), mActivity.getString(R.string.tag_ShoppingCartActivity), "add.mShoppingCartView", mActivity.getString(R.string.tag_ShoppingCartActivity), "05013", String.valueOf(model.getProductId()));
				}
			});
			
			//减少数量按钮
			holder.downBtn.setVisibility(mEditView && !mProductIsErrorItems? View.VISIBLE : View.GONE);
	//		holder.downBtn.setEnabled(pBuyCount > pLowCount);
			holder.downBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if( (pBuyCount == 1 && pLowCount <= 1) ||  (pBuyCount <= pLowCount && pLowCount > 1)){
						//是否是n件起售商品
						boolean isLowProduct = (pLowCount > 1) ? true : false;
						mActivity.deleteProduct(model.getProductId(), isLowProduct);
					}else{
						int num = pBuyCount - 1;
						setBuyCountToOnlineShoppingCart(model, num);
					}
					
					ToolUtil.sendTrack(ShoppingCartActivity.class.getClass().getName(), mActivity.getString(R.string.tag_ShoppingCartActivity), "decrease.mShoppingCartView", mActivity.getString(R.string.tag_ShoppingCartActivity), "05014", String.valueOf(model.getProductId()));
				}
			});
			
			holder.proStatus.setVisibility(mProductIsErrorItems ? View.VISIBLE : View.GONE);
			holder.proStatus.setText(model.getProductConflictDesc());
		}
		
		//加载商品图片
		if(model.IsWangGou())
		{
			loadImage(holder.image, model.getSCWanggouUrl(80));
		}
		else
		{
			loadImage(holder.image, model.getAdapterProductUrl(80));
		}

		convertView.setTag(R.layout.shoppingcart_activity_item, model.getProductId());
		convertView.setOnCreateContextMenuListener(mActivity);
		convertView.setEnabled(true);
		return convertView;
	}
	


	private void setBuyCountToOnlineShoppingCart(ShoppingCartProductModel model, int num) {
		int numLimit = model.getNumLimit();
		if (numLimit != 0 && num > numLimit) {
			UiUtils.makeToast(mActivity, "该商品最多购买" + numLimit + "件, 请直接修改购买数量");
			return ;
		}
		int lowestNum = model.getLowestNum();
		if (lowestNum != 0 && num < lowestNum) {
			UiUtils.makeToast(mActivity, "商品\"" + model.getNameNoHTML() + "\"最低" + lowestNum + "件起售");
			return ;
		}
		
		final long uid = ILogin.getLoginUid();
//		Ajax ajax = ServiceConfig.getAjax(Config.URL_CART_UPDATE_PRODUCT);
		Ajax ajax = AjaxUtil.post("http://mgray.yixun.com/cart/ModifyProduct?mod=cart");
		
		if( null == ajax )
			return ;
		
		int nPromoType = model.getPromoType();
		int nRuleId = model.getRuleId();
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("district", FullDistrictHelper.getDistrictId());
		data.put("uid", uid);
		data.put("pid", model.getProductId());
		data.put("pnum", num);
		if(ShoppingCartProductModel.PRODUCT_BENEFIT_FREE_GFIT == nPromoType || ShoppingCartProductModel.PRODUCT_BENEFIT_LESS_PRICE_BUY == nPromoType) {
			//满赠或加价购商品
			data.put("promotionId", nRuleId);
		}
		//ajax.setId(REQUEST_SET_BUYCOUNT);
		ajax.setData(data);
		ajax.setParser(new JSONParser());
		ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>(){
			@Override
			public void onSuccess(JSONObject v, Response response) {
				int errno = v.optInt("errno", -1);
				String strErrMsg = v.optString("data");
				mActivity.getShoppingCartView().saveShoppingCart();
				
				if(0 == errno)
					return;
				if( TextUtils.isEmpty(strErrMsg) ) 
					strErrMsg = "修改数量失败";
				UiUtils.makeToast(mActivity, strErrMsg);
			}
			
		});
		ajax.setOnErrorListener(mActivity);
		mActivity.addAjax(ajax);
		ajax.send();
	}

	private void loadImage(ImageView view, String url) {
		if(null==mImageLoader)
			mImageLoader = new ImageLoader(mActivity, Config.PIC_CACHE_DIR, true);
		final Bitmap data = mImageLoader.get(url);
		if (data != null) {
			view.setImageBitmap(data);
			return;
		}
//		view.setImageResource(mImageLoader.getLoadingId());
		view.setImageBitmap(mImageLoader.getLoadingBitmap(mActivity));
		mImageLoader.get(url, this);
	}

	@Override
	public void onLoaded(Bitmap image, String url) {
		notifyDataSetChanged();
	}
	
	@Override
	public void onError(String strUrl) {
		
	}

	@Override
	public void onDestroy() {
		mShoppingCartProductModels = null;
		cleanUpBitmap();
		mInflater = null;
		mActivity = null;
	}
	public void cleanUpBitmap()
	{
		if( null != mImageLoader )
		{
			mImageLoader.cleanup();
			mImageLoader = null;
		}
	}

	private class ItemHolder {
		TextView 	name;
		TextView 	price;
		TextView 	promo;
		EditText 	edit;
		Button 		upBtn;
		Button 		downBtn;
		ImageView 	image;
		ImageView 	deleteBtn;
		TextView 	collectBtn;
		TextView 	proStatus;
		ImageView   productTag; //商品的角标
		TextView	textViewNum; //满赠 加价购商品的数量
	}
}
