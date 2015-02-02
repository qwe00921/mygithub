package com.niuan.wificonnector;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.niuan.wificonnector.lib.ui.BaseFragment;

public class SecurityFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ScrollView sView = new ScrollView(getActivity());
		TextView view = new TextView(getActivity());
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		sView.setLayoutParams(params);
		// view.setImageResource(R.drawable.image_1);

		view.setTextSize(18);
		view.setLineSpacing(5, 1);
		view.setText("1. 尽量不要使用公共wifi热点。\n"
				+ "在公共地方的wifi热点，容易被黑客伪装，伪装成官方的wifi热点，一般市民难以区分。连接上这些伪装的公共wifi后，黑客会窃取用户的私密信息，或者植入后门，盗取银行卡密码等相关信息。免费的，未必就是安全的。\n"
				+ "2. 手机、平板电脑关闭自动连接wifi功能。\n"
				+ "自动连接公共wifi这个功能，容易接入伪装的wifi热点，造成数据被盗。为了设备的安全，请关闭这个耗电不讨好的功能。\n"
				+ "3. 安装安全防护软件。\n"
				+ "笔者在连接陌生网络前后，都会用安全的防护软件进行一次杀毒，另外，在使用软件的过程，可以开启安全软件的网络保护和隐私保护功能，对网页和其他软件的行为进行检测，并更新安全软件，防止黑客侵入。注意不要裸奔。\n"
				+ "4. 公共wifi下不要进行私密操作。\n"
				+ "如果实在需要连接到公共wifi热点下，请不要进行银行卡付款，或者网上购物等私密操作，可以去看网页，微信微博，如果是需要重新键入密码的，请不要在公共wifi下操作。\n"
				+ "5. 警惕钓鱼网站。\n"
				+ "对于公共的wifi热点，黑客经常利用的是伪装成正规银行网页或者支付的页面，误导用户登录，骗取用户名和密码。如上一条所说的，请不要公共网络进行私密操作，特殊的情况可以切换到手机的GPRS或者3G网络，流量使用不会太多，但最安全。\n"

		);
		sView.addView(view);
		return sView;
	}
	//
	// private Spanned getBoldText(String text) {
	// return Html.fromHtml("<fon");
	// }
}
