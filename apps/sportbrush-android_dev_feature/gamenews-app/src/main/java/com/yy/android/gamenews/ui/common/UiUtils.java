package com.yy.android.gamenews.ui.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

//import com.tencent.djcity.R;
//import com.tencent.djcity.R;
//import com.tencent.djcity.lib.ui.RadioDialog.OnRadioSelectListener;
//import com.tencent.djcity.util.IcsonApplication;
//import com.tencent.djcity.util.ImageHelper;
//import com.tencent.djcity.util.ImageLoadListener;
//import com.tencent.djcity.util.ImageLoader;
import com.yy.android.gamenews.ui.view.AppDialog;
import com.yy.android.sportbrush.R;

public class UiUtils {

	public static AppDialog showDialog(Context aContext, int nCaptionResId,
			String strMessage, int nPositiveResId) {
		String strCaption = aContext.getString(nCaptionResId);
		return UiUtils.showDialog(aContext, strCaption, strMessage,
				nPositiveResId);
	}

	public static AppDialog showDialog(Context aContext, int nCaptionResId,
			int nMessageResId, int nPositiveResId) {
		return UiUtils.showDialog(aContext, nCaptionResId, nMessageResId,
				nPositiveResId, 0, null);
	}

	public static AppDialog showDialog(Context aContext, String strCaption,
			String strMessage, int nPositiveResId,
			AppDialog.OnClickListener aListener) {
		return UiUtils.showDialog(aContext, strCaption, strMessage,
				nPositiveResId, 0, aListener);
	}

	public static AppDialog showDialog(Context aContext, int nCaptionResId,
			int nMessageResId, int nPositiveResId,
			AppDialog.OnClickListener aListener) {
		String strCaption = aContext.getString(nCaptionResId);
		String strMessage = aContext.getString(nMessageResId);
		return UiUtils.showDialog(aContext, strCaption, strMessage,
				nPositiveResId, 0, aListener);
	}

	public static AppDialog showDialog(Context aContext, int nCaptionResId,
			int nMessageResId, int nPositiveResId, int nNegativeResId,
			AppDialog.OnClickListener aListener) {
		String strCaption = aContext.getString(nCaptionResId);
		String strMessage = aContext.getString(nMessageResId);
		return UiUtils.showDialog(aContext, strCaption, strMessage,
				nPositiveResId, nNegativeResId, aListener);
	}

	public static AppDialog showDialog(Context aContext, String strCaption,
			String strMessage, String strPositive, String strNegative,
			AppDialog.OnClickListener aListener) {

		AppDialog pUiDialog = new AppDialog(aContext, aListener);

		pUiDialog.setProperty(strCaption, strMessage, strPositive, strNegative);

		// Show the new dialog.
		pUiDialog.show();

		return pUiDialog;
	}

	public static AppDialog showDialog(Context context, String strCaption,
			View custView, String positive, String negative,
			AppDialog.OnClickListener aListener) {
		AppDialog pUiDialog = new AppDialog(context, aListener);

		pUiDialog.setProperty(strCaption, custView, positive, negative);

		// Show the new dialog.
		pUiDialog.show();

		return pUiDialog;
	}

	public static void showAlertDialog(Context aContext, int strCaption,
			int strMessage, int strPositive, int strNegative,
			OnClickListener aListener) {
		new AlertDialog.Builder(aContext).setTitle(strCaption)
				.setMessage(strMessage)
				.setPositiveButton(strPositive, aListener)
				.setNegativeButton(strNegative, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create().show();
	}

	public static AppDialog showDialog(Context aContext, String strCaption,
			String strMessage, int nPositiveResId, int nNegativeResId,
			AppDialog.OnClickListener aListener) {
		return UiUtils.showDialog(aContext, strCaption, strMessage, aContext
				.getString(nPositiveResId),
				(nNegativeResId > 0 ? aContext.getString(nNegativeResId) : ""),
				aListener);
	}

	public static AppDialog showDialog(Context aContext, String strCaption,
			String strMessage, int nPositiveResId) {
		return UiUtils.showDialog(aContext, strCaption, strMessage,
				nPositiveResId, 0, null);
	}

	public static AppDialog showDialog(Context aContext, String strCaption,
			String strMessage, String strPositive) {
		return UiUtils.showDialog(aContext, strCaption, strMessage,
				strPositive, "", null);
	}

	public static AppDialog showDialogWithCheckbox(Context aContext,
			String strCaption, String strMessage, int nPositiveResId,
			int nNegativeResId, String strCheckMessage,
			AppDialog.OnClickListener aListener) {
		AppDialog pUiDialog = new AppDialog(aContext, aListener);

		pUiDialog.setProperty(strCaption, strMessage,
				aContext.getString(nPositiveResId),
				aContext.getString(nNegativeResId), strCheckMessage);

		// Show the new dialog.
		pUiDialog.show();

		return pUiDialog;
	}

	public static Dialog loginingDialogShow(Context context) {
		Dialog mLoginingDialog = new Dialog(context, R.style.BaseDialog);
		mLoginingDialog.setContentView(R.layout.base_dialog_progress);
		mLoginingDialog.setCancelable(true);
		mLoginingDialog.setCanceledOnTouchOutside(false);
		mLoginingDialog.show();
		return mLoginingDialog;
	}

	public static Dialog cleanCacheDialogShow(Context context, String message) {
		ProgressDialog mDialog = new ProgressDialog(context, R.style.BaseDialog);
		mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		mDialog.setMessage(message);
		mDialog.setIndeterminate(false);// 设置进度条是否为不明确
		mDialog.setCancelable(false);// 设置进度条是否可以按退回键取消
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.show();
		return mDialog;
	}

	public static void dialogDismiss(Dialog mDialog) {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}
}
