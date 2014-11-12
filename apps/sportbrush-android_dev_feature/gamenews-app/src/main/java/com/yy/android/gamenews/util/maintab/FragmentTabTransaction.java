package com.yy.android.gamenews.util.maintab;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class FragmentTabTransaction {

	private FragmentTransaction mTransaction;

	public static FragmentTabTransaction beginTransaction(
			FragmentActivity activity) {
		FragmentTransaction transaction = activity.getSupportFragmentManager()
				.beginTransaction();

		return new FragmentTabTransaction(transaction);
	}

	private FragmentTabTransaction(FragmentTransaction transaction) {
		mTransaction = transaction;
	}

	FragmentTransaction getTransaction() {
		return mTransaction;
	}

	public FragmentTabTransaction add(MainFragmentTab tab) {
		if (tab != null) {
			tab.add(this);
		}
		return this;
	}

	public FragmentTabTransaction show(MainFragmentTab tab) {
		if (tab != null) {
			tab.show(this);
		}
		return this;
	}

	public FragmentTabTransaction hide(MainFragmentTab tab) {
		if (tab != null) {
			tab.hide(this);
		}
		return this;
	}

	public void commit() {
		mTransaction.commitAllowingStateLoss();
	}
}
