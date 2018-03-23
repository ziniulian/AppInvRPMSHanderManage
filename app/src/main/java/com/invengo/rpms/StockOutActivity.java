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
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.invengo.rpms.R;
import com.invengo.lib.diagnostics.InvengoLog;
import com.invengo.rpms.StockInActivity.PartsAdapter;
import com.invengo.rpms.entity.OpType;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.entity.PartsStorageLocationEntity;
import com.invengo.rpms.entity.TbCodeEntity;
import com.invengo.rpms.entity.UserEntity;
import com.invengo.rpms.util.ReaderMessageHelper;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

public class StockOutActivity extends BaseActivity {

	TextView txtStatus;
	Spinner sprPartsT5;
	Spinner sprPartsFactory;
	Spinner sprPartsSort;
	Spinner sprPartsHost;
	Spinner sprPartsName;
	EditText edtNum;
	Button btnQuery;
	Button btnConfig;
	TextView txtInfo;

	private ListView mEpcListView;
	private PartsAdapter mListAdapter;
	private List<Map<String, Object>> listPartsData = new ArrayList<Map<String, Object>>();
	private PartsEntity PartsEntityCur;
	private List<PartsStorageLocationEntity> listInfoAll=new ArrayList<PartsStorageLocationEntity>();
	private List<String> listPartsName = new ArrayList<String>();
	private List<String> listPartsSort = new ArrayList<String>();
	private ArrayAdapter<String> adapterPartsName;
	private ArrayAdapter<String> adapterPartsSort;
	private String factoryCodeSelected;
	private String sortCodeSelected;
	private String hostCodeSelected;
	private int stockOutCount = 0;
	private List<String> listPartsCodeSucess = new ArrayList<String>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stockout);
		
		reader.onMessageNotificationReceived.clear();
		reader.onMessageNotificationReceived.add(StockOutActivity.this);

		txtStatus = (TextView) findViewById(R.id.txtStatus);
		txtInfo = (TextView) findViewById(R.id.txtInfo);

		btnQuery = (Button) findViewById(R.id.btnQuery);
		btnQuery.setOnTouchListener(btnQueryTouchListener);
		btnQuery.setOnClickListener(btnQueryClickListener);

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
				boolean result=saveResult();
				if (isReading) {
					showToast("请先停止读取");
				} else {
					finish();
				}
			}
		});


		edtNum = (EditText) findViewById(R.id.edtNum);

		sprPartsT5 = (Spinner) findViewById(R.id.sprPartsT5);
		List<String> listPartsT5 = new ArrayList<String>();
		List<TbCodeEntity> listCodeT5 = SqliteHelper.queryDbCodeByType("05");
		for (TbCodeEntity entity : listCodeT5) {
			listPartsT5.add(entity.dbCode);
		}

		ArrayAdapter<String> adapterPartsT5 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, listPartsT5);
		adapterPartsT5
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsT5.setAdapter(adapterPartsT5);

		sprPartsFactory = (Spinner) findViewById(R.id.sprPartsFactory);
		List<String> listPartsFactory = new ArrayList<String>();
		List<TbCodeEntity> listCodeFactory = SqliteHelper
				.queryDbCodeByType("06");
		for (TbCodeEntity entity : listCodeFactory) {
			listPartsFactory.add(entity.dbCode + " " + entity.dbName);
		}
		if (listPartsFactory.size() > 0) {
			factoryCodeSelected = listPartsFactory.get(0).toString().split(" ")[0];
		}

		final ArrayAdapter<String> adapterPartsFactory = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, listPartsFactory);
		adapterPartsFactory
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsFactory.setAdapter(adapterPartsFactory);
		sprPartsFactory.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				String factorySelected = adapterPartsFactory.getItem(position)
						.toString();
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

		adapterPartsSort = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, listPartsSort);
		adapterPartsSort
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsSort.setAdapter(adapterPartsSort);
		sprPartsSort.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				String sortSelected = adapterPartsSort.getItem(position)
						.toString();
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

		final ArrayAdapter<String> adapterPartsHost = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, listPartsHost);
		adapterPartsHost
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsHost.setAdapter(adapterPartsHost);
		sprPartsHost.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				String hostSelected = adapterPartsHost.getItem(position)
						.toString();
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
		adapterPartsName = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, listPartsName);
		adapterPartsName
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsName.setAdapter(adapterPartsName);

		mEpcListView = (ListView) this.findViewById(R.id.lstPartsStockOutView);
		// 创建SimpleAdapter适配器将数据绑定到item显示控件上
		mListAdapter = new PartsAdapter(this, listPartsData,
				R.layout.listview_parts_item, new String[] { "sqeNo",
						"partsCode", "partsInfo", "delIcon" }, new int[] {
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

			AlertDialog.Builder builder = new Builder(StockOutActivity.this,
					R.style.AppTheme);
			builder.setTitle("温馨提示");
			builder.setMessage(getResources().getString(
					R.string.pairsStockOutTipInfo));
			builder.setPositiveButton("关闭", null);
			builder.show();
		}
	};

	private OnTouchListener btnQueryTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnQuery.setBackgroundResource(R.color.lightwhite);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnQuery.setBackgroundResource(R.color.lightwhite);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnQuery.setBackgroundResource(R.color.yellow);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnQueryClickListener = new OnClickListener() {
		public void onClick(View v) {

			String numStr = edtNum.getText().toString().trim();
			if (numStr.length() == 0) {
				showToast("请输入数量");
				return;
			}
			int num = 0;
			try {
				num = Integer.parseInt(numStr);
			} catch (Exception e) {
				e.printStackTrace();
				showToast("输入数量非法");
			}

			String partsT5Str = "";
			if (sprPartsT5.getCount() > 0) {
				partsT5Str = sprPartsT5.getSelectedItem().toString();
			}

			String partsFactoryStr = "";
			if (sprPartsFactory.getCount() > 0) {
				partsFactoryStr = sprPartsFactory.getSelectedItem().toString()
						.split(" ")[0];
			}

			String partsSortStr = "";
			if (sprPartsSort.getCount() > 0) {
				partsSortStr = sprPartsSort.getSelectedItem().toString()
						.split(" ")[0];
			}

			String partsHostStr = "";
			if (sprPartsHost.getCount() > 0) {
				partsHostStr = sprPartsHost.getSelectedItem().toString()
						.split(" ")[0];
			}

			String partsNameStr = "";
			if (sprPartsName.getCount() > 0) {
				partsNameStr = sprPartsName.getSelectedItem().toString()
						.split(" ")[0];
			}

			String key = partsT5Str + partsFactoryStr + partsSortStr
					+ partsHostStr + partsNameStr;
			List<PartsStorageLocationEntity> listInfo = SqliteHelper.queryPartsStorageLocation(key, num);
			if (listInfo.size() > 0) {				
				listPartsEntity.clear();
				int no = listPartsData.size() + 1;
				for (PartsStorageLocationEntity partsEntity : listInfo) {

					boolean isExsit = false;
					for (Map<String, Object> item : listPartsData) {
						if (item.get("partsCode").equals(partsEntity.PartsCode)) {
							isExsit = true;
							break;
						}
					}
					if (!isExsit) {
						listInfoAll.add(partsEntity);
						Map<String, Object> item = new HashMap<String, Object>();
						item.put("sqeNo", no);
						item.put("partsCode", partsEntity.PartsCode);
						item.put("partsInfo", String.format("库位:%s    入库：%s",
								partsEntity.StorageLocationCode,
								f.format(partsEntity.StockinTime)));
						 Drawable delDr = getResources().getDrawable(
						 R.drawable.delete);
						 item.put("delIcon", delDr);
						item.put("isfocus", false);
						listPartsData.add(item);
						no++;
					}
				}
				mListAdapter.notifyDataSetChanged();
			} else {

				showToast(String.format("没有满足条件待%s%s", getResources()
						.getString(R.string.stockOut), getResources()
						.getString(R.string.parts)));
			}
		}
	};

	private boolean StockOutOP(PartsEntity partsEntiry) {

		// 写入标签用户数据
		WriteUserData_6C msg = ReaderMessageHelper.GetWriteUserData_6C(
				partsEntiry.Epc, OpType.StockOut);

		//listPartsCodeSucess.clear();
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
		
		/*
		if(isSucess)
		{
		boolean resultSave = saveResult();
		if (!resultSave) {
			return false;
		}
		}
*/
		return isSucess;
	}

	private boolean saveResult() {
		if (listPartsCodeSucess.size() > 0) {

			// 保存操作记录
			String user = myApp.getUserId();
			String opTime = f.format(new Date());
			String remark = "";
			String info = user + "," + opTime + "," + remark;
			boolean result = SqliteHelper.SaveOpRecord(listPartsCodeSucess,
					OpType.StockOut, info, listInfoAll);

			if (result) {
				listPartsCodeSucess.clear();
				return true;
			} else {
				return false;
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

					// 找到配件信息并且验证是否允许出库
					if (UtilityHelper.CheckEpc(epc) == 0
							&& UtilityHelper.CheckUserData(userData,
									OpType.StockOut)) {
						if (IsValidEpc(epc, true)) {
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
				for (Map<String, Object> entity : listPartsData) {
					if (entity.get("partsCode").toString()
							.equals(PartsEntityCur.PartsCode)
							&& entity.get("isfocus").toString().equals("false")) {
						StopRead();
						boolean result = StockOutOP(PartsEntityCur);
						if (result) {
							entity.put("isfocus", true);
							mListAdapter.notifyDataSetChanged();
							sp.play(music1, 1, 1, 0, 0, 1);

							stockOutCount++;
							showToast("出库成功");
							txtInfo.setText(String.format("已出库数:%s",
									stockOutCount));
						} else {
							listPartsEntity.remove(PartsEntityCur);
							showToast("出库失败，请重试");
							sp.play(music2, 1, 1, 0, 0, 1);
						}
						break;
					}
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

			Boolean isFocus = (Boolean) mdata.get(position).get("isfocus");
			if (isFocus) {
				convertView.setBackgroundColor(Color.YELLOW);
			} else {
				convertView.setBackgroundColor(Color.WHITE);
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
