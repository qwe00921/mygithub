package com.niuan.remoteconnector.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class WaitableThread extends Thread {
	
	public enum Status {
		RESUME,
		PAUSE,
		EXIT
	}
	private Status mStatus = Status.PAUSE;
	
	public WaitableThread() {
		setName(getClass().getSimpleName());
	}
	
//	private Object mWaitObject = new Object();
	private WaitableEvent mWaitableObject = new WaitableEvent();
	public void updateStatus(Status status) {
		synchronized(this) {
			mStatus = status;
//			System.out.println("status = " + status);
			switch(status) {
				case RESUME: {
					doResume();
					break;
				}
				case EXIT: {
					break;
				}
				case PAUSE: {
					break;
				}
			}
		}
	}
	
	public Status getStatus() {
		synchronized(this) {
			return mStatus;
		}
	}
	
	private static boolean ENABLE_RESUME_LOG = false;
	private static boolean ENABLE_PAUSE_LOG = false;
	
//	private static boolean ENABLE_PAUSE_LOG = true;
//	private static boolean ENABLE_RESUME_LOG = true;
	Lock mLock = new ReentrantLock();
	private void doPause() {
		
		
		
		if(ENABLE_PAUSE_LOG) {
			System.out.println("pause");
		}

		boolean b = false;
		synchronized (this) {
			if (mStatus == Status.PAUSE) {
				b = true;
			}
		}
		
		if(b) {
			if (ENABLE_PAUSE_LOG) {
				System.out.println("doPause");
			}
			mWaitableObject.waitForever();
		}
		
//		synchronized (mWaitObject) {
//
//			if (b) {
//				
//				if (ENABLE_PAUSE_LOG) {
//					System.out.println("doPause");
//				}
//				// Need to set status to pause
//				mStatus = Status.PAUSE;
//				try {
//					mWaitObject.wait();
//					return;
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
		
	}
	
	private void doResume() {
		if(ENABLE_RESUME_LOG) {
			
			System.out.println("resume");
		}
		
		if (ENABLE_RESUME_LOG) {
			System.out.println("doResume");
		}
		mWaitableObject.signal();
//		synchronized (mWaitObject) {
//
//			if (ENABLE_RESUME_LOG) {
//				System.out.println("doResume");
//			}
//			mWaitObject.notify();
//		}
	}
	
	@Override
	public final void run() {
		Status status;
		while(true) {
			synchronized(this) {
				status = mStatus;
			}
			switch(status) {
				case EXIT: {
					return;
				}
				case PAUSE: {
					doPause();
					break;
				}
			}
			execute();
		}
	}
	
	public abstract void execute();
	
	private class WaitableEvent {
		public void waitForever() {
			synchronized (this) {
				try {
					mStatus = Status.PAUSE;
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public void signal() {
			synchronized (this) {
				notify();
			}
		}
	}
}
