package com.invengo.rpms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.invengo.rfid.EmCb;
import com.invengo.rfid.InfTagListener;
import com.invengo.rfid.util.Str;
import com.invengo.rfid.xc2910.Rd;
import com.invengo.rpms.bean.THDSEntity;
import com.invengo.rpms.util.Btn001;
import com.invengo.rpms.util.HelpClick;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

import java.util.ArrayList;
import java.util.List;

import static com.invengo.rpms.BaseActivity.START_READ;
import static com.invengo.rpms.BaseActivity.STOP_READ;

/**
 * 站点发卡
 * Created by LZR on 2018/6/13.
 */

public class StationSendCardActivity extends Activity {
	public static final int SSD_OLD = 1301;		// 旧标签
	public static final int SSD_ERR = 1302;		// 写入失败
	public static final int SSD_OK = 1303;		// 写入成功
	public static final int SSD_FLUSH = 1304;		// 刷新搜索列表

	private Context con = this;
	private Rd rfd = new Rd();	// 读写器
	private TextView txtStatus;	// 提示框
	private LinearLayout dlog;	// 弹出框
	private Button btno;		// 跳转按钮
	private Spinner sts;		// 选择框
	private List<THDSEntity> asts;	// 站点数据
	private List<String> rsch = new ArrayList<String>();	// 搜索结果
	private ArrayAdapter asch;		// 搜索结果匹配器
	private String cod;		// 站点EPC
	private int stu = 0;	// 状态
	private String tid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stationsendcard);

		initView();
		initRd();
	}

	// 初始化控件
	private void initView() {
		Button btn;

		// 弹出框
		dlog = (LinearLayout) findViewById(R.id.dlog);

		// 提示框
		txtStatus = (TextView)findViewById(R.id.txtStatus);

		// 跳转按钮
		btno =  (Button) findViewById(R.id.btnOK);
		btno.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(con, QueryTagActivity.class);
				startActivity(intent);
				finish();
			}
		});

		// 帮助按钮
		btn = (Button) findViewById(R.id.btnHelp);
		btn.setOnClickListener(new HelpClick(con, getResources().getString(R.string.pairsStationSendCardTipInfo)));

		// 退出按钮
		btn = (Button) findViewById(R.id.btnBack);
		btn.setOnTouchListener(new Btn001());
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				close();
				finish();
			}
		});

		// 取消按钮
		btn = (Button) findViewById(R.id.btnNo);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				close();
				dlog.setVisibility(View.GONE);
			}
		});

		// 确认按钮
		btn = (Button) findViewById(R.id.btnYes);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				stu = 2;
				setRate();
				dlog.setVisibility(View.GONE);
			}
		});

		// 下拉框
		sts = (Spinner) findViewById(R.id.sts);
		asts = SqliteHelper.getAllStations();
		ArrayAdapter<THDSEntity> adsts = new ArrayAdapter<THDSEntity>(this,android.R.layout.simple_spinner_item, asts);
		adsts.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sts.setAdapter(adsts);
		sts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				cod = "5A" + Str.Dat2Hexstr(asts.get(i).THDSCode);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {}
		});

		// 搜索结果列表
		ListView ls = (ListView) findViewById(R.id.searchResults);
		asch = new ArrayAdapter<String>(con, android.R.layout.simple_list_item_1, rsch);
		ls.setAdapter(asch);
		ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				String s = rsch.get(i);
				int d = s.indexOf(".");
				d = Integer.parseInt(s.substring(0, d));
				sts.setSelection(d - 1);
			}
		});

		// 搜索输入框
		EditText search = (EditText) findViewById(R.id.search);
		search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if (i == KeyEvent.ACTION_DOWN) {
					String d = textView.getText().toString();
					String r;
					rsch.clear();
					if (d.length() > 0) {
						for (i = 0; i < asts.size(); i ++) {
							r = asts.get(i).THDSName;
							if (r.contains(d)) {
								rsch.add((i + 1) + ". " + r);
							}
						}
					}
					sendMsg(SSD_FLUSH);
				}
				return false;
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
//					default:	// 所有标签
					case -1:	// 新标签
						tid = bt.getTidHexstr();
						stu = 3;
						setRate();
						break;
					case 2:	// 旧标签
						tid = bt.getTidHexstr();
						sendMsg(SSD_OLD);
						break;
				}
			}

			@Override
			public void onWrtTag(com.invengo.rfid.tag.Base bt, InfTagListener itl) {
				sendMsg(SSD_OK);
			}

			@Override
			public void cb(EmCb e, String[] args) {
				switch (e) {
					case ErrWrt:
						switch (stu) {
							case 2:		// 写旧标签
								sendMsg(SSD_ERR);
								break;
							case 3:		// 写新标签
								open();
								break;
						}
						break;
					case RateChg:
						switch (stu) {
							case 1:		// 开始读标签
								rfd.scan();
								break;
							case 2:		// 写旧标签
							case 3:		// 写新标签
								rfd.wrt("epc", cod, tid);
								break;
						}
						break;
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
					btno.setVisibility(View.GONE);
					txtStatus.setText("再次扣动扳机停止，正在写入中...");
					break;
				case STOP_READ:
					txtStatus.setText("扣动扳机，写入标签");
					break;
				case SSD_OLD:
					rfd.stop();
					dlog.setVisibility(View.VISIBLE);
					break;
				case SSD_ERR:
					close();
					Toast.makeText(con, "写入失败", Toast.LENGTH_SHORT).show();
					break;
				case SSD_OK:
					close();
					btno.setVisibility(View.VISIBLE);
					Toast.makeText(con, "写入成功", Toast.LENGTH_SHORT).show();
					break;
				case SSD_FLUSH:
					asch.notifyDataSetChanged();
					break;
			}
		}
	};

	private void sendMsg (int w) {
		hd.sendMessage(hd.obtainMessage(w));
	}

	private void open() {
		stu = 1;
		setRate(true);
		sendMsg(START_READ);
	}

	private void close() {
		stu = 0;
		setRate();
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
