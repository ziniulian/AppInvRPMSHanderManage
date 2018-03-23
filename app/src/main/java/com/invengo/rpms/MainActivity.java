package com.invengo.rpms;

import invengo.javaapi.protocol.IRP1.PowerOff;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.invengo.rpms.R;
import com.invengo.rpms.entity.CheckDetailEntity;
import com.invengo.rpms.entity.CheckEntity;
import com.invengo.rpms.entity.ConfigEntity;
import com.invengo.rpms.entity.TbPartsOpEntity;
import com.invengo.rpms.util.ConfigHelper;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

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

		btnConfig = (Button) findViewById(R.id.btnConfig);
		btnConfig.setOnTouchListener(btnConfigTouchListener);
		btnConfig.setOnClickListener(btnConfigClickListener);

		final Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnTouchListener(new OnTouchListener() {
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
				getResources().getString(R.string.About)
		};
		int[] resIds = {
				R.drawable.query,
				R.drawable.stockin,
				R.drawable.stockout,
				R.drawable.sendout,
				R.drawable.check,
				R.drawable.use,
				R.drawable.stop,
				R.drawable.fault,
				R.drawable.sendrepair,
				R.drawable.repair,
				R.drawable.backfactory,
				R.drawable.scrap,
				R.drawable.about,
				R.drawable.about,
				R.drawable.about
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
				}
				 else if (title.equals(getResources()
							.getString(R.string.pairsSendCard))) {
						Intent intent = new Intent(MainActivity.this,
								SendCardActivity.class);
						startActivity(intent);
					}
			}
		});

		timer.schedule(task, 1000 * 30, TIME_EXCT); // 1s后执行task,经过TIME_EXCT再次执行
	}

	Timer timer = new Timer();
	TimerTask task = new TimerTask() {
		@Override
		public void run() {
			try {

				// 上传操作记录
				if (true) {
					listTbPartsOp = SqliteHelper.queryOpRecord();
					String opInfo = "";
					if (listTbPartsOp.size() > 0) {
						for (TbPartsOpEntity entity : listTbPartsOp) {
							String strs = entity.PartsCode + ","
									+ entity.OpType + "," + entity.Info;
							strs = strs.replace("-", "===");
							opInfo += strs + "-";
						}
					}
					if (opInfo.length() > 0) {
						opInfo = opInfo.substring(0, opInfo.length() - 1);

						what = 1;
						HashMap<String, String> values = new HashMap<String, String>();
						values.put("opInfo", opInfo);
						String keys = "opInfo";
						Request("SavaPartsOp", values, keys);
					}
				}

				// 上传盘点记录
				if (work == 1) {
					List<CheckEntity> listCheck = SqliteHelper.queryCheck(true);
					String opInfo = "";
					if (listCheck.size() > 0) {
						for (CheckEntity entity : listCheck) {
							checkCode = entity.CheckCode;
							String checkPartsType = entity.CheckPartsType;
							String addUser = entity.AddUser;
							String addTime = f.format(entity.AddTime);
							String remark = entity.Remark;
							remark = remark.replace(",", "+++");
							String strs = checkCode + "," + checkPartsType
									+ "," + addUser + "," + addTime + ","
									+ remark;
							strs = strs.replace("-", "===");
							opInfo += strs + "-";

							List<CheckDetailEntity> listCheckDetail = SqliteHelper
									.queryCheckDetailByCheckCode(checkCode);
							for (CheckDetailEntity entityDetail : listCheckDetail) {

								String partsCode = entityDetail.PartsCode;
								String isFind = entityDetail.IsFind;
								String checkUser = entityDetail.CheckUser;
								String checkTime = entityDetail.CheckTime;

								String strs1 = partsCode + "," + isFind + ","
										+ checkUser + "," + checkTime;
								strs1 = strs1.replace("-", "===");
								opInfo += strs1 + "-";
							}
						}
					}
					if (opInfo.length() > 0) {
						opInfo = opInfo.substring(0, opInfo.length() - 1);

						what = 2;
						HashMap<String, String> values = new HashMap<String, String>();
						values.put("opInfo", opInfo);
						String keys = "opInfo";
						Request("SavaCheckOp", values, keys);
					}
				}

				// 下载故障信息
				if (work == 2) {
					what = 3;

					page = 1;
					count = 0;
					listSql.clear();
					listSql.add("delete from TbSendRepair");

					HashMap<String, Integer> values = new HashMap<String, Integer>();
					values.put("page", page);
					values.put("pageSize", pageSize);
					String keys = "page,pageSize";
					Request("GetPartsNoRepair", values, keys);
				}

				work++;
				if (work == 3) {
					work = 0;
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("exception...");
			}
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler cardOperationHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String result = (String) msg.obj;
			switch (what) {
			case 1:
				if (result.equals("0")) {
					List<String> listSql = new ArrayList<String>();
					for (TbPartsOpEntity entiry : listTbPartsOp) {
						listSql.add("delete from TbPartsOp where PartsCode='"
								+ entiry.PartsCode + "' and OpType='"
								+ entiry.OpType + "'");
					}
					SqliteHelper.ExceSql(listSql);
					showToast("温馨提醒：上传操作数据成功");
				} else {
					showToast("温馨提醒：有操作数据待上传服务器，请及时上传");
				}
				break;
			case 2:
				if (result.equals("0")) {
					List<String> listSql = new ArrayList<String>();
					listSql.add("delete from TbCheck where CheckCode='"
							+ checkCode + "'");
					listSql.add("delete from TbCheckDetail where CheckCode='"
							+ checkCode + "'");
					SqliteHelper.ExceSql(listSql);
					showToast("温馨提醒：上传盘点数据成功");
				} else {
					showToast("温馨提醒：有盘点数据待上传服务器，请及时上传");
				}
				break;
			case 3:
				DownLoadPartsNoRepair(result);
				break;
			default:
				break;
			}
		};
	};

	private void DownLoadPartsNoRepair(String result) {
		if (result.equals("error")) {
			// showToast("下载故障失败,请检查网络");
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
			// if (r) {
			// showToast(String.format("成功下载%s条故障信息", count));
			// } else {
			// showToast(String.format("下载故障信息失败", count));
			// }
		}
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

			Intent intent = new Intent(MainActivity.this, SysSetActivity.class);
			startActivity(intent);
		}
	};

	public class ListViewAdapter extends BaseAdapter {
		View[] itemViews;

		public ListViewAdapter(String[] itemTitles, int[] itemImageRes) {
			itemViews = new View[itemTitles.length];
			for (int i = 0; i < itemViews.length; ++i) {
				itemViews[i] = makeItemView(itemTitles[i], itemImageRes[i]);
			}
		}

		public int getCount() {
			return itemViews.length;
		}

		public View getItem(int position) {
			return itemViews[position];
		}

		public long getItemId(int position) {
			return position;
		}

		private View makeItemView(String strTitle, int resId) {
			LayoutInflater inflater = (LayoutInflater) MainActivity.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			// 使用View的对象itemView与R.layout.item关联
			View itemView = inflater.inflate(R.layout.listview_menu_item, null);

			// 通过findViewById()方法实例R.layout.item内各组件
			TextView title = (TextView) itemView
					.findViewById(R.id.itemMenuTitleName);
			title.setText(strTitle);

			ImageView image = (ImageView) itemView.findViewById(R.id.itemImage);
			image.setImageResource(resId);
			return itemView;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// if (convertView == null) 此处不做判断，避免listview滚动时显示混乱
			return itemViews[position];
			// return convertView;
		}
	}

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

		HttpTransportSE ht = new HttpTransportSE(WEB_SERVICE_URL);
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

}
