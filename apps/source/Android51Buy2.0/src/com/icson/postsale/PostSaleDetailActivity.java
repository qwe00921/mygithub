package com.icson.postsale;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.icson.R;
import com.icson.item.ImageGalleryAdapter;
import com.icson.item.ItemImageActivity;
import com.icson.lib.AppStorage;
import com.icson.lib.ILogin;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.ui.LinearListView;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.postsale.PostSaleDetailModel.HandleDetail;
import com.icson.postsale.PostSaleDetailModel.TwoColObject;
import com.icson.util.Config;
import com.icson.util.ImageHelper;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.Log;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnErrorListener;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class PostSaleDetailActivity extends BaseActivity implements OnSuccessListener<JSONObject>, OnErrorListener {
	
	private PostSaleControl mPostSaleControl;
	private Ajax mAjax;
	private static final String TAG = PostSaleDetailActivity.class.getSimpleName();
	private int mApplyId;
	private boolean mHasRequestedUrgent;
	private ImageLoader mImageLoader;
	private Handler mHandler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_postsale_detail);
		mImageLoader = new ImageLoader(this, true);
		initUI();

		Intent intent = getIntent();
		if(intent != null) {
			mApplyId = intent.getIntExtra(Constants.KEY_APPLY_ID, 0);
			mAjax = mPostSaleControl.getProductChangeDetail(mApplyId, this, this);
		} else {
			// Error handling
		}
	}
	
	@Override
	protected void onDestroy()
	{
		if(null!=mImageLoader)
		{
			mImageLoader.cleanup();
			mImageLoader = null;
		}
		mProductImageLoadListener = null;
		mApplyDetailImageLoadListener = null;
		super.onDestroy();
	}
	
	@Override
	public void onSuccess(JSONObject v, Response response) {

		try {
			final int errno = v.getInt("errno");

			if (errno != 0) {
				String strMsg = v.optString("data", "");
				if (errno == Config.NOT_LOGIN) {
					ILogin.clearAccount();
					UiUtils.makeToast(this,
							TextUtils.isEmpty(strMsg) ? "您已退出登录" : strMsg);
					MainActivity.startActivity(this, MainActivity.TAB_MY);
					return;
				}

				strMsg = TextUtils.isEmpty(strMsg) ? Config.NORMAL_ERROR
						: strMsg;
				UiUtils.makeToast(this, strMsg);
				mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						finish();
					}
				}, 1000);
				
				return;
			}
			JSONObject data = v.getJSONObject("data");
			PostSaleDetailModel detailModel = new PostSaleDetailModel();
			detailModel.parse(data);
			refreshData(detailModel);
			showLoading(false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void refreshProductInfoSection(PostSaleDetailModel model) {
		if(model == null) {
			Log.d(TAG, "[initProductInfoSection] model is null");
			return;
		}
		
		PostSaleItemModel item = model.getItem();
		if(item == null) {
			Log.d(TAG, "[initProductInfoSection] item is null");
			return;
		}
		
		setTextViewContent(R.id.order_info, "订单号：" + model.getOrderCharId());
		setTextViewContent(R.id.order_status, model.getStatus());
		
		String url = IcsonProImgHelper.getAdapterPicUrl(item.getProductCharId(), 95);
		refreshProductImage(url);
		
		setTextViewContent(R.id.product_title, item.getProductName());
		setTextViewContent(R.id.product_count, "共" + item.getProductNum() + "件");

		Button urgentButton = (Button) findViewById(R.id.postsale_urgent_button);
		if(urgentButton != null) {
			if(model.getCanUrgent() == 1) {
				urgentButton.setVisibility(View.VISIBLE);
			} else {
				urgentButton.setVisibility(View.GONE);
			}
			
			urgentButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mHasRequestedUrgent) {
						return;
					}
					mHasRequestedUrgent = true;
					mPostSaleControl.sendLevelUpRequest(mApplyId, mOnSendUrgentSuccessListener, PostSaleDetailActivity.this);
				}
			});
		}
	}
	
	private void refreshProductImage(String url) {
		Bitmap data = mImageLoader.get(url);

		ImageView imageView = (ImageView) findViewById(R.id.product_image);
		if(imageView != null) {
			imageView.setTag(url);
			if (data == null) {
				mImageLoader.get(url, mProductImageLoadListener);
				imageView.setImageBitmap(ImageHelper.getResBitmap(this, mImageLoader.getLoadingId()));
			} else {
				imageView.setImageBitmap(data);
			}
		}
	}
	
	private OnSuccessListener<JSONObject> mOnSendUrgentSuccessListener = new OnSuccessListener<JSONObject>() {
		@Override
		public void onSuccess(JSONObject v, Response response) {
			
			try {
				int errno = v.getInt("errno");
				if (errno != 0) {
					mUIHandler.sendEmptyMessage(MSG_SEND_URGENT_FAILED);
				} else {
					mUIHandler.sendEmptyMessage(MSG_SEND_URGENT_SUCCEED);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};
	
	private void refreshHandlePreviewSection(PostSaleDetailModel model) {
		if(model == null) {
			Log.d(TAG, "[initHandlePreviewSection] model is null");
			return;
		}
		setTextViewContent(R.id.handle_method_value, model.getHandleType());
		setTextViewContent(R.id.apply_reason_content, model.getCustomerDesc());
	}
	
	private int[] mImageViewArray = new int[] {R.id.picImage1, R.id.picImage2, R.id.picImage3, R.id.picImage4, R.id.picImage5};
	private void refreshImagesURLSection(final PostSaleDetailModel model) {
		if(model == null) {
			Log.d(TAG, "[refreshImagesURLSection] model is null");
			return;
		}
		
		List<String> imagesUrlList = model.getImagesUrlList();
		
		if(imagesUrlList != null) {
			View imageContainer = findViewById(R.id.postsale_pic_container);
			if(imagesUrlList.isEmpty()) {
				if(imageContainer != null) {
					imageContainer.setVisibility(View.GONE);
				}
			} else {
				if(imageContainer != null) {
					imageContainer.setVisibility(View.VISIBLE);
				}
				
				// 为每个ImageView设置图片
				for(int i = 0; i < imagesUrlList.size() && i < mImageViewArray.length; i++) {
					String url = imagesUrlList.get(i);
					Bitmap data = mImageLoader.get(url);

					ImageView imageView = (ImageView) findViewById(mImageViewArray[i]);
					if(imageView != null) {
						imageView.setTag(url);
						if (data == null) {
							mImageLoader.get(url, mApplyDetailImageLoadListener);
							imageView.setImageBitmap(ImageHelper.getResBitmap(this, mImageLoader.getLoadingId()));
						} else {
							imageView.setImageBitmap(data);
						}
						
						//点击查看大图
						final int viewPosition = i;
						imageView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View view) {
								Bundle param = new Bundle();
								ArrayList<String> bigImgUrlList = (ArrayList<String>) model.getBigImagesUrlList();
								if(bigImgUrlList != null && bigImgUrlList.size() > 0) {
									param.putStringArrayList(ItemImageActivity.REQUEST_IMGURL_LIST, bigImgUrlList);
									param.putInt(ItemImageActivity.REQUEST_PIC_INDEX, viewPosition);
									param.putInt(ItemImageActivity.REQUEST_DATASOURCE_TYPE, ImageGalleryAdapter.TYPE_IMAGE_URL_ARRAY);
									
									Activity activity = PostSaleDetailActivity.this;
									ToolUtil.startActivity(activity, ItemImageActivity.class, param);
								}
							}
						});
					}
				}
			}
		}
		
//		HorizontalListView list = (HorizontalListView) findViewById(R.id.item_pic_listview);
//		List<String> imagesUrlList = model.getImagesUrlList();
//		if(imagesUrlList != null && imagesUrlList.size() > 0) {
//			list.setVisibility(View.VISIBLE);
//			ImagesUrlAdapter adapter = new ImagesUrlAdapter(this, imagesUrlList);
//			list.setAdapter(adapter);
//			adapter.notifyDataSetChanged();
//		} else {
//			list.setVisibility(View.GONE);
//		}
	}
	
	private void refreshImageViewByUrl(Bitmap bitmap, String url) {
		if(url == null || bitmap == null) {
			return;
		}
		for(int i = 0; i < mImageViewArray.length; i++) {
			ImageView imageView = (ImageView) findViewById(mImageViewArray[i]);
			if(imageView != null) {
				if(url.equals(imageView.getTag())) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}
	
	private void refreshHandleDetailSection(PostSaleDetailModel model) {
		if(model == null) {
			Log.d(TAG, "[initHandleDetailSection] model is null");
			return;
		}
		HandleDetail detail = model.getHandleDetail();
		if(detail != null) {
			setTextViewContent(R.id.handle_detail_title, detail.getTitle());
			setTextViewContent(R.id.handle_detail_method, detail.getMethod());
			setTextViewContent(R.id.handle_detail_method_detail, detail.getMethodDetail());
			
			LinearListView methodFormListView = (LinearListView) findViewById(R.id.handle_detail_methodform);
			List<TwoColObject> methodFormList = detail.getMethodFormList();
			if(methodFormList != null && methodFormList.size() > 0) {
				PostSaleHandleDetailAdapter adapter = new PostSaleHandleDetailAdapter(this, PostSaleHandleDetailAdapter.VIEW_TYPE_METHOD_FORM);
				adapter.setDataSource(methodFormList);
				methodFormListView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				methodFormListView.setVisibility(View.VISIBLE);
			} else {
				methodFormListView.setVisibility(View.GONE);
			}
			
			LinearListView textAreaListView = (LinearListView) findViewById(R.id.handle_detail_textarea);
			List<String> textAreaList = detail.getTextAreaList();
			if(textAreaList != null && textAreaList.size() > 0) {
				PostSaleHandleDetailAdapter adapter = new PostSaleHandleDetailAdapter(this, PostSaleHandleDetailAdapter.VIEW_TYPE_TEXTAREA);
				adapter.setDataSource(textAreaList);
				textAreaListView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				textAreaListView.setVisibility(View.VISIBLE);
			} else {
				textAreaListView.setVisibility(View.GONE);
			}
			
			LinearListView formListView = (LinearListView) findViewById(R.id.handle_detail_form);
			List<TwoColObject> formList = detail.getFormList();
			if(formList != null && formList.size() > 0) {
				PostSaleHandleDetailAdapter adapter = new PostSaleHandleDetailAdapter(this, PostSaleHandleDetailAdapter.VIEW_TYPE_FORM);
				adapter.setDataSource(formList);
				formListView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				formListView.setVisibility(View.VISIBLE);
			} else {
				formListView.setVisibility(View.GONE);
			}
		}
	}
	
	private void refreshRevAddressSection(PostSaleDetailModel model) {
		if(model == null) {
			Log.d(TAG, "[initRevAddressSection] model is null");
			return;
		}
		setTextViewContent(R.id.rev_addr_value, model.getRevAddress());
	}
	
	private void refreshLogListSection(PostSaleDetailModel model) {
		if(model == null) {
			Log.d(TAG, "[initLogListSection] model is null");
			return;
		}
		LinearListView logListView = (LinearListView) findViewById(R.id.orderdetail_linear_log);
		List<PostSaleLogModel> logList = model.getLogModelList();
		if(logList != null && logList.size() > 0) {
			PostSaleLogAdapter adapter = new PostSaleLogAdapter(this, logList);
			logListView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			logListView.setVisibility(View.VISIBLE);
		} else {
			logListView.setVisibility(View.GONE);
		}
	}
	
	private void refreshData(PostSaleDetailModel model) {
		if(model == null) {
			Log.d(TAG, "[refreshData] model is null");
			return;
		}
		
		refreshProductInfoSection(model);
		refreshHandlePreviewSection(model);
		refreshImagesURLSection(model);
		refreshHandleDetailSection(model);
		refreshRevAddressSection(model);
		refreshLogListSection(model);
	}
	
	private void setTextViewContent(int tvId, Object content) {
		TextView textView = (TextView) findViewById(tvId);
		if(textView != null) {
			textView.setText(String.valueOf(content));
		}
	}
	
	private void initUI() {
		
		loadNavBar(R.id.postsale_detail_navigation_bar);
		setNavBarText(R.string.post_sale_request);
		mPostSaleControl = new PostSaleControl(this);
		showLoading(true);
	}
	
	private void showLoading(boolean show) {
		View loadingIcon = findViewById(R.id.global_loading);
		if(loadingIcon != null) {
			if(show) {
				loadingIcon.setVisibility(View.VISIBLE);
			} else {
				loadingIcon.setVisibility(View.GONE);
			}
		}
		
		View dataView = findViewById(R.id.global_container);
		if(loadingIcon != null) {
			if(show) {
				dataView.setVisibility(View.GONE);
			} else {
				dataView.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onError(Ajax ajax, Response response) {
		showLoading(false);
		if (mAjax == ajax) {
			mAjax = null;
			UiUtils.makeToast(this, R.string.network_error);

			// Reset the reload value.
			AppStorage.setData(AppStorage.SCOPE_DEFAULT,
					AppStorage.KEY_MINE_RELOAD, "1", false);
		} else {
			super.onError(ajax, response);
		}
	}
	
	private void onSendUrgentSucceed() {
		Button urgentButton = (Button) findViewById(R.id.postsale_urgent_button);
		if(urgentButton != null) {
			urgentButton.setVisibility(View.GONE);
		}
		UiUtils.makeToast(this, R.string.message_send_success);
	}
	
	private void onSendUrgentFailed() {
		UiUtils.makeToast(this, R.string.message_send_fail);
	}
	
	private UIHandler mUIHandler = new UIHandler(this);
	public static final int MSG_SEND_URGENT_SUCCEED = 2001;
	public static final int MSG_SEND_URGENT_FAILED = 2002;
	private static class UIHandler extends Handler {
		private WeakReference<PostSaleDetailActivity> mRef;
		public UIHandler(PostSaleDetailActivity activity) {
			mRef = new WeakReference<PostSaleDetailActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			if(msg == null) {
				Log.e(TAG, "[handleMessage] msg is null!");
				return;
			}
			
			PostSaleDetailActivity activity = mRef.get();
			if(activity == null) {
				Log.w(TAG, "[handleMessage] activity is null when handle message, the activity should be destoryed already");
				return;
			}
			int what = msg.what;
			switch(what) {
				case MSG_SEND_URGENT_SUCCEED: {
					activity.onSendUrgentSucceed();
					break;
				}
				case MSG_SEND_URGENT_FAILED: {
					activity.onSendUrgentFailed();
					break;
				}
				default: {
					// do nothing
					break;
				}
			}
		}
	}
	
	private ImageLoadListener mApplyDetailImageLoadListener = new ImageLoadListener() {
		@Override
		public void onLoaded(Bitmap aBitmap, String strUrl) {
			refreshImageViewByUrl(aBitmap, strUrl);		
		}


		@Override
		public void onError(String strUrl) {
			Log.d(TAG, "[onError] strUrl = " + strUrl);
		}
	};
	
	private ImageLoadListener mProductImageLoadListener = new ImageLoadListener() {
		@Override
		public void onLoaded(Bitmap aBitmap, String strUrl) {
			refreshProductImage(strUrl);		
		}


		@Override
		public void onError(String strUrl) {
			Log.d(TAG, "[onError] strUrl = " + strUrl);
		}
	};

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_PostSaleDetailActivity);
	}
}
