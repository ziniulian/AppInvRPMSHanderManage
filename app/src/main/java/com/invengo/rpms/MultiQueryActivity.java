package com.invengo.rpms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.invengo.rfid.EmCb;
import com.invengo.rfid.InfTagListener;
import com.invengo.rfid.tag.Base;
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

import invengo.javaapi.core.Util;

import static com.invengo.rpms.BaseActivity.CONNECT;
import static com.invengo.rpms.BaseActivity.DATA_ARRIVED_PAIRS;
import static com.invengo.rpms.BaseActivity.DATA_ARRIVED_STATION;
import static com.invengo.rpms.BaseActivity.DATA_ARRIVED_STORAGE_LOCATION;
import static com.invengo.rpms.BaseActivity.START_READ;
import static com.invengo.rpms.BaseActivity.STOP_READ;

/**
 * 多标签查询
 * Created by LZR on 2018/9/19.
 */

public class MultiQueryActivity extends Activity {
	private Context con = this;
	private Rd rfd = new Rd();	// 读写器
	private TextView txtStatus;	// 提示框

	private Map<String, Object> ms = new HashMap<String, Object>();
	private List<String> rsc = new ArrayList<String>();	// 扫描结果
	private ArrayAdapter asc;		// 扫描结果匹配器

	private SoundPool sp;
	private int music2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multiquery);

		sp = new SoundPool(3, AudioManager.STREAM_SYSTEM, 5);// 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
		music2 = sp.load(this, R.raw.right, 2); // 把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级

		initView();
		initRd();
	}

	// 初始化控件
	private void initView() {
		Button btn;

		// 提示框
		txtStatus = (TextView)findViewById(R.id.txtStatus);

		// 帮助按钮
		btn = (Button) findViewById(R.id.btnHelp);
		btn.setOnClickListener(new HelpClick(con, getResources().getString(R.string.pairsMultiQueryTipInfo)));

		// 退出按钮
		btn = (Button) findViewById(R.id.btnBack);
		btn.setOnTouchListener(new Btn001());
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				close();
				finish();
			}
		});

		// 清空按钮
		btn = (Button) findViewById(R.id.btnNo);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				close();
				rsc.clear();
				ms.clear();
				sendMsg(CONNECT);
			}
		});

		// 搜索结果列表
		ListView ls = (ListView) findViewById(R.id.scans);
		asc = new ArrayAdapter<String>(con, R.layout.listview_simple_item, rsc);
		ls.setAdapter(asc);
		ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				close();

				String epc;
				String s = rsc.get(i);
				int typ;
				int d = s.indexOf("\n");
				if (d > 0) {
					if (s.charAt(0) == '站') {
						typ = 2;
						epc = s.substring(5, d);
					} else {
						typ = 0;
						epc = s.substring(0, d);
					}
				} else {
					typ = 1;
					epc = s.substring(5);
				}
//Log.i("------", typ + " , " + epc);
				Intent it = new Intent(con, MultiQueryOneActivity.class);
				it.putExtra("epc", epc);
				it.putExtra("typ", typ);
				if (typ == 0) {
					byte[] bs = (byte[]) ms.get(epc);
					it.putExtra("stu", bs[0]);
					it.putExtra("err", bs[1]);
				}
				startActivity(it);
			}
		});
	}

	// 初始化读写器
	private void initRd () {
		rfd.setHex(true);
		rfd.setTagListenter(new InfTagListener() {
			@Override
			public void onReadTag(com.invengo.rfid.tag.Base bt, InfTagListener itl) {
				String epc = bt.getEpcHexstr();
//Log.i("------", epc);
				switch (UtilityHelper.CheckEpc(epc)) {
					case 0:
						byte[] bs = Util.convertHexStringToByteArray(bt.getUseHexstr());
						sendMsg(DATA_ARRIVED_PAIRS, new Object[] {UtilityHelper.GetCodeByEpc(epc), bs});
						break;
					case 1:
						sendMsg(DATA_ARRIVED_STORAGE_LOCATION, UtilityHelper.GetCodeByEpc(epc));
						break;
					case 2:
						sendMsg(DATA_ARRIVED_STATION, UtilityHelper.GetCodeByEpc(epc));
						break;
				}
			}

			@Override
			public void onWrtTag(Base bt, InfTagListener itl) {}

			@Override
			public void cb(EmCb e, String[] args) {}
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
			String epc;
			switch (msg.what) {
				case START_READ:
					txtStatus.setText("再次扣动扳机停止");
					break;
				case STOP_READ:
					txtStatus.setText("扣动扳机，读取标签");
					break;
				case DATA_ARRIVED_PAIRS:
					Object[] os = (Object[]) msg.obj;
					epc = (String) os[0];
					if (!ms.containsKey(epc)) {
						ms.put(epc, os[1]);
						PartsEntity pe = UtilityHelper.GetPairEntityByCode(epc);
//						rsc.add(String.format("配件编码：%s\n厂家：%s\n型号：%s\n类别：%s\n名称：%s\n序列号：%s", epc, pe.FactoryName, pe.PartsType, pe.BoxType, pe.PartsName, pe.SeqNo));
						rsc.add(String.format("%s\n%s-%s", epc, pe.BoxType, pe.PartsName));
						asc.notifyDataSetChanged();
						sp.play(music2, 1, 1, 0, 0, 1);
					}
					break;
				case DATA_ARRIVED_STORAGE_LOCATION:
					epc = (String) msg.obj;
					if (!ms.containsKey(epc)) {
						ms.put(epc, rsc.size());
						rsc.add("库位编码：" + epc);
						asc.notifyDataSetChanged();
						sp.play(music2, 1, 1, 0, 0, 1);
					}
					break;
				case DATA_ARRIVED_STATION:
					epc = (String) msg.obj;
					if (!ms.containsKey(epc)) {
						ms.put(epc, rsc.size());
						StationEntity se = SqliteHelper.queryStationByCode(epc);
						rsc.add(String.format("站点编码：%s\n站点名称：%s", epc, se.StationName));
						asc.notifyDataSetChanged();
						sp.play(music2, 1, 1, 0, 0, 1);
					}
					break;
				case CONNECT:
					asc.notifyDataSetChanged();
					break;
			}
		}
	};

	private void sendMsg (int w) {
		hd.sendMessage(hd.obtainMessage(w));
	}

	private void sendMsg (int w, Object o) {
		hd.sendMessage(hd.obtainMessage(w, o));
	}

	private void open() {
		rfd.scan();
		sendMsg(START_READ);
	}

	private void close() {
		rfd.stop();
		sendMsg(STOP_READ);
	}

	// 设置功率
	protected void setRate (boolean isMin) {
		String rat;
		if (isMin) {
			rat = SqliteHelper.kvGet("minRate");
			if (rat == null) {
				rat = "5";
				SqliteHelper.kvSet("minRate", rat);
			}
		} else {
			rat = SqliteHelper.kvGet("maxRate");
			if (rat == null) {
				rat = "30";
				SqliteHelper.kvSet("maxRate", rat);
			}
		}
		rfd.rate(rat);
	}
	protected void setRate () {
		setRate(false);
	}

}
