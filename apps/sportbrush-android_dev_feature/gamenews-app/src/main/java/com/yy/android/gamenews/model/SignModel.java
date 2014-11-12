package com.yy.android.gamenews.model;

import java.util.ArrayList;
import java.util.List;

import u.aly.E;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.duowan.gamenews.Button;
import com.duowan.gamenews.ButtonType;
import com.duowan.gamenews.CheckInActionReq;
import com.duowan.gamenews.CheckInActionRsp;
import com.duowan.gamenews.CheckInButton;
import com.duowan.gamenews.CheckInIndexReq;
import com.duowan.gamenews.CheckInIndexRsp;
import com.duowan.jce.wup.UniPacket;
import com.yy.android.gamenews.ServerConstants;
import com.yy.android.gamenews.util.Preference;
import com.yy.android.gamenews.util.Util;
import com.yy.android.sportbrush.R;

public class SignModel extends CommentModel {

	private static final boolean TEST = false;

	public static void sendSignReq(
			final ResponseListener<CheckInIndexRsp> listener) {

		if (TEST) {
			CheckInIndexRsp rsp = new CheckInIndexRsp();
			rsp.desc = "说明：1.T豆点券领取只能二选一;\n    2.点券领取只限每日签到前2500名玩家;\n    3.T豆领取不限名额（仅限YY用户）";

			rsp.checkInButton = new ArrayList<CheckInButton>();

			CheckInButton btn = new CheckInButton();
			btn.setCheckInType(0);
			btn.setIcon("http://183.60.218.177/channel/pq2.jpg?v=1402324319475");
			btn.setIsValid(true);
			btn.setName("btn1");
			rsp.checkInButton.add(btn);

			btn = new CheckInButton();
			btn.setCheckInType(1);
			btn.setIcon("http://183.60.218.177/channel/pq2.jpg?v=1402324319475");
			btn.setIsValid(true);
			btn.setName("btn2");
			rsp.checkInButton.add(btn);

			listener.onResponse(rsp);
			// listener.onResponse(getErrorIndexRsp(listener.get(),
			// ServerConstants.SUBCODE.GET_USER_INFO_FAIL));
			return;
		}

		UniPacket packet = createUniPacket("CheckInIndex");

		CheckInIndexReq req = new CheckInIndexReq();
		req.setPlatType(Preference.getInstance().getLoginType());
		packet.put("request", req);
		new Request(listener.get(), packet) {

			@Override
			public void onResponse(UniPacket response) {
				CheckInIndexRsp rsp = new CheckInIndexRsp();
				rsp = response.getByClass("result", rsp);

				if (rsp == null) {
					int subCode = response.get(ServerConstants.SUBCODE.KEY, 0);
					String desc = response.get(ServerConstants.MESSAGE.KEY, "");
					rsp = getErrorIndexRsp(listener.get(), subCode, desc);
				}

				listener.onResponse(rsp);
			}

			@Override
			public void onError(Exception e) {
				listener.onError(e);
				super.onError(e);
			}
		}.execute();
	}

	public static void sendCheckInActionReq(
			final ResponseListener<CheckInActionRsp> listener, int type) {
		if (TEST) {

			CheckInActionRsp rsp = new CheckInActionRsp();
			rsp.setDesc("您 是XXX个签到的用户！恭喜您得到100Q币！");
			rsp.setGiftCode("aoodjklfjw2ojflkje");

			ArrayList<Button> btnList = new ArrayList<Button>();
			Button btn = new Button();
			btn.buttonType = ButtonType._GO_YY_LOGIN;
			btn.name = "YY登陆";

			btnList.add(btn);

			btn = new Button();
			btn.buttonType = ButtonType._GO_CANCEL;
			btn.name = "取消";

			btnList.add(btn);

			rsp.setButton(btnList);
			listener.onResponse(getErrorActionRsp(listener.get(),
					ServerConstants.SUBCODE.LOGIN_FAIL, ""));
			return;
		}

		UniPacket packet = createUniPacket("CheckInAction");
		CheckInActionReq req = new CheckInActionReq();
		req.setCheckInType(type);
		req.setHash(Util.getKey());
		req.setPlatType(Preference.getInstance().getLoginType());
		packet.put("request", req);

		new Request(listener.get(), packet) {

			@Override
			public void onResponse(UniPacket response) {
				CheckInActionRsp rsp = new CheckInActionRsp();
				rsp = response.getByClass("result", rsp);

				if (rsp == null) {
					int subCode = response.get(ServerConstants.SUBCODE.KEY, 0);
					String desc = response.get(ServerConstants.MESSAGE.KEY, "");
					rsp = getErrorActionRsp(listener.get(), subCode, desc);
				}

				listener.onResponse(rsp);
			}

			@Override
			public void onError(Exception e) {
				listener.onError(e);
				super.onError(e);
			}
		}.execute();
	}

	public static CheckInActionRsp getErrorActionRsp(Context context,
			int subCode, String desc) {
		CheckInActionRsp rsp = new CheckInActionRsp();

		rsp.setButton(getErrorBtns(context, subCode));
		rsp.setDesc(getErrorDesc(context, subCode, desc));
		return rsp;
	}

	public static CheckInIndexRsp getErrorIndexRsp(Context context,
			int subCode, String desc) {
		CheckInIndexRsp rsp = new CheckInIndexRsp();

		rsp.setButton(getErrorBtns(context, subCode));
		rsp.setDesc(getErrorDesc(context, subCode, desc));
		return rsp;
	}

	private static String getErrorDesc(Context context, int subCode, String desc) {

		if (TextUtils.isEmpty(desc)) {
			Resources res = context.getResources();
			switch (subCode) {
			case ServerConstants.SUBCODE.LOGIN_FAIL: {
				desc = res.getString(R.string.sign_please_login);
				break;
			}
			case ServerConstants.SUBCODE.USER_SIGNED: {
				desc = res.getString(R.string.sign_already_signed);
				break;
			}
			default: {
				desc = res.getString(R.string.sign_fail);
				break;
			}
			}
		}
		return desc;
	}

	private static ArrayList<Button> getErrorBtns(Context context, int subCode) {
		ArrayList<Button> btnList = new ArrayList<Button>();
		Resources res = context.getResources();

		switch (subCode) {
		case ServerConstants.SUBCODE.LOGIN_FAIL: {
			btnList.add(new Button(res.getString(R.string.global_login),
					ButtonType._GO_USER_CENTER, ""));
			btnList.add(new Button(res.getString(R.string.global_cancel),
					ButtonType._GO_CANCEL, ""));

			break;
		}
		case ServerConstants.SUBCODE.USER_SIGNED: {
			btnList.add(new Button(res.getString(R.string.global_ok),
					ButtonType._GO_CANCEL, ""));
			break;
		}
		default: {

			btnList.add(new Button(res.getString(R.string.global_ok),
					ButtonType._GO_CANCEL, ""));
			break;
		}
		}

		return btnList;
	}
}
