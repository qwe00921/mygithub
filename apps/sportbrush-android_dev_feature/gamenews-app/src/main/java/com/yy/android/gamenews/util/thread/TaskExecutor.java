package com.yy.android.gamenews.util.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.SparseArray;

/**
 * 以队列的方式执行任务，会调用任务的doInBackground方法用于执行后台任务，当doInBackground执行完成之后，会调用onPostExecuted方法
 * 可以使用pause方法暂停后台线程的执行，需要时调用resume来恢复运行
 * 
 * 每个Executor都有唯一的线程，Task在相同id的executor里执行会运行在相同的线程中。
 * 
 * @author carlosliu
 *
 */
public class TaskExecutor {
	public enum Status {
		RESUME,
		PAUSE,
	}
	private Handler mNonUiHandler;
	private HandlerThread mNonUiThread;
	private Handler mUiHandler;
	private static final int DEFAULT_MESSAGE_ID = 1001;
	
	private static SparseArray<TaskExecutor> mExecutorPool = new SparseArray<TaskExecutor>();
	private static final String THREAD_NAME = "AsyncTaskExecutorThread";

	private int mId;
	private ThreadSwitcher mSyncObj;
	private TaskExecutor(int id) {
		setId(id);
		mSyncObj = new ThreadSwitcher();
		
		mNonUiThread = new HandlerThread(THREAD_NAME + id);
		mNonUiThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
		mNonUiThread.start();
		
		mNonUiHandler = new Handler(mNonUiThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
			
				BackgroundTask<?, ?, ?> task = (BackgroundTask<?, ?, ?>) msg.obj;
				if(task != null && task.isCanceled()) {
					return;
				}
				doInBackground(task);
				
				Message uiMessage = mUiHandler.obtainMessage(DEFAULT_MESSAGE_ID);
				uiMessage.obj = task;
				uiMessage.setTarget(mUiHandler);
				uiMessage.sendToTarget();
			}
		};
		
		mUiHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				BackgroundTask<?, ?, ?> task = (BackgroundTask<?, ?,?>) msg.obj;
				
				if(task != null) {
					onPostExecuted(task);
				}
			}
		};
	}
	
	/**
	 * 通过executorId来获取executor
	 * @param executorId
	 * @return
	 */
	public static TaskExecutor getInstance(int executorId) {
		
		TaskExecutor executor = null;
		synchronized(TaskExecutor.class) {
			executor = mExecutorPool.get(executorId);
		}
		if(executor == null) {
			synchronized(TaskExecutor.class) {
				if(executor == null) {
					executor = new TaskExecutor(executorId);
					mExecutorPool.put(executorId, executor);
				}
			}
		}
		
		return executor;
	}
	
	public int getId() {
		return mId;
	}

	public void setId(int id) {
		this.mId = id;
	}

	public void executeNow(BackgroundTask<?, ?,?> task) {
		resume();
		
		Message msg = mNonUiHandler.obtainMessage(DEFAULT_MESSAGE_ID);
		msg.obj = task;
		msg.what = DEFAULT_MESSAGE_ID;
		mNonUiHandler.sendMessageAtFrontOfQueue(msg);
	}
	
	public void execute(BackgroundTask<?, ?,?> task) {
		resume();
		queue(task);
	}
	
	public void queue(BackgroundTask<?, ?,?> task) {
		
		Message msg = mNonUiHandler.obtainMessage(DEFAULT_MESSAGE_ID);
		msg.obj = task;
		msg.what = DEFAULT_MESSAGE_ID;
		mNonUiHandler.sendMessage(msg);
	}
	
	public void pause() {
		synchronized(this) {
			mStatus = Status.PAUSE;
		}
	}
	
	public void resume() {
		synchronized(this) {
			if(mStatus == Status.RESUME) {
				return;
			}
			mStatus = Status.RESUME;
			doResume();
		}
	}
	
	public void remove(BackgroundTask<?, ?,?> task) {
		if(task != null) {
			mNonUiHandler.removeMessages(DEFAULT_MESSAGE_ID, task);
		}
	}
	
	private void doPause() {
		boolean needPause = false;
		synchronized (this) {
			if (mStatus == Status.PAUSE) {
				needPause = true;
			}
		}
		
		if(needPause) {
			mSyncObj.pause();
		}
		
	}
	
	private void doResume() {
		mSyncObj.resume();
	}
	
	/**
	 * 检查是否需要暂停，当用户调用了pause或者resume时，会对executor的状态进行改变。
	 * 该方法会在状态为PAUSE的时候对线程进行阻塞，用户需要调用resume来唤起线程继续工作
	 */
	private void checkStatus() {
		Status status;
		synchronized(this) {
			status = mStatus;
		}
		switch(status) {
//			case EXIT: {
//				mSyncObj = null;
//				return;
//			}
			case RESUME: {
				break;
			}
			case PAUSE: {
				doPause();
				break;
			}
		}
	}
	
	private Status mStatus = Status.PAUSE;
	private void doInBackground(BackgroundTask<?, ?,?> task) {
		checkStatus();
		
		if(task != null) {
			task.callbackFromBackgroundThread();
		}
	}

	private void onPostExecuted(BackgroundTask<?, ?,?> task) {
		if (task != null) {
			task.backToUIThread();
		}
	}
	
}
