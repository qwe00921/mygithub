package com.tencent.djcity.util.activity;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.tencent.djcity.R;
import com.tencent.djcity.lib.ui.AppDialog;
import com.tencent.djcity.lib.ui.NavigationBar;
import com.tencent.djcity.lib.ui.UiUtils;
import com.tencent.djcity.main.MainActivity;
import com.tencent.djcity.util.ajax.Ajax;
import com.tencent.djcity.util.ajax.OnErrorListener;
import com.tencent.djcity.util.ajax.Parser;
import com.tencent.djcity.util.ajax.Response;

public abstract class BaseActivity extends Activity implements OnErrorListener, OnClickListener {
//	private HashMap<Integer, LoadingSwitcher> mLoadingSwitchers;
	private SparseArray<LoadingSwitcher> mLoadingSwitchers;
	private boolean   beenSeen;
	private Dialog    errorDialog;
	private Drawable  mBG;
	public String     reportExtraInfo;
	public String     reportPid;
	public boolean 	  isReportPV = true;
	
	public boolean isBeenSeen()
	{
		return beenSeen;
	}
	
	public BaseActivity() {
		reportExtraInfo = "";
		reportPid = "";
		this.destroyListenerList = new ArrayList<DestroyListener>();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onRestart();
		beenSeen = true;
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		beenSeen = false;
		UiUtils.cancelToast();
		
		closeProgressLayer();
	}
	
	/*
	@Override
	public void setContentView (int layoutResID) {
		setContentView(layoutResID, true);
	}
	
	public void setContentView(int layoutResID, boolean enableTheme) {
		super.setContentView(layoutResID);

		//if( enableTheme ) {
			// Update background configuration.
		//	this.setThemeConfig();
		//}
	}
	*/
	
	protected void loadNavBar(int nViewId) {
		loadNavBar(nViewId, 0);
	}
	
	protected void loadNavBar(int nViewId, int nTextId) {
		initNavBar(nViewId);
		
		if( (null != mNavBar) && (nTextId > 0) ) {
			mNavBar.setText(nTextId);
		}
	}
	
	protected void loadNavBar(int nViewId, String strText) {
		initNavBar(nViewId);
		
		if( null != mNavBar )
			mNavBar.setText(strText);
	}
	
	/**
	 * init nav bar
	 * @param nViewId
	 */
	private void initNavBar(int nViewId) {
		if( (null == mNavBar) && (nViewId > 0) ) {
			mNavBar = (NavigationBar)findViewById(nViewId);
			
			mNavBar.setOnLeftButtonClickListener(new NavigationBar.OnLeftButtonClickListener() {
				@Override
				public void onClick() {
					processBack();
				}
			});
		}
	}
	
	public void setNavBarText(int nResId) {
		setNavBarText(getString(nResId));
	}
	
	public void setNavBarText(String strText) {
		if( null != mNavBar ) {
			mNavBar.setText(strText);
		}
	}
	
	public void setNavBarRightVisibility(int  pVisibilit){
		if( null != mNavBar ) {
			mNavBar.setRightVisibility(pVisibilit);
		}
	}
	
	public void setNavBarRightText(int nResId) {
		setNavBarRightText(getString(nResId));
	}
	
	public void setNavBarRightText(String strText){
		setNavBarRightText(strText, null);
	}
	
	public void setNavBarRightText(String strText, Drawable pDrawable){
		if( null != mNavBar){
			mNavBar.setRightText(strText, pDrawable);
		}
	}
	
	public void showProgressLayer(String title, String message) {
		closeProgressLayer();
		
		
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置�???�为???形�??�????

		if (title != null) {
			mProgressDialog.setTitle("???�?");// 设置???�?
		}

		// mProgressDialog.setIcon(R.drawable.icon);//设置??��??
		if (message != null) {
			mProgressDialog.setMessage(message);
		}

		mProgressDialog.setIndeterminate(true);// 设置�?�???��?????为�?????�?
		mProgressDialog.setCancelable(true);// 设置�?�???��????????以�??????????????�?
		if(!isBeenSeen())
			return;
		
		mProgressDialog.show();
	}

	public void showProgressLayer() {
		showProgressLayer(null, getString(R.string.wait_for_submit));
	}

	public void showProgressLayer(String message) {
		showProgressLayer(null, message);
	}

	private class LoadingSwitcher {
		View contentContainer;
		View loadingContainer;
	}

	public void setLoadingSwitcher(int id, View contentContainer, View loadingContainer) {
		if (mLoadingSwitchers == null) {
			mLoadingSwitchers = new SparseArray<LoadingSwitcher>();
		}

		LoadingSwitcher switcher = new LoadingSwitcher();
		switcher.contentContainer = contentContainer;
		switcher.loadingContainer = loadingContainer;
		mLoadingSwitchers.put(id, switcher);
	}

	public void setDefaultBodyContainer(View view) {
		mDefaultBodyContainer = view;
	}

	public void setDefaultLoadingContainer(View view) {
		mDefaultLoadingContainer = view;
	}

	public void showLoadingLayer(int id, boolean hideContent) {
		if (mLoadingSwitchers == null) {
			setLoadingSwitcher(id, mDefaultBodyContainer == null ? findViewById(R.id.global_container) : mDefaultBodyContainer, 
					mDefaultLoadingContainer == null ? findViewById(R.id.global_loading) : mDefaultLoadingContainer);
		}
		LoadingSwitcher switcher = mLoadingSwitchers.get(id);
		if (switcher != null) {
			if (switcher.loadingContainer != null) {
				switcher.loadingContainer.setVisibility(View.VISIBLE);
			}
			if (switcher.contentContainer != null) {
				switcher.contentContainer.setVisibility(hideContent ? View.GONE : View.VISIBLE);
			}
		}
	}

	public void showLoadingLayer(int id) {
		showLoadingLayer(id, true);
	}

	public void showLoadingLayer(boolean hideContent) {
		showLoadingLayer(LOADING_SWITCHER_FLAG_DEFAULT, hideContent);
	}

	public void showLoadingLayer() {
		showLoadingLayer(LOADING_SWITCHER_FLAG_DEFAULT, true);
	}

	public void closeLoadingLayer(int id, boolean hideContent) {
		if (mLoadingSwitchers == null)
			return;
		
		final int nKey = mLoadingSwitchers.indexOfKey(id) >= 0 ? id : LOADING_SWITCHER_FLAG_DEFAULT;
		LoadingSwitcher switcher = mLoadingSwitchers.get(nKey);
		if (switcher != null) {
			if (switcher.loadingContainer != null) {
				switcher.loadingContainer.setVisibility(View.GONE);
			}

			if (switcher.contentContainer != null) {
				switcher.contentContainer.setVisibility(hideContent ? View.GONE : View.VISIBLE);
			}
		}
	}

	public void closeLoadingLayer(int id) {
		closeLoadingLayer(id, false);
	}

	public void closeLoadingLayer(boolean hidenContent) {
		closeLoadingLayer(LOADING_SWITCHER_FLAG_DEFAULT, hidenContent);
	}

	public void closeLoadingLayer() {
		closeLoadingLayer(LOADING_SWITCHER_FLAG_DEFAULT, false);
	}

	public void closeProgressLayer() {
		if (mProgressDialog != null) {
			mProgressDialog.cancel();
			mProgressDialog = null;
		}
	}

	public void addDestroyListener(DestroyListener destroyListener) {
		if (this.destroyListenerList == null)
			return;

		this.destroyListenerList.add(destroyListener);
	}

	public void addPauseListener(PauseListener paramPauseListener) {
		if (this.pauseListenerList == null) {
			this.pauseListenerList = new ArrayList<BaseActivity.PauseListener>();
		}
		this.pauseListenerList.add(paramPauseListener);
	}

	public void addResumeListener(ResumeListener paramResumeListener) {
		if (this.resumeListenerList == null) {
			this.resumeListenerList = new ArrayList<BaseActivity.ResumeListener>();
		}

		this.resumeListenerList.add(paramResumeListener);
	}

	public void addAjax(Ajax ajax) {
		if (null == mAjaxs) {
			mAjaxs = new ArrayList<Ajax>();
		}

		mAjaxs.add(ajax);
	}
	
	protected Ajax getAjax(int nPos)
	{
		if( (null == mAjaxs) || (0 > nPos) )
			return null;
		final int nSize = mAjaxs.size();
		return (nPos < nSize ? mAjaxs.get(nPos) : null);
	}

	protected void onDestroy() {
		
		
		mLoadingSwitchers = null;

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}

		if (null != this.destroyListenerList) {
			Iterator<DestroyListener> destroyListenerIterator = this.destroyListenerList.iterator();
			while (destroyListenerIterator.hasNext()) {
				destroyListenerIterator.next().onDestroy();
			}

			destroyListenerIterator = null;
			destroyListenerList = null;

			cleanAllAjaxs();
			
		}
		
		super.onDestroy();
	}

	public void cleanAllAjaxs()
	{
		if (mAjaxs != null) {
			for (Ajax ajax : mAjaxs) {
				if (ajax != null) {
					ajax.abort();
					ajax = null;
				}
			}
			mAjaxs.clear();
			mAjaxs = null;
		}
	}
	
	public interface DestroyListener {
		public void onDestroy();
	}

	public interface PauseListener {
		public void onPause();
	}

	public interface ResumeListener {
		public void onResume();
	}

	@Override
	public void onError(final Ajax ajax, final Response response) {
		this.onError(ajax, response, null);
	}
	
	/**
	 * @param ajax
	 * @param aResponse
	 * @param strErrMsg
	 */
	public void onError(final Ajax ajax, final Response aResponse, String strErrMsg)
	{
		boolean isPostOperation = mProgressDialog != null;
		closeProgressLayer();

		if (ajax == null)
			return;

		@SuppressWarnings("rawtypes")
		Parser pParser = ajax.getParser();
		String pErrMsg = null == pParser ? getString(R.string.network_error_info) : pParser.getErrMsg();
		
		String strToastMsg = getString(R.string.title_network_error);
		if (isPostOperation) {
			UiUtils.makeToast(this, strToastMsg);
			return;
		}
		
		//make sure this Activity is seend.Otherwise WindowManager$BadTokenException
		if(!isBeenSeen())
			return;
		
		String strMessage = TextUtils.isEmpty(strErrMsg) ? (TextUtils.isEmpty(pErrMsg) ? this.getString(R.string.network_error_info): pErrMsg) : strErrMsg;
		String strTitle = getString(R.string.network_error);
		if(null!=errorDialog && errorDialog.isShowing())
		{
			errorDialog.dismiss();
		}
		
		errorDialog = UiUtils.showDialog(this, strTitle, strMessage, R.string.btn_retry, R.string.btn_cancel, new AppDialog.OnClickListener() {
			@Override
			public void onDialogClick(int nButtonId) {
				if (nButtonId == AppDialog.BUTTON_POSITIVE) {
					ajax.send();
				} else {
					closeLoadingLayer(aResponse.getId(), true);
					onErrorDialogCacneled(ajax, aResponse);
				}
			}
			});
	}
	
	public void onErrorDialogCacneled(final Ajax ajax, final Response response) {
	}

	@Override
	public void onClick(View v) {
	}

	public boolean registerBottomMenu() {
		return true;
	}

	public boolean isShowSearchPanel() {
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if( this.handleBack() )
				return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	protected void processBack() {
		if( !handleBack() ) {
			onBackPressed();
		}
	}
	
	private boolean handleBack() {
		final Activity parent = getParent();
		if (parent != null && parent instanceof MainActivity) {
			MainActivity pActivity = (MainActivity)parent;
			if( pActivity.handleBack() ) {
				return true;
			}
		}
		return false;
	}
	
	private static final int MENU_SEARCH = 1;
	private static final int MENU_CATEGORY = 2;
	private static final int MENU_HISTORY = 3;
	private static final int MENU_CART = 4;
	private static final int MENU_LOGIN = 5;
	private static final int MENU_VERSION = 6;
	private static final int MENU_MORE = 7;
	private static final int MENU_EXIT = 8;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (registerBottomMenu() == false) {
			return super.onCreateOptionsMenu(menu);
		}

		int order = 1;
		menu.add(0, MENU_SEARCH, order++, R.string.menu_search).setIcon(R.drawable.i_menu_search);
		menu.add(0, MENU_CATEGORY, order++, R.string.category).setIcon(R.drawable.i_menu_category);
		menu.add(0, MENU_HISTORY, order++, R.string.settings_view_history).setIcon(R.drawable.i_menu_history);
		menu.add(0, MENU_CART, order++, R.string.shoppingcart).setIcon(R.drawable.i_menu_cart);
		// menu.add(0, MENU_LOGIN, order++, ILogin.getLoginUid() == 0 ? "??��??" :
		// "注�??").setIcon(R.drawable.i_menu_login);
		// menu.add(0, MENU_VERSION, order++,
		// "�?�???��??").setIcon(R.drawable.i_menu_version);
		menu.add(0, MENU_MORE, order++, R.string.settings_more).setIcon(R.drawable.i_menu_more);
		menu.add(0, MENU_EXIT, order++, R.string.btn_exit).setIcon(R.drawable.i_menu_exit);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (registerBottomMenu() == false) {
			return super.onOptionsItemSelected(item);
		}
/*
		switch (item.getItemId()) {
		case MENU_SEARCH:
			ToolUtil.startActivity(this, SearchActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_SearchActivity), SearchActivity.class.getName(), getString(R.string.tag_SearchActivity), "02011");
			break;
		case MENU_CATEGORY:
			MainActivity.startActivity(this, MainActivity.TAB_CATEGORY);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_CategoryActivity), CategoryActivity.class.getName(), getString(R.string.tag_CategoryActivity), "02012");
			break;
		case MENU_HISTORY:
			ToolUtil.startActivity(this, ViewHistoryActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_ViewHistoryActivity), ViewHistoryActivity.class.getName(), getString(R.string.tag_ViewHistoryActivity), "02013");
			break;
		case MENU_CART:
			MainActivity.startActivity(this, MainActivity.TAB_CART);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_ShoppingCartActivity), ShoppingCartActivity.class.getName(), getString(R.string.tag_ShoppingCartActivity), "02014");
			break;
		case MENU_LOGIN:
			ToolUtil.startActivity(this, LoginActivity.class);
			break;
		case MENU_VERSION:
			break;
		case MENU_MORE:
			ToolUtil.startActivity(this, MoreActivity.class);
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_MoreActivity), MoreActivity.class.getName(), getString(R.string.tag_MoreActivity), "02015");
			break;
		case MENU_EXIT:
			UiUtils.showDialog(this, R.string.caption_hint, R.string.message_exit, R.string.btn_stay, R.string.btn_exit, new AppDialog.OnClickListener() {
				@Override
				public void onDialogClick(int nButtonId) {
					if (nButtonId == AppDialog.BUTTON_NEGATIVE) {
						MainActivity.startActivity(BaseActivity.this, MainActivity.TAB_HOME, true);
					}
				}
			});
			break;
		}
*/
		return true;
	}
	
	/*
	 protected void setThemeConfig() {
		ViewGroup contentView = (ViewGroup)getWindow().getDecorView().findViewById(android.R.id.content);
		View rootView = (null != contentView ? contentView.getChildAt(0) : null);
		if( null != rootView ) {
			Drawable bg = BgUpdater.getBgDrawable();
			if(null!=bg)
			{
				if(null!=mBG && mBG.equals(bg))
					return;
				mBG = bg;
				rootView.setBackgroundDrawable(bg);
			}else
				rootView.setBackgroundResource(R.drawable.bg_daytime_shape);
		}
	}
	*/
	/**
	 *  to do that. Because of sdk_int >= 11 onBackPressed() will mFragments.popBackStackImmediate() and call checkStateLoss() and dispatchStop()
	 *  set mStateSaved = true, which will cause onSaveinstanceState() illegalStateException
	 *  
	 *  Another way to solve this problem
	 *  1. onSaveinstanceState just don't call super.onSaveinstanceState()  -->some view state will lost
	 *  2.onSaveinstanceState(Bundle outState)
	 *  {
	 *  	super.onSaveinstanceState(outState);
	 *  	invokeFragmentManagerNoteStateNotSaved();
	 *  }
	 *  
	 *  // to set mStateSaved = false in public void noteStateNotSaved()
	 *  private void invokeFragmentManagerNoteStateNotSaved()
	 *  {
	 *  	if(Build.VERSION.SDK_INT <11)
	 *  		return;
	 *  
	 *  	try
	 *  	{
	 *  		Class cls = getClass();
	 *  		do{
	 *  			cls = cls.getSuperclass();
	 *  		}while(!"Activity".equals(cls.getSimpleName()));
	 *  
	 *  		Field fragmentMgrField = cls.getDeclaredField("mFragments");
	 *  		fragmentMgrField.setAccessible(true);
	 *  
	 *  		Object fragmentMgr = fragmentMgrField.get(this);
	 *     		cls = fragmentMgr.getClass();
	 *     
	 *      	Method noteStateNotSavedMethod = cls.getDeclaredMethod("noteStateNotSaved",
	 *      				new Class[]{ noteStateNotSavedMethod.invoke(fragmentMgr, new Object[]{}};
	 *      
	 *      	Log.d("DLoutState", "SUcc call for noteStateNotSaved");
	 *      }catch(Exception ex)
	 *      {
	 *      } 
	 *}
	 */
	@Override
	public void onBackPressed()
	{
		 finish();
	}

	
	protected final static String LOG_TAG = BaseActivity.class.getName();
	public static final int LOADING_SWITCHER_FLAG_DEFAULT = 0;
	private ArrayList<DestroyListener> destroyListenerList;
	private ArrayList<PauseListener> pauseListenerList;
	private ArrayList<ResumeListener> resumeListenerList;
	private ArrayList<Ajax> mAjaxs;
	private ProgressDialog mProgressDialog;
	private View mDefaultBodyContainer;
	private View mDefaultLoadingContainer;
	protected NavigationBar mNavBar;
}
