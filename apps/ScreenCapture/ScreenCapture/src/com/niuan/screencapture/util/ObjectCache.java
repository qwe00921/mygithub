package com.niuan.screencapture.util;

import java.util.LinkedList;
import java.util.List;

import com.niuan.remoteconnector.util.WaitableThread;
import com.niuan.remoteconnector.util.WaitableThread.Status;

public abstract class ObjectCache<E> {

	protected abstract E getObject();
	
	private List<E> mCacheList = new LinkedList<E>();
	
	private int mMaxCacheSize = 10;
	
	private Object mSyncObj = new Object();
	
	public void setMaxCacheSize(int size) {
		mMaxCacheSize = size;
	}
	
	public int getMaxCacheSize() {
		return mMaxCacheSize;
	}
	
	public void startFillingCache() {
		mGetObjectThread.start();
		mGetObjectThread.updateStatus(Status.RESUME);
	}
	
	public E pop() {
		synchronized(mSyncObj) {
			E obj = null;
			if (mCacheList.size() > 0) {
				obj = mCacheList.get(0);
				mCacheList.remove(0);
			}

			if (mCacheList.size() <= mMaxCacheSize / 2 && mGetObjectThread.getStatus() == Status.PAUSE) {
				mGetObjectThread.updateStatus(Status.RESUME);
			}

			return obj;

		}
	}
	
	protected void push(E object) {

		synchronized(mSyncObj) {
			mCacheList.add(object);
			if(getSize() == mMaxCacheSize) {
				mGetObjectThread.updateStatus(Status.PAUSE);
			}
		}
	}
	
	public int getSize() {
		synchronized(mSyncObj) {
			return mCacheList.size();
		}
	}
	
	public boolean hasNext() {
		synchronized(mSyncObj) {
			return mCacheList.isEmpty();
		}
	}
	
	public void clear() {
		synchronized(mSyncObj) {
			mCacheList.clear();
		}
	}
	
	private WaitableThread mGetObjectThread = new WaitableThread() {

		@Override
		public void execute() {
			synchronized(mSyncObj) {
				E obj = getObject();
				push(obj);
			}
		}
	};

	
	public static void main(String args[]) {
		
		new Thread() {
			public void run() {
				TestClass test = new TestClass();
				
				test.setMaxCacheSize(20);
				test.startFillingCache();
				while(true) {
					Object obj = test.pop();
					if(obj == null) {
//						try {
//							Thread.sleep(10);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
					} else {
						System.out.println(obj);
						
					}

				}
			};
		}.start();
		
	}
}


class TestClass extends ObjectCache {

	private long i = 0;
	@Override
	protected Object getObject() {
		return i++;//ScreenCaptureHelper.snapshot();
	}
	
}
