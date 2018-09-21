package com.invengo.rpms;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.invengo.rpms.entity.OpType;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.entity.PartsStorageLocationEntity;
import com.invengo.rpms.entity.TbCodeEntity;
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
	Button btnOk;
	TextView outNumV;
	int otn = 0;
	int sid = 0;
	String user;
	boolean isOut = false;

	private ListView mEpcListView;
	private SimpleAdapter mListAdapter;
	private List<Map<String, Object>> listPartsData = new ArrayList<Map<String, Object>>();
	private PartsEntity PartsEntityCur;
	private List<PartsStorageLocationEntity> listInfoAll=new ArrayList<PartsStorageLocationEntity>();
	private List<String> listPartsName = new ArrayList<String>();
	private List<String> listPartsSort = new ArrayList<String>();
	private List<String> listPartsHost = new ArrayList<String>();
	private ArrayAdapter<String> adapterPartsName;
	private ArrayAdapter<String> adapterPartsSort;
	private ArrayAdapter<String> adapterPartsHost;
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

		user = myApp.getUserId();

		txtStatus = (TextView) findViewById(R.id.txtStatus);
		outNumV = (TextView) findViewById(R.id.outNum);

		btnQuery = (Button) findViewById(R.id.btnQuery);
		btnQuery.setOnClickListener(btnQueryClickListener);

		btnConfig = (Button) findViewById(R.id.btnConfig);
		btnConfig.setOnClickListener(btnConfigClickListener);

		btnOk = (Button) findViewById(R.id.btnOK);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (setRate()) {	// 最大功率
					sid = listPartsData.size() - 1;
					if (sid >= 0) {
						isReading = true;
						txtStatus.setText("正在出库，请稍候 ...");
						isOut = false;
						sop();
					} else {
						sendMsg(WRT_UD_ERR, null);
					}
				} else {
					sendMsg(WRT_UD_ERR, null);
				}
			}
		});

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

		adapterPartsSort = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, listPartsSort);
		adapterPartsSort
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsSort.setAdapter(adapterPartsSort);
		sprPartsSort.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				String sortSelected = adapterPartsSort.getItem(position).toString();
				sortCodeSelected = sortSelected.split(" ")[0];

				listPartsHost.clear();
				List<TbCodeEntity> listCodeHost = SqliteHelper
						.queryDbCodeByType("08");
				for (TbCodeEntity entityName : listCodeHost) {
					if (UtilityHelper.IsExsitCodeBeyond(
							entityName.dbCodeBeyond, sortCodeSelected)) {
						listPartsHost.add(entityName.dbCode + " "
								+ entityName.dbName);
					}
				}
				adapterPartsHost.notifyDataSetChanged();

				listPartsName.clear();
				List<TbCodeEntity> listCodeName = SqliteHelper
						.queryDbCodeByType("09");
				for (TbCodeEntity entityName : listCodeName) {
					if (UtilityHelper.IsExsitCodeBeyond(
							entityName.dbCodeBeyond, sortCodeSelected,
							hostCodeSelected)) {
						listPartsName.add(entityName.dbCode + " "
								+ entityName.dbName);
					}
				}
				adapterPartsName.notifyDataSetChanged();
			}

			// 没有选中时的处理
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		sprPartsHost = (Spinner) findViewById(R.id.sprPartsHost);
		listPartsHost.clear();
		List<TbCodeEntity> listCodeHost = SqliteHelper.queryDbCodeByType("08");
		for (TbCodeEntity entity : listCodeHost) {
			listPartsHost.add(entity.dbCode + " " + entity.dbName);
		}
		if (listCodeHost.size() > 0) {
			hostCodeSelected = listCodeHost.get(0).toString().split(" ")[0];
		}

		adapterPartsHost = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, listPartsHost);
		adapterPartsHost
				.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
		sprPartsHost.setAdapter(adapterPartsHost);
		sprPartsHost.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				String hostSelected = adapterPartsHost.getItem(position).toString();
				hostCodeSelected = hostSelected.split(" ")[0];

				listPartsName.clear();
				List<TbCodeEntity> listCodeName = SqliteHelper
						.queryDbCodeByType("09");
				for (TbCodeEntity entityName : listCodeName) {
					if (UtilityHelper.IsExsitCodeBeyond(
							entityName.dbCodeBeyond, sortCodeSelected,
							hostCodeSelected)) {
						listPartsName.add(entityName.dbCode + " "
								+ entityName.dbName);
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
		mListAdapter = new SimpleAdapter(this, listPartsData,
				R.layout.listview_out_item,
				new String[] { "sqeNo", "partsCode", "partsInfo", "inTim", "tim", "self" },
				new int[] { R.id.sqeNo, R.id.partsCode, R.id.partsInfo, R.id.inTim, R.id.tim, R.id.sec });
		mListAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				if (view instanceof CheckBox && data instanceof CsdListener) {
					CheckBox v = (CheckBox) view;
					CsdListener cl = (CsdListener) data;
					Map<String, Object> mo = cl.getMo();

					if(!v.isEnabled() && (((Integer) mo.get("tim")) > 0)) {
						v.setEnabled(true);
					}

					v.setOnCheckedChangeListener(cl);
					v.setChecked((Boolean)mo.get("sec"));
					return true;
				}
				return false;
			}
		});

		// 实现列表的显示
		mEpcListView.setAdapter(mListAdapter);
	}

	private OnClickListener btnConfigClickListener = new OnClickListener() {
		public void onClick(View v) {
			AlertDialog.Builder builder = new Builder(StockOutActivity.this, R.style.AppTheme);
			builder.setTitle("温馨提示");
			builder.setMessage(getResources().getString(
					R.string.pairsStockOutTipInfo));
			builder.setPositiveButton("关闭", null);
			builder.show();
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
				int no = listPartsData.size() + 1;
				for (PartsStorageLocationEntity partsEntity : listInfo) {
					Map<String, Object> mo = getMo(partsEntity.PartsCode);
					if (mo == null) {
						Map<String, Object> item = new HashMap<String, Object>();
						item.put("sqeNo", no);
						item.put("partsCode", partsEntity.PartsCode);
						item.put("partsInfo", partsEntity.StorageLocationCode);
						item.put("inTim", f.format(partsEntity.StockinTime));
						item.put("tim", 0);
						item.put("sec", false);
						item.put("self", new CsdListener(item));
						listPartsData.add(item);
						no++;
					} else {
						mo.put("tim", 0);
					}
				}
			} else {
				showToast(String.format("没有满足条件待%s%s", getResources()
						.getString(R.string.stockOut), getResources()
						.getString(R.string.parts)));
			}
			mListAdapter.notifyDataSetChanged();
		}
	};

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
								Map<String, Object> mo = getMo(partsCode);
								if (mo != null) {
									if (!mo.containsKey("tid")) {
										mo.put("tid", Util.convertByteArrayToHexString(data.getReceivedMessage().getTID()));
										mo.put("oud", userData.substring(0, 4));
									}
									sendMsg(DATA_ARRIVED_PAIRS, mo);
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
		if (keyCode == KeyEvent.KEYCODE_BACK && !backDown) {
			if (isReading) {
				showToast("请先停止读取");
				return true;
			} else {
				backDown = true;
			}
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

		sendMsg(START_READ, result);
	}

	private void StopRead() {
		isReading = false;
		boolean result = setRate();	// 最大功率;

		sendMsg(STOP_READ, result);
	}

	private Map<String, Object> getMo (String cod) {
		for (int i = 0; i < listPartsData.size(); i++) {
			if (listPartsData.get(i).get("partsCode").equals(cod)) {
				return listPartsData.get(i);
			}
		}
		return null;
	}

	private void sendMsg (int w, Object o) {
		cardOperationHandler.sendMessage(cardOperationHandler.obtainMessage(w, o));
	}

	private void writeCard(String[] sa, int a1, int a2) {
		WrtUdRa r = new WrtUdRa(
				reader,
				sa,
				cardOperationHandler,
				a1,
				a2
		);
		new Thread(r).start();
	}

	private void sop () {
		if (isReading) {
			Map<String, Object> mo = listPartsData.get(sid);
			Boolean b = (Boolean) mo.get("sec");
			if (b) {
				writeCard(new String[] {mo.get("tid").toString(), "", "0300"}, 0, sid);
			} else if (sid > 0) {
				sid --;
				sop();
			} else if (isOut) {
				sendMsg(DATA_ARRIVED_STATION, null);
			} else {
				sendMsg(WRT_UD_ERR, null);
			}
		}
	}

	// 更新数据库
	private void upDb (Map<String, Object> mo) {
		String opTime = f.format(new Date());
		String remark = "";
		String info = mo.get("partsInfo") + "," + user + "," + opTime + "," + remark;
		List<String> listSql = new ArrayList<String>();

		// 保存OP操作
		String sql = "insert into TbPartsOp values('"
				+ mo.get("partsCode") + "', '"
				+ OpType.StockOut + "', '"
				+ info + "')";
		listSql.add(sql);

		// 修改本地数据库信息
		sql = "update TbParts set Status='S',Code=null,"
				+ "LastOpTime='" + SqliteHelper.f.format(new Date())
				+ "',OpUser='" + user
				+ "' where PartsCode='" + mo.get("partsCode") + "'";
		listSql.add(sql);

		SqliteHelper.ExceSql(listSql);
	}

	private class CsdListener implements CompoundButton.OnCheckedChangeListener {
		private Map<String, Object> mo;

		CsdListener (Map<String, Object> m) {
			this.mo = m;
		}

		Map<String, Object> getMo() {
			return mo;
		}

		@Override
		public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//			if (b) {
//				otn ++;
//			} else {
//				otn --;
//			}
//			outNumV.setText(otn + "");
//Log.i("----", otn + "");
			mo.put("sec", b);
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler cardOperationHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			Map<String, Object> mo;
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
				mo = (Map<String, Object>) msg.obj;
				mo.put("sec", true);
				int n = (Integer) mo.get("tim");
				n ++;
				mo.put("tim", n);
				mListAdapter.notifyDataSetChanged();
				sp.play(music2, 1, 1, 0, 0, 1);
				break;
			case WRT_UD_ERR:	// 用户区写入失败
				isReading = false;
				showToast("出库失败!");
				txtStatus.setText("出库失败!");
				sp.play(music3, 1, 1, 0, 0, 1);
				break;
			case DATA_ARRIVED_STATION:
				isReading = false;
				showToast("出库成功!");
				txtStatus.setText("出库成功!");
				sp.play(music1, 1, 1, 0, 0, 1);
				break;
			case WRT_OK:// 接收配件数据
				otn ++;
				outNumV.setText(otn + "");
				isOut = true;
				upDb(listPartsData.remove(msg.arg2));
				mListAdapter.notifyDataSetChanged();
				if (sid > 0) {
					sid --;
					sop();
				} else {
					sendMsg(DATA_ARRIVED_STATION, null);
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
		}
	};
}
