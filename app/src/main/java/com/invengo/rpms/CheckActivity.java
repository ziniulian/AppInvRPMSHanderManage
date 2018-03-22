package com.invengo.rpms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.invengo.rpms.R;
import com.invengo.rpms.StockInActivity.PartsAdapter;
import com.invengo.rpms.entity.CheckEntity;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

public class CheckActivity extends Activity {

	ListView lstMenuView;
	Button btnConfig;

	private static SimpleDateFormat f = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");
	private ListView mEpcListView;
	private CheckAdapter mListAdapter;
	private List<Map<String, Object>> listCheckData = new ArrayList<Map<String, Object>>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check);
		
		btnConfig = (Button) findViewById(R.id.btnConfig);
		btnConfig.setOnTouchListener(btnConfigTouchListener);
		btnConfig.setOnClickListener(btnConfigClickListener);
		
		final Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnTouchListener( new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
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


		mEpcListView = (ListView) this.findViewById(R.id.lstCheckView);
		mEpcListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				// TODO Auto-generated method stub
				Map<String, Object> entity = listCheckData.get(arg2);
				final String checkCodeSelected = entity.get("checkCode")
						.toString();
				final String isFinish = entity.get("isFinish").toString();
				Intent intent = new Intent(CheckActivity.this,
						CheckDetailActivity.class);

				// 用Bundle携带数据
				Bundle bundle = new Bundle();
				bundle.putString("checkCode", checkCodeSelected);
				bundle.putString("isFinish", isFinish);
				intent.putExtras(bundle);

				startActivityForResult(intent, 1);
				//startActivity(intent);
			}
		});

		// 创建SimpleAdapter适配器将数据绑定到item显示控件上
		mListAdapter = new CheckAdapter(this, listCheckData,
				R.layout.listview_check_item, new String[] { "sqeNo",
						"checkCode", "checkInfo", "isFinish", "delIcon" },
				new int[] { R.id.sqeNo, R.id.txvCheckCode, R.id.txvCheckInfo,
						R.id.txvCheckStatus, R.id.delImage });
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

		loadData();
	}

	private void loadData() {
		List<CheckEntity> listCheckEntity = SqliteHelper.queryCheck(false);
		int no = 1;
		listCheckData.clear();
		for (CheckEntity checkEntity : listCheckEntity) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("sqeNo", no);
			item.put("checkCode", checkEntity.CheckCode);
			item.put("checkInfo",
					String.format("创建时间:%s", f.format(checkEntity.AddTime)));
			String isFinishStr = "未完成";
			if (checkEntity.IsFinish.equals("Y")) {
				isFinishStr = "已完成";
			}
			item.put("isFinish", isFinishStr);
			Drawable delDr = getResources().getDrawable(
					R.drawable.delete);
			item.put("delIcon", delDr);
			listCheckData.add(item);
			no++;
		}
		mListAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode== 0) {
			loadData();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_create, menu);
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_create:
			Intent intent = new Intent(CheckActivity.this,
					CheckCreateActivity.class);
			startActivityForResult(intent, 1);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private OnTouchListener btnConfigTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
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
			
			Intent intent = new Intent(CheckActivity.this,
					CheckCreateActivity.class);
			startActivityForResult(intent, 1);
		}
	};


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

	public class CheckAdapter extends SimpleAdapter {
		List<Map<String, Object>> mdata;

		public CheckAdapter(Context context, List<Map<String, Object>> data,
				int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
			this.mdata = data;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mdata.size();
		}

		@Override
		public Map<String, Object> getItem(int position) {
			// TODO Auto-generated method stub
			return mdata.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = LinearLayout.inflate(getBaseContext(),
						R.layout.listview_check_item, null);
			}

			ImageView imgDel = (ImageView) convertView
					.findViewById(R.id.delImage);
			// 设置回调监听
			imgDel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Map<String, Object> entity = listCheckData.get(position);
					final String checkCode = entity.get("checkCode").toString();
					final String isFinish = entity.get("isFinish").toString();
					if (isFinish.equals("已完成")) {
						showToast("该任务已经完成盘点，无法删除");

					} else {

						Builder builder = new Builder(CheckActivity.this);
						builder.setMessage("确认删除该盘点任务吗？");
						builder.setTitle("提示");
						builder.setPositiveButton("确认",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										boolean result = SqliteHelper
												.DeleteCheck(checkCode);
										if (result) {
											showToast("删除成功");
											listCheckData.remove(position);
											mListAdapter.notifyDataSetChanged();
										} else {
											showToast("删除失败");
										}
										
										dialog.dismiss();
									}
								});

						builder.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});

						builder.create().show();
					}

				}

			});

			return super.getView(position, convertView, parent);
		}
	}
}
