package com.invengo.rpms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.invengo.rpms.util.Btn001;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.SynchroDbRa;

public class SysSetActivity extends Activity {

	Button btnTest;
	EditText edtServiceIp;
	EditText edtServiceCom;
	EditText maxRate;
	EditText minRate;
	TextView txtInfo;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sysset);
		
		final Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnTouchListener(new Btn001());
		btnBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
					finish();
			}
		});

		txtInfo = (TextView) findViewById(R.id.txtInfo);
		edtServiceIp = (EditText) findViewById(R.id.edtServiceIp);
		edtServiceCom = (EditText) findViewById(R.id.edtServiceCom);
		maxRate = (EditText) findViewById(R.id.maxRate);
		minRate = (EditText) findViewById(R.id.minRate);
		edtServiceIp.setText(SqliteHelper.kvGet("synUrlIp"));
		edtServiceCom.setText(SqliteHelper.kvGet("synUrlPort"));
		String s = SqliteHelper.kvGet("maxRate");
		if (s != null) {
			maxRate.setText(s);
		}
		s = SqliteHelper.kvGet("minRate");
		if (s != null) {
			minRate.setText(s);
		}

		btnTest = (Button) findViewById(R.id.btnTest);
		btnTest.setOnTouchListener(btnTestTouchListener);
		btnTest.setOnClickListener(btnTestClickListener);
	}

	private OnTouchListener btnTestTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					// 按住事件发生后执行代码的区域
					btnTest.setBackgroundResource(R.color.lightwhite);
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					// 移动事件发生后执行代码的区域
					btnTest.setBackgroundResource(R.color.lightwhite);
					break;
				}
				case MotionEvent.ACTION_UP: {
					// 松开事件发生后执行代码的区域
					btnTest.setBackgroundResource(R.color.yellow);
					break;
				}
			}
			return false;
		}
	};

	private OnClickListener btnTestClickListener = new OnClickListener() {
		public void onClick(View v) {
			rate(maxRate, "maxRate");
			rate(minRate, "minRate");
			syn();
		}

		// 功率设置
		private void rate (EditText et, String k) {
			int v = Integer.parseInt(et.getText().toString());
			if (v < 1) {
				v = 1;
			} else if (v > 30) {
				v = 30;
			}
			String s = v + "";
			et.setText(s);
			SqliteHelper.kvSet(k, s);
		}

		// 数据同步
		private void syn () {
			String serverIp = edtServiceIp.getText().toString().trim();
			if (serverIp.length() == 0) {
				showToast("请输入服务地址");
				return;
			}

			String serverCom = edtServiceCom.getText().toString().trim();
			if (serverIp.length() == 0) {
				showToast("请输入服务端口");
				return;
			}

			txtInfo.setText("正在同步数据，请稍候... ");
			SynchroDbRa.reset(serverIp, serverCom, testHd);
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler testHd = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SynchroDbRa.SYN_OK:
					txtInfo.setText("同步成功！");
					break;
				default:
					txtInfo.setText("同步失败！");
					break;
			}
		}
	};

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyUp(keyCode, event);
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}
