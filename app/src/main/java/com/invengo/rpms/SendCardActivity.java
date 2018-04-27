package com.invengo.rpms;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.entity.StationEntity;
import com.invengo.rpms.entity.TbCodeEntity;
import com.invengo.rpms.util.Btn001;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;
import com.invengo.rpms.util.WrtRa;

import java.util.ArrayList;
import java.util.List;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.PowerOff;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;

public class SendCardActivity extends BaseActivity {

	TextView txtStatus;
	Spinner sprPartsT5;
	Spinner sprPartsFactory;
	Spinner sprPartsSort;
	Spinner sprPartsHost;
	Spinner sprPartsName;
	Spinner sprPartsStatus;
	Spinner sprDw;
	Button btnConfig;
	TextView txtSqe;
	TextView txtInfo;
	TextView txtRemark;
	TextView txtRemarkV;

	private String partsCode;
	private String code;
	private String typ;
	private byte typCod;
	private boolean lockRd = false;
	private String tid;

	private static final int GET_STATION = 101;		// 先读取站点标签
	private static final int GET_STORAGE_LOCATION = 102;	// 先读取库位标签
	private static final int GET_PAIRS = 103;	// 读取配件标签
	private static final int READY_WRITE = 104;	// 准备写入

	private List<String> listPartsName = new ArrayList<String>();
	private List<String> listPartsSort = new ArrayList<String>();
	private ArrayAdapter<String> adapterPartsName;
	private ArrayAdapter<String> adapterPartsSort;
	private ArrayAdapter adapterDw;
	private String factoryCodeSelected;
	private String sortCodeSelected;
	private String hostCodeSelected;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sendcard);

		reader.onMessageNotificationReceived.clear();
		reader.onMessageNotificationReceived.add(SendCardActivity.this);

		txtStatus = (TextView) findViewById(R.id.txtStatus);
		txtInfo = (TextView) findViewById(R.id.txtInfo);
		txtRemark = (TextView) findViewById(R.id.txtRemark);
		txtRemarkV = (TextView) findViewById(R.id.txtRemarkV);

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

		txtSqe = (TextView) findViewById(R.id.txtSqe);

		sprPartsT5 = (Spinner) findViewById(R.id.sprPartsT5);
		List<String> listPartsT5 = new ArrayList<String>();
		List<TbCodeEntity> listCodeT5 = SqliteHelper.queryDbCodeByType("05");
		for (TbCodeEntity entity : listCodeT5) {
			listPartsT5.add(entity.dbCode);
		}

		ArrayAdapter<String> adapterPartsT5 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listPartsT5);
		adapterPartsT5.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsT5.setAdapter(adapterPartsT5);

		sprPartsFactory = (Spinner) findViewById(R.id.sprPartsFactory);
		List<String> listPartsFactory = new ArrayList<String>();
		List<TbCodeEntity> listCodeFactory = SqliteHelper.queryDbCodeByType("06");
		for (TbCodeEntity entity : listCodeFactory) {
			listPartsFactory.add(entity.dbCode + " " + entity.dbName);
		}
		if (listPartsFactory.size() > 0) {
			factoryCodeSelected = listPartsFactory.get(0).toString().split(" ")[0];
		}

		final ArrayAdapter<String> adapterPartsFactory = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listPartsFactory);
		adapterPartsFactory.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsFactory.setAdapter(adapterPartsFactory);
		sprPartsFactory.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				String factorySelected = adapterPartsFactory.getItem(position).toString();
				String factoryCodeSelected = factorySelected.split(" ")[0];

				listPartsSort.clear();
				List<TbCodeEntity> listCodeSort = SqliteHelper
						.queryDbCodeByType("07");
				for (TbCodeEntity entitySort : listCodeSort) {
					if (entitySort.dbTypeBeyond.equals("06")
							&& entitySort.dbCodeBeyond
									.equals(factoryCodeSelected)) {
						listPartsSort.add(entitySort.dbCode + " "
								+ entitySort.dbName);
					}
				}
				adapterPartsSort.notifyDataSetChanged();
				if (listPartsSort.size() > 0) {
					sortCodeSelected = listPartsSort.get(0).toString()
							.split(" ")[0];
				}

				listPartsName.clear();
				List<TbCodeEntity> listCodeName = SqliteHelper
						.queryDbCodeByType("09");
				for (TbCodeEntity entityName : listCodeName) {

					if (entityName.dbCodeBeyond.indexOf(hostCodeSelected) == 0) {
						String s = sortCodeSelected;
						int d = entityName.dbCodeBeyond.indexOf(s);
						if (d > 0) {
							listPartsName.add(entityName.dbCode + " "
									+ entityName.dbName);
						}
					}
				}
				adapterPartsName.notifyDataSetChanged();
			}

			// 没有选中时的处理
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		sprPartsSort = (Spinner) findViewById(R.id.sprPartsSort);
		listPartsSort.clear();
		List<TbCodeEntity> listCodeSort = SqliteHelper.queryDbCodeByType("07");
		for (TbCodeEntity entity : listCodeSort) {
			if (entity.dbTypeBeyond.equals("06")
					&& entity.dbCodeBeyond.equals(factoryCodeSelected)) {
				listPartsSort.add(entity.dbCode + " " + entity.dbName);
			}
		}
		if (listPartsSort.size() > 0) {
			sortCodeSelected = listPartsSort.get(0).toString().split(" ")[0];
		}

		adapterPartsSort = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listPartsSort);
		adapterPartsSort.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsSort.setAdapter(adapterPartsSort);
		sprPartsSort.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				String sortSelected = adapterPartsSort.getItem(position).toString();
				sortCodeSelected = sortSelected.split(" ")[0];

				listPartsName.clear();
				List<TbCodeEntity> listCodeName = SqliteHelper
						.queryDbCodeByType("09");
				for (TbCodeEntity entityName : listCodeName) {

					if (entityName.dbCodeBeyond.indexOf(hostCodeSelected) == 0) {
						String s = sortCodeSelected;
						int d = entityName.dbCodeBeyond.indexOf(s);
						if (d > 0) {
							listPartsName.add(entityName.dbCode + " "
									+ entityName.dbName);
						}
					}
				}
				adapterPartsName.notifyDataSetChanged();
			}

			// 没有选中时的处理
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		sprPartsHost = (Spinner) findViewById(R.id.sprPartsHost);
		List<String> listPartsHost = new ArrayList<String>();
		List<TbCodeEntity> listCodeHost = SqliteHelper.queryDbCodeByType("08");
		for (TbCodeEntity entity : listCodeHost) {
			listPartsHost.add(entity.dbCode + " " + entity.dbName);
		}
		if (listCodeHost.size() > 0) {
			hostCodeSelected = listCodeHost.get(0).toString().split(" ")[0];
		}

		final ArrayAdapter<String> adapterPartsHost = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listPartsHost);
		adapterPartsHost.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsHost.setAdapter(adapterPartsHost);
		sprPartsHost.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				String hostSelected = adapterPartsHost.getItem(position).toString();
				hostCodeSelected = hostSelected.split(" ")[0];

				listPartsName.clear();
				List<TbCodeEntity> listCodeName = SqliteHelper
						.queryDbCodeByType("09");
				for (TbCodeEntity entityName : listCodeName) {

					if (entityName.dbCodeBeyond.indexOf(hostCodeSelected) == 0) {
						String s = sortCodeSelected;
						int d = entityName.dbCodeBeyond.indexOf(s);
						if (d > 0) {
							listPartsName.add(entityName.dbCode + " "
									+ entityName.dbName);
						}
					}
				}
				adapterPartsName.notifyDataSetChanged();
			}

			// 没有选中时的处理
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		sprPartsName = (Spinner) findViewById(R.id.sprPartsName);
		String hostSelected = adapterPartsHost.getItem(0).toString();
		String hostCodeSelected = hostSelected.split(" ")[0];
		listPartsName.clear();
		List<TbCodeEntity> listCodeName = SqliteHelper.queryDbCodeByType("09");
		for (TbCodeEntity entityName : listCodeName) {
			if (entityName.dbCodeBeyond.indexOf(hostCodeSelected) == 0) {
				String s = sortCodeSelected;
				int d = entityName.dbCodeBeyond.indexOf(s);
				if (d > 0) {
					listPartsName.add(entityName.dbCode + " "
							+ entityName.dbName);
				}
			}

		}
		adapterPartsName = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listPartsName);
		adapterPartsName.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsName.setAdapter(adapterPartsName);

		// 单位选项改为下拉列表，从数据库中读取所有单位选项 单位类型编号：01
		sprDw = (Spinner) findViewById(R.id.sprDw);
		List<TbCodeEntity> listDw = SqliteHelper.queryDbCodeByType("01");
		List<String> listDwStr = new ArrayList<String>();
		for (TbCodeEntity e : listDw) {
			listDwStr.add(e.dbCode + " " + e.dbName);
		}
		adapterDw = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listDwStr);
		adapterDw.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprDw.setAdapter(adapterDw);
		sprDw.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				code = adapterDw.getItem(i).toString().split(" ")[0];
			}
			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		sprPartsStatus = (Spinner) findViewById(R.id.sprPartsStatus);
		List<String> listStatus = new ArrayList<String>();
		listStatus.add("在库");
		listStatus.add("在段");
		listStatus.add("启用");
		listStatus.add("在所 - 待厂修");
		listStatus.add("在所 - 待入库");
		listStatus.add("在所 - 待报废");
		listStatus.add("在所");
		ArrayAdapter<String> adapterStatus = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listStatus);
		adapterStatus.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsStatus.setAdapter(adapterStatus);
		sprPartsStatus.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:
						txtRemark.setText("库位：");
						sprDw.setVisibility(View.GONE);
						txtRemarkV.setVisibility(View.VISIBLE);
						txtRemarkV.setText("");
						cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_STORAGE_LOCATION));
						break;
					case 1:
						txtRemark.setText("单位：");
						txtRemarkV.setVisibility(View.GONE);
						sprDw.setVisibility(View.VISIBLE);
						cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_PAIRS));
						break;
					case 2:
						txtRemark.setText("站点：");
						sprDw.setVisibility(View.GONE);
						txtRemarkV.setVisibility(View.VISIBLE);
						txtRemarkV.setText("");
						cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_STATION));
						break;
					case 3:
					case 4:
					case 5:
					case 6:
						txtRemark.setText("");
						txtRemarkV.setVisibility(View.GONE);
						sprDw.setVisibility(View.GONE);
						cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_PAIRS));
						break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
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

			AlertDialog.Builder builder = new Builder(SendCardActivity.this,
					R.style.AppTheme);
			builder.setTitle("温馨提示");
			builder.setMessage(getResources().getString(
					R.string.pairsStockOutTipInfo));
			builder.setPositiveButton("关闭", null);
			builder.show();
		}
	};

	private void writeCard(boolean isNew) {
		txtStatus.setText("信息写入中，请稍候 ...");
		StringBuilder sb = new StringBuilder();

		// 组织epc头部
		Object o;
		o = sprPartsT5.getSelectedItem();
		if (o == null) {
			showToast("请选择编码");
			return;
		} else {
			sb.append(o.toString().substring(0, 2));
		}

		o = sprPartsFactory.getSelectedItem();
		if (o == null) {
			showToast("请选择厂家");
			return;
		} else {
			sb.append(o.toString().substring(0, 2));
		}

		o = sprPartsSort.getSelectedItem();
		if (o == null) {
			showToast("请选择产品型号");
			return;
		} else {
			sb.append(o.toString().substring(0, 1));
		}

		o = sprPartsHost.getSelectedItem();
		if (o == null) {
			showToast("请选择部件类别");
			return;
		} else {
			sb.append(o.toString().substring(0, 2));
		}

		o = sprPartsName.getSelectedItem();
		if (o == null) {
			showToast("请选择部件名称");
			return;
		} else {
			sb.append(o.toString().substring(0, 3));
		}
//Log.i("---", sb.toString());

		// 生成序列号，界面显示序列号
		int n = SqliteHelper.queryPartsNumByType(sb.toString());
		String sn = UtilityHelper.getSnStr(n);
		txtSqe.setText(sn);
		sb.append(sn);
		partsCode = sb.toString();

		// 写标签
		WrtRa r = new WrtRa(
				reader,
				Util.convertHexStringToByteArray(tid),
				UtilityHelper.convertEpcP(partsCode),
				new byte[] {typCod, 0},
				isNew,
				cardOperationHandler
		);
		new Thread(r).start();
	}

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
					tid = Util.convertByteArrayToHexString(data
							.getReceivedMessage().getTID());
//Log.i("----", epc);

					int p = (int)sprPartsStatus.getSelectedItemId();
					switch (p) {
						case 0:	// 库位
							typ = "W";
							typCod = 0x02;
							switch (UtilityHelper.CheckEpc(epc)) {
								case 1:
									cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_PAIRS, 1, 0, epc));
									break;
								case 0:
									if (txtRemarkV.getText().length() == 0) {
										cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_STORAGE_LOCATION));
									} else {
										cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(READY_WRITE, 1, 1, new String[] {tid, epc, userData}));
									}
									break;
								case -1:
									if (txtRemarkV.getText().length() == 0) {
										cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_STORAGE_LOCATION));
									} else {
										cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(READY_WRITE, 1, 0, new String[] {tid}));
									}
									break;
								default:
									if (txtRemarkV.getText().length() == 0) {
										cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_STORAGE_LOCATION));
									} else {
										cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_PAIRS));
									}
									break;
							}
							break;
						case 2:	// 站点
							typ = "U";
							typCod = 0x05;
							switch (UtilityHelper.CheckEpc(epc)) {
								case 2:
									cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_PAIRS, 2, 0, epc));
									break;
								case 0:
									if (txtRemarkV.getText().length() == 0) {
										cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_STATION));
									} else {
										cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(READY_WRITE, 2, 1, new String[] {tid, epc, userData}));
									}
									break;
								case -1:
									if (txtRemarkV.getText().length() == 0) {
										cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_STATION));
									} else {
										cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(READY_WRITE, 2, 0, new String[] {tid}));
									}
									break;
								default:
									if (txtRemarkV.getText().length() == 0) {
										cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_STATION));
									} else {
										cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_PAIRS));
									}
									break;
							}
							break;
						case 1:	// 单位
							typ = "D";
							typCod = 0x07;
							switch (UtilityHelper.CheckEpc(epc)) {
								case 0:
									cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(READY_WRITE, 4, 1, new String[] {tid, epc, userData}));
									break;
								case -1:
									cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(READY_WRITE, 4, 0, new String[] {tid}));
									break;
								default:
									cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_PAIRS));
									break;
							}
							break;
						case 3:	// 在所，在所的状态有很多种： 9 - A
						case 4:
						case 5:
						case 6:
							typ = "S";
							typCod = (byte)(9 + (p - 3));
							switch (UtilityHelper.CheckEpc(epc)) {
								case 0:
									cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(READY_WRITE, 3, 1, new String[] {tid, epc, userData}));
									break;
								case -1:
									cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(READY_WRITE, 3, 0, new String[] {tid}));
									break;
								default:
									cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(GET_PAIRS));
									break;
							}
							break;
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
			if (isReading) {
				StopRead();
			} else {
				StartRead();
			}

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void StartRead() {
		if (!lockRd) {
			if (sprPartsT5.getSelectedItem() == null) {
				showToast("请选择编码");
				return;
			}
			if (sprPartsFactory.getSelectedItem() == null) {
				showToast("请选择厂家");
				return;
			}
			if (sprPartsSort.getSelectedItem() == null) {
				showToast("请选择产品型号");
				return;
			}
			if (sprPartsHost.getSelectedItem() == null) {
				showToast("请选择部件类别");
				return;
			}
			if (sprPartsName.getSelectedItem() == null) {
				showToast("请选择部件名称");
				return;
			}

			isReading = true;
			listEPCEntity.clear();
			txtSqe.setText("");
			ReadTag readTag = new ReadTag(ReadMemoryBank.EPC_TID_UserData_6C);
			boolean result = reader.send(readTag);

			Message readMessage = new Message();
			readMessage.what = START_READ;
			readMessage.obj = result;
			cardOperationHandler.sendMessage(readMessage);
		}
	}

	private void StopRead() {
		if (isReading) {
			isReading = false;
			boolean result = reader.send(new PowerOff());
			Message powerOffMsg = new Message();
			powerOffMsg.what = STOP_READ;
			powerOffMsg.obj = result;
			cardOperationHandler.sendMessage(powerOffMsg);
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler cardOperationHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String s;
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
					if (txtStatus.getText().equals(getResources()
							.getString(R.string.reading))) {
						txtStatus.setText(getResources().getString(R.string.stop));
					}
					isReading = false;
				} else {
					showToast(getResources().getString(R.string.stopFail));
				}
				break;
			case GET_STATION:	// 先读取站点标签
				StopRead();
				txtStatus.setText(getResources().getString(R.string.memo_GET_STATION));
				break;
			case GET_STORAGE_LOCATION:	// 先读取库位标签
				StopRead();
				txtStatus.setText(getResources().getString(R.string.memo_GET_STORAGE_LOCATION));
				break;
			case GET_PAIRS:	// 读取配件标签
				StopRead();
				txtStatus.setText(getResources().getString(R.string.memo_GET_PAIRS));
				switch (msg.arg1) {
					case 1:	// 库位
						s = (String) msg.obj;
						s = UtilityHelper.GetCodeByEpc(s);
						code = s;
						s = UtilityHelper.getStorageLocationInfo(s);
						if (s.length() == 0) {
							s = code;
						}
						txtRemarkV.setText(s);
						break;
					case 2:	// 站点
						s = (String) msg.obj;
						s = UtilityHelper.GetCodeByEpc(s);
						code = s;
						StationEntity entity = SqliteHelper.queryStationByCode(s);
						if (entity != null) {
							s = entity.StationName;
						}
						txtRemarkV.setText(s);
						break;
				}
				break;
			case READY_WRITE:	// 准备写入
				lockRd = true;
				StopRead();
				String[] sa = (String[]) msg.obj;
				tid = sa[0];
				if (msg.arg2 == 1) {
//Log.i("epc", sa[1]);
					s = UtilityHelper.GetCodeByEpc(sa[1]);
					PartsEntity entity = UtilityHelper.GetPairEntityByCode(s);
					String info = String.format(
							"编码：%s\n厂家：%s\n型号：%s\n类别：%s\n名称： %s\n序列号：%s\n状态：%s",
							entity.PartsCode,
							entity.FactoryName,
							entity.PartsType,
							entity.BoxType,
							entity.PartsName,
							entity.SeqNo,
							UtilityHelper.GetPairStatus(sa[2])
					);

					AlertDialog.Builder builder = new Builder(SendCardActivity.this, R.style.AppTheme);
					builder.setTitle("温馨提示");
					builder.setMessage("该标签已经写入配件信息，确定重新该标签吗?配件信息\n\n" + info);
					partsCode = entity.PartsCode;
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SqliteHelper.delOnePart(partsCode);
							writeCard(false);
						}
					});
					builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							lockRd = false;
							txtStatus.setText(getResources().getString(R.string.memo_GET_PAIRS));
						}
					});
					builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialogInterface) {
							lockRd = false;
							txtStatus.setText(getResources().getString(R.string.memo_GET_PAIRS));
						}
					});
					builder.show();
				} else {
					writeCard(true);
				}
				break;
			case WrtRa.WRT_PWD_ERR:
//				showToast("密码修改失败");
			case WrtRa.WRT_LOCK_ERR:
//				showToast("密码锁定失败");
			case WrtRa.WRT_UD_ERR:
//				showToast("用户区写入失败");
			case WrtRa.WRT_OK:
				// 将创建的信息写入数据库
				SqliteHelper.savOnePart(typ, partsCode, code);

				lockRd = false;
				txtStatus.setText(getResources().getString(R.string.memo_WRT_OK));
				break;
			case WrtRa.WRT_EPC_ERR:
				txtSqe.setText("");

				// 释放锁
				lockRd = false;
				txtStatus.setText(getResources().getString(R.string.memo_WRT_ERR));
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
		}
	};
}
