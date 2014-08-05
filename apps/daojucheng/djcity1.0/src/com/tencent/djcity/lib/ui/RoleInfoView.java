package com.tencent.djcity.lib.ui;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.djcity.R;
import com.tencent.djcity.more.GameInfo;
import com.tencent.djcity.more.SelectHelper;

public class RoleInfoView extends RelativeLayout {
	private TextView mRoleInfoTv;
	private Button mChangeAreaBtn;
	public RoleInfoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public RoleInfoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RoleInfoView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(final Context context) {
		inflate(context, R.layout.layout_role_info, this);
		mRoleInfoTv = (TextView) findViewById(R.id.category_role_info);
		
		mChangeAreaBtn = (Button) findViewById(R.id.category_change_area_btn);
		mChangeAreaBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SelectHelper.changeArea((Activity)context);
			}
		});
		refreshInfo();
	}
	
	public void refreshInfo() {
		GameInfo info = GameInfo.getGameInfoFromPreference();
		if(info != null) {
			
			boolean isNeedBind = info.needBind();
			if(isNeedBind) {
				mRoleInfoTv.setText("您还未绑定区服角色");
				mChangeAreaBtn.setText("绑定");
			} else {
				mRoleInfoTv.setText(info.getDescription());
				mChangeAreaBtn.setText("切换");
			}
		}
	}

}
