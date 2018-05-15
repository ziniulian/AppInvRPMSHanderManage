package com.invengo.rpms;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rpms.entity.OpType;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.entity.StorageLocationEntity;
import com.invengo.rpms.util.Btn001;
import com.invengo.rpms.util.ReaderMessageHelper;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.PowerOff;
import invengo.javaapi.protocol.IRP1.RXD_TagData;
import invengo.javaapi.protocol.IRP1.ReadTag;
import invengo.javaapi.protocol.IRP1.ReadTag.ReadMemoryBank;
import invengo.javaapi.protocol.IRP1.WriteUserData_6C;

public class StockInActivity extends BaseActivity {

	TextView txtStatus;
	EditText edtStorageLocation;
	TextView txtRemark;
	TextView txtInfo;
	Button btnConfig;

	private ListView mEpcListView;
	private PartsAdapter mListAdapter;
	private List<Map<String, Object>> listPartsData = new ArrayList<Map<String, Object>>();
	private String storageLocationStr = "";
	private PartsEntity PartsEntityCur;
	private List<String> listPartsCodeSucess = new ArrayList<String>();
	int id = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stockin);

		reader.onMessageNotificationReceived.clear();
		reader.onMessageNotificationReceived.add(StockInActivity.this);

		txtRemark = (TextView) findViewById(R.id.txtRemark);
		// txtRemark.setText("温馨提示：先读取到待入库库位");
		txtStatus = (TextView) findViewById(R.id.txtStatus);
		txtInfo = (TextView) findViewById(R.id.txtInfo);

		btnConfig = (Button) findViewById(R.id.btnConfig);
//		btnConfig.setOnTouchListener(btnConfigTouchListener);
		btnConfig.setOnClickListener(btnConfigClickListener);

		final Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnTouchListener(new Btn001());
		btnBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean result=saveResult();
				if (isReading) {
					showToast("请先停止读取");
				} else {
					finish();
				}
			}
		});

		final Button btnClear = (Button) findViewById(R.id.btnClear);
		btnClear.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN: {
					// 按住事件发生后执行代码的区域
					btnClear.setBackgroundResource(R.color.lightwhite);
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					// 移动事件发生后执行代码的区域
					btnClear.setBackgroundResource(R.color.lightwhite);
					break;
				}
				case MotionEvent.ACTION_UP: {
					// 松开事件发生后执行代码的区域
					btnClear.setBackgroundResource(R.color.yellow);
					break;
				}
				default:

					break;
				}
				return false;
			}
		});
		btnClear.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				edtStorageLocation.setText("");
				storageLocationStr = "";
				txtRemark.setText("");
				txtInfo.setText("已入库数:0");
				listPartsData.clear();
				mListAdapter.notifyDataSetChanged();
			}
		});

		edtStorageLocation = (EditText) findViewById(R.id.edtStorageLocation);
		edtStorageLocation.setCursorVisible(false);
		edtStorageLocation.setFocusable(false);
		edtStorageLocation.setFocusableInTouchMode(false);
		edtStorageLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}

		});

		mEpcListView = (ListView) this.findViewById(R.id.lstPartsStockInView);
		/*
		 * mEpcListView.setOnItemClickListener(new OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView<?> arg0, View arg1, int
		 * arg2, long arg3) {
		 * 
		 * listPartsData.get(arg2); final String partsCodeSelected =
		 * entity.get("partsCode") .toString();
		 * 
		 * for (PartsEntity partsEntiry : listPartsEntity) { if
		 * (partsEntiry.PartsCode.equals(partsCodeSelected)) {
		 * listPartsEntity.remove(partsEntiry); break; } }
		 * 
		 * listPartsData.remove(arg2); mListAdapter.notifyDataSetChanged(); }
		 * 
		 * });
		 */

		// 创建SimpleAdapter适配器将数据绑定到item显示控件上
		mListAdapter = new PartsAdapter(this, listPartsData,
				R.layout.listview_parts_item, new String[] { "sqeNo",
						"partsCode", "partsName" }, new int[] { R.id.sqeNo,
						R.id.partsCode, R.id.partsInfo });

		// 实现列表的显示
		mEpcListView.setAdapter(mListAdapter);
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

			AlertDialog.Builder builder = new Builder(StockInActivity.this,
					R.style.AppTheme);
			builder.setTitle("温馨提示");
			builder.setMessage(getResources().getString(
					R.string.pairsStockInTipInfo));
			builder.setPositiveButton("关闭", null);
			builder.show();
		}
	};

	private boolean StockInOP(PartsEntity partsEntiry) {

		// 判断库位是否能存放
		if (!canStockIn(storageLocationStr, partsEntiry)) {
			sp.play(music1, 2, 2, 0, 0, 1);
			return false;
		}

		// 写入标签用户数据
		WriteUserData_6C msg = ReaderMessageHelper.GetWriteUserData_6C(
				partsEntiry.Epc, OpType.StockIn);

		// listPartsCodeSucess.clear();
		boolean isSucess = false;
		//int count = 0;
		//while (count < writeUserDataCount) {
			SystemClock.sleep(100);
			boolean result = reader.send(msg);
			if (result) {
				listPartsCodeSucess.add(partsEntiry.PartsCode);
				isSucess = true;
				//break;
			}
			//count++;
		//}

		if (isSucess) {
			id++;
			showToast(String.valueOf(id));
		}
		/*
		 * 
		 * if(isSucess) { boolean resultSave = saveResult(); if (!resultSave) {
		 * return false; } }
		 */

		return isSucess;
	}

	private boolean saveResult() {
		if (listPartsCodeSucess.size() > 0) {
			// 保存操作记录
			String user = myApp.getUserId();
			String opTime = f.format(new Date());
			String remark = "".replace(",", "+++");
			String info = storageLocationStr + "," + user + "," + opTime + ","
					+ remark;
			boolean result = SqliteHelper.SaveOpRecord(listPartsCodeSucess,
					OpType.StockIn, info, storageLocationStr, user);
			if (result) {
				listPartsCodeSucess.clear();
				return true;
			} else {
				return false;
			}
		}

		return true;
	}

	// 判断库位是否能存放
	private boolean canStockIn(String storageLocationStr,
			PartsEntity partsEntiry) {

		StorageLocationEntity entity = SqliteHelper
				.queryStorageLocationByCode(storageLocationStr);
		if (entity != null) {
			if (entity.IsEnable.equals("N")) {
				showToast("该库位被禁用");
				return false;
			}

			if (entity.PartsAllow.length() == 10) {
				String t5 = entity.PartsAllow.substring(0, 2);
				String factotyCode = entity.PartsAllow.substring(2, 4);
				String partsSort = entity.PartsAllow.substring(4, 5);
				String partsType = entity.PartsAllow.substring(5, 7);
				String partsName = entity.PartsAllow.substring(7, 10);

				if (!t5.equals("00")) {

					if (!partsEntiry.PartsCode.subSequence(0, 2).equals(t5)) {
						showToast(String.format("%s不允许放此库位",
								partsEntiry.PartsName));
						return false;
					}

				}

				if (!factotyCode.equals("00")) {

					if (!partsEntiry.PartsCode.subSequence(2, 4).equals(
							factotyCode)) {
						showToast(String.format("%s不允许放此库位",
								partsEntiry.PartsName));
						return false;
					}

				}

				if (!partsSort.equals("0")) {

					if (!partsEntiry.PartsCode.subSequence(4, 5).equals(
							partsSort)) {
						showToast(String.format("%s不允许放此库位",
								partsEntiry.PartsName));
						return false;
					}

				}

				if (!partsType.equals("00")) {

					if (!partsEntiry.PartsCode.subSequence(5, 7).equals(
							partsType)) {
						showToast(String.format("%s不允许放此库位",
								partsEntiry.PartsName));
						return false;
					}

				}

				if (!partsName.equals("000")) {

					if (!partsEntiry.PartsCode.subSequence(7, 10).equals(
							partsName)) {
						showToast(String.format("%s不允许放此库位",
								partsEntiry.PartsName));
						return false;
					}
				}
			}

			if (entity.MaxVolume > 0) {
				int pairsNum = SqliteHelper
						.queryPairsNumByStorageLocationCode(storageLocationStr);
				if (pairsNum + 1 > entity.MaxVolume) {
					showToast("库位存放将超出最大容量，请确认");
					return false;
				}
			}
		}

		return true;
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

					if (storageLocationStr.length() > 0) {

						// 找到配件信息并且验证是否允许入库
						if (UtilityHelper.CheckEpc(epc) == 0
								&& UtilityHelper.CheckUserData(userData,
										OpType.StockIn)) {
							if (IsValidEpc(epc, false)) {
								String partsCode = UtilityHelper
										.GetCodeByEpc(epc);
								if (partsCode.length() > 0) {
									boolean isExsit = false;
									for (PartsEntity entity : listPartsEntity) {
										if (entity.PartsCode.equals(partsCode)) {
											isExsit = true;
											break;
										}
									}

									if (!isExsit) {

										PartsEntityCur = UtilityHelper
												.GetPairEntityByCode(partsCode);
										PartsEntityCur.Epc = epc;
										listPartsEntity.add(PartsEntityCur);

										Message dataArrivedMsg = new Message();
										dataArrivedMsg.what = DATA_ARRIVED_PAIRS;
										cardOperationHandler
												.sendMessage(dataArrivedMsg);
									}
								}
							}
						}
					}

					// 找到库位信息
					if (UtilityHelper.CheckEpc(epc) == 1) {
						if (IsValidEpc(epc, true)
								&& storageLocationStr.length() == 0) {
							storageLocationStr = UtilityHelper
									.GetCodeByEpc(epc);
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
			boolean result=saveResult();
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
				StopRead();
				boolean result = StockInOP(PartsEntityCur);
				if (result) {
					Map<String, Object> item = new HashMap<String, Object>();
					item.put("sqeNo", listPartsData.size() + 1);
					item.put("partsCode", PartsEntityCur.PartsCode);
					item.put("partsName", PartsEntityCur.PartsType + "  "
							+ PartsEntityCur.PartsName);
					// Drawable delDr = getResources().getDrawable(
					// R.drawable.delete);
					// item.put("delIcon", delDr);
					listPartsData.add(item);

					txtInfo.setText(String.format("已入库数量:%s",
							listPartsData.size()));
					mListAdapter.notifyDataSetChanged();
					sp.play(music1, 2, 2, 0, 0, 1);
				} else {
					listPartsEntity.remove(PartsEntityCur);
				}
				StartRead();
				break;
			case DATA_ARRIVED_STORAGE_LOCATION:// 接收库位数据
				StopRead();
				edtStorageLocation.setText(storageLocationStr);
				txtRemark.setText("温馨提示："
						+ UtilityHelper
								.getStorageLocationInfo(storageLocationStr));
				txtInfo.setText("已入库数:0");
				sp.play(music1, 1, 1, 0, 0, 1);
				break;
			case CONNECT:// 读写器连接
				boolean result1 = (Boolean) msg.obj;
				if (result1) {
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
