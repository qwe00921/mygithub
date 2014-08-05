package com.icson.message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.icson.R;
import com.icson.event.EventActivityFactory;
import com.icson.event.EventBaseActivity;
import com.icson.home.HTML5LinkActivity;
import com.icson.home.HomeActivity;
import com.icson.item.ItemActivity;
import com.icson.lib.ILogin;
import com.icson.lib.IcsonProImgHelper;
import com.icson.lib.ui.UiUtils;
import com.icson.main.MainActivity;
import com.icson.more.FeedBackHistoryActivity;
import com.icson.my.coupon.CouponShowActivity;
import com.icson.my.coupon.MyCouponActivity;
import com.icson.my.main.MyPointsActivity;
import com.icson.my.orderdetail.OrderDetailActivity;
import com.icson.postsale.Constants;
import com.icson.postsale.PostSaleDetailActivity;
import com.icson.push.MessageParser;
import com.icson.push.MsgEntity;
import com.icson.slotmachine.SlotMachineActivity;
import com.icson.statistics.StatisticsEngine;
import com.icson.statistics.StatisticsUtils;
import com.icson.util.Config;
import com.icson.util.ImageLoadListener;
import com.icson.util.ImageLoader;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.JSONParser;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;
import com.icson.virtualpay.VirtualPayActivity;

public class MessageActivity extends BaseActivity implements
		OnSuccessListener<Object>, OnItemClickListener, OnScrollListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_message);

		// Load navigation bar.
		this.loadNavBar(R.id.message_navbar);

		mListView = (ListView) findViewById(R.id.message_list);
		mNoMessages = findViewById(R.id.message_empty_layout);

		mMsgCache = new MessageCache(this);
		mAdapter = new MessageAdapter(this, mMsgCache);
		mParser = new MessageParser();

		// Update adapter.
		mListView.setOnItemClickListener(this);
		mListView.setHeaderDividersEnabled(false);
		mListView.setFooterDividersEnabled(false);
		mListView.setOnScrollListener(this);

		mListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				final int nEvent = event.getAction();
				final int nFirstPosition = mListView.getFirstVisiblePosition();
				switch (nEvent) {
				case MotionEvent.ACTION_DOWN:
					if ((!mScrolling) && (0 == nFirstPosition)) {
						mScrolling = true;
						mInitY = (int) event.getY();
					}
					break;
				case MotionEvent.ACTION_MOVE:
					mMoveY = (int) event.getY();
					if ((!mScrolling) && (0 == nFirstPosition)) {
						mScrolling = true;
						mInitY = (int) event.getY();
					}
					if (!mScrolling || PULL_STATUS_LOADING == mStatus) {
						return false;
					}

					final int nOffset = (mMoveY - mInitY) / 2;
					handleMove(nOffset);
					break;
				case MotionEvent.ACTION_UP:
					mLastTouch.right = (int) event.getX();
					mLastTouch.bottom = (int) event.getY();

					// Handle event for up for scrolling.
					mScrolling = false;
					handleUp();
					break;
				}

				return false;
			}
		});

		mFooterView = (LinearLayout) LayoutInflater.from(this).inflate(
				R.layout.global_listview_loading, null);
		mHeaderView = (LinearLayout) LayoutInflater.from(this).inflate(
				R.layout.global_listview_loading, null);
		this.addHeaderView();

		mListView.setAdapter(mAdapter);

		// Try to request the latest message list.
		this.requestMessages(true);
	}

	private boolean handleMove(int nOffset) {
		if (null == mListView)
			return false;
		boolean bHandled = false;
		switch (mStatus) {
		case PULL_STATUS_NONE:
			if (nOffset > 0) {
				this.addHeaderView();

				// Update status to pulling.
				mStatus = PULL_STATUS_PULLING;
				bHandled = true;
			}
			break;

		case PULL_STATUS_PULLING:
			if (nOffset > 40) {

				// Change the status to releasing.
				mStatus = PULL_STATUS_RELEASE;

				// Update text for header view.
			}
			break;
		}

		return bHandled;
	}

	private boolean handleUp() {
		switch (mStatus) {
		case PULL_STATUS_NONE:
		case PULL_STATUS_LOADING:
			break;

		case PULL_STATUS_PULLING:
			this.removeHeaderView();
			mStatus = PULL_STATUS_NONE;
			break;

		case PULL_STATUS_RELEASE:
			mStatus = PULL_STATUS_LOADING;
			this.requestMessages(true);
			break;
		}

		return false;
	}

	@Override
	protected void onDestroy() {
		if (null != mMsgCache) {
			mMsgCache.saveCache();
			mMsgCache = null;
		}

		if (null != mAdapter) {
			mAdapter.notifyDataSetChanged();
			mAdapter.cleanup();
			mAdapter = null;
		}

		super.onDestroy();
	}

	/**
	 * requestLatest
	 */
	private void requestMessages(boolean bGetLatest) {
		if (null != mActive)
			return;

		mGetLatest = bGetLatest;

		// Send request for get latest message.
		mActive = ServiceConfig.getAjax(Config.URL_GET_MESSAGES);
		if (null == mActive)
			return;
		mActive.setParser(mParser);
		mActive.setOnSuccessListener(this);
		mActive.setOnErrorListener(this);

		final long nStart = mGetLatest ? mMsgCache.getFirstTag() : 0;
		final long nEnd = mGetLatest ? 0 : mMsgCache.getLastTag();

		mActive.setData("begin", nStart);
		mActive.setData("end", nEnd);
		mActive.setData("uid", ILogin.getLoginUid());
		mActive.setData("deviceId", StatisticsUtils.getDeviceUid(this));

		addAjax(mActive);
		mActive.send();
	}

	@Override
	public void onSuccess(Object v, Response response) {
		mActive = null;

		@SuppressWarnings("unchecked")
		ArrayList<MsgEntity> aEntities = (ArrayList<MsgEntity>) v;
		final int nSize = (null != aEntities ? aEntities.size() : 0);
		if (null == mMsgCache) {
			return;
		}

		// Check need show more. Always!
		mShowMore = (nSize >= mParser.mDefaultNum);

		// Check the size.
		if (mGetLatest) {
			final int nDefaultNum = mParser.mDefaultNum;
			if (nSize >= nDefaultNum) {
				// Clear previous content.
				mMsgCache.clear();
			}

			// Remove header view.
			this.removeHeaderView();
			mStatus = PULL_STATUS_NONE;

			// Save the entities.
			for (int nIdx = nSize - 1; nIdx >= 0; nIdx--) {
				MsgEntity entity = aEntities.get(nIdx);
				mMsgCache.addFirst(entity);
			}
		} else {
			// Remove footer view.
			this.removeFooterView();

			for (int nIdx = 0; nIdx < nSize; nIdx++) {
				MsgEntity entity = aEntities.get(nIdx);
				mMsgCache.append(entity);
			}
		}

		if (0 >= mMsgCache.getCount()) {
			mListView.setEmptyView(mNoMessages);
		} 
		
		if (null != mAdapter) {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onError(Ajax ajax, Response aResponse) {
		mActive = null;

		mStatus = PULL_STATUS_NONE;
		this.removeHeaderView();
		this.removeFooterView();

		// super.onError(ajax, aResponse);
		if (0 >= mMsgCache.getCount()) {
			mListView.setEmptyView(mNoMessages);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// Update position information.
		int nOffset = position;
		if ((null != mHeaderView) && (mListView.getHeaderViewsCount() > 0))
			nOffset--;

		if (nOffset < 0)
			return;

		if (spanClicked(mListView, view, R.id.message_text)) {
			return;
		}

		MsgEntity selected = (MsgEntity) (null != mAdapter ? mAdapter
				.getItem(nOffset) : null);
		if (null == selected)
			return;

		// if( null != view ) {
		// view.setBackgroundResource(R.drawable.i_gift_tab_bg);
		// }

		// Operation on current message entity selected.
		MessageActivity.processEntity(this, selected, "msgcenter");
	}

	/**
	 * viewDetail
	 * 
	 * @param aEntity
	 */
	public static void processEntity(BaseActivity aParent, MsgEntity aEntity,
			String strFrom) {
		if (null == aEntity)
			return;

		// Check status.
		if (MsgEntity.STATUS_UNREAD == aEntity.mStatus) {
			// Send request to set status.
			Ajax pAjax = ServiceConfig.getAjax(Config.URL_SET_MESSAGE_STATUS);
			if (null == pAjax)
				return;
			pAjax.setParser(new JSONParser());
			pAjax.setOnSuccessListener(new OnSuccessListener<JSONObject>() {
				@Override
				public void onSuccess(JSONObject v, Response response) {
				}
			});

			pAjax.setData("msgId", aEntity.mId);
			pAjax.setData("status", MsgEntity.STATUS_READ);
			pAjax.setData("uid", ILogin.getLoginUid());
			pAjax.setData("deviceId", StatisticsUtils.getDeviceUid(aParent));
			pAjax.setData("from", strFrom);

			aParent.addAjax(pAjax);
			pAjax.send();

			// Track the event.
			StatisticsEngine.trackEvent(aParent,
					strFrom.equals("push") ? "read_push" : "read_message");
		}

		// Update status.
		aEntity.mStatus = MsgEntity.STATUS_READ;
		String strLocationId = String.valueOf(aEntity.mBizId);

		switch (aEntity.mBizId) {
		case MsgEntity.BIZ_ID_NEW_ARRIVALS:
		case MsgEntity.BIZ_ID_PRO_INFO:
			if (!TextUtils.isEmpty(aEntity.mValue)
					&& TextUtils.isDigitsOnly(aEntity.mValue)) {
				Bundle pBundle = new Bundle();
				final long nProductId = Long.valueOf(aEntity.mValue);
				pBundle.putLong(ItemActivity.REQUEST_PRODUCT_ID, nProductId);

				// Check the multi-price id.
				if (!TextUtils.isEmpty(aEntity.mExtra)
						&& TextUtils.isDigitsOnly(aEntity.mExtra)) {
					pBundle.putInt(ItemActivity.REQUEST_CHANNEL_ID,
							Integer.valueOf(aEntity.mExtra));
				}

				ToolUtil.startActivity(aParent, ItemActivity.class, pBundle);

				ToolUtil.sendTrack("push_message",
						aParent.getString(R.string.tag_push_message),
						ItemActivity.class.getName(),
						aParent.getString(R.string.tag_ItemActivity),
						strLocationId, String.valueOf(nProductId));
				StatisticsEngine
						.trackEvent(
								aParent,
								strFrom
										+ (MsgEntity.BIZ_ID_PRO_INFO == aEntity.mBizId ? "_new_arrival"
												: "_view_detail"), "productId="
										+ nProductId);
			}
			break;
		case MsgEntity.BIZ_ID_RECHARGE: {
			ToolUtil.startActivity(aParent, VirtualPayActivity.class);
			ToolUtil.sendTrack("push_message",
					aParent.getString(R.string.tag_push_message),
					VirtualPayActivity.class.getName(),
					aParent.getString(R.string.tag_VirtualPayActivity),
					strLocationId);
			StatisticsEngine.trackEvent(aParent, strFrom + "_recharge");
		}
			break;

		case MsgEntity.BIZ_ID_COUPON: {
			ToolUtil.startActivity(aParent, CouponShowActivity.class);
			ToolUtil.sendTrack("push_message",
					aParent.getString(R.string.tag_push_message),
					CouponShowActivity.class.getName(),
					aParent.getString(R.string.tag_CouponShowActivity),
					strLocationId);
			StatisticsEngine.trackEvent(aParent, strFrom + "_show_coupon");
		}
			break;

		case MsgEntity.BIZ_ID_EVENT:
			if ((!TextUtils.isEmpty(aEntity.mValue))
					&& (TextUtils.isDigitsOnly(aEntity.mValue))
					&& (!TextUtils.isEmpty(aEntity.mExtra))
					&& (TextUtils.isDigitsOnly(aEntity.mExtra))) {
				// Handle URL result for event.
				Class<?> pClassName = EventActivityFactory
						.getEventActivityClass(Integer.valueOf(aEntity.mValue));
				if (null != pClassName) {
					Bundle pBundle = new Bundle();
					final long nEventId = Long.valueOf(aEntity.mExtra);
					pBundle.putLong(EventBaseActivity.ERQUEST_EVENT_ID,
							nEventId);
					ToolUtil.startActivity(aParent, pClassName, pBundle);
					/*ToolUtil.sendTrack(
							"push_message",
							aParent.getString(R.string.tag_push_message),
							EventActivityFactory.class.getName(),
							aParent.getString(R.string.tag_EventActivityFactory),
							strLocationId);*/
				}
			}
			break;

		case MsgEntity.BIZ_ID_COMMENT_HINT:
		case MsgEntity.BIZ_ID_COMMENT_EXPI:
			if (!TextUtils.isEmpty(aEntity.mValue)
					&& !TextUtils.isEmpty(aEntity.mExtra)
					&& TextUtils.isDigitsOnly(aEntity.mExtra)) {
				String strOrderCharId = aEntity.mValue;
				final int nStatus = Integer.valueOf(aEntity.mExtra);

				// Start activity for order detail.
				Bundle param = new Bundle();
				param.putString(OrderDetailActivity.REQUEST_ORDER_CHAR_ID,
						strOrderCharId);
				param.putInt(OrderDetailActivity.REQUEST_ORDER_STATUS, nStatus);
				ToolUtil.startActivity(aParent, OrderDetailActivity.class,
						param);
				ToolUtil.sendTrack("push_message",
						aParent.getString(R.string.tag_push_message),
						OrderDetailActivity.class.getName(),
						aParent.getString(R.string.tag_OrderDetailActivity),
						strLocationId);
			}
			break;

		case MsgEntity.BIZ_ID_SHOW_PAGE:
			if (!TextUtils.isEmpty(aEntity.mValue)) {
				Bundle bundle = new Bundle();
				bundle.putString(HTML5LinkActivity.LINK_URL, aEntity.mValue);
				ToolUtil.startActivity(aParent, HTML5LinkActivity.class, bundle);
				ToolUtil.sendTrack("push_message",
						aParent.getString(R.string.tag_push_message),
						HTML5LinkActivity.class.getName(),
						aParent.getString(R.string.tag_HTML5LinkActivity),
						strLocationId);
				StatisticsEngine.trackEvent(aParent, strFrom + "_show_page",
						"url=" + aEntity.mValue);
			}
			break;

		case MsgEntity.BIZ_ID_ACTIVATE_APP:
			if (!strFrom.equalsIgnoreCase("push")) {
				MainActivity.startActivity(aParent, MainActivity.TAB_HOME);
				ToolUtil.sendTrack("push_message",
						aParent.getString(R.string.tag_push_message),
						HomeActivity.class.getName(),
						aParent.getString(R.string.tag_Home), strLocationId);
				StatisticsEngine
						.trackEvent(aParent, strFrom + "_activate_page");
			}
			break;

		case MsgEntity.BIZ_ID_SLOT_MACHINE: {
			ToolUtil.startActivity(aParent, SlotMachineActivity.class);
			ToolUtil.sendTrack("push_message",
					aParent.getString(R.string.tag_push_message),
					SlotMachineActivity.class.getName(),
					aParent.getString(R.string.tag_SlotMachineActivity),
					strLocationId);
			StatisticsEngine.trackEvent(aParent, strFrom + "_slot_machine");
		}
			break;

		case MsgEntity.BIZ_ID_REFUND_ACCEPT:
		case MsgEntity.BIZ_ID_REFUND_REFUSE:
		case MsgEntity.BIZ_ID_PRICE_MATCH:
		case MsgEntity.BIZ_ID_PRICE_REFUSE:
			if (!TextUtils.isEmpty(aEntity.mExtra)) {
				Bundle bundle = new Bundle();
				bundle.putString(HTML5LinkActivity.LINK_URL, aEntity.mExtra);
				ToolUtil.startActivity(aParent, HTML5LinkActivity.class, bundle);
				ToolUtil.sendTrack("push_message",
						aParent.getString(R.string.tag_push_message),
						HTML5LinkActivity.class.getName(),
						aParent.getString(R.string.tag_HTML5LinkActivity),
						strLocationId);
			}
			break;

		case MsgEntity.BIZ_ID_PRICE_ACCEPT: { // 跳转到积分流水页�?
			Bundle point = new Bundle();
			point.putInt(MyPointsActivity.TYPE, MyPointsActivity.MY_POINTS);
			ToolUtil.startActivity(aParent, MyPointsActivity.class, point);
			ToolUtil.sendTrack("push_message",
					aParent.getString(R.string.tag_push_message),
					MyPointsActivity.class.getName(),
					aParent.getString(R.string.tag_MyPointsActivity),
					strLocationId);
			StatisticsEngine.trackEvent(aParent, strFrom + "_price_accept");
		}
			break;

		case MsgEntity.BIZ_ID_LOGISTICS:
			if (!TextUtils.isEmpty(aEntity.mValue)
					&& !TextUtils.isEmpty(aEntity.mExtra)) {
				Bundle param = new Bundle();
				param.putString(OrderDetailActivity.REQUEST_ORDER_CHAR_ID,
						aEntity.mValue);
				final int nStatus = Integer.valueOf(aEntity.mExtra);
				param.putInt(OrderDetailActivity.REQUEST_ORDER_STATUS, nStatus);
				ToolUtil.startActivity(aParent, OrderDetailActivity.class,
						param);
				ToolUtil.sendTrack("push_message",
						aParent.getString(R.string.tag_push_message),
						OrderDetailActivity.class.getName(),
						aParent.getString(R.string.tag_OrderDetailActivity),
						strLocationId);
				StatisticsEngine
						.trackEvent(aParent, strFrom + "_my_logisitics");
			}
			break;

		case MsgEntity.BIZ_ID_MSG_CENTER:
			if (!strFrom.equalsIgnoreCase("msgcenter")) {
				final long nLoginUid = ILogin.getLoginUid();
				if (nLoginUid > 0) {
					ToolUtil.startActivity(aParent, MessageActivity.class);
					ToolUtil.sendTrack("push_message",
							aParent.getString(R.string.tag_push_message),
							MessageActivity.class.getName(),
							aParent.getString(R.string.tag_MessageActivity),
							strLocationId);
				}
			}
			break;
		case MsgEntity.BIZ_ID_MY_POINTS: {
			Bundle point = new Bundle();
			point.putInt(MyPointsActivity.TYPE, MyPointsActivity.MY_POINTS);
			ToolUtil.startActivity(aParent, MyPointsActivity.class, point);
			ToolUtil.sendTrack("push_message",
					aParent.getString(R.string.tag_push_message),
					MyPointsActivity.class.getName(),
					aParent.getString(R.string.tag_MyPointsActivity),
					strLocationId);
			StatisticsEngine.trackEvent(aParent, strFrom + "_my_points");
		}
			break;

		case MsgEntity.BIZ_ID_MY_BALANCE: {
			Bundle balance = new Bundle();
			balance.putInt(MyPointsActivity.TYPE, MyPointsActivity.MY_BALANCE);
			ToolUtil.startActivity(aParent, MyPointsActivity.class, balance);
			ToolUtil.sendTrack("push_message",
					aParent.getString(R.string.tag_push_message),
					MyPointsActivity.class.getName(),
					aParent.getString(R.string.tag_MyPointsActivity),
					strLocationId);
			StatisticsEngine.trackEvent(aParent, strFrom + "_my_balance");
		}
			break;

		case MsgEntity.BIZ_ID_MY_COUPON: {
			ToolUtil.startActivity(aParent, MyCouponActivity.class);
			ToolUtil.sendTrack("push_message",
					aParent.getString(R.string.tag_push_message),
					MyCouponActivity.class.getName(),
					aParent.getString(R.string.tag_MyCouponActivity),
					strLocationId);
			StatisticsEngine.trackEvent(aParent, strFrom + "_my_coupon");
		}
			break;

		case MsgEntity.BIZ_ID_FEEDBACK: {
			ToolUtil.checkLoginOrRedirect(aParent,
					FeedBackHistoryActivity.class);
			ToolUtil.sendTrack("push_message",
					aParent.getString(R.string.tag_push_message),
					FeedBackHistoryActivity.class.getName(),
					aParent.getString(R.string.tag_FBHistoryActivity),
					strLocationId);
			StatisticsEngine.trackEvent(aParent, strFrom + "_my_feedback");
		}
			break;
		
		case MsgEntity.BIZ_ID_GOOD_REPAIR:{
			
			if(aEntity.mUid == ILogin.getActiveAccount().getUid()) 
			{
				Bundle param = new Bundle();
				param.putInt(Constants.KEY_APPLY_ID, aEntity.mApplyId);
				ToolUtil.startActivity(aParent, PostSaleDetailActivity.class, param, -1);
			}
			else
			{
				UiUtils.makeToast(aParent, "您更换了登录账号，无法查看上次账号的消息通知");
			}
		}
			break;
			
		default:
			break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView aView, int nScrollState) {
		if ((OnScrollListener.SCROLL_STATE_IDLE == nScrollState)
				&& (aView.getLastVisiblePosition() == aView.getCount() - 1)) {
			// Check whether is requesting.
			if ((mShowMore) && (null == mActive)) {
				// Add footer view.
				this.addFooterView();

				// Send request for next page.
				this.requestMessages(false);
			}
		}
	}

	/**
	 * addFooterView
	 */
	private void addFooterView() {
		if ((null != mFooterView) && (null != mListView)) {
			if (0 >= mListView.getFooterViewsCount())
				mListView.addFooterView(mFooterView);
			else
				setLoadingVisibility(mFooterView, View.VISIBLE);
		}
	}

	/**
	 * removeFooterView
	 */
	private void removeFooterView() {
		if ((null != mFooterView) && (null != mListView)
				&& (mListView.getFooterViewsCount() > 0)) {
			// mListView.removeFooterView(mFooterView);
			setLoadingVisibility(mFooterView, View.GONE);
		}
	}

	private void addHeaderView() {
		if ((null != mHeaderView) && (null != mListView)) {
			if (0 >= mListView.getHeaderViewsCount())
				mListView.addHeaderView(mHeaderView);
			else
				setLoadingVisibility(mHeaderView, View.VISIBLE);
		}
	}

	private void removeHeaderView() {
		if ((null != mHeaderView) && (null != mListView)
				&& (mListView.getHeaderViewsCount() > 0)) {
			// mListView.removeHeaderView(mHeaderView);
			setLoadingVisibility(mHeaderView, View.GONE);
		}
	}

	private void setLoadingVisibility(View aView, int aVisibility) {
		if (null == aView)
			return;

		View pLoadingBar = aView.findViewById(R.id.global_loading_bar);
		if (null != pLoadingBar)
			pLoadingBar.setVisibility(aVisibility);
		View pLoadingText = aView.findViewById(R.id.global_loading_text);
		if (null != pLoadingText)
			pLoadingText.setVisibility(aVisibility);
		aView.setVisibility(aVisibility);
	}

	final static private class MessageHolder {
		View mLayout;
		ImageView mIcon;
		TextView mCaption;
		TextView mText;
		TextView mTimetag;
	}

	/**
	 * implementation of MessageAdapter
	 */
	final static private class MessageAdapter extends BaseAdapter implements
			ImageLoadListener {
		/**
		 * Default constructor of MessageAdapter
		 * 
		 * @param aContext
		 * @param aCache
		 */
		public MessageAdapter(Context aContext, MessageCache aCache) {
			mContext = aContext;
			mCache = aCache;
			mImageLoader = new ImageLoader(aContext, true);
		}

		@Override
		public int getCount() {
			return (null != mCache ? mCache.getCount() : 0);
		}

		@Override
		public Object getItem(int nPos) {
			return (null != mCache ? mCache.getEntity(nPos) : null);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MessageHolder pHolder = null;
			if (null == convertView) {
				if (null == mInflater) {
					mInflater = LayoutInflater.from(mContext);
				}
				convertView = mInflater.inflate(R.layout.message_item, null);
				pHolder = new MessageHolder();
				pHolder.mLayout = convertView
						.findViewById(R.id.message_item_layout);
				pHolder.mIcon = (ImageView) convertView
						.findViewById(R.id.message_icon);
				pHolder.mCaption = (TextView) convertView
						.findViewById(R.id.message_caption);
				pHolder.mText = (TextView) convertView
						.findViewById(R.id.message_text);
				pHolder.mTimetag = (TextView) convertView
						.findViewById(R.id.message_time);
				convertView.setTag(pHolder);
			} else {
				pHolder = (MessageHolder) convertView.getTag();
			}

			// Update the content.
			MsgEntity current = mCache.getEntity(position);
			if (null != current) {
				pHolder.mCaption.setText(current.mTitle);
				// String strText =
				// "<font style=\"font-size:12px\">您关注的商品<font color='blue'>Samsung 三星 S19B300NW 19英寸宽屏液晶显示器�?/font><br/>已经到货啦，数量有限，赶紧去购买吧！</font>";
				pHolder.mText.setText(Html.fromHtml(current.mMessage),
						TextView.BufferType.SPANNABLE);

				// Update background for caption.
				pHolder.mCaption
						.setBackgroundResource(getCaptionBkId(current.mBizId));

				// Update the background image for read.
				final int nResId = 0 == current.mStatus ? R.drawable.message_bk_unread
						: R.drawable.message_bk_normal;
				pHolder.mLayout.setBackgroundResource(nResId);
				
				//这里的mCharId无用，json中没有改数据段
				String strUrl = null;
				if (!TextUtils.isEmpty(current.mCharId)) {
					strUrl = IcsonProImgHelper
							.getAdapterPicUrl(current.mCharId, 80);
				}

				pHolder.mIcon
						.setVisibility(TextUtils.isEmpty(strUrl) ? View.GONE
								: View.VISIBLE);
				final Bitmap pBitmap = mImageLoader.get(strUrl);
				if (null != pBitmap) {
					pHolder.mIcon.setImageBitmap(pBitmap);
				} else {
					mImageLoader.get(strUrl, this);
				}

				Date pData = new Date(current.mTimetag);
				String strTime = mFormat.format(pData);
				pHolder.mTimetag.setText(strTime);
			}

			return convertView;
		}

		private int getCaptionBkId(int nBizId) {
			return MsgEntity.BIZ_ID_NEW_ARRIVALS == nBizId ? R.drawable.message_title_bk_yellow
					: R.drawable.message_title_bk_blue;
		}

		public void cleanup() {
			if (null != mImageLoader) {
				mImageLoader.cleanup();
				mImageLoader = null;
			}
		}

		@Override
		public void onLoaded(Bitmap aBitmap, String strUrl) {
			notifyDataSetChanged();
		}

		@Override
		public void onError(String strUrl) {
		}

		private LayoutInflater mInflater = null;
		private Context mContext = null;
		private MessageCache mCache = null;
		private ImageLoader mImageLoader = null;
		private SimpleDateFormat mFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm");
	}

	private boolean spanClicked(ListView list, View view, int textViewId) {
		final TextView widget = (TextView) view.findViewById(textViewId);
		list.offsetRectIntoDescendantCoords(widget, mLastTouch);
		int x = mLastTouch.right;
		int y = mLastTouch.bottom;

		x -= widget.getTotalPaddingLeft();
		y -= widget.getTotalPaddingTop();
		x += widget.getScrollX();
		y += widget.getScrollY();

		final Layout layout = widget.getLayout();
		final int line = layout.getLineForVertical(y);
		final int off = layout.getOffsetForHorizontal(line, x);

		CharSequence sequence = widget.getText();
		if (sequence instanceof Spannable) {
			Spannable sp = (Spannable) sequence;
			URLSpan[] urls = sp.getSpans(off, off, URLSpan.class);
			if (null != urls && urls.length > 0) {
				URLSpan span = urls[0];
				span.onClick(widget);

				return true;
			}
		}

		return false;
	}
	
	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_MessageActivity);
	}
	

	// Member instance.
	private final Rect mLastTouch = new Rect();
	private boolean mGetLatest = false;
	private boolean mShowMore = false;
	private boolean mScrolling = false;
	private int mInitY = 0;
	private int mMoveY = 0;
	private int mStatus = PULL_STATUS_NONE;
	private Ajax mActive = null;
	private View mFooterView;
	private View mHeaderView;
	private ListView mListView;
	private View mNoMessages;
	private MessageAdapter mAdapter;
	private MessageParser mParser;
	private MessageCache mMsgCache;

	private static final int PULL_STATUS_NONE = 0;
	private static final int PULL_STATUS_PULLING = (PULL_STATUS_NONE + 1);
	private static final int PULL_STATUS_RELEASE = (PULL_STATUS_NONE + 2);
	private static final int PULL_STATUS_LOADING = (PULL_STATUS_NONE + 3);
}
