package com.invengo.rpms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.invengo.rpms.R;
import com.invengo.rpms.entity.CheckEntity;
import com.invengo.rpms.entity.TbCodeEntity;
import com.invengo.rpms.entity.UserEntity;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

public class CheckCreateActivity extends Activity {

	Button btnCreate;
	LinearLayout T5_CheckBoxList;
	LinearLayout Factory_CheckBoxList;
	LinearLayout PartsSort_CheckBoxList;
	LinearLayout HostType_CheckBoxList;
	LinearLayout PartsName_CheckBoxList;
	EditText edtRemark;

	private List<CheckBox> checkBoxList_T5 = new ArrayList<CheckBox>();
	private List<CheckBox> checkBoxList_Factory = new ArrayList<CheckBox>();
	private List<CheckBox> checkBoxList_PartsSort = new ArrayList<CheckBox>();
	private List<CheckBox> checkBoxList_HostType = new ArrayList<CheckBox>();
	private List<CheckBox> checkBoxList_PartsName = new ArrayList<CheckBox>();
	private List<String> listSortCodeSelected = new ArrayList<String>();
	private String hostCodeSelected;
	private List<TbCodeEntity> listPartsName;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_create);

		btnCreate = (Button) findViewById(R.id.btnCreate);
		btnCreate.setOnTouchListener(btnCreateTouchListener);
		btnCreate.setOnClickListener(btnCreateClickListener);
		
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


		List<TbCodeEntity> listCode = SqliteHelper.queryDbCodeByType("05");
		T5_CheckBoxList = (LinearLayout) findViewById(R.id.T5_CheckBoxList);
		for (TbCodeEntity entity : listCode) {
			CheckBox checkBox = new CheckBox(this);
			checkBox.setText(entity.dbCode);
			checkBox.setTag(entity.dbCode);
			T5_CheckBoxList.addView(checkBox);
			checkBoxList_T5.add(checkBox);
		}

		listCode = SqliteHelper.queryDbCodeByType("06");
		Factory_CheckBoxList = (LinearLayout) findViewById(R.id.Factory_CheckBoxList);
		for (TbCodeEntity entity : listCode) {
			CheckBox checkBox = new CheckBox(this);
			checkBox.setText(entity.dbName);
			checkBox.setTag(entity.dbCode);
			Factory_CheckBoxList.addView(checkBox);
			checkBoxList_Factory.add(checkBox);
		}

		listCode = SqliteHelper.queryDbCodeByType("07");
		PartsSort_CheckBoxList = (LinearLayout) findViewById(R.id.PartsSort_CheckBoxList);
		for (TbCodeEntity entity : listCode) {
			CheckBox checkBox = new CheckBox(this);
			checkBox.setText(entity.dbName);
			checkBox.setTag(entity.dbCode);
			// 给CheckBox设置事件监听
			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						String sortCodeSelected = buttonView.getTag()
								.toString();
						listSortCodeSelected.add(sortCodeSelected);

					} else {
						String sortCodeSelected = buttonView.getTag()
								.toString();
						listSortCodeSelected.remove(sortCodeSelected);

					}
				}
			});

			PartsSort_CheckBoxList.addView(checkBox);
			checkBoxList_PartsSort.add(checkBox);
		}

		listPartsName = SqliteHelper.queryDbCodeByType("09");
		PartsName_CheckBoxList = (LinearLayout) findViewById(R.id.PartsName_CheckBoxList);

		listCode = SqliteHelper.queryDbCodeByType("08");
		HostType_CheckBoxList = (LinearLayout) findViewById(R.id.HostType_CheckBoxList);
		for (TbCodeEntity entity : listCode) {
			CheckBox checkBox = new CheckBox(this);
			checkBox.setText(entity.dbName);
			checkBox.setTag(entity.dbCode);
			// 给CheckBox设置事件监听
			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						String hostCodeSelected = buttonView.getTag()
								.toString();
						for (TbCodeEntity entity : listPartsName) {
							if (entity.dbTypeBeyond.equals("08,07")
									&& entity.dbCodeBeyond
											.indexOf(hostCodeSelected) == 0) {
								for (String sortCodeSelected : listSortCodeSelected) {
									if (entity.dbCodeBeyond
											.indexOf(sortCodeSelected) > 0) {
										CheckBox checkBox1 = new CheckBox(
												CheckCreateActivity.this);
										checkBox1.setText(entity.dbName);
										checkBox1.setTag(entity.dbCode);
										PartsName_CheckBoxList
												.addView(checkBox1);
										checkBoxList_PartsName.add(checkBox1);
									}
								}
							}
						}
					} else {
						String hostCodeSelected = buttonView.getTag()
								.toString();

						for (TbCodeEntity entity : listPartsName) {
							if (entity.dbTypeBeyond.equals("08,07")
									&& entity.dbCodeBeyond
									.indexOf(hostCodeSelected) == 0) {

								String checkName = entity.dbName;
								String checkCode = entity.dbCode;
								int count = PartsName_CheckBoxList
										.getChildCount();
								for (int i = 0; i < count; i++) {
									CheckBox checkBox1 = (CheckBox) PartsName_CheckBoxList
											.getChildAt(i);
									if (checkBox1 != null) {
										if (checkBox1.getText().equals(
												checkName)
												&& checkBox1.getTag()
														.toString()
														.equals(checkCode)) {
											PartsName_CheckBoxList
													.removeView(checkBox1);
											break;
										}
									}
								}
								for (CheckBox c : checkBoxList_PartsName) {
									if (c.getText().equals(checkName)
											&& c.getTag().toString()
													.equals(checkCode)) {
										checkBoxList_PartsName.remove(c);
										break;
									}
								}
							}
						}
					}
				}
			});
			HostType_CheckBoxList.addView(checkBox);
			checkBoxList_HostType.add(checkBox);
		}

		edtRemark = (EditText) findViewById(R.id.edtRemark);
	}

	private OnTouchListener btnCreateTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnCreate.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnCreate.setBackgroundResource(R.drawable.btnclick);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnCreate.setBackgroundResource(R.drawable.btnnormal);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnCreateClickListener = new OnClickListener() {
		public void onClick(View v) {

			String t5Str = "";
			for (CheckBox checkBox : checkBoxList_T5) {
				if (checkBox.isChecked()) {
					t5Str += checkBox.getTag().toString();
				}
			}
			if (t5Str.equals(""))
				t5Str = "0";

			String factoryStr = "";
			for (CheckBox checkBox : checkBoxList_Factory) {
				if (checkBox.isChecked()) {
					factoryStr += checkBox.getTag().toString();
				}
			}
			if (factoryStr.equals(""))
				factoryStr = "0";

			String partsSortStr = "";
			for (CheckBox checkBox : checkBoxList_PartsSort) {
				if (checkBox.isChecked()) {
					partsSortStr += checkBox.getTag().toString();
				}
			}
			if (partsSortStr.equals(""))
				partsSortStr = "0";

			String hostTypeStr = "";
			for (CheckBox checkBox : checkBoxList_HostType) {
				if (checkBox.isChecked()) {
					hostTypeStr += checkBox.getTag().toString();
				}
			}
			if (hostTypeStr.equals(""))
				hostTypeStr = "0";

			String partsNameStr = "";
			for (CheckBox checkBox : checkBoxList_PartsName) {
				if (checkBox.isChecked()) {
					partsNameStr += checkBox.getTag().toString();
				}
			}
			if (partsNameStr.equals(""))
				partsNameStr = "0";

			SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
			String checkCode = f.format(new Date());
			String checkPartsType = t5Str + "-" + factoryStr + "-"
					+ partsSortStr + "-" + hostTypeStr + "-" + partsNameStr;
			String remark = edtRemark.getText().toString().trim();
			MyApp myApp = (MyApp) getApplication();
			String user = myApp.getUserId();

			CheckEntity entity = new CheckEntity();
			entity.CheckCode = checkCode;
			entity.CheckPartsType = checkPartsType;
			entity.Remark = remark;
			entity.IsFinish = "N";
			entity.AddTime = new Date(System.currentTimeMillis());
			entity.AddUser = user;

			boolean result = SqliteHelper.CreateCheck(entity);
			if (result) {
				showToast("创建成功");
				finish();
			} else {
				showToast("无满足条件配件，创建失败");
			}
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

}
