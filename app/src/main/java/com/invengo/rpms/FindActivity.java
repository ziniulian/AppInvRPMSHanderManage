package com.invengo.rpms;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.invengo.rfid.EmCb;
import com.invengo.rfid.InfTagListener;
import com.invengo.rfid.xc2910.Rd;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.entity.StationEntity;
import com.invengo.rpms.util.Btn001;
import com.invengo.rpms.util.HelpClick;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.invengo.rpms.BaseActivity.START_READ;
import static com.invengo.rpms.BaseActivity.STOP_READ;

/**
 * 配件巡检
 * Created by Administrator on 2018/3/27.
 */

public class FindActivity extends Activity {
	public static final int FIND_SNAM = 1201;
	public static final int FIND_FLUSH = 1202;

	private Context con = this;
	private Rd rfd = new Rd();	// 读写器
	private String cod = null;	// 站点代码
	private int num = 0;		// 扫描到的个数
	private TextView snamTv;	// 站点名
	private TextView numTv;		// 扫描到的个数
	private TextView totalTv;		// 配件总数
	private TextView txtStatus;	// 提示框
	private ListView lv;
	private Map<String, HashMap<String, String>> dat = null;	// 数据
	private List<HashMap<String, String>> adpdat = new ArrayList<HashMap<String, String>>();	// 适配器数据
	private SimpleAdapter adp;

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
//		btn.setOnTouchListener(new Btn001());
		btn.setOnClickListener(new HelpClick(con, getResources().getString(R.string.pairsStockOutTipInfo)));

		// 退出按钮
		btn = (Button) findViewById(R.id.btnBack);
		btn.setOnTouchListener(new Btn001());
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				close();
				finish();
			}
		});

		snamTv = (TextView)findViewById(R.id.findStationName);	// 站点名
		numTv = (TextView)findViewById(R.id.findNum);	// 扫描到的个数
		totalTv = (TextView)findViewById(R.id.findCount);	// 配件总数
		txtStatus = (TextView)findViewById(R.id.txtStatus);	// 提示框

		// 列表
		lv = (ListView) findViewById(R.id.findLv);
		adp = new SimpleAdapter(con, adpdat, R.layout.listview_find_item,
			new String[] {"ok", "txt"}, new int[] {R.id.findOk, R.id.findTxt});
		lv.setAdapter(adp);
	}

	// 初始化读写器
	private void initRd () {
//		rfd.setHex(true);
		rfd.setTagListenter(new InfTagListener() {
			@Override
			public void onReadTag(com.invengo.rfid.tag.Base bt, InfTagListener itl) {
				String epc = bt.getEpcHexstr();
//Log.i("---", epc);
				if (cod == null) {
					if (UtilityHelper.CheckEpc(epc) == 2) {
						cod = UtilityHelper.GetCodeByEpc(epc);
						close();
					}
				} else if (dat != null && UtilityHelper.CheckEpc(epc) == 0) {
					// 若是配件标签：若与页面罗列的配件信息一致，则勾选配件，更新扫描到的数目
					String s = UtilityHelper.GetCodeByEpc(epc);
//Log.i("--P--", s);
					if (dat.containsKey(s)) {
						HashMap<String, String> m = dat.get(s);
						if (m.get("ok").length() == 0) {
							num ++;
							m.put("ok", "OK");
//Log.i("--OK--", s);
							sendMsg(FIND_FLUSH);
						}
					}
				}
			}

			@Override
			public void onWrtTag(com.invengo.rfid.tag.Base bt, InfTagListener itl) {
				// 无用 ...
			}

			@Override
			public void cb(EmCb e, String[] args) {
//Log.i("--c--", e.name());
				if (e == EmCb.Stopped && cod != null && dat == null) {
					dat = SqliteHelper.queryPartsInStation(cod);	// 查询数据库
					HashMap<String, String> m;
					for (String s: dat.keySet()) {
						PartsEntity entity = UtilityHelper.GetPairEntityByCode(s);
						m = dat.get(s);
						m.put("ok", "");
						m.put("txt", String.format(
								"厂家：%s\n型号：%s\n类别：%s\n名称：%s\n序列号：%s",
								entity.FactoryName,
								entity.PartsType,
								entity.BoxType,
								entity.PartsName,
								entity.SeqNo
						));
						adpdat.add(m);
					}
					sendMsg(FIND_SNAM);
					sendMsg(FIND_FLUSH);
				}
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_SHIFT_LEFT:
			case KeyEvent.KEYCODE_SHIFT_RIGHT:
			case KeyEvent.KEYCODE_SOFT_RIGHT:
				if (event.getRepeatCount() <= 0) {
					if (rfd.isBusy()) {
						close();
					} else {
						open();
					}
					return true;
				}
				break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private Handler hd = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case START_READ:
					txtStatus.setText(getResources().getString(R.string.reading));
					break;
				case STOP_READ:
					if (cod == null) {
						txtStatus.setText(getResources().getString(R.string.memo_FIND_STATION));
					} else {
						txtStatus.setText(getResources().getString(R.string.memo_FIND_PAIRS));
					}
					break;
				case FIND_FLUSH:
					numTv.setText(String.valueOf(num));
					adp.notifyDataSetChanged();
					break;
				case FIND_SNAM:
					StationEntity entity = SqliteHelper.queryStationByCode(cod);
					if (entity != null) {
						snamTv.setText(entity.StationName);
					} else {
						snamTv.setText(cod);
					}
					totalTv.setText(String.valueOf(dat.size()));
//Log.i("-------", "999");
					break;
			}
		}
	};

	private void sendMsg (int w) {
		hd.sendMessage(hd.obtainMessage(w));
	}

	private void open() {
		rfd.scan();
		sendMsg(START_READ);
	}

	private void close() {
		rfd.stop();
		sendMsg(STOP_READ);
	}
}
