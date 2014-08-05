package com.icson.search;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.icson.R;
import com.icson.lib.IPageCache;
import com.icson.lib.inc.CacheKeyFactory;
import com.icson.lib.model.SearchModel;
import com.icson.lib.ui.UiUtils;
import com.icson.list.ListActivity;
import com.icson.statistics.StatisticsEngine;
import com.icson.util.Config;
import com.icson.util.IcsonApplication;
import com.icson.util.Log;
import com.icson.util.ServiceConfig;
import com.icson.util.ToolUtil;
import com.icson.util.activity.BaseActivity;
import com.icson.util.ajax.Ajax;
import com.icson.util.ajax.OnSuccessListener;
import com.icson.util.ajax.Response;

public class SearchActivity extends BaseActivity implements OnSuccessListener<SmartBoxModel>, OnItemClickListener {
	private static final long DELAY_POST_TIME = 500;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 2628;
	private static final int MAX_HISTORY_WORDS_LENGTH = 10;
	private Handler mHandler = new Handler();

	private SearchSuggestParser mSearchSuggestParser;
	private HotKeyParser mHotKeyParser;
	private ArrayList<AutoCompleteModel> mAutoCompleteModels;
	private ArrayList<AutoCompleteModel> mHotKeyModels;    //
	private ArrayList<AutoCompleteModel> mHistoryWordsModels;
	private AutoCompleteCatModel mAutoCompleteCatModel;
	private AutoCompleteAdapter mAutoCompleteAdapter;
	private AutoCompleteAdapter mAutoCompleteHotkeyAdapter;
	private AutoCompleteControl mAutoCompleteControl;
	private ListView mListViewSearch;
	private ListView mListViewHotkey;
	private Ajax mHotWordsAjax;
	private Ajax mAutoCompleteAjax;
	private String mKeyWord;
	private EditText mEditText;
	private View suggestPanel;
	private TextView mListViewLabel;
	private Button mClearHistoryWordsButton;
	private ImageView mSearchClear;
	private View mSmartBoxHeaderView;
	private TextView mSearchWithCategoryView;

	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			sendRequest();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		mEditText = (EditText) findViewById(R.id.search_edittext);
		mEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if(null!= event && event.getAction() != KeyEvent.ACTION_DOWN )
					return false;
				
				if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL)
				{
					searchKeyword();
					return true;
				}
				return false;
			}
		});
		
		Intent intent = getIntent();
		mKeyWord = intent.getStringExtra("key_word");//？这里是为了得到上个activity传递过来的查询关键字的吗？
		if(null!=mKeyWord)
			mEditText.append(mKeyWord);
		suggestPanel = findViewById(R.id.search_relative_empty); //suggestPanel是一个大的LinearLayout
		mClearHistoryWordsButton = (Button) findViewById(R.id.clear_history_words_button);
		mListViewSearch = (ListView) findViewById(R.id.search_listview);
		mListViewHotkey = (ListView) findViewById(R.id.hotkeys_listview);
		mListViewLabel = (TextView) findViewById(R.id.list_textview_label);
		findViewById(R.id.search_button_search).setOnClickListener(this);
		
		LinearLayout view  = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.search_item_header, null);
		mSmartBoxHeaderView = (View)view.findViewById(R.id.search_item_root);
		
		mListViewSearch.addHeaderView(view);
		
		mSearchClear = (ImageView) findViewById(R.id.search_button_clear);
		mSearchClear.setOnClickListener(this);
		mClearHistoryWordsButton.setOnClickListener(this);
		
		//下面为了使在搜索框为空时候，不显示清除搜索框内容的按键
		String strSearch = mEditText.getText().toString();
		if(strSearch.equals("")){
			mSearchClear.setVisibility(View.GONE);
		}else{
			mSearchClear.setVisibility(View.VISIBLE);
		}
		
		
		/*这里刚开始的时候历史记录的adapter和热门搜索词的adapter里面的数据源都初始化为mAutoCompleteModels，
		然后再下面根据实际的情况来填充mAutoCompleteModels里面的实际内容*/
		mAutoCompleteModels = new ArrayList<AutoCompleteModel>();
		mAutoCompleteAdapter = new AutoCompleteAdapter(this, mAutoCompleteModels);
		mListViewSearch.setAdapter(mAutoCompleteAdapter);
		
		mHotKeyModels = new ArrayList<AutoCompleteModel>();
		mHistoryWordsModels = new ArrayList<AutoCompleteModel>();
		mAutoCompleteHotkeyAdapter = new AutoCompleteAdapter(this, mAutoCompleteModels);
		mListViewHotkey.setAdapter(mAutoCompleteHotkeyAdapter);
		
		getHistoryWords();
		
		//如果历史记录为空，那么就搜索热门关键字，否则就对历史记录界面进行初始化
		if(0 == mHistoryWordsModels.size()){
			searchHotKey();
		}else{
			initHistoryWordsView();
		}

		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				search();
			}
		});


		mListViewSearch.setOnItemClickListener(this);
		mListViewHotkey.setOnItemClickListener(this);

	}
	/*
	 * Get search history words from DB
	 */
	private void getHistoryWords(){
		IPageCache cache = new IPageCache();
		String content = cache.get(CacheKeyFactory.CACHE_SEARCH_HISTORY_WORDS);
		if(null != content){
			if(content.length() <= 0){
				mHistoryWordsModels.clear();
				return;
			}
			
			//添加界限保护
			int start = 1, end = content.length()-1;
			if (start > end)
				return;
			String newCacheStr = content.substring(start, end);
			if(null == newCacheStr || newCacheStr.length() <= 0)
				return;
			
			String [] nameArray = newCacheStr.split(", ");
			//添加null check
			if(nameArray == null) {
				Log.w(LOG_TAG, "[getHistoryWords] nameArray is null, content = " + content);
				return;
			}
			int nSize = nameArray.length;
			
			mHistoryWordsModels.clear();
			for(int nId = 0; nId < nSize; nId++){
				if(!TextUtils.isEmpty(nameArray[nId])) {
					AutoCompleteModel model = new AutoCompleteModel();
					model.setName(nameArray[nId]);
					
					mHistoryWordsModels.add(model);
				}
			}
		}
	}
	
	/*
	 * Save search words to DB
	 */
	private void saveHistoryWords(){
		ArrayList<String> list = new ArrayList<String>();
		
		if((null != mHistoryWordsModels) && (0 != mHistoryWordsModels.size())){
			int nSize = mHistoryWordsModels.size();
			String strWord;
			
			for(int nId=0; nId<nSize; nId++){
				strWord = mHistoryWordsModels.get(nId).getName();
				list.add(strWord);
			}
		}
		
		IPageCache cache = new IPageCache();
		cache.set(CacheKeyFactory.CACHE_SEARCH_HISTORY_WORDS, list.toString(), 0);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		UiUtils.showSoftInputDelayed(this, mEditText, mHandler);
	}
	
	private void searchHotKey() {
		if((null != mHotKeyModels) && (0 != mHotKeyModels.size()) ){
			initHotKeyView();
			return;
		}

		if (null == mHotKeyParser) {
			mHotKeyParser = new HotKeyParser();
		}
		if (mHotWordsAjax != null) {
			mHotWordsAjax.abort();
			mHotWordsAjax = null;
		}
		mHotWordsAjax = ServiceConfig.getAjax(Config.URL_HOT_SEARCH_WORDS);
		if( null == mHotWordsAjax )
			return ;
		mHotWordsAjax.setOnSuccessListener( new OnSuccessListener<ArrayList<AutoCompleteModel>>(){
			@Override
			public void onSuccess(ArrayList<AutoCompleteModel> models, Response response) {
				if (null != models && models.size() >= 0) {
					mHotKeyModels.addAll(models);
					initHotKeyView();
				}			
			}			
		});
		mHotWordsAjax.setOnErrorListener(this);
		mHotWordsAjax.setParser(mHotKeyParser);
		//addAjax(mHotWordsAjax);   non destroy activity
		mHotWordsAjax.send();
	}
	
	private void initHistoryWordsView(){
		mListViewSearch.setVisibility(View.GONE);
		suggestPanel.setVisibility(View.VISIBLE);
		mClearHistoryWordsButton.setVisibility(View.VISIBLE);
		
		mListViewLabel.setText("历史搜索词");
		mAutoCompleteModels.clear();
		mAutoCompleteModels.addAll(reverseArrayList(mHistoryWordsModels));
		mAutoCompleteAdapter.notifyDataSetChanged();
		mAutoCompleteHotkeyAdapter.notifyDataSetChanged();
	}
	
	private void initHotKeyView(){
		mListViewSearch.setVisibility(View.GONE);
		suggestPanel.setVisibility(View.VISIBLE);
		mClearHistoryWordsButton.setVisibility(View.GONE);
		
		mListViewLabel.setText("热门搜索词");
		mAutoCompleteModels.clear();
		mAutoCompleteModels.addAll(mHotKeyModels);
		mAutoCompleteAdapter.notifyDataSetChanged();
		mAutoCompleteHotkeyAdapter.notifyDataSetChanged();
	}
	
	

	private void search() {
		if (mRunnable != null) {
			mHandler.removeCallbacks(mRunnable);
		}

		mKeyWord = mEditText.getText().toString().replaceFirst("^(\\s+)", "");

		boolean isEmpty = mKeyWord.equals("");
		
		if(isEmpty){
			mSearchClear.setVisibility(View.GONE);
		}else{
			mSearchClear.setVisibility(View.VISIBLE);
		}
		
		boolean isHostoryWordsEmpty = (0 == mHistoryWordsModels.size())? true : false;
		if (isEmpty) {
			if(isHostoryWordsEmpty) {
				initHotKeyView();
			}else {
				initHistoryWordsView();
			}
			return;
		}
		
		mHandler.postDelayed(mRunnable, DELAY_POST_TIME);
	}

	private void sendRequest() {
		if (mAutoCompleteAjax != null) {
			mAutoCompleteAjax.abort();
			mAutoCompleteAjax = null;
		}

		if (mSearchSuggestParser == null) {
			mSearchSuggestParser = new SearchSuggestParser();
		}

		if (mAutoCompleteControl == null) {
			mAutoCompleteControl = new AutoCompleteControl();
		}

		mAutoCompleteAjax = mAutoCompleteControl.send(mSearchSuggestParser, mKeyWord, this, null);
	}

	@Override
	public void onSuccess(SmartBoxModel models, Response response) {
		if(null == mListViewSearch || null == suggestPanel ||
				null == mSmartBoxHeaderView || 
				null == models || null == models.getAutoComleteModels() || 
				null == mAutoCompleteAdapter || null == mAutoCompleteHotkeyAdapter)
			return;
		
		mListViewSearch.setVisibility(View.VISIBLE);
		suggestPanel.setVisibility(View.GONE);
		if(null != models.getAutoCompleteCatModel())
		{
			mAutoCompleteCatModel = models.getAutoCompleteCatModel();
			reloadSmartBoxHeaderView();
		}
		else
		{
			mSmartBoxHeaderView.setVisibility(View.GONE);
		}
		
		if(null == mAutoCompleteModels) {
			mAutoCompleteModels = new ArrayList<AutoCompleteModel>();
		}
		
		mAutoCompleteModels.clear();
		if (null != models.getAutoComleteModels() && models.getAutoComleteModels().size() > 0)
			mAutoCompleteModels.addAll(models.getAutoComleteModels());
		mAutoCompleteAdapter.notifyDataSetChanged();
		mAutoCompleteHotkeyAdapter.notifyDataSetChanged();
			
	}

	private void reloadSmartBoxHeaderView() {
		if(null != mSmartBoxHeaderView && null != mAutoCompleteCatModel)
		{
			String cateName = mAutoCompleteCatModel.getCategoryName();
			String keyWord = mAutoCompleteCatModel.getKeyWords();
			if(TextUtils.isEmpty(cateName)){
				mSmartBoxHeaderView.setVisibility(View.GONE);
				return;
			}
			
			mSmartBoxHeaderView.setVisibility(View.VISIBLE);
			if(null == mSearchWithCategoryView)
				mSearchWithCategoryView= (TextView) mSmartBoxHeaderView.findViewById(R.id.search_item_info);
			
			if(TextUtils.isEmpty(keyWord))
			{
				mSearchWithCategoryView.setText(Html.fromHtml(getString(R.string.search_into_category, cateName)));
			}
			else
			{
				mSearchWithCategoryView.setText(Html.fromHtml(keyWord + 
						this.getString(R.string.search_in_one_category,cateName)));
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
/*		case R.id.search_button_voice:
			ToolUtil.startVoiceSearchActivity(this, VOICE_RECOGNITION_REQUEST_CODE);
			
			ToolUtil.sendTrack(SearchActivity.class.getName(), "VoiceSearch", getString(R.string.tag_SearchActivity)+"01011");
			break;
			*/
		case R.id.search_button_search:
			this.searchKeyword();
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21002");
			break;
			
		case R.id.clear_history_words_button:
			ToolUtil.reportStatisticsClick(getActivityPageId(), "21003");
			mHistoryWordsModels.clear();
			mAutoCompleteModels.clear();
			
			// Need to notifyDatasetChanged after data source changed. For Exception#60508612
			mAutoCompleteAdapter.notifyDataSetChanged();
			mAutoCompleteHotkeyAdapter.notifyDataSetChanged();
			searchHotKey();
			break;
		case R.id.search_button_clear:
			mEditText.setText("");
			break;
		}
	}
	
	private void searchKeyword() {
		String keyWord = mEditText.getText().toString().replaceFirst("^(\\s+)", "");
		if (!keyWord.equals("")) {
			redirect(keyWord);
		}
		ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_SearchActivity), ListActivity.class.getName(), getString(R.string.tag_ListActivity), "01012");
	}

	@Override
	public void onDestroy() {
		//save search words
		saveHistoryWords();
		
		mHandler = null;
		mHotKeyParser = null;
		mSearchSuggestParser = null;
		mRunnable = null;
		mEditText = null;
		mListViewSearch = null;
		mListViewHotkey = null;
		
		super.onDestroy();
	}
	
	@Override
	protected void onPause()
	{
		if (mHotWordsAjax != null) {
			mHotWordsAjax.abort();
			mHotWordsAjax = null;
		}
		
		if (mAutoCompleteAjax != null) {
			mAutoCompleteAjax.abort();
			mAutoCompleteAjax = null;
		}
		UiUtils.hideSoftInput(this, mEditText);
		super.onPause();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ToolUtil.reportStatisticsClick(getActivityPageId(), "30000");
		if(mListViewSearch.getVisibility() == View.GONE)
		{
			//历史搜索词点击
			redirect(mAutoCompleteModels.get(position).getName());
			ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_SearchActivity), ListActivity.class.getName(), getString(R.string.tag_ListActivity), "02011");
		}
		else
		{	//推荐条目点击
			if(0 == position )
			{
				redirectToListWithCategory();
			}
			else
			{
				redirect(mAutoCompleteModels.get(position-1).getName());
				ToolUtil.sendTrack(this.getClass().getName(), getString(R.string.tag_SearchActivity), ListActivity.class.getName(), getString(R.string.tag_ListActivity), "02011");
			}
			//mta上报点击位置
			if(mSmartBoxHeaderView.getVisibility() == View.VISIBLE)
			{
				StatisticsEngine.trackEvent(IcsonApplication.app, "smartbox_clicked", "position=" + position );
			}
		}
	}

	private void redirectToListWithCategory() {
		// TODO Auto-generated method stub
		SearchModel mSearchModel = new SearchModel();
		if(!TextUtils.isEmpty(mAutoCompleteCatModel.getKeyWords()))
		{
			mSearchModel.setKeyWord(mAutoCompleteCatModel.getKeyWords());
		}
		mSearchModel.setPath(mAutoCompleteCatModel.getPath());
		Bundle param = new Bundle();
		param.putSerializable(ListActivity.REQUEST_SEARCH_MODEL, mSearchModel);
		param.putBoolean(ListActivity.REQUEST_SEARCH_CATE, true);
		ToolUtil.startActivity(this, ListActivity.class, param);
		return ;
	}

	private void redirect(String keyWord) {
		/*int xy=1;
		if(TextUtils.isDigitsOnly(keyWord))
			com.icson.amap.CargoMapActivity.showMap(SearchActivity.this, "", "", "", keyWord);
		else
		{*/
		SearchModel mSearchModel = new SearchModel();
		mSearchModel.setKeyWord(keyWord);
		
		// Update keyword.
		if( (null != mEditText) && (!TextUtils.isEmpty(keyWord)) ) {
			mEditText.setText(keyWord);
			mEditText.setSelection(keyWord.length());
			
			//save history words
			AutoCompleteModel pModel = new AutoCompleteModel();
			pModel.setName(keyWord);
			
			addObject(pModel);
		}

		Bundle param = new Bundle();
		param.putSerializable(ListActivity.REQUEST_SEARCH_MODEL, mSearchModel);
		param.putSerializable(ListActivity.REQUEST_PAGE_TITLE, keyWord);
		param.putBoolean("from_search", true);
		ToolUtil.startActivity(this, ListActivity.class, param);
		//}
	}
	
	private void addObject(AutoCompleteModel item){
		//If keyword has already existed, remove it
		int position = hasObject(mHistoryWordsModels, item);
		if(-1 != position){
			mHistoryWordsModels.remove(position);
		}
		
		//If length is greater than MAX length, remove the first element	
		if(mHistoryWordsModels.size() >= MAX_HISTORY_WORDS_LENGTH){
			mHistoryWordsModels.remove(0);
		}
		
		mHistoryWordsModels.add(item);
	}
	
	/*
	 * reverse arraylist
	 */
	private ArrayList<AutoCompleteModel> reverseArrayList(ArrayList<AutoCompleteModel> autoModels){
		if(autoModels == null){
			return null;
		}
		
		int nSize = autoModels.size();
		if(nSize > 1) {
			ArrayList<AutoCompleteModel> pmodels = new ArrayList<AutoCompleteModel>();
			for(int nId=nSize-1; nId>=0; nId--){
				pmodels.add(autoModels.get(nId));
			}
			
			return pmodels;
		}
		
		return autoModels;
	}
	
	/*
	 * check whether list has item or not.
	 * 
	 */
	private int hasObject(ArrayList<AutoCompleteModel> list, AutoCompleteModel item) {
		int nSize = (null == list) ? 0 : list.size();
		for(int nId=0; nId<nSize; nId++){
			AutoCompleteModel pItem = list.get(nId);
			if(null != pItem  && null != item && TextUtils.equals(pItem.getName(), item.getName())){
				return nId;
			}
		}
		
		return -1;
	}

	@Override
	public boolean isShowSearchPanel() {
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		// 回调获取从谷歌得到的数据
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {
			// 取得语音的字符
			ArrayList<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (results != null && results.size() > 0)
				redirect(results.get(0));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public String getActivityPageId() {
		return getString(R.string.tag_SearchActivity);
	}
}
