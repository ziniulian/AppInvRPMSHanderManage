package com.invengo.rpms;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
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
import invengo.javaapi.protocol.IRP1.PowerOff;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;
import invengo.javaapi.protocol.IRP1.WriteUserData_6C;

public class RepairActivity extends BaseActivity {

	LinearLayout layoutRepair;
	LinearLayout FaultType_CheckBoxList;
	CheckBox cbxComfirmRepair;
	TextView txtStatus;
	TextView txtTagInfo;
	EditText edtFaultDes;
	EditText edtRemark;
	LinearLayout llayoutCheck;
	RadioGroup radioGroupCheckOpition;
	EditText edtCheckResult;
	EditText edtCheckGroupUser;
	Button btnConfig;

	private List<CheckBox> checkBoxList = new ArrayList<CheckBox>();
	private String partsCode = "";
	private String id="";
	private String repairOpition = "";
	private String partsEpc = "";
	private boolean isGetFault=false;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_repair);

		cbxComfirmRepair = (CheckBox) findViewById(R.id.cbxComfirmRepair);
		txtStatus = (TextView) findViewById(R.id.txtStatus);
		txtTagInfo = (TextView) findViewById(R.id.txtTagInfo);

		edtFaultDes = (EditText) findViewById(R.id.edtFaultDes);
		edtFaultDes.setEnabled(false);
		edtRemark = (EditText) findViewById(R.id.edtRemark);
		
		btnConfig = (Button) findViewById(R.id.btnConfig);
//		btnConfig.setOnTouchListener(btnConfigTouchListener);
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


		List<TbCodeEntity> listCode = SqliteHelper.queryDbCodeByType("04");
		FaultType_CheckBoxList = (LinearLayout) findViewById(R.id.FaultType_CheckBoxList);
		for (TbCodeEntity entity : listCode) {
			CheckBox checkBox = new CheckBox(this);
			checkBox.setText(entity.dbName);
			checkBox.setTag(entity.dbCode);
			checkBox.setEnabled(false);
			FaultType_CheckBoxList.addView(checkBox);
			checkBoxList.add(checkBox);
		}
		CheckBox checkBox = new CheckBox(this);
		checkBox.setText("其他");
		checkBox.setTag("00");
		checkBox.setEnabled(false);
		FaultType_CheckBoxList.addView(checkBox);
		checkBoxList.add(checkBox);

		layoutRepair = (LinearLayout) findViewById(R.id.layoutRepair);
		layoutRepair.setVisibility(View.GONE);

		llayoutCheck = (LinearLayout) findViewById(R.id.llayoutCheck);
		llayoutCheck.setVisibility(View.GONE);
		radioGroupCheckOpition = (RadioGroup) this
				.findViewById(R.id.radioGroupCheckOpition);
		radioGroupCheckOpition
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup arg0, int arg1) {
						// 获取变更后的选中项的ID
						int radioButtonId = arg0.getCheckedRadioButtonId();
						// 根据ID获取RadioButton的实例
						RadioButton rb = (RadioButton) RepairActivity.this
								.findViewById(radioButtonId);

						repairOpition = rb.getText().toString();
					}
				});
		edtCheckResult = (EditText) findViewById(R.id.edtCheckResult);
		edtCheckGroupUser = (EditText) findViewById(R.id.edtCheckGroupUser);
		
		reader.onMessageNotificationReceived.clear();
		reader.onMessageNotificationReceived.add(RepairActivity.this);
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
			
			AlertDialog.Builder builder  = new Builder(RepairActivity.this, R.style.AppTheme);
			 builder.setTitle("温馨提示" ) ;
			 builder.setMessage(getResources().getString(R.string.RepairCheckTipInfo)) ;
			 builder.setPositiveButton("关闭" ,  null );
			 builder.show();
		}
	};



	private void CheckOP() {

		if (partsCode.length() == 0) {
			showToast(String.format("请扫描到%s",
					getResources().getString(R.string.parts)));
			return;
		}
		
		if(!isGetFault)
		{
			showToast("还未获得故障信息");
			return;
		}

		String checkResult = edtCheckResult.getText().toString().trim().replace(",","+++");;
		String checkGroupUser = edtCheckGroupUser.getText().toString().trim();

		int opType = 0;
		if (repairOpition.equals(getResources().getString(
				R.string.CheckOpition_BackFactory))) {
			opType = OpType.Repair_B;
			repairOpition="C";
		}
		if (repairOpition.equals(getResources().getString(
				R.string.CheckOpition_RepairWell))) {
			opType = OpType.Repair_O;
			repairOpition="Z";
		}
		if (repairOpition.equals(getResources().getString(
				R.string.CheckOpition_RepairScrap))) {
			opType = OpType.Repair_S;
			repairOpition="B";
		}
		if (opType == 0) {
			showToast(String.format("请选择%s",
					getResources().getString(R.string.CheckOpition)));
			return;
		}

		// 写入标签用户数据
		WriteUserData_6C msg = ReaderMessageHelper.GetWriteUserData_6C(
				partsEpc, opType);

		boolean result = false;
		//int count = 0;
		//while (count < writeUserDataCount) {
		    result = reader.send(msg);
			//if (result) {
			//	break;
			//}
			//count++;
		//}
		
		if (result) {

			// 保存操作记录
			String user = myApp.getUserId();
			String checkTime = f.format(new Date());
			String info =id + ","+ repairOpition + "," + checkResult + ","
					+ checkGroupUser + "," + user + "," + checkTime;
			List<String> listPartsCodeSucess = new ArrayList<String>();
			listPartsCodeSucess.add(partsCode);

			result = SqliteHelper.SaveOpRecord(listPartsCodeSucess, OpType.Repair,
					info);

			if (result) {
				showToast(String.format("%s成功",
						getResources().getString(R.string.repaircheck)));
				
				//删除故障记录信息
				SqliteHelper.DeteleSendRepair(listPartsCodeSucess);
				
			} else {
				showToast(String.format("%s失败，请重新操作",
						getResources().getString(R.string.repaircheck)));
			}

		} else {
			showToast(String.format("%s失败，请重新操作",
					getResources().getString(R.string.repaircheck)));
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

					// 找到配件信息并且验证是否允许维修检测
					if (UtilityHelper.CheckEpc(epc) == 0
							&& UtilityHelper.CheckUserData(userData,
									OpType.Repair)) {
						if (IsValidEpc(epc, true)) {
							partsEpc = epc;
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

			if (cbxComfirmRepair.isChecked()) {
				CheckOP();
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
						"编码：%s\n厂家：%s\n型号：%s\n类别：%s\n名称： %s\n序列号：%s",
						entity.PartsCode, entity.FactoryName, entity.PartsType,
						entity.BoxType, entity.PartsName, entity.SeqNo));
				layoutRepair.setVisibility(View.VISIBLE);
				llayoutCheck.setVisibility(View.VISIBLE);

				SendRepairEntity entitySendRepair = SqliteHelper
						.querySendRepaiByCode(partsCode);
				if (entitySendRepair != null) {
					id=entitySendRepair.ID;
					String faultCodes = entitySendRepair.FaultCode;
					while (faultCodes.length() > 1) {
						String faultCode=faultCodes.substring(0,2);
						for (CheckBox checkBox : checkBoxList) {
							checkBox.setEnabled(false);
							if (checkBox.getTag().toString().equals(faultCode)) {
								checkBox.setChecked(true);
								break;
							}
						}
						faultCodes=faultCodes.substring(2,faultCodes.length());
					}
					
					edtFaultDes.setText(entitySendRepair.FaultDes);
					edtFaultDes.setEnabled(false);
					edtRemark.setText(entitySendRepair.Remark);
					edtRemark.setEnabled(false);
					isGetFault=true;
				}
				else
				{
					showToast("未查询到故障信息，请先下载配件故障信息");
					isGetFault=false;
				}
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