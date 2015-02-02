package com.niuan.wificonnector.lib.ui;

import java.io.Serializable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.niuan.wificonnector.R;
import com.niuan.wificonnector.lib.ui.observer.FragmentObserver;

public class InputDialogFragment extends BaseDialogFragment {

	private Dialog dialog;
	private Serializable data;
	private EditText editText;
	private String title;

	public static InputDialogFragment newInstance(String title,
			Serializable params) {
		InputDialogFragment fragment = new InputDialogFragment();
		Bundle args = new Bundle();
		args.putSerializable("data", params);
		args.putSerializable("title", title);
		fragment.setArguments(args);
		return fragment;
	}

	public void finish() {
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();

		title = args.getString("title");
		data = args.getSerializable("data");
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View customView = inflater.inflate(R.layout.dialog_input, null);

		editText = (EditText) customView.findViewById(R.id.password);
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
				.setTitle(title)
				.setPositiveButton("连接", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Fragment parent = getParentFragment();
						if (parent instanceof FragmentObserver) {
							((FragmentObserver) parent).onFragmentEvent(
									AppEvent.FRAGMENT_DIALOG_PASSWORD, null,
									data, editText.getEditableText()
											.toString());
						}
						dialog.dismiss();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setView(customView).create();
		// dialog = new Dialog(getActivity(), R.style.BaseDialog);
		// dialog.setContentView(R.layout.dialog_input);
		// dialog.findViewById(R.id.btn_ok).setOnClickListener(
		// new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Fragment parent = getParentFragment();
		// if (parent instanceof FragmentObserver) {
		// ((FragmentObserver) parent).onFragmentEvent(
		// AppEvent.FRAGMENT_DIALOG_INPUT_OK, null,
		// index);
		// }
		// // FragmentObserver observer = getFragmentObserver();
		// // if (observer != null) {
		// // observer.onFragmentEvent(AppEvent.FRAGMENT_DIALOG_INPUT_OK,
		// // getNotifyIndex(), index);
		// // }
		// dismissAllowingStateLoss();
		// }
		// });
		// dialog.setCancelable(true);
		// dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}
}