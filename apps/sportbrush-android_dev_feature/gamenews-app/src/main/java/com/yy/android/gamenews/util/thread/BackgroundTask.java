package com.yy.android.gamenews.util.thread;

/**
 * SyncBackgroundTask是在后台运行的任务，该任务会根据executorId的不同被安排在不同线程，
 * 相同executorId的任务会运行在相同的线程，并且根据execute调用方法的先后顺序而进行排队，先调用的会先执行
 * 
 * @author carlosliu
 * 
 */
public abstract class BackgroundTask<PARAMS, PROGRESS, RESULT> {

	private boolean mIsCanceled;
	public static final int EXECUTOR_ID_DEFAULT = 1001;
	public static final int EXECUTOR_ID_DAEMON = 1002;

	private int mExecutorId = EXECUTOR_ID_DEFAULT;

	public int getExecutorId() {
		return mExecutorId;
	}

	public void setExecutorId(int executorId) {
		this.mExecutorId = executorId;
		mExecutor = TaskExecutor.getInstance(mExecutorId);
	}

	private void ensureExecutor() {
		if (mExecutor == null) {
			mExecutor = TaskExecutor.getInstance(mExecutorId);
		}
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

	protected void onPostExecute(RESULT result) {

	};

	private PARAMS[] params;
	private RESULT result;

	/**
	 * 将任务添加到队列，如果线程暂停，会唤醒线程继续执行任务
	 * 
	 * @param params
	 */
	public void execute(PARAMS... params) {
		this.params = params;
		ensureExecutor();
		mExecutor.execute(this);
	}

	/**
	 * 将该任务排在队列最前面，如果线程暂停，会唤醒线程继续执行任务
	 * 
	 * @param params
	 */
	public void executeNow(PARAMS... params) {
		this.params = params;
		ensureExecutor();
		mExecutor.executeNow(this);
	}
}
