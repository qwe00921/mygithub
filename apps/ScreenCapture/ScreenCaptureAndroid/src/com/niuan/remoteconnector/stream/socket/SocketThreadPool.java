package com.niuan.remoteconnector.stream.socket;

import java.util.ArrayList;
import java.util.List;

import com.niuan.remoteconnector.util.WaitableThread;

public class SocketThreadPool {

	

}

/**
 * This class is used to execute task in background thread for multiple tasks.
 * For example, we need to send an object to multiple output streams, we will 
 * use this class to do this thing. It will send the object to each stream
 * by different thread. Each stream will get a unique thread for it, the thread 
 * will be reused the next time send the data.
 * 
 * You can set the max thread number via setMaxThread(int), if the thread number 
 * reaches the max, it will not allocate new threads for new added executor, it will
 * queue the tasks, and execute it immediately when thread is available.
 * @author chaoqun_liu
 *
 */
abstract class AsyncTaskExecutor {
	private int mMaxThreads = 10;
	
	private List<Object> mTagList = new ArrayList<Object>();
	private List<Runnable> mRunnableList = new ArrayList<Runnable>();
	
	public void addTag(Object tag) {
		synchronized(this) {
			if(!mTagList.contains(tag)) {
				mTagList.add(tag);
			}
		}
	}
	
	public void removeTag(Object tag) {
		synchronized(this) {
			if(!mTagList.contains(tag)) {
				mTagList.add(tag);
			}
		}
	}
	
	public abstract void execute(Runnable runnable);
	
	public abstract void onExecuteFinished();
	
	public boolean isTaskExecuted(Runnable runnable, Object tag) {
		return false;
	}
}

class TaskHolder {
	private Object mTag;
	private Runnable mRunnable;
	private boolean mIsExecuted;

	public Object getTag() {
		return mTag;
	}

	public void setTag(Object tag) {
		mTag = tag;
	}

	public Runnable getRunnable() {
		return mRunnable;
	}

	public void setRunnable(Runnable runnable) {
		mRunnable = runnable;
	}

	public boolean isIsExecuted() {
		return mIsExecuted;
	}

	public void setIsExecuted(boolean isExecuted) {
		mIsExecuted = isExecuted;
	}

}


class ExecutorThread extends WaitableThread {
	
	@Override
	public void execute() {
		// Get runnable from pool and run;
	}
}
