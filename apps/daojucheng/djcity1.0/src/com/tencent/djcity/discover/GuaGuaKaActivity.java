package com.tencent.djcity.discover;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.djcity.R;
import com.tencent.djcity.discover.GuaGuaKaView.OnFinishScratchListener;
import com.tencent.djcity.lib.ILogin;
import com.tencent.djcity.lib.ui.NavigationBar;
import com.tencent.djcity.lib.ui.NavigationBar.OnLeftButtonClickListener;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.util.AjaxUtil;
import com.tencent.djcity.util.activity.BaseActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.JSONParser;
import com.tencent.djcity.util.ajax.OnErrorListener;
import com.tencent.djcity.util.ajax.OnSuccessListener;
import com.tencent.djcity.util.ajax.Response;

public class GuaGuaKaActivity extends BaseActivity {
	
	private TextView mAmountView;
	private GuaGuaKaView mGuaGuaKaView;
//	private Button mHistoryBtn;
	private TextView mGiftCenterBtn;
	private TextView mHowToBtn;
	
	private View mGuaguaKaLayout;
	private View mEmpty;
	
	private GestureDetector mDetector;
	
	private boolean mHasRequested;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activitiy_guaguaka);
        
        mNavBar = (NavigationBar) findViewById(R.id.category_navbar);
        mNavBar.setOnIndicatorClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0)
			{
				Intent intent = new Intent(GuaGuaKaActivity.this, HistoryActivity.class);
				
				startActivity(intent);				
			}
		});
        
        mNavBar.setOnLeftButtonClickListener(new OnLeftButtonClickListener() {
        	@Override
        	public void onClick()
        	{
        		onBackPressed();
        	}
        });
        
        mEmpty = findViewById(R.id.guaguaka_empty_layout);
        mGuaguaKaLayout = findViewById(R.id.guaguaka_layout);
        mGuaguaKaLayout.setVisibility(View.INVISIBLE);
        mAmountView = (TextView) findViewById(R.id.guaguaka_amount);
//        mHistoryBtn = (Button) findViewById(R.id.info_btn);
        
//        mHistoryBtn.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(GuaGuaKaActivity.this, HistoryActivity.class);
//				
//				startActivity(intent);
//			}
//		});
//        mHistoryBtn.setText(R.string.show_history);
        mGuaGuaKaView = (GuaGuaKaView)findViewById(R.id.guaguaka_view);
        mGuaGuaKaView.setOnFinishScratchListener(new OnFinishScratchListener() {
			
			@Override
			public void onFinish() {
					
				sendScratchFinish();
			}
		});
        
        mGiftCenterBtn = (TextView) findViewById(R.id.guaguaka_gift_center);
        
        mGiftCenterBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0)
			{
				UiUtils.startActivity(GuaGuaKaActivity.this, GiftcenterActivity.class
						, true);
			}
		});
        
        mHowToBtn = (TextView) findViewById(R.id.guaguaka_tips);
        mHowToBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0)
			{
				//TODO: 跳转到tips界面
//				UiUtils.startActivity(GuaGuaKaActivity.this, GiftcenterActivity.class, true);
			}
		});
        
        
        mDetector = new GestureDetector(mGestureListener);
        prepareNewCard();
    }
    
    private OnGestureListener mGestureListener = new OnGestureListener() {
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}
		
		@Override
		public void onShowPress(MotionEvent e) {
			
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			return false;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			
	        if (e1.getX() - e2.getX() < -120) {
	        	if(!mGuaGuaKaView.hasScratched()) {
	        		showToast(getString(R.string.guaguaka_cant_next));
	        	} else {
	        		prepareNewCard();
	        	}
	            return true;
	        }
	        return true;
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}
	};
	
	private void showToast(String text) {

		Toast toast = Toast.makeText(GuaGuaKaActivity.this, text, Toast.LENGTH_LONG);
		
//		int[] array = new int[2];
//		mGiftCenterBtn.getLocationInWindow(array);
//		
//		int y = array[1];
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
    private void requestAmountData() {
    	Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/getLimit.php?type=scrath&uin=1234565");
    	
    	if(ajax == null) {
    		return;
    	}
    	
    	ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
    		public void onSuccess(JSONObject v, Response response) {
				int ret = v.optInt("ret");
				String msg = v.optString("msg");
				if(ret != 0) {
					UiUtils.makeToast(GuaGuaKaActivity.this, msg);
					return;
				}
				
				JSONObject data = v.optJSONObject("data");
				if(data != null) {
					
					int left = data.optInt("left");
//					String str = getResources().getString(R.string.guaguaka_amount_text, left);
					mAmountView.setText(left + "张");
					if(left == 0) {
						closeProgressLayer();
						showEmptyView();
					} else {
						hideEmptyView();
					}
				}
				
    		};
		});
    	
    	ajax.setOnErrorListener(this);
    	ajax.setParser(new JSONParser());
    	ajax.send();
    }
    
    private void hideEmptyView() {
    	mEmpty.setVisibility(View.INVISIBLE);
    	mGuaguaKaLayout.setVisibility(View.VISIBLE);
    }
    
    private void showEmptyView() {
    	mEmpty.setVisibility(View.VISIBLE);
    	mGuaguaKaLayout.setVisibility(View.INVISIBLE);
    }
    
    private void sendScratchFinish() {
    	
    	showProgressLayer("处理中...");
    	Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/getScratch.php");
    	if(ajax == null) {
    		return;
    	}
    	ajax.setData("uin", ILogin.getLoginUin());
    	ajax.setData("id", currentScratchId);
    	ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
    		public void onSuccess(JSONObject v, Response response) {
    			closeProgressLayer();
    			int ret = v.optInt("ret");
				String msg = v.optString("msg");
				if(ret != 0) {
					UiUtils.makeToast(GuaGuaKaActivity.this, msg);
					return;
				} else {
					//刮成功了，可以向左滑动来拿下一张
					showToast(getString(R.string.guaguaka_next_tips));
				}
    		};
		});
    	ajax.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public void onError(Ajax ajax, Response response) {
				closeProgressLayer();
			}
		});
    	ajax.setParser(new JSONParser());
    	ajax.send();
    }
    
	private void prepareNewCard() {
		showProgressLayer("正在准备刮刮卡，请稍候...");
		requestAmountData();
        requestScratchData();
	}
    
    private int currentScratchId;
    private void requestScratchData() {
    	Ajax ajax = AjaxUtil.get("http://apps.game.qq.com/daoju/v3/test_apps/getScratch.php");
    	if(ajax == null) {
    		return;
    	}
    	ajax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
    		public void onSuccess(JSONObject v, Response response) {
    			closeProgressLayer();
				int ret = v.optInt("ret");
				String msg = v.optString("msg");
				if(ret != 0) {
					UiUtils.makeToast(GuaGuaKaActivity.this, msg);
					return;
				}
				JSONArray data = v.optJSONArray("list");
				if(data != null) {
					JSONObject object = data.optJSONObject(0);
					
					if(object != null) {
						String goodsName = object.optString("sGoodsName");
						int quantity = object.optInt("iQuantity");
						currentScratchId = object.optInt("iScratchId");
						mGuaGuaKaView.reset();
						mGuaGuaKaView.setText(goodsName);
					}
//					int left = data.optInt("left");
//					String str = getResources().getString(R.string.guaguaka_amount_text, left);
//					mAmountView.setText(str);
//					if(left == 0) {
//						showEmptyView();
//					} else {
//						hideEmptyView();
//						requestScratchData();
//					}
				}
				
    		};
		});
    	ajax.setOnErrorListener(this);
    	ajax.setParser(new JSONParser());
    	ajax.send();
    }
    
    @Override
    public void onError(Ajax ajax, Response response) {
    	closeProgressLayer();
		showEmptyView();
    	super.onError(ajax, response);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	return mDetector.onTouchEvent(event);
    }
}