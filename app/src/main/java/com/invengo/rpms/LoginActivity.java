package com.invengo.rpms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.invengo.rpms.entity.TbCodeEntity;
import com.invengo.rpms.entity.UserEntity;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.SynchroDbRa;
import com.invengo.rpms.util.UtilityHelper;

import java.util.Date;

public class LoginActivity extends Activity {

	Button btnLogin;
	Button btnConfig;
	EditText edtUserId;
	EditText edtPassword;

	private SharedPreferences sp = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		sp = this.getSharedPreferences("userinfo", Context.MODE_PRIVATE);

		// 数据库初始化检查
		SqliteHelper.InintDatabase();

		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLogin.setOnTouchListener(btnLoginTouchListener);
		btnLogin.setOnClickListener(btnLoginClickListener);
		
		btnConfig = (Button) findViewById(R.id.btnConfig);
		btnConfig.setOnTouchListener(btnConfigTouchListener);
		btnConfig.setOnClickListener(btnConfigClickListener);

		edtUserId = (EditText) findViewById(R.id.edtUserId);
		edtPassword = (EditText) findViewById(R.id.edtPassword);
		edtUserId.setText(sp.getString("uname", null));

		checkTim();
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
			Intent intent = new Intent(LoginActivity.this,
					SysSetActivity.class);
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
				btnConfig.setBackgroundResource(R.color.yellow);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnConfig.setBackgroundResource(R.color.yellow);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnConfig.setBackgroundResource(R.color.chinacartred);
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
			
			Intent intent = new Intent(LoginActivity.this,
					SysSetActivity.class);
			startActivity(intent);
		}
	};


	private OnTouchListener btnLoginTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				btnLogin.setBackgroundResource(R.color.yellow);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				btnLogin.setBackgroundResource(R.color.yellow);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				btnLogin.setBackgroundResource(R.color.chinacartred);
				break;
			}
			default:

				break;
			}
			return false;
		}
	};

	private OnClickListener btnLoginClickListener = new OnClickListener() {
		public void onClick(View v) {

			String UserIdStr = edtUserId.getText().toString().trim();
			if (UserIdStr.length() == 0) {
				showToast("请输入用户名");
				return;
			}

			String passwordStr = edtPassword.getText().toString().trim();
			if (passwordStr.length() == 0) {
				showToast("请输入密码");
				return;
			}

			UserEntity entityUser = SqliteHelper.queryUserById(UserIdStr);
			if (entityUser == null) {
				showToast("登录失败");
			} else {
				String passwordSha1 = UtilityHelper.getSha1(passwordStr);
				if (!entityUser.password.equals(passwordSha1)) {
					showToast("密码错误");
				} else {
					if (entityUser.isEnable.equals("N")) {
						showToast("用户被禁用");
					} else {
						
						Editor editor = sp.edit();
						editor.putString("uname", UserIdStr);
						editor.commit();

						for (TbCodeEntity entity : SqliteHelper
								.queryDbCodeByType("01")) {
							if (entity.dbCode.equals(entityUser.deptCode)) {
								entityUser.deptName = entity.dbName;
								break;
							}
						}

						for (TbCodeEntity entity : SqliteHelper
								.queryDbCodeByType("02")) {
							if (entity.dbCode.equals(entityUser.groupCode)) {
								entityUser.groupName = entity.dbName;
								break;
							}
						}

						for (TbCodeEntity entity : SqliteHelper
								.queryDbCodeByType("03")) {
							if (entity.dbCode.equals(entityUser.postCode)) {
								entityUser.postName = entity.dbName;
								break;
							}
						}

						MyApp myApp = (MyApp) getApplication();
						myApp.setUserId(UserIdStr);
						myApp.setUserName(entityUser.userName);
						myApp.setDeptCode(entityUser.deptCode);
						myApp.setDeptName(entityUser.deptName);
						myApp.setGroupCode(entityUser.groupCode);
						myApp.setGroupName(entityUser.groupName);
						myApp.setPostCode(entityUser.postCode);
						myApp.setPostName(entityUser.postName);
						myApp.setTel(entityUser.tel);
						
						Intent intent = new Intent(LoginActivity.this,
								MainActivity.class);
						startActivity(intent);
						finish();
					}
				}
			}
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
				SynchroDbRa.oneStop();	// 关闭同步线程
				finish();
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	// 时间校验
	private void checkTim() {
		Date dt = new Date(SqliteHelper.getSynTim());
		Date dd = new Date("2018/04/12 12:12:12");	// 标准时间
		Date dn = new Date();
//Log.i("---", dt.toString() + " , " + dd.toString() + " , " + dn.toString());
		if (dd.compareTo(dn) > 0 || dt.compareTo(dn) > 0) {
			// 时间错误提示
			AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.AppTheme);
			builder.setTitle("警告：");
			builder.setMessage("警告：\n\n系统时间出现严重误差！\n\n请修改系统时间后重启软件！\n\n");

			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
					startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialogInterface) {
					finish();
					startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
				}
			});
			builder.show();
		} else {
			SynchroDbRa.oneStart();	// 开启同步线程
		}
	}

}
