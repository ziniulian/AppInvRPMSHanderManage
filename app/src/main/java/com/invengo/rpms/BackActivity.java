package com.invengo.rpms;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rpms.entity.OpType;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.util.Btn001;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;
import com.invengo.rpms.util.WrtUdRa;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;

import static com.invengo.rpms.util.WrtRa.WRT_OK;
import static com.invengo.rpms.util.WrtRa.WRT_UD_ERR;

public class BackActivity extends BaseActivity {

	TextView txtStatus;
	TextView txtInfo;
	Button btnConfig;

	private ListView mEpcListView;
	private PartsAdapter mListAdapter;
	private List<Map<String, Object>> listPartsData = new ArrayList<Map<String, Object>>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_back);

		reader.onMessageNotificationReceived.clear();
		reader.onMessageNotificationReceived.add(BackActivity.this);

		txtInfo = (TextView) findViewById(R.id.txtInfo);
		txtStatus = (TextView) findViewById(R.id.txtStatus);

		btnConfig = (Button) findViewById(R.id.btnConfig);
		btnConfig.setOnClickListener(btnConfigClickListener);

		final Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnTouchListener(new Btn001());
		btnBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (isReading) {
					showToast("请先停止读取");
				} else {
					finish();
				}
			}
		});

		mEpcListView = (ListView) this.findViewById(R.id.lstPartsRunAndStopView);
		// 创建SimpleAdapter适配器将数据绑定到item显示控件上
		mListAdapter = new PartsAdapter(this, listPartsData,
				R.layout.listview_parts_item2,
				new String[] { "sqeNo", "partsCode", "partsName"},
				new int[] {R.id.sqeNo, R.id.partsCode, R.id.partsInfo});
		// 实现列表的显示
		mEpcListView.setAdapter(mListAdapter);
	}

	@Override
	protected void onDestroy() {
		if (listPartsEntity.size() > 0) {
			List<String> ps = new ArrayList<String>();
			List<String> listSql = new ArrayList<String>();
			String user = myApp.getUserId();
			for (PartsEntity partsEntiry : listPartsEntity) {
				ps.add(partsEntiry.PartsCode);
				listSql.add("update TbParts set Status='D',"
						+ "LastOpTime='" + SqliteHelper.f.format(new Date())
//						+ "',Code='" + stationCodeStr
						+ "',OpUser='" + user
						+ "' where PartsCode='" + partsEntiry.PartsCode + "'");
			}
			String opTime = f.format(new Date());
			String remark = "";
			String info = user + "," + opTime + "," + remark;

			SqliteHelper.SaveOpRecord(ps, OpType.Back, info);	// 保存操作记录
			SqliteHelper.ExceSql(listSql);	// 更新本地数据库信息
		}

		super.onDestroy();
	}

	private OnClickListener btnConfigClickListener = new OnClickListener() {
		public void onClick(View v) {
			AlertDialog.Builder builder = new Builder(BackActivity.this, R.style.AppTheme);
			builder.setTitle("温馨提示");
			String info = getResources().getString(R.string.pairsBackTipInfo);
			builder.setMessage(info);
			builder.setPositiveButton("关闭", null);
			builder.show();
		}
	};

	@Override
	public void messageNotificationReceivedHandle(BaseReader reader, IMessageNotification msg) {
		if (isConnected && isReading) {
			try {
				if (msg instanceof RXD_TagData) {
					RXD_TagData data = (RXD_TagData) msg;
					String epc = Util.convertByteArrayToHexString(data
							.getReceivedMessage().getEPC());
					String userData = Util.convertByteArrayToHexString(data
							.getReceivedMessage().getUserData());

					// 找到配件信息并且验证是否允许启用
					if (UtilityHelper.CheckEpc(epc) == 0
							&& UtilityHelper.CheckUserData(userData, OpType.Back)) {
						if (IsValidEpc(epc, false)) {
							String partsCode = UtilityHelper.GetCodeByEpc(epc);
							if (partsCode.length() > 0) {
								boolean isExsit = false;
								for (PartsEntity entity : listPartsEntity) {
									if (entity.PartsCode.equals(partsCode)) {
										isExsit = true;
										break;
									}
								}

								if (!isExsit) {
									String tid = Util.convertByteArrayToHexString(data.getReceivedMessage().getTID());
									cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(DATA_ARRIVED_PAIRS, 0, 0, new String[] {tid, epc, "0700", partsCode, userData.substring(0, 4)}));
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		InvengoLog.i(TAG, "INFO.onKeyDown().");
		if (keyCode == KeyEvent.KEYCODE_BACK && !backDown) {
			backDown = true;
		} else if ((keyCode == KeyEvent.KEYCODE_SHIFT_LEFT
				|| keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SOFT_RIGHT)
				&& event.getRepeatCount() <= 0 && isConnected) {

			if (isReading == false) {
				StartRead();
			} else if (isReading == true) {
				StopRead();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void StartRead() {
		setRate(true);	// 最小功率

		isReading = true;
		listEPCEntity.clear();
		ReadTag readTag = new ReadTag(ReadMemoryBank.EPC_TID_UserData_6C);
		boolean result = reader.send(readTag);

		Message readMessage = new Message();
		readMessage.what = START_READ;
		readMessage.obj = result;
		cardOperationHandler.sendMessage(readMessage);
	}

	private void StopRead() {
		Message powerOffMsg = new Message();
		powerOffMsg.what = STOP_READ;
		powerOffMsg.obj = setRate();	// 最大功率
		cardOperationHandler.sendMessage(powerOffMsg);
	}

	// 写用户区
	private void writeCard(String[] sa, int a1, int a2) {
		if (setRate()) {	// 最大功率
			WrtUdRa r = new WrtUdRa(
					reader,
					sa,
					cardOperationHandler,
					a1,
					a2
			);
			new Thread(r).start();
		} else {
			StartRead();
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler cardOperationHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case START_READ:// 开始读卡
				boolean start = (Boolean) msg.obj;
				if (start) {
					txtStatus.setText(getResources()
							.getString(R.string.reading));
					isReading = true;
				} else {
					showToast(getResources().getString(R.string.sendFail));
				}
				break;
			case STOP_READ:// 停止读卡
				boolean stop = (Boolean) msg.obj;
				if (stop) {
					txtStatus.setText(getResources().getString(R.string.stop));
					isReading = false;
				} else {
					showToast(getResources().getString(R.string.stopFail));
				}
				break;
			case DATA_ARRIVED_PAIRS:// 接收配件数据
				writeCard((String[]) msg.obj, 0, 0);
				break;
			case WRT_UD_ERR:	// 用户区写入失败
				if (msg.arg1 == 1) {
					// 数据删除失败
					showToast("删除失败");
					StopRead();
				} else {
					StartRead();
				}
				break;
			case WRT_OK:// 接收配件数据
				String[] sa = (String[]) msg.obj;
				if (msg.arg1 == 1) {
					// 数据删除成功
					Map<String, Object> m = listPartsData.get(msg.arg2);
					PartsEntity o = (PartsEntity) m.get("obj");
					listPartsEntity.remove(o);
					listPartsData.remove(msg.arg2);
					// 调整序号
					for (int i = msg.arg2; i < listPartsData.size(); i ++) {
						listPartsData.get(msg.arg2).put("sqeNo", i + 1);
					}
					mListAdapter.notifyDataSetChanged();
					txtInfo.setText(String.format("数量:%s", listPartsData.size()));
					StopRead();
				} else {
					PartsEntity pe = UtilityHelper.GetPairEntityByCode(sa[3]);
					pe.Epc = sa[1];
					listPartsEntity.add(pe);

					Map<String, Object> item = new HashMap<String, Object>();
					item.put("sqeNo", listPartsData.size() + 1);
					item.put("partsCode", pe.PartsCode);
					item.put("partsName", pe.PartsType + "  " + pe.PartsName);
					item.put("tid", sa[0]);
					item.put("oud", sa[4]);		// 原用户区信息
					item.put("obj", pe);
					listPartsData.add(item);

					mListAdapter.notifyDataSetChanged();
					txtInfo.setText(String.format("数量:%s", listPartsData.size()));
					sp.play(music1, 1, 1, 0, 0, 1);
					StartRead();
				}
				break;
			case CONNECT:// 读写器连接
				boolean result = (Boolean) msg.obj;
				if (result) {
					txtStatus.setText(getResources().getString(
							R.string.connecSuccess));
					isConnected = true;
				} else {
					txtStatus.setText(getResources().getString(
							R.string.connecFail));
					isConnected = false;
				}
				break;
			}
		};
	};

	public class PartsAdapter extends SimpleAdapter {
		List<Map<String, Object>> mdata;

		public PartsAdapter(Context context, List<Map<String, Object>> data,
				int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
			this.mdata = data;
		}

		@Override
		public int getCount() {
			return mdata.size();
		}

		@Override
		public Map<String, Object> getItem(int position) {
			return mdata.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LinearLayout.inflate(getBaseContext(),
						R.layout.listview_parts_item2, null);
			}

			// 设置回调监听
			Button btnd = (Button) convertView.findViewById(R.id.btnDel);
			btnd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Map<String, Object> entity = listPartsData.get(position);
					String[] sa = new String[] {
							entity.get("tid").toString(),
							"",
							entity.get("oud").toString()
					};
					writeCard(sa, 1, position);
				}

			});

			return super.getView(position, convertView, parent);
		}
	}
}
