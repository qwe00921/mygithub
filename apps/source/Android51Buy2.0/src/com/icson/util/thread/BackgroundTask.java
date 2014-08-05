package com.icson.util.thread;

/**
 * SyncBackgroundTask是在后台运行的任务，该任务会根据executorId的不同被安排在不同线程，
 * 相同executorId的任务会运行在相同的线程，并且根据execute调用方法的先后顺序而进行排队，先调用的会先执行
 * 
 * @author carlosliu
 *
 */
public abstract class BackgroundTask<PARAMS, RESULT> {
	
	private boolean mIsCanceled;
	public static final int DEFAULT_EXECUTOR_ID = 1001;
	
	private int mExecutorId = DEFAULT_EXECUTOR_ID;
	
	public int getExecutorId() {
		return mExecutorId;
	}

	public void setExecutorId(int executorId) {
		this.mExecutorId = executorId;
	}

	public final void cancel() {
		mIsCanceled = true;
		mExecutor.remove(this);
	}
	
	public boolean isCanceled() {
		return mIsCanceled;
	}
	
	private TaskExecutor mExecutor;
	
	public BackgroundTask() {
		mExecutor = TaskExecutor.getInstance(mExecutorId);
	}
	
	/**
	 * 会被TaskExecutor调用，运行于后台线程
	 */
	void callbackFromBackgroundThread() {
		result = doInBackground(params);
	}
	
	/**
	 * 当doInBackground调用完毕后，会调用onPosExecute方法回到UI线程
	 */
	void backToUIThread() {
		onPostExecute(result);
	}
	
	protected abstract RESULT doInBackground(PARAMS... params);
	protected abstract void onPostExecute(RESULT result);
	
	private PARAMS[] params;
	private RESULT result;
	/**
	 * 将任务添加到队列，如果线程暂停，会唤醒线程继续执行任务
	 * @param params
	 */
	public void execute(PARAMS... params) {
		this.params = params;
		if(mExecutor != null) {
			mExecutor.execute(this);
		}
	}
	
	/**
	 * 将该任务排在队列最前面，如果线程暂停，会唤醒线程继续执行任务
	 * @param params
	 */
	public void executeNow(PARAMS... params) {
		this.params = params;
		if(mExecutor != null) {
			mExecutor.executeNow(this);
		}
	}
}
