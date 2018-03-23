package com.invengo.rpms;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.PowerOff;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;
import invengo.javaapi.protocol.IRP1.WriteUserData_6C;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.invengo.rpms.R;
import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rpms.entity.OpType;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.entity.StationEntity;
import com.invengo.rpms.util.ReaderMessageHelper;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

public class RunActivity extends BaseActivity {

	TextView txtStatus;
	EditText edtStation;
	CheckBox cbxComfirmRun;
	TextView txtInfo;
	Button btnConfig;
	TextView txtTitle;

	private ListView mEpcListView;
	private PartsAdapter mListAdapter;
	private List<Map<String, Object>> listPartsData = new ArrayList<Map<String, Object>>();
	private String stationCodeStr = "";
	private List<String> listPartsCodeSucess = new ArrayList<String>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_run);

		txtInfo = (TextView) findViewById(R.id.txtInfo);
		txtTitle = (TextView) findViewById(R.id.txtTitle);

		btnConfig = (Button) findViewById(R.id.btnConfig);
		btnConfig.setOnTouchListener(btnConfigTouchListener);
		btnConfig.setOnClickListener(btnConfigClickListener);
		
		final Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnTouchListener( new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN: {
					// 按住事件发生后执行代码的区域
					btnBack.setBackgroundResource(R.color.lightwhite);
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					// 移动事件发生后执行代码的区域
					btnBack.setBackgroundResource(R.color.lightwhite);
					break;
				}
				case MotionEvent.ACTION_UP: {
					// 松开事件发生后执行代码的区域
					btnBack.setBackgroundResource(R.color.yellow);
					break;
				}
				default:

					break;
				}
				return false;
			}
		});
		btnBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (isReading) {
					showToast("请先停止读取");
				} else {
					finish();
				}
			}
		});


		cbxComfirmRun = (CheckBox) findViewById(R.id.cbxComfirmRun);
		txtStatus = (TextView) findViewById(R.id.txtStatus);

		edtStation = (EditText) findViewById(R.id.edtStation);
		edtStation.setCursorVisible(false);
		edtStation.setFocusable(false);
		edtStation.setFocusableInTouchMode(false);

		mEpcListView = (ListView) this
				.findViewById(R.id.lstPartsRunAndStopView);

		// 创建SimpleAdapter适配器将数据绑定到item显示控件上
		mListAdapter = new PartsAdapter(this, listPartsData,
				R.layout.listview_parts_item, new String[] { "sqeNo",
						"partsCode", "partsName", "delIcon" }, new int[] {
						R.id.sqeNo, R.id.partsCode, R.id.partsInfo,
						R.id.delImage });
		mListAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				if (view instanceof ImageView && data instanceof Drawable) {
					ImageView iv = (ImageView) view;
					iv.setImageDrawable((Drawable) data);
					return true;
				}
				return false;
			}
		});
		// 实现列表的显示
		mEpcListView.setAdapter(mListAdapter);
		
		reader.onMessageNotificationReceived.clear();
		reader.onMessageNotificationReceived.add(RunActivity.this);
	}

	private OnTouchListener btnConfigTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnConfig.setBackgroundResource(R.color.lightwhite);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnConfig.setBackgroundResource(R.color.lightwhite);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnConfig.setBackgroundResource(R.color.yellow);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnConfigClickListener = new OnClickListener() {
		public void onClick(View v) {

			AlertDialog.Builder builder = new Builder(RunActivity.this,
					R.style.AppTheme);
			builder.setTitle("温馨提示");
			String info = getResources().getString(R.string.pairsRunTipInfo);

			builder.setMessage(info);
			builder.setPositiveButton("关闭", null);
			builder.show();
		}
	};

	private void RunOP() {

		if (stationCodeStr.length() == 0) {
			showToast(String.format("请扫描到%s",
					getResources().getString(R.string.stattion)));
			return;
		}

		if (listPartsEntity.size() == 0) {
			showToast(String.format("请扫描到待%s%s",
					getResources().getString(R.string.run),
					getResources().getString(R.string.parts)));
			return;
		}

		for (PartsEntity partsEntiry : listPartsEntity) {
			if (!listPartsCodeSucess.contains(partsEntiry.PartsCode)) {
				// 写入标签用户数据
				WriteUserData_6C msg = ReaderMessageHelper.GetWriteUserData_6C(
						partsEntiry.Epc, OpType.Use);

				//int count = 0;
				//while (count < writeUserDataCount) {
					boolean result = reader.send(msg);
					if (result) {
						listPartsCodeSucess.add(partsEntiry.PartsCode);
						//break;
					}
					//count++;
		        //}
			}
		}

		if (listPartsCodeSucess.size() < listPartsEntity.size()) {
			showToast(String.format("%s失败，请重新操作",
					getResources().getString(R.string.pairsRun)));
			return;
		}

		// 保存操作记录
		String user = myApp.getUserId();
		String opTime = f.format(new Date());
		String remark = "";
		String hostCodeStr = "";
		String info = stationCodeStr + "," + hostCodeStr + "," + user + ","
				+ opTime + "," + remark;
		boolean result = SqliteHelper.SaveOpRecord(listPartsCodeSucess,
				OpType.Use, info);

		if (result) {

			showToast(String.format("%s成功",
					getResources().getString(R.string.pairsRun)));

			// 清除记录
			listPartsCodeSucess.clear();
			listPartsEntity.clear();
			listPartsData.clear();
			mListAdapter.notifyDataSetChanged();

		} else {
			showToast(String.format("%s失败，请重新操作",
					getResources().getString(R.string.pairsRun)));
		}
	}

	@Override
	public void messageNotificationReceivedHandle(BaseReader reader,
			IMessageNotification msg) {
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
							&& UtilityHelper
									.CheckUserData(userData, OpType.Use)) {
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

									PartsEntity entity = UtilityHelper
											.GetPairEntityByCode(partsCode);
									entity.Epc = epc;
									listPartsEntity.add(entity);

									Message dataArrivedMsg = new Message();
									dataArrivedMsg.what = DATA_ARRIVED_PAIRS;
									cardOperationHandler
											.sendMessage(dataArrivedMsg);
								}
							}
						}
					}

					// 找到站点信息
					if (UtilityHelper.CheckEpc(epc) == 2) {
						stationCodeStr = UtilityHelper.GetCodeByEpc(epc);
						if (stationCodeStr.length() > 0) {

							Message dataArrivedMsg = new Message();
							dataArrivedMsg.what = DATA_ARRIVED_STORAGE_LOCATION;
							cardOperationHandler.sendMessage(dataArrivedMsg);
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

			if (cbxComfirmRun.isChecked()) {
				if (isReading == true) {
					StopRead();
				}
				RunOP();
			} else {
				InvengoLog.i(TAG, "INFO.Start/Stop read tag.");
				if (isReading == false) {
					StartRead();
				} else if (isReading == true) {
					StopRead();
				}

			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void StartRead() {
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
		isReading = false;
		boolean result = reader.send(new PowerOff());
		Message powerOffMsg = new Message();
		powerOffMsg.what = STOP_READ;
		powerOffMsg.obj = result;
		cardOperationHandler.sendMessage(powerOffMsg);
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
				listPartsData.clear();
				int no = 1;
				for (PartsEntity partsEntity : listPartsEntity) {
					Map<String, Object> item = new HashMap<String, Object>();
					item.put("sqeNo", no);
					item.put("partsCode", partsEntity.PartsCode);
					item.put("partsName", partsEntity.PartsType + "  "
							+ partsEntity.PartsName);
					Drawable delDr = getResources().getDrawable(
							R.drawable.delete);
					item.put("delIcon", delDr);
					listPartsData.add(item);
					no++;
				}
				mListAdapter.notifyDataSetChanged();
				txtInfo.setText(String.format("数量:%s", listPartsData.size()));
				sp.play(music1, 1, 1, 0, 0, 1);
				break;
			case DATA_ARRIVED_STORAGE_LOCATION:// 接收站点数据
				StationEntity entity = SqliteHelper
						.queryStationByCode(stationCodeStr);
				if (entity != null) {
					edtStation.setText(entity.StationName);
				} else {
					edtStation.setText(stationCodeStr);
				}
				sp.play(music1, 1, 1, 0, 0, 1);
				StopRead();
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
			default:
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = LinearLayout.inflate(getBaseContext(),
						R.layout.listview_parts_item, null);
			}

			ImageView imgDel = (ImageView) convertView
					.findViewById(R.id.delImage);
			// 设置回调监听
			imgDel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Map<String, Object> entity = listPartsData.get(position);
					final String partsCodeSelected = entity.get("partsCode")
							.toString();

					for (PartsEntity partsEntiry : listPartsEntity) {
						if (partsEntiry.PartsCode.equals(partsCodeSelected)) {
							listPartsEntity.remove(partsEntiry);
							break;
						}
					}

					listPartsData.remove(position);
					mListAdapter.notifyDataSetChanged();
					txtInfo.setText(String.format("数量:%s", listPartsData.size()));
				}

			});

			return super.getView(position, convertView, parent);
		}
	}
}
