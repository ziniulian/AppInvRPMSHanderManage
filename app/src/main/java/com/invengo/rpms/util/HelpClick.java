package com.invengo.rpms.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import com.invengo.rpms.R;

/**
 * 帮助按钮动作
 * Created by Administrator on 2018/3/27.
 */

public class HelpClick implements View.OnClickListener {
	private String txt;
	private Context con;

	public HelpClick (Context c, String s) {
		this.con = c;
		this.txt = s;
	}

	@Override
	public void onClick(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(con, R.style.AppTheme);
		builder.setTitle("温馨提示");
		builder.setMessage(txt);
		builder.setPositiveButton("关闭", null);
		builder.show();
	}
}
