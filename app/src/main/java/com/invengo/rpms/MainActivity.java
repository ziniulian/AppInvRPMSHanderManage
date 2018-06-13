package com.invengo.rpms;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.invengo.rpms.entity.ConfigEntity;
import com.invengo.rpms.entity.TbPartsOpEntity;
import com.invengo.rpms.util.Btn001;
import com.invengo.rpms.util.ConfigHelper;
import com.invengo.rpms.util.SynchroDbRa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import invengo.javaapi.protocol.IRP1.PowerOff;

public class MainActivity extends BaseActivity {

	TextView txtUserInfo;
	Button btnConfig;

	// 服务器链接
	final String Namespace = "http://tempuri.org/";// 命名空间
	private String WEB_SERVICE_URL = "http://127.0.0.1:8000/Service.svc";

	private SharedPreferences sp = null;
	private int TIME_EXCT = 1000 * 30 * 1; // 执行间隔时间
	private int what = 0;
	private String[] titlesSelected;
	private String checkCode;
	private SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private List<TbPartsOpEntity> listTbPartsOp;

	private int work = 0;
	private int page = 1;
	private int pageSize = 100;
	private int count = 0;
	private List<String> listSql = new ArrayList<String>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SynchroDbRa.setToaCtx(this);
		btnConfig = (Button) findViewById(R.id.btnConfig);
		btnConfig.setOnClickListener(btnConfigClickListener);

		final Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnTouchListener(new Btn001());
		btnBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				AlertDialog.Builder builder = new Builder(MainActivity.this,
						R.style.AppTheme);
				builder.setTitle("温馨提示");
				builder.setMessage("确定要退出系统?");
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// ...To-do
								System.exit(0);
							}
						});
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// ...To-do
							}
						});
				builder.show();	
			}
		});

		sp = this.getSharedPreferences("ServerInfo", Context.MODE_PRIVATE);

		WEB_SERVICE_URL = String
				.format("http://%s:%s/Service.svc",
						sp.getString("ServerIp", null),
						sp.getString("ServerCom", null));

		txtUserInfo = (TextView) findViewById(R.id.txtUserInfo);
		MyApp myApp = (MyApp) getApplication();
		txtUserInfo.setText(myApp.getUserName() + "  " + myApp.getDeptName()
				+ "  " + myApp.getGroupName() + "  " + myApp.getPostName());

		String[] titles = new String[] {
				getResources().getString(R.string.tagQuety),
				getResources().getString(R.string.pairsStockIn),
				getResources().getString(R.string.pairsStockOut),
				getResources().getString(R.string.pairsSendOut),
				getResources().getString(R.string.pairsCheck),
				getResources().getString(R.string.pairsRun),
				getResources().getString(R.string.pairsStop),
				getResources().getString(R.string.pairsBack),
				getResources().getString(R.string.pairsSendRepair),
				getResources().getString(R.string.RepairCheck),
				getResources().getString(R.string.pairsBackFactory),
				getResources().getString(R.string.pairsScrap),
				getResources().getString(R.string.pairsSendCard),
				getResources().getString(R.string.pairsFind),
				getResources().getString(R.string.About),
				getResources().getString(R.string.stattionSendCard)
		};
		int[] resIds = {
				R.drawable.hom_tagquery,
				R.drawable.hom_partstockin,
				R.drawable.hom_partsstockout,
				R.drawable.hom_parthandout,
				R.drawable.hom_partscheck,
				R.drawable.hom_partstart,
				R.drawable.hom_partstop,
				R.drawable.hom_partback,
				R.drawable.hom_partsendrepair,
				R.drawable.hom_parttest,
				R.drawable.hom_partbackfactoty,
				R.drawable.hom_partscrap,
				R.drawable.hom_partsendcard,
				R.drawable.hom_partquerycheck,
				R.drawable.hom_about,
				R.drawable.hom_partbackfactoty
		};

		String roleStr = "";
		ConfigHelper config = new ConfigHelper();
		ConfigEntity configEntity = config.get_config_info();
		if (myApp.getUserId().equals("admin")) {
			roleStr = configEntity.RoleAdmin;
		} else {

			if (myApp.getDeptCode().equals("01")
					&& myApp.getGroupCode().equals("04")) {
				// 检测所材料组
				roleStr = configEntity.Role1;
			} else if (myApp.getDeptCode().equals("01")
					&& !myApp.getGroupCode().equals("02")) {
				// 检测所备件检测组
				roleStr = configEntity.Role4;
			} else if (!myApp.getDeptCode().equals("01")
					&& myApp.getPostCode().equals("03")) {
				// 车辆段组员
				roleStr = configEntity.Role2;
			} else if (!myApp.getDeptCode().equals("01")
					&& myApp.getPostCode().equals("02")) {
				// 车辆段组长
				roleStr = configEntity.Role3;
			} else {
				roleStr = configEntity.Role5;
			}
		}

		String[] roleStrArray = roleStr.split(",");
		titlesSelected = new String[roleStrArray.length];
		int[] resIdsSelected = new int[roleStrArray.length];
		for (int i = 0; i < roleStrArray.length; i++) {
			int num = Integer.parseInt(roleStrArray[i]);
			titlesSelected[i] = titles[num - 1];
			resIdsSelected[i] = resIds[num - 1];
		}

		GridView gridview = (GridView) findViewById(R.id.gridview);
		int length = resIdsSelected.length;

		// 生成动态数组，并且转入数据
		ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemImage", resIdsSelected[i]);// 添加图像资源的ID
			map.put("ItemText", titlesSelected[i]);// 按序号做ItemText
			lstImageItem.add(map);
		}
		// 生成适配器的ImageItem 与动态数组的元素相对应
		SimpleAdapter saImageItems = new SimpleAdapter(this, lstImageItem,// 数据来源
				R.layout.gridview_item,// item的XML实现

				// 动态数组与ImageItem对应的子项
				new String[] { "ItemImage", "ItemText" },

				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.imgItem, R.id.txtItem });
		// 添加并且显示
		gridview.setAdapter(saImageItems);
		// 添加消息处理
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Toast.makeText(MainActivity.this,name[position],Toast.LENGTH_LONG).show();

				String title = titlesSelected[position];

				if (title.equals(getResources().getString(R.string.tagQuety))) {
					Intent intent = new Intent(MainActivity.this,
							QueryTagActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources().getString(
						R.string.pairsStockIn))) {
					Intent intent = new Intent(MainActivity.this,
							StockInActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources().getString(
						R.string.pairsStockOut))) {
					Intent intent = new Intent(MainActivity.this,
							StockOutActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources().getString(
						R.string.pairsSendOut))) {
					Intent intent = new Intent(MainActivity.this,
							SendOutActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources().getString(
						R.string.pairsCheck))) {
					Intent intent = new Intent(MainActivity.this,
							CheckActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources().getString(
						R.string.pairsRun))) {
					Intent intent = new Intent(MainActivity.this,
							RunActivity.class);

					startActivity(intent);
				} else if (title.equals(getResources().getString(
						R.string.pairsStop))) {
					Intent intent = new Intent(MainActivity.this,
							StopActivity.class);

					startActivity(intent);
				} else if (title.equals(getResources().getString(
						R.string.pairsBack))) {
					Intent intent = new Intent(MainActivity.this,
							BackActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources().getString(
						R.string.pairsSendRepair))) {
					Intent intent = new Intent(MainActivity.this,
							SendRepairActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources().getString(
						R.string.RepairCheck))) {
					Intent intent = new Intent(MainActivity.this,
							RepairActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources().getString(
						R.string.pairsBackFactory))) {
					Intent intent = new Intent(MainActivity.this,
							BackFactoryActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources().getString(
						R.string.pairsScrap))) {
					Intent intent = new Intent(MainActivity.this,
							ScrapActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources()
						.getString(R.string.About))) {
					Intent intent = new Intent(MainActivity.this,
							AboutActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources().getString(R.string.pairsSendCard))) {
					Intent intent = new Intent(MainActivity.this, SendCardActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources().getString(R.string.pairsFind))) {
					Intent intent = new Intent(MainActivity.this, FindActivity.class);
					startActivity(intent);
				} else if (title.equals(getResources().getString(R.string.stattionSendCard))) {
					Intent intent = new Intent(MainActivity.this, StationSendCardActivity.class);
					startActivity(intent);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_set, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_setting:
			Intent intent = new Intent(MainActivity.this, SysSetActivity.class);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private OnClickListener btnConfigClickListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, SysSetActivity.class);
			startActivity(intent);
		}
	};

	private boolean backDown;
	private long firstTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && !backDown) {
			backDown = true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && backDown) {
			backDown = false;
			long currentTime = System.currentTimeMillis();
			if (currentTime - firstTime > 2000) { // 如果两次按键时间间隔大于2秒，则不退出
				showToast("再按一次退出程序");
				firstTime = currentTime;// 更新firstTime
				return true;
			} else { // 两次按键小于2秒时，退出应用
				System.exit(0);
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isReading) {
			reader.send(new PowerOff());
		}

		if (reader != null) {
			reader.disConnect();
			isConnected = false;
		}

		SynchroDbRa.oneStop();	// 关闭同步线程
	}

}
