package com.invengo.rpms;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rpms.entity.OpType;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.entity.SendRepairEntity;
import com.invengo.rpms.entity.TbCodeEntity;
import com.invengo.rpms.util.Btn001;
import com.invengo.rpms.util.ReaderMessageHelper;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;
import invengo.javaapi.protocol.IRP1.WriteUserData_6C;

public class StopActivity extends BaseActivity {

	LinearLayout layoutRepair;
	LinearLayout FaultType_CheckBoxList;
	TextView txtStatus;
	TextView txtTagInfo;
	EditText edtFaultDes;
	EditText edtRemark;
	Button btnConfig;
	Button btnSav;

	private List<CheckBox> checkBoxList = new ArrayList<CheckBox>();
	private String partsCode = "";
	private String partsEpc="";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stop);
		
		reader.onMessageNotificationReceived.clear();
		reader.onMessageNotificationReceived.add(StopActivity.this);

		txtStatus = (TextView) findViewById(R.id.txtStatus);
		txtTagInfo = (TextView) findViewById(R.id.txtTagInfo);

		edtFaultDes = (EditText) findViewById(R.id.edtFaultDes);
		edtRemark = (EditText) findViewById(R.id.edtRemark);

		btnSav = (Button) findViewById(R.id.btnSav);
		btnSav.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				FaultOP();
			}
		});

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

		layoutRepair = (LinearLayout) findViewById(R.id.layoutRepair);
		FaultType_CheckBoxList = (LinearLayout) findViewById(R.id.FaultType_CheckBoxList);
	}

	private OnClickListener btnConfigClickListener = new OnClickListener() {
		public void onClick(View v) {
			AlertDialog.Builder builder  = new Builder(StopActivity.this, R.style.AppTheme);
			builder.setTitle("温馨提示" ) ;
			builder.setMessage(getResources().getString(R.string.pairsFaultTipInfo)) ;
			builder.setPositiveButton("关闭" ,  null );
			builder.show();
		}
	};

	private void FaultOP() {
		SendRepairEntity se = new SendRepairEntity();
		String faultCodeStr = "";
		for (CheckBox checkBox : checkBoxList) {
			if (checkBox.isChecked()) {
				if (se.FaultCode == null) {
					se.FaultCode = "";
				} else {
					se.FaultCode += ",";
				}
				se.FaultCode += checkBox.getTag().toString();
				faultCodeStr += checkBox.getTag().toString();
			}
		}
		if (faultCodeStr.length() == 0) {
			showToast(String.format("请选择%s", getResources().getString(R.string.FaultType)));
			sp.play(music3, 1, 1, 0, 0, 1);
			return;
		}

		se.FaultDes = edtFaultDes.getText().toString().trim();
		se.Remark = edtRemark.getText().toString().trim();
		String faultDes = se.FaultDes.replace(",","+++");
		String remark = se.Remark.replace(",","+++");

		// 写入标签用户数据
		setRate();
		WriteUserData_6C msg = ReaderMessageHelper.GetWriteUserData_6C(partsEpc, OpType.Stop);
		boolean result = reader.send(msg);

		if (result) {
			// 保存操作记录
			String user = myApp.getUserId();
			String opTime = f.format(new Date());
			String info = faultCodeStr + "," + faultDes + "," + remark + "," + user + "," + opTime;
			List<String> listPartsCodeSucess = new ArrayList<String>();
			listPartsCodeSucess.add(partsCode);
			result = SqliteHelper.SaveOpRecord(listPartsCodeSucess, OpType.Stop, info);

			if (result) {
				List<String> listSql = new ArrayList<String>();
				listSql.add("update TbParts set Status='T',"
						+ "LastOpTime='" + SqliteHelper.f.format(new Date())
						+ "',OpUser='" + user
						+ "',Code=null"
						+ " where PartsCode='" + partsCode + "'");
				listSql.add("insert into TbSendRepair values('', '"		// TODO: 2018/6/12 测试用功能，实际发布后，无需在此处保存故障信息。
						+ partsCode + "', '"
						+ se.FaultCode + "', '"
						+ se.FaultDes + "', '"
						+ se.Remark + "')");
				SqliteHelper.ExceSql(listSql);	// 更新本地数据库信息
				showToast(String.format("%s成功", getResources().getString(R.string.pairsStop)));

				// 清空页面
				txtTagInfo.setText("");
				layoutRepair.setVisibility(View.GONE);
				sp.play(music2, 1, 1, 0, 0, 1);
			} else {
				showToast(String.format("%s失败，请重新操作", getResources().getString(R.string.pairsStop)));
				sp.play(music3, 1, 1, 0, 0, 1);
			}
		} else {
			showToast(String.format("%s失败，请重新操作", getResources().getString(R.string.pairsStop)));
			sp.play(music3, 1, 1, 0, 0, 1);
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

					// 找到配件信息并且验证是否允许提交故障
					if (UtilityHelper.CheckEpc(epc) == 0
							&& UtilityHelper.CheckUserData(userData,
									OpType.Stop)) {
						if (IsValidEpc(epc, true)) {
							partsEpc=epc;
						    partsCode = UtilityHelper.GetCodeByEpc(epc);
							if (partsCode.length() > 0) {
								Message dataArrivedMsg = new Message();
								dataArrivedMsg.what = DATA_ARRIVED_PAIRS;
								cardOperationHandler
										.sendMessage(dataArrivedMsg);
								sp.play(music1, 1, 1, 0, 0, 1);
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

	private void showList () {
		FaultType_CheckBoxList.removeAllViews();
		checkBoxList.clear();
		List<TbCodeEntity> listCode = SqliteHelper.qrySnag(partsCode.substring(5,7));
		for (TbCodeEntity entity : listCode) {
			CheckBox checkBox = new CheckBox(this);
			checkBox.setText(entity.dbName);
			checkBox.setTag(entity.dbCode);
			FaultType_CheckBoxList.addView(checkBox);
			checkBoxList.add(checkBox);
		}
		CheckBox checkBox = new CheckBox(this);
		checkBox.setText("其他");
		checkBox.setTag("00");
		FaultType_CheckBoxList.addView(checkBox);
		checkBoxList.add(checkBox);
		edtFaultDes.setText("");
		edtRemark.setText("");
		layoutRepair.setVisibility(View.VISIBLE);
	}

	@SuppressLint("HandlerLeak")
	private Handler cardOperationHandler = new Handler() {
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
				PartsEntity entity = UtilityHelper.GetPairEntityByCode(partsCode);
				txtTagInfo.setText(String.format(
						"编码：%s\n厂家：%s\n型号：%s\n类别：%s\n名称： %s\n序列号：%s",
						entity.PartsCode, entity.FactoryName, entity.PartsType,
						entity.BoxType, entity.PartsName, entity.SeqNo));
				showList();
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