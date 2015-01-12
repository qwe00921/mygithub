package com.niuan.wificonnector.lib.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public class FrameFragmentItem {
	public FrameButton btn;
	public Fragment fragment;
	public String name;

	void show(FragmentTabTransaction t) {
		FragmentTransaction transaction = t.getTransaction();
		transaction.show(fragment);
		onShown();
	}

	void hide(FragmentTabTransaction t) {
		FragmentTransaction transaction = t.getTransaction();
		transaction.hide(fragment);
		onHidden();
	}

	void add(FragmentTabTransaction t) {
		FragmentTransaction transaction = t.getTransaction();
		transaction.add(FrameFragmentLayout.CONTAINER_ID, fragment, getName());
	}

	void remove(FragmentTabTransaction t) {
		FragmentTransaction transaction = t.getTransaction();
		transaction.remove(fragment);
	}

	void replace(FragmentTabTransaction t) {
		FragmentTransaction transaction = t.getTransaction();
		transaction.replace(FrameFragmentLayout.CONTAINER_ID, fragment);
	}

	public String getName() {
		return name;
	}

	protected void onShown() {

	}

	protected void onHidden() {

	}

}
