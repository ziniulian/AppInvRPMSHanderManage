package com.invengo.rpms;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.entity.StationEntity;
import com.invengo.rpms.entity.TbCodeEntity;
import com.invengo.rpms.util.Btn001;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

import java.util.HashMap;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.PowerOff;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;

public class QueryTagActivity extends BaseActivity {

	TextView txtStatus;
	TextView txtTagInfo;

	private String storageLocationStr = "";
	private String partsCode = "";
	private String stationCode = "";
	private String userData="";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_querytag);

		txtStatus = (TextView) findViewById(R.id.txtStatus);
		txtTagInfo = (TextView) findViewById(R.id.txtTagInfo);
		
		reader.onMessageNotificationReceived.clear();
		reader.onMessageNotificationReceived.add(QueryTagActivity.this);
		
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
					userData = Util.convertByteArrayToHexString(data
							.getReceivedMessage().getUserData());

					if (!IsValidEpc(epc, true)) {
						return;
					}

					// 找到配件信息
					if (UtilityHelper.CheckEpc(epc) == 0) {
						partsCode = UtilityHelper.GetCodeByEpc(epc);
						if (partsCode.length() > 0) {
							Message dataArrivedMsg = new Message();
							dataArrivedMsg.what = DATA_ARRIVED_PAIRS;
							cardOperationHandler.sendMessage(dataArrivedMsg);
							sp.play(music1, 1, 1, 0, 0, 1);
						}
					}

					// 找到库位信息
					if (UtilityHelper.CheckEpc(epc) == 1) {
						storageLocationStr = UtilityHelper.GetCodeByEpc(epc);
						if (storageLocationStr.length() > 0) {
							Message dataArrivedMsg = new Message();
							dataArrivedMsg.what = DATA_ARRIVED_STORAGE_LOCATION;
							cardOperationHandler.sendMessage(dataArrivedMsg);
							sp.play(music1, 1, 1, 0, 0, 1);
						}
					}

					// 找到站点信息
					if (UtilityHelper.CheckEpc(epc) == 2) {
						stationCode = UtilityHelper.GetCodeByEpc(epc);
						if (stationCode.length() > 0) {
							Message dataArrivedMsg = new Message();
							dataArrivedMsg.what = DATA_ARRIVED_STATION;
							cardOperationHandler.sendMessage(dataArrivedMsg);
							sp.play(music1, 1, 1, 0, 0, 1);
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

			InvengoLog.i(TAG, "INFO.Start/Stop read tag.");
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
		isReading = true;
		txtTagInfo.setText("");
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

	// 解析配件数据
	private StringBuilder parseParts (String cod, String ud) {
		StringBuilder r = new StringBuilder();
		PartsEntity entity = UtilityHelper.GetPairEntityByCode(cod);
		r.append("编码：");
		r.append(entity.PartsCode);
		r.append("\n\n厂家：");
		r.append(entity.FactoryName);
		r.append("\n\n型号：");
		r.append(entity.PartsType);
		r.append("\n\n类别：");
		r.append(entity.BoxType);
		r.append("\n\n名称：");
		r.append(entity.PartsName);
		r.append("\n\n序列号：");
		r.append(entity.SeqNo);

		r.append(qryPartPosition(cod, ud));
		return r;
	}

	// 查询配件位置
	private StringBuilder qryPartPosition (String cod, String ud) {
		StringBuilder r = new StringBuilder();
		if (ud.length() >= 4) {
			byte[] bs = Util.convertHexStringToByteArray(ud);
			r.append("\n\n状态：");
			if (bs[1] == 0x01) {
				r.append("已报废");
			} else {
				HashMap<String, String> m = SqliteHelper.queryOnePart(cod);

				// 状态
				switch (bs[0]) {
					case 0x01:
						r.append("在所（待入库）");
						break;
					case 0x02:
						r.append("已入库");
						break;
					case 0x03:
						r.append("在所（已出库）");
						break;
					case 0x04:
					case 0x06:
					case 0x07:
						r.append("在段");
						break;
					case 0x05:
						r.append("已启用");
						break;
					case 0x08:
						r.append("在所（已送修）");
						break;
					case 0x09:
						r.append("在所（待厂修）");
						break;
					case 0x0A:
						r.append("在所（待入库）");
//						r.append("在所（已修竣）");
						break;
					case 0x0B:
						r.append("在所（待报废）");
						break;
				}

				if (m != null) {
					r.append("\n\n位置：");
					switch (bs[0]) {
						case 0x01:
						case 0x03:
						case 0x08:
						case 0x09:
						case 0x0A:
						case 0x0B:
							r.append("检测所");
							break;
						case 0x02:
							r.append("库位_" + m.get("Code"));
							break;
						case 0x04:
						case 0x06:
						case 0x07:
							TbCodeEntity ce = SqliteHelper.queryDbCodeByType("01", m.get("Code"));
							if (ce != null) {
								r.append("单位_" + ce.dbName);
							}
							break;
						case 0x05:
							StationEntity entityStation = SqliteHelper.queryStationByCode(m.get("Code"));
							if (entityStation != null) {
								r.append("站点_" + entityStation.StationName);
							}
							break;
					}
				}
			}
		}
		return r;
	}

	@SuppressLint("HandlerLeak")
	protected Handler cardOperationHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case START_READ:// 开始读卡
				boolean start = (Boolean) msg.obj;
				if (start) {
					txtStatus.setText(getResources().getString(R.string.reading));
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
				StopRead();
				txtTagInfo.setText(parseParts(partsCode, userData).toString());
				break;
			case DATA_ARRIVED_STORAGE_LOCATION:// 接收库位数据
				StopRead();
				txtTagInfo
						.setText(String.format("库位编码：%s\n%s", storageLocationStr,UtilityHelper.getStorageLocationInfo(storageLocationStr)));
				break;
			case DATA_ARRIVED_STATION:// 接收站点数据
				StopRead();
				StationEntity entityStation = SqliteHelper
						.queryStationByCode(stationCode);
				txtTagInfo.setText(String.format("站点编码：%s\n\n站点名称：%s\n\n厂家：%s",
						stationCode, entityStation.StationName,
						entityStation.FactoryName));
				break;
			case CONNECT:// 读写器连接
				boolean result = (Boolean) msg.obj;
				if (result) {
					txtStatus.setText(getResources().getString(R.string.connecSuccess));
					isConnected = true;
				} else {
					txtStatus.setText(getResources().getString(R.string.connecFail));
					isConnected = false;
				}
				break;
			default:
				break;
			}
		};
	};

}
