package com.invengo.rpms;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rpms.bean.CheckDetailEntity;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.util.Btn001;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

import java.util.ArrayList;
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

public class CheckDetailActivity extends BaseActivity {

	TextView txtStatus;
	TextView txtInfo;
	CheckBox cbxComfirmCheck;
	Button btnConfig;

	private ListView mEpcListView;
	private PartsAdapter mListAdapter;
	private List<Map<String, Object>> listPartsData = new ArrayList<Map<String, Object>>();
	private String checkCode = "";
	private int countCheckNo = 0;
	private int countChecked = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chekc_detail);

		// 新页面接收数据
		Bundle bundle = this.getIntent().getExtras();
		String isFinish = bundle.getString("isFinish");
		checkCode = bundle.getString("checkCode");
		
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


		cbxComfirmCheck = (CheckBox) findViewById(R.id.cbxComfirmCheck);
		if (isFinish.equals("已完成")) {
			cbxComfirmCheck.setEnabled(false);
		}
		txtStatus = (TextView) findViewById(R.id.txtStatus);
		txtInfo = (TextView) findViewById(R.id.txtInfo);

		List<CheckDetailEntity> listCheckEntity = SqliteHelper
				.queryCheckDetailByCheckCode(checkCode);
		int no = 1;
		for (CheckDetailEntity checkEntity : listCheckEntity) {

			String isCheckStr = "未盘";
			if (checkEntity.IsFind.equals("Y")) {
				countChecked++;
				isCheckStr = "已盘";
			} else {
				countCheckNo++;
			}

			Map<String, Object> item = new HashMap<String, Object>();
			item.put("sqeNo", no);
			item.put("partsCode", checkEntity.PartsCode);
			item.put("partsInfo", String.format("库位：%s                        %s",
					checkEntity.StorageLocation, isCheckStr));

			listPartsData.add(item);
			no++;
		}

		txtInfo.setText(String.format("待盘数：%s  已盘数:%s", countCheckNo,
				countChecked));

		mEpcListView = (ListView) this.findViewById(R.id.lstPartsCheckView);
		// 创建SimpleAdapter适配器将数据绑定到item显示控件上
		mListAdapter = new PartsAdapter(this, listPartsData,
				R.layout.listview_parts_item, new String[] { "sqeNo",
						"partsCode", "partsInfo" }, new int[] { R.id.sqeNo,
						R.id.partsCode, R.id.partsInfo });
		// 实现列表的显示
		mEpcListView.setAdapter(mListAdapter);
		mListAdapter.notifyDataSetChanged();
		
		reader.onMessageNotificationReceived.clear();
		reader.onMessageNotificationReceived.add(CheckDetailActivity.this);
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
			
			AlertDialog.Builder builder  = new Builder(CheckDetailActivity.this, R.style.AppTheme);
			 builder.setTitle("温馨提示" ) ;
			 builder.setMessage(getResources().getString(R.string.pairsCheckTipInfo)) ;
			 builder.setPositiveButton("关闭" ,  null );
			 builder.show();
		}
	};


	private void CheckConfirmOP() {

		boolean result = SqliteHelper.SaveCheckRecord(checkCode);
		if (result) {
			showToast(String.format("结束%s成功",
					getResources().getString(R.string.check)));
		} else {

			showToast(String.format("结束%s失败，请重新操作",
					getResources().getString(R.string.check)));
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

					String partsCode = UtilityHelper.GetCodeByEpc(epc);
					if (partsCode.length() > 0) {
						for (Map<String, Object> entity : listPartsData) {
							String partsCodeSelected = entity.get("partsCode")
									.toString();
							if (partsCodeSelected.equals(partsCode)) {
								listPartsData.remove(entity);

								// 保存操作记录
								String user = myApp.getUserId();
								boolean result = SqliteHelper
										.SaveCheckDetaiRecord(checkCode,
												partsCode, user);
								if (result) {
									countCheckNo--;
									countChecked++;
									Message dataArrivedMsg = new Message();
									dataArrivedMsg.what = DATA_ARRIVED_PAIRS;
									cardOperationHandler
											.sendMessage(dataArrivedMsg);
									break;
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

			if (!cbxComfirmCheck.isChecked()) {
				InvengoLog.i(TAG, "INFO.Start/Stop read tag.");
				if (isReading == false) {
					if (countCheckNo == 0) {
						showToast("已经盘完");
					} else {
						StartRead();
					}
				} else if (isReading == true) {
					StopRead();
				}
			} else {
				CheckConfirmOP();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void StartRead() {
		isReading = true;
		ReadTag readTag = new ReadTag(ReadMemoryBank.EPC_6C);
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
				txtInfo.setText(String.format("待盘数：%s   已盘数:%s", countCheckNo,
						countChecked));
				sp.play(music1, 1, 1, 0, 0, 1);
				mListAdapter.notifyDataSetChanged();
				//if (countCheckNo == 0) {
				//	StopRead();
				//	CheckConfirmOP();
				//}
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

				}

			});

			return super.getView(position, convertView, parent);
		}
	}
}
