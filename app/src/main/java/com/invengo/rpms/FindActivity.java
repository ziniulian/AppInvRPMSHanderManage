package com.invengo.rpms;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.invengo.rfid.EmCb;
import com.invengo.rfid.InfTagListener;
import com.invengo.rfid.xc2910.Rd;
import com.invengo.rpms.util.Btn001;
import com.invengo.rpms.util.HelpClick;

/**
 * 配件巡检
 * Created by Administrator on 2018/3/27.
 */

public class FindActivity extends Activity {
	private Context con = this;
	private Rd rfd = new Rd();
	private String cod = null;	// 站点代码

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find);

		initView();
		initRd();
	}

	// 初始化控件
	private void initView() {
		Button btn;
		// 帮助按钮
		btn = (Button) findViewById(R.id.btnConfig);
		btn.setOnTouchListener(new Btn001());
		btn.setOnClickListener(new HelpClick(con, getResources().getString(R.string.pairsStockOutTipInfo)));

		// 退出按钮
		btn = (Button) findViewById(R.id.btnBack);
		btn.setOnTouchListener(new Btn001());
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				rfd.stop();
				finish();
			}
		});

		// TODO: 2018/3/28 初始化列表控件
	}

	// 初始化读写器
	private void initRd () {
		rfd.setHex(true);
		rfd.setTagListenter(new InfTagListener() {
			@Override
			public void onReadTag(com.invengo.rfid.tag.Base bt, InfTagListener itl) {
				// TODO: 2018/3/28 判断有无站点信息
				// 无站点代码，则判断是否为站点标签
					// 若是站点标签：则存储站点代码，停止扫描
				// 有站点代码，则判断是否为配件标签
					// 若是配件标签：若与页面罗列的配件信息一致，则勾选配件，更新扫描到的数目
			}

			@Override
			public void onWrtTag(com.invengo.rfid.tag.Base bt, InfTagListener itl) {
				//
			}

			@Override
			public void cb(EmCb e, String[] args) {
Log.i("-c-", e.name());
				// TODO: 2018/3/28 响应扫描停止事件
				// 若已有站点代码，且配件信息为空，则查询数据库，罗列站点相关的配件信息
			}
		});
		rfd.init();
	}

	@Override
	protected void onResume() {
		rfd.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		rfd.close();
		super.onPause();
	}

	// TODO: 2018/3/28 添加按键事件
	// TODO: 2018/3/28 添加 Handle
}
