package com.invengo.rpms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.invengo.rpms.R;
import com.invengo.rpms.entity.CheckEntity;
import com.invengo.rpms.util.SqliteHelper;

public class SysSetActivity extends Activity {

	Button btnTest;
	Button btnSave;
	Button btnDownloadStation;
	Button btnDownloadCode;
	Button btnDownloadUser;
	Button btnDownloadStoragelocation;
	Button btnDownloadCheck;
	Button btnDownloadSendRepair;
	Button btnDownloadStorage;
	EditText edtServiceIp;
	EditText edtServiceCom;
	TextView txtStatus;
	LinearLayout dealTip;

	final int TEST = 0;
	final int DOWNLOAD_STATION = 1;
	final int DOWNLOAD_CODE = 2;
	final int DOWNLOAD_USER = 3;
	final int DOWNLOAD_STORAGE_LOCATION = 4;
	final int DOWNLOAD_CHECK = 5;
	final int DOWNLOAD_CHECK_DETAIL = 6;
	final int DOWNLOAD_PARTS_SENDREPAIR = 7;
	final int DOWNLOAD_STORAGE = 8;

	// 服务器链接
	final String Namespace = "http://tempuri.org/";// 命名空间
	private String WEB_SERVICE_URL = "http://127.0.0.1:8000/Service.svc";

	private SharedPreferences sp = null;
	private boolean isTest = false;
	private String serverAddr = "";
	private int what;
	private int page = 1;
	private int pageSize = 100;
	private int count = 0;
	private List<String> listSql = new ArrayList<String>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sysset);
		
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
				
					finish();
				
			}
		});


		sp = this.getSharedPreferences("ServerInfo", Context.MODE_PRIVATE);
		edtServiceIp = (EditText) findViewById(R.id.edtServiceIp);
		edtServiceIp.setText(sp.getString("ServerIp", null));
		edtServiceCom = (EditText) findViewById(R.id.edtServiceCom);
		edtServiceCom.setText(sp.getString("ServerCom", null));

		dealTip = (LinearLayout) findViewById(R.id.dealTip);
		dealTip.bringToFront();

		WEB_SERVICE_URL = String
				.format("http://%s:%s/Service.svc",
						sp.getString("ServerIp", null),
						sp.getString("ServerCom", null));

		txtStatus = (TextView) findViewById(R.id.txtStatus);

		btnTest = (Button) findViewById(R.id.btnTest);
		btnTest.setOnTouchListener(btnTestTouchListener);
		btnTest.setOnClickListener(btnTestClickListener);

		btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setOnTouchListener(btnSaveTouchListener);
		btnSave.setOnClickListener(btnSaveClickListener);

		btnDownloadStation = (Button) findViewById(R.id.btnDownloadStation);
		btnDownloadStation.setOnTouchListener(btnDownloadStationTouchListener);
		btnDownloadStation.setOnClickListener(btnDownloadStationClickListener);

		btnDownloadCode = (Button) findViewById(R.id.btnDownloadCode);
		btnDownloadCode.setOnTouchListener(btnDownloadCodeTouchListener);
		btnDownloadCode.setOnClickListener(btnDownloadCodeClickListener);

		btnDownloadUser = (Button) findViewById(R.id.btnDownloadUser);
		btnDownloadUser.setOnTouchListener(btnDownloadUserTouchListener);
		btnDownloadUser.setOnClickListener(btnDownloadUserClickListener);

		btnDownloadStoragelocation = (Button) findViewById(R.id.btnDownloadStoragelocation);
		btnDownloadStoragelocation
				.setOnTouchListener(btnDownloadStoragelocationTouchListener);
		btnDownloadStoragelocation
				.setOnClickListener(btnDownloadStoragelocationClickListener);

		btnDownloadCheck = (Button) findViewById(R.id.btnDownloadCheck);
		btnDownloadCheck.setOnTouchListener(btnDownloadCheckTouchListener);
		btnDownloadCheck.setOnClickListener(btnDownloadCheckClickListener);
		btnDownloadCheck.setVisibility(View.GONE);

		btnDownloadSendRepair = (Button) findViewById(R.id.btnDownloadSendRepair);
		btnDownloadSendRepair
				.setOnTouchListener(btnDownloadSendRepairTouchListener);
		btnDownloadSendRepair
				.setOnClickListener(btnDownloadSendRepairClickListener);
		btnDownloadSendRepair.setVisibility(View.GONE);

		btnDownloadStorage = (Button) findViewById(R.id.btnDownloadStorage);
		btnDownloadStorage.setOnTouchListener(btnDownloadStorageTouchListener);
		btnDownloadStorage.setOnClickListener(btnDownloadStorageClickListener);

		btnDownloadSendRepair.setVisibility(View.GONE);
		
		MyApp myApp = (MyApp) getApplication();
		if (myApp.getUserId() == null) {
			btnDownloadStation.setVisibility(View.GONE);
			btnDownloadCode.setVisibility(View.GONE);
			btnDownloadStoragelocation.setVisibility(View.GONE);
			btnDownloadStorage.setVisibility(View.GONE);
		} else if (!myApp.getUserId().equals("admin")) {
			if (myApp.getDeptCode().equals("01") && myApp.getGroupCode().equals("04")) {
				// 检测所材料组
				
			}
			else if(myApp.getDeptCode().equals("01") && !myApp.getGroupCode().equals("02"))
			{
				// 检测所备件检测组
				btnDownloadStation.setVisibility(View.GONE);
				btnDownloadStoragelocation.setVisibility(View.GONE);
				btnDownloadStorage.setVisibility(View.GONE);
			}
			else if(!myApp.getDeptCode().equals("01") && myApp.getPostCode().equals("03"))
			{
				// 车辆段组员
				btnDownloadStoragelocation.setVisibility(View.GONE);
				btnDownloadStorage.setVisibility(View.GONE);
			}
			else if(!myApp.getDeptCode().equals("01") && myApp.getPostCode().equals("02"))
			{
				// 车辆段组长
				btnDownloadStoragelocation.setVisibility(View.GONE);
				btnDownloadStorage.setVisibility(View.GONE);
			}
			else
			{
				btnDownloadStoragelocation.setVisibility(View.GONE);
				btnDownloadStorage.setVisibility(View.GONE);
				btnDownloadStation.setVisibility(View.GONE);
			}
		}
	}

	private OnTouchListener btnTestTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnTest.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnTest.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnTest.setBackgroundResource(R.drawable.btnnormal);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnTestClickListener = new OnClickListener() {
		public void onClick(View v) {
			String serverIp = edtServiceIp.getText().toString().trim();
			if (serverIp.length() == 0) {
				showToast("请输入服务地址");
				return;
			}
			String serverCom = edtServiceCom.getText().toString().trim();
			if (serverIp.length() == 0) {
				showToast("请输入服务端口");
				return;
			}

			serverAddr = serverIp + ":" + serverCom;

			isTest = false;
			what = TEST;
			Request("HelloWorld");
			txtStatus.setText("正在测试... ");
		}
	};

	private OnTouchListener btnSaveTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnSave.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnSave.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnSave.setBackgroundResource(R.drawable.btnnormal);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnSaveClickListener = new OnClickListener() {
		public void onClick(View v) {

			if (!isTest) {
				showToast("测试成功才能保存");
				return;
			}

			try {

				WEB_SERVICE_URL = String.format("http://%s/Service.svc",
						serverAddr);
				Editor editor = sp.edit();
				editor.putString("ServerIp", serverAddr.split(":")[0]);
				editor.putString("ServerCom", serverAddr.split(":")[1]);
				editor.commit();
				showToast("保存成功");
			} catch (Exception e) {
				e.printStackTrace();
				showToast("保存失败");
			}
		}
	};

	private OnTouchListener btnDownloadStationTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnDownloadStation.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnDownloadStation.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnDownloadStation.setBackgroundResource(R.drawable.btnnormal);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnDownloadStationClickListener = new OnClickListener() {
		public void onClick(View v) {

			page = 1;
			count = 0;
			what = DOWNLOAD_STATION;
			dealTip.setVisibility(View.VISIBLE);

			listSql.clear();
			listSql.add("delete from TbTHDS");

			HashMap<String, Integer> values = new HashMap<String, Integer>();
			values.put("page", page);
			values.put("pageSize", pageSize);
			String keys = "page,pageSize";
			Request("GetTHDSByPage", values, keys);
			txtStatus.setText("正在下载站点信息... ");
		}
	};

	private OnTouchListener btnDownloadCodeTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnDownloadCode.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnDownloadCode.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnDownloadCode.setBackgroundResource(R.drawable.btnnormal);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnDownloadCodeClickListener = new OnClickListener() {
		public void onClick(View v) {

			page = 1;
			count = 0;
			what = DOWNLOAD_CODE;
			dealTip.setVisibility(View.VISIBLE);

			listSql.clear();
			listSql.add("delete from TbCode");

			HashMap<String, Integer> values = new HashMap<String, Integer>();
			values.put("page", page);
			values.put("pageSize", pageSize);
			String keys = "page,pageSize";
			Request("GetCodeByPage", values, keys);
			txtStatus.setText("正在下载数据字典信息... ");

		}
	};

	private OnTouchListener btnDownloadUserTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnDownloadUser.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnDownloadUser.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnDownloadUser.setBackgroundResource(R.drawable.btnnormal);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnDownloadUserClickListener = new OnClickListener() {
		public void onClick(View v) {

			page = 1;
			count = 0;
			what = DOWNLOAD_USER;
			dealTip.setVisibility(View.VISIBLE);

			listSql.clear();
			listSql.add("delete from TbUser");

			HashMap<String, Integer> values = new HashMap<String, Integer>();
			values.put("page", page);
			values.put("pageSize", pageSize);
			String keys = "page,pageSize";
			Request("GetUserByPage", values, keys);
			txtStatus.setText("正在下载用户信息... ");
		}
	};

	private OnTouchListener btnDownloadStoragelocationTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnDownloadStoragelocation
						.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnDownloadStoragelocation
						.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnDownloadStoragelocation
						.setBackgroundResource(R.drawable.btnnormal);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnDownloadStoragelocationClickListener = new OnClickListener() {
		public void onClick(View v) {

			page = 1;
			count = 0;
			what = DOWNLOAD_STORAGE_LOCATION;
			dealTip.setVisibility(View.VISIBLE);

			listSql.clear();
			listSql.add("delete from StorageLocation");

			HashMap<String, Integer> values = new HashMap<String, Integer>();
			values.put("page", page);
			values.put("pageSize", pageSize);
			String keys = "page,pageSize";
			Request("GetStorageLocationByPage", values, keys);
			txtStatus.setText("正在下载库位信息... ");

		}
	};

	private OnTouchListener btnDownloadCheckTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnDownloadCheck.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnDownloadCheck.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnDownloadCheck.setBackgroundResource(R.drawable.btnnormal);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnDownloadCheckClickListener = new OnClickListener() {
		public void onClick(View v) {

			page = 1;
			count = 0;
			what = DOWNLOAD_CHECK;
			dealTip.setVisibility(View.VISIBLE);

			listSql.clear();

			HashMap<String, Integer> values = new HashMap<String, Integer>();
			values.put("page", page);
			values.put("pageSize", pageSize);
			String keys = "page,pageSize";
			Request("GetCheckWork", values, keys);
			txtStatus.setText("正在下载盘点任务信息... ");

		}
	};

	private OnTouchListener btnDownloadSendRepairTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnDownloadSendRepair
						.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnDownloadSendRepair
						.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnDownloadSendRepair
						.setBackgroundResource(R.drawable.btnnormal);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnDownloadSendRepairClickListener = new OnClickListener() {
		public void onClick(View v) {

			page = 1;
			count = 0;
			what = DOWNLOAD_PARTS_SENDREPAIR;
			dealTip.setVisibility(View.VISIBLE);

			listSql.clear();
			listSql.add("delete from TbSendRepair");

			HashMap<String, Integer> values = new HashMap<String, Integer>();
			values.put("page", page);
			values.put("pageSize", pageSize);
			String keys = "page,pageSize";
			Request("GetPartsNoRepair", values, keys);
			txtStatus.setText("正在下载送修信息... ");

		}
	};

	private OnTouchListener btnDownloadStorageTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnDownloadStorage.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnDownloadStorage.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnDownloadStorage.setBackgroundResource(R.drawable.btnnormal);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnDownloadStorageClickListener = new OnClickListener() {
		public void onClick(View v) {

			page = 1;
			count = 0;
			what = DOWNLOAD_STORAGE;
			dealTip.setVisibility(View.VISIBLE);

			listSql.clear();
			listSql.add("delete from PartsStorageLocation");

			HashMap<String, Integer> values = new HashMap<String, Integer>();
			values.put("page", page);
			values.put("pageSize", pageSize);
			String keys = "page,pageSize";
			Request("GetPartsStorageByPage", values, keys);
			txtStatus.setText("正在下载库存信息... ");

		}
	};

	@SuppressLint("HandlerLeak")
	private Handler cardOperationHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String result = (String) msg.obj;
			switch (what) {
			case TEST:
				if (result.equals("HelloWorld!")) {
					isTest = true;
					txtStatus.setText("测试成功");
				} else {
					txtStatus.setText("测试失败");
				}
				break;
			case DOWNLOAD_STATION:
				DownLoadStation(result);
				break;
			case DOWNLOAD_CODE:
				DownLoadCode(result);
				break;
			case DOWNLOAD_USER:
				DownLoadUser(result);
				break;
			case DOWNLOAD_STORAGE_LOCATION:
				DownLoadStorageLocation(result);
				break;
			case DOWNLOAD_CHECK:
				DownLoadCheck(result);
				break;
			case DOWNLOAD_PARTS_SENDREPAIR:
				DownLoadPartsNoRepair(result);
				break;
			case DOWNLOAD_STORAGE:
				DownLoadStorage(result);
				break;
			default:
				break;
			}
		};
	};

	private void DownLoadStation(String result) {
		if (result.equals("error")) {
			dealTip.setVisibility(View.GONE);
			txtStatus.setText("下载失败,请检查网络");
			return;
		}
		if (result.length() > 1) {
			result = result.replace("-", "&**&&");
			result = result.replace("\r\n\r\n", "-");
			result = result.replace("\r\n", "-");
			String[] resutlStrArray = result.split("-");
			for (String str : resutlStrArray) {
				String strBack = str.replace("&**&&", "-");
				String[] strArray = strBack.split(",", -1);
				if (strArray.length == 3) {
					String sql = String.format(
							"insert into TbTHDS values('%s','%s','%s','')",
							strArray[0], strArray[1], strArray[2]);
					listSql.add(sql);
					count++;
				}
			}

			page++;
			HashMap<String, Integer> values = new HashMap<String, Integer>();
			values.put("page", page);
			values.put("pageSize", pageSize);
			String keys = "page,pageSize";
			Request("GetTHDSByPage", values, keys);
		} else {
			if (listSql.size() > 1) {
				boolean r = SqliteHelper.ExceSql(listSql);
				if (r) {
					txtStatus.setText(String.format("成功下载%s条站点信息", count));
				} else {
					txtStatus.setText(String.format("下载站点信息失败", count));
				}
			} else {
				txtStatus.setText(String.format("无站点信息下载", count));
			}

			dealTip.setVisibility(View.GONE);
		}
	}

	private void DownLoadCode(String result) {
		if (result.equals("error")) {
			dealTip.setVisibility(View.GONE);
			txtStatus.setText("下载失败,请检查网络");
			return;
		}
		if (result.length() > 1) {
			result = result.replace("-", "&**&&");
			result = result.replace("\r\n\r\n", "-");
			result = result.replace("\r\n", "-");
			String[] resutlStrArray = result.split("-");
			for (String str : resutlStrArray) {
				String strBack = str.replace("&**&&", "-");
				String[] strArray = strBack.split(",", -1);
				if (strArray.length == 5) {
					String sql = String
							.format("insert into TbCode values('%s','%s','%s','%s','%s')",
									strArray[0], strArray[1], strArray[2],
									strArray[3].replace("&*&", ","), strArray[4].replace("&*&", ","));
					listSql.add(sql);
					count++;
				}
			}

			page++;
			HashMap<String, Integer> values = new HashMap<String, Integer>();
			values.put("page", page);
			values.put("pageSize", pageSize);
			String keys = "page,pageSize";
			Request("GetCodeByPage", values, keys);
		} else {
			if (listSql.size() > 1) {
				boolean r = SqliteHelper.ExceSql(listSql);
				if (r) {
					txtStatus.setText(String.format("成功下载%s条字典信息", count));
				} else {
					txtStatus.setText(String.format("下载字典信息失败", count));
				}
			} else {
				txtStatus.setText(String.format("无字典信息下载", count));
			}

			dealTip.setVisibility(View.GONE);
		}
	}

	private void DownLoadUser(String result) {
		if (result.equals("error")) {
			dealTip.setVisibility(View.GONE);
			txtStatus.setText("下载失败,请检查网络");
			return;
		}
		if (result.length() > 1) {
			result = result.replace("-", "&**&&");
			result = result.replace("\r\n\r\n", "-");
			result = result.replace("\r\n", "-");
			String[] resutlStrArray = result.split("-");
			for (String str : resutlStrArray) {
				String strBack = str.replace("&**&&", "-");
				String[] strArray = strBack.split(",", -1);
				if (strArray.length == 8) {
					String sql = String
							.format("insert into TbUser values('%s','%s','%s','%s','%s','%s','%s','%s')",
									strArray[0], strArray[1], strArray[2],
									strArray[3], strArray[4], strArray[5],
									strArray[6], strArray[7]);
					listSql.add(sql);
					count++;
				}
			}

			page++;
			HashMap<String, Integer> values = new HashMap<String, Integer>();
			values.put("page", page);
			values.put("pageSize", pageSize);
			String keys = "page,pageSize";
			Request("GetUserByPage", values, keys);
		} else {
			if (listSql.size() > 1) {
				boolean r = SqliteHelper.ExceSql(listSql);
				if (r) {
					txtStatus.setText(String.format("成功下载%s条用户信息", count));
				} else {
					txtStatus.setText(String.format("下载用户信息失败", count));
				}
			} else {
				txtStatus.setText(String.format("无用户信息下载", count));
			}

			dealTip.setVisibility(View.GONE);
		}
	}

	private void DownLoadStorageLocation(String result) {
		if (result.equals("error")) {
			dealTip.setVisibility(View.GONE);
			txtStatus.setText("下载失败,请检查网络");
			return;
		}
		if (result.length() > 1) {
			result = result.replace("-", "&**&&");
			result = result.replace("\r\n\r\n", "-");
			result = result.replace("\r\n", "-");
			String[] resutlStrArray = result.split("-");
			for (String str : resutlStrArray) {
				String strBack = str.replace("&**&&", "-");
				String[] strArray = strBack.split(",", -1);
				if (strArray.length == 4) {
					String sql = String
							.format("insert into StorageLocation values('%s','%s','%s','%s')",
									strArray[0], strArray[1], strArray[2],
									strArray[3]);
					listSql.add(sql);
					count++;
				}
			}

			page++;
			HashMap<String, Integer> values = new HashMap<String, Integer>();
			values.put("page", page);
			values.put("pageSize", pageSize);
			String keys = "page,pageSize";
			Request("GetStorageLocationByPage", values, keys);
		} else {
			if (listSql.size() > 1) {
				boolean r = SqliteHelper.ExceSql(listSql);
				if (r) {
					txtStatus.setText(String.format("成功下载%s条库位信息", count));
				} else {
					txtStatus.setText(String.format("下载库位信息失败", count));
				}
			} else {
				txtStatus.setText(String.format("无库位信息下载", count));
			}

			dealTip.setVisibility(View.GONE);
		}
	}

	private void DownLoadCheck(String result) {
		if (result.equals("error")) {
			dealTip.setVisibility(View.GONE);
			txtStatus.setText("下载失败,请检查网络");
			return;
		}
		if (result.length() > 1) {
			List<String> listCheckExsit = new ArrayList<String>();
			result = result.replace("-", "&**&&");
			result = result.replace("\r\n\r\n", "-");
			result = result.replace("\r\n", "-");
			String[] resutlStrArray = result.split("-");
			for (String str : resutlStrArray) {
				String strBack = str.replace("&**&&", "-");
				String[] strArray = strBack.split(",", -1);

				String checkCode = strArray[0];
				if (listCheckExsit.contains(checkCode)) {
					continue;
				}
				CheckEntity entity = SqliteHelper.queryCheckByCode(checkCode);
				if (entity != null) {
					listCheckExsit.add(checkCode);
					continue;
				}

				if (strArray.length == 4) {
					String sql = String
							.format("insert into TbCheck values('%s','%s','%s','%s','N')",
									strArray[0], strArray[1], strArray[2],
									strArray[3]);
					listSql.add(sql);
					count++;
				} else if (strArray.length == 3) {
					String sql = String
							.format("insert into TbCheckDetail values('{0}','{1}','{2}','N')",
									strArray[0], strArray[1], strArray[2]);
					listSql.add(sql);
				}
			}

		} else {
			if (listSql.size() > 1) {
				boolean r = SqliteHelper.ExceSql(listSql);
				if (r) {
					txtStatus.setText(String.format("成功下载%s条盘点任务", count));
				} else {
					txtStatus.setText(String.format("下载盘点任务失败", count));
				}
			} else {
				txtStatus.setText(String.format("无盘点任务下载", count));
			}

			dealTip.setVisibility(View.GONE);
		}
	}

	private void DownLoadPartsNoRepair(String result) {
		if (result.equals("error")) {
			dealTip.setVisibility(View.GONE);
			txtStatus.setText("下载失败,请检查网络");
			return;
		}
		if (result.length() > 1) {
			result = result.replace("-", "&**&&");
			result = result.replace("\r\n\r\n", "-");
			result = result.replace("\r\n", "-");
			String[] resutlStrArray = result.split("-");
			for (String str : resutlStrArray) {
				String strBack = str.replace("&**&&", "-");
				String[] strArray = strBack.split(",", -1);
				if (strArray.length == 5) {
					String sql = String
							.format("insert into TbSendRepair values('%s','%s','%s','%s','%s')",
									strArray[0], strArray[1], strArray[2],
									strArray[3], strArray[4]);
					listSql.add(sql);
					count++;
				}
			}

			boolean r = SqliteHelper.ExceSql(listSql);
			if (r) {
				txtStatus.setText(String.format("成功下载%s条故障信息", count));
			} else {
				txtStatus.setText(String.format("下载故障信息失败", count));
			}

		} else {
			txtStatus.setText(String.format("无故障信息下载", count));
		}

		dealTip.setVisibility(View.GONE);

	}

	private void DownLoadStorage(String result) {
		if (result.equals("error")) {
			dealTip.setVisibility(View.GONE);
			txtStatus.setText("下载失败,请检查网络");
			return;
		}
		if (result.length() > 1) {
			result = result.replace("-", "&**&&");
			result = result.replace("\r\n\r\n", "-");
			result = result.replace("\r\n", "-");
			String[] resutlStrArray = result.split("-");
			for (String str : resutlStrArray) {
				String strBack = str.replace("&**&&", "-");
				String[] strArray = strBack.split(",", -1);
				if (strArray.length == 3) {
					String sql = String
							.format("insert into PartsStorageLocation values('%s','%s','%s')",
									strArray[0], strArray[1], strArray[2]);
					listSql.add(sql);
					count++;
				}
			}

			page++;
			HashMap<String, Integer> values = new HashMap<String, Integer>();
			values.put("page", page);
			values.put("pageSize", pageSize);
			String keys = "page,pageSize";
			Request("GetPartsStorageByPage", values, keys);
		} else {
			if (listSql.size() > 1) {
				boolean r = SqliteHelper.ExceSql(listSql);
				if (r) {
					txtStatus.setText(String.format("成功下载%s条库存信息", count));
				} else {
					txtStatus.setText(String.format("下载库存信息失败", count));
				}
			} else {
				txtStatus.setText(String.format("无库存信息下载", count));
			}

			dealTip.setVisibility(View.GONE);
		}
	}

	/**
	 * 调用WebService
	 * 
	 * @return WebService的返回值
	 * 
	 */
	public String CallWebService(String MethodName, Map<String, String> Params,
			String keys) {

		// 1、指定webservice的命名空间和调用的方法名
		SoapObject request = new SoapObject(Namespace, MethodName);
		// 2、设置调用方法的参数值，如果没有参数，可以省略，
		if (Params != null) {
			String[] keyStrs = keys.split(",");
			for (int i = 0; i < keyStrs.length; i++) {
				String key = keyStrs[i];
				request.addProperty(key, Params.get(key));
			}
		}

		// 3、生成调用Webservice方法的SOAP请求信息。该信息由SoapSerializationEnvelope对象描述
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.bodyOut = request;
		// c#写的应用程序必须加上这句
		envelope.dotNet = true;
		String url = WEB_SERVICE_URL;
		if (what == TEST) {
			url = String.format("http://%s/Service.svc", serverAddr);
		}
		HttpTransportSE ht = new HttpTransportSE(url);
		// 使用call方法调用WebService方法
		try {
			String soapAction = Namespace + "IService/" + MethodName;
			ht.call(soapAction, envelope);
		} catch (HttpResponseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		try {
			final SoapPrimitive result = (SoapPrimitive) envelope.getResponse();
			// Object result = (Object) envelope.getResponse();
			if (result != null) {
				Log.d("----收到的回复----", result.toString());
				return result.toString();
			}

		} catch (SoapFault e) {
			Log.e("----发生错误---", e.getMessage());
			e.printStackTrace();
		}

		Message message = new Message();
		message.obj = "error";
		cardOperationHandler.sendMessage(message);
		return null;
	}

	/**
	 * 执行异步任务
	 * 
	 * @param params
	 *            方法名+参数列表（哈希表形式）
	 */
	public void Request(Object... params) {
		new AsyncTask<Object, Object, String>() {

			@Override
			protected String doInBackground(Object... params) {
				if (params != null && params.length == 3) {
					return CallWebService((String) params[0],
							(Map<String, String>) params[1], (String) params[2]);
				} else if (params != null && params.length == 1) {
					return CallWebService((String) params[0], null, "");
				} else {
					return null;
				}
			}

			protected void onPostExecute(String result) {
				if (result != null) {
					Message message = new Message();
					message.obj = result;
					cardOperationHandler.sendMessage(message);
				}
			};

		}.execute(params);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyUp(keyCode, event);
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}
