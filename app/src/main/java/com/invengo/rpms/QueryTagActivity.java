package com.invengo.rpms;

import java.util.List;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.PowerOff;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import com.invengo.rpms.R;
import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.entity.PartsStorageLocationEntity;
import com.invengo.rpms.entity.StationEntity;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

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
				PartsEntity entity = UtilityHelper
						.GetPairEntityByCode(partsCode);
				
				txtTagInfo.setText(String.format(
						"编码：%s\n\n厂家：%s\n\n型号：%s\n\n类别：%s\n\n名称： %s\n\n序列号：%s\n\n状态：%s",
						entity.PartsCode, entity.FactoryName, entity.PartsType,
						entity.BoxType, entity.PartsName, entity.SeqNo,UtilityHelper.GetPairStatus(userData)));
				if(UtilityHelper.GetPairStatus(userData).equals("已入库"))
				{
					String key=partsCode;
					List<PartsStorageLocationEntity> listInfo = SqliteHelper.queryPartsStorageLocation(key, 1);
					if(listInfo.size()>0)
					{
						txtTagInfo.setText(txtTagInfo.getText()+"\n\n所在库位："+listInfo.get(0).StorageLocationCode);
					}
				}
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
