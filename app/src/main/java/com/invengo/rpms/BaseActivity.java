package com.invengo.rpms;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.invengo.rpms.entity.EPCEntity;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import invengo.javaapi.core.BaseReader;
import invengo.javaapi.core.IMessageNotification;
import invengo.javaapi.handle.IMessageNotificationReceivedHandle;
import invengo.javaapi.protocol.IRP1.PowerOff;
import invengo.javaapi.protocol.IRP1.Reader;
import invengo.javaapi.protocol.IRP1.SysConfig_800;

public class BaseActivity extends Activity implements
		IMessageNotificationReceivedHandle {

	protected static final String TAG = BaseActivity.class.getSimpleName();
	protected static final int readCountForMulit = 2;// 读取次数，多标签读取时都到此次数认为有效
	protected static final int readCountForSingle = 5; // 读取次数，单标签读取时都到此次数认为有效
	protected static final int writeUserDataCount = 10;// 写用户数据次数
	protected static Reader reader;

	protected List<EPCEntity> listEPCEntity = new CopyOnWriteArrayList<EPCEntity>();
	protected List<PartsEntity> listPartsEntity = new CopyOnWriteArrayList<PartsEntity>();
	protected MyApp myApp;

	protected String address;
	protected static boolean isReading;
	protected static boolean isConnected=false;

	protected SoundPool sp;// 声明一个SoundPool
	protected int music1;//
	protected int music2;//
	protected int music3;//

	protected static final int START_READ = 0;
	protected static final int STOP_READ = 1;
	public static final int DATA_ARRIVED_PAIRS = 2;
	protected static final int CONNECT = 3;
	protected static final int DATA_ARRIVED_STORAGE_LOCATION = 4;
	protected static final int DATA_ARRIVED_STATION = 5;

	protected boolean isRun = true;
	protected boolean backDown;
	protected long firstTime = 0;
	protected SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private ConnectReaderTask mConnectTask;

	// 断开连接（模块兼容）
	public static void disCont () {
		reader.disConnect();
		isConnected = false;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_stockin);

		myApp = (MyApp) getApplication();
		address = getResources().getString(R.string.ip_address) + ","
				+ getResources().getString(R.string.portNumber);
		if (!isConnected) {
			connectReader(address);
		}
		
		sp = new SoundPool(3, AudioManager.STREAM_SYSTEM, 5);// 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
		music1 = sp.load(this, R.raw.click, 1); // 把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级
		music2 = sp.load(this, R.raw.right, 2); // 把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级
		music3 = sp.load(this, R.raw.error, 3); // 把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级
	}

	public boolean checkNetState(Context context) {
		ConnectivityManager netManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null == netManager) {
			return false;
		}
		NetworkInfo activeNetworkInfo = netManager.getActiveNetworkInfo();
		if (null == activeNetworkInfo) {
			return false;
		}
		return activeNetworkInfo.isConnected();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// mIpText.setText(IntegrateReaderManager.getPortName());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isReading) {
			reader.send(new PowerOff());
		}
	}

	protected void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void messageNotificationReceivedHandle(BaseReader reader,
			IMessageNotification msg) {

	}

	private void connectReader(String address) {
		mConnectTask = new ConnectReaderTask();
		mConnectTask.execute(address);
		// new Thread(new ConnectRunnable(address)).start();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isReading) {
				showToast("请先停止读取");
				return true;
			} else {
				finish();
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	private class ConnectReaderTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected Boolean doInBackground(String... params) {
			String address = params[0];
			reader = new Reader("Reader1", "RS232", address);
			reader.disConnect();
			if (reader.connect()) {
				reader.onMessageNotificationReceived.add(BaseActivity.this);
				isConnected = true;
			} else {
				isConnected = false;
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mConnectTask = null;

			// 连接读写器结果
			Message readMessage = new Message();
			readMessage.what = CONNECT;
			readMessage.obj = result;
			// cardOperationHandler.sendMessage(readMessage);

			if (!result) {
				isConnected = false;
				showToast("打开读取器失败");
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mConnectTask = null;

		}
	}

	protected boolean IsValidEpc(String epc, boolean isReadSingleTag) {
		boolean isExists = false;
		for (EPCEntity entity : listEPCEntity) {
			String old = entity.getEpcData();
			if (epc.equals(old)) {
				isExists = true;
				int oldNumber = entity.getNumber();
				entity.setNumber(oldNumber + 1);
				if (isReadSingleTag) {
					if (entity.getNumber() >= readCountForSingle) {
						return true;
					}
				} else {
					if (entity.getNumber() >= readCountForMulit) {
						return true;
					}
				}
				break;
			}
		}

		if (!isExists) {

			EPCEntity newEntity = new EPCEntity();
			newEntity.setNumber(1);
			newEntity.setEpcData(epc);
			listEPCEntity.add(newEntity);
		}

		return false;
	}

	// 设置功率
	protected boolean setRate (boolean isMin) {
		boolean r = false;
		if (reader.send(new PowerOff())) {
			isReading = false;
			byte pm = 0x65;		// 功率的配置参数
			byte len = 0x02;	// 字长
			byte ant = 0x00;	// 天线端口号
			String rat;			// 功率大小
			if (isMin) {
				rat = SqliteHelper.kvGet("minRate");
				if (rat == null) {
					rat = "5";
					SqliteHelper.kvSet("minRate", rat);
				}
			} else {
				rat = SqliteHelper.kvGet("maxRate");
				if (rat == null) {
					rat = "30";
					SqliteHelper.kvSet("maxRate", rat);
				}
			}
			byte[] d = {len, ant, Byte.parseByte(rat)};
			SysConfig_800 sc = new SysConfig_800(pm, d);
			r = reader.send(sc);
//Log.i("----", "..." + r);
		}
		return r;
	}
	protected boolean setRate () {
		return setRate(false);
	}

	@Deprecated
	private class ConnectRunnable implements Runnable {

		private String hostName;
		private int port;
		private Socket client;
		private int timeout = 1000;
		private OutputStream writer;
		private InputStream reader;
		private byte[] inputMsg = new byte[1024];

		public ConnectRunnable(String address) {
			this.hostName = address.substring(0, address.indexOf(':'));
			this.port = Integer
					.parseInt(address.substring(address.indexOf(':') + 1));
		}

		@Override
		public void run() {
			isRun = true;
			try {
				client = new Socket();
				InetSocketAddress remoteAddr = new InetSocketAddress(
						this.hostName, this.port);
				client.connect(remoteAddr, timeout);

				writer = client.getOutputStream();
				reader = client.getInputStream();

				byte[] msg = new byte[] { 0x00, 0x02, (byte) 0xD2, 0x00,
						(byte) 0xEC, 0x24 };
				writer.write(msg, 0, msg.length);
				writer.flush();

				Thread.sleep(500);

				while (isRun) {
					if (client.isConnected()) {
						int hasData = reader.read(inputMsg, 0, inputMsg.length);
						if (hasData > 0) {
							byte[] temp = new byte[inputMsg.length];
							System.arraycopy(inputMsg, 0, temp, 0,
									inputMsg.length);
							Log.i(getLocalClassName(),
									"Data:"
											+ UtilityHelper
													.convertByteArrayToHexString(temp));
						}
					} else {
						Log.w(getLocalClassName(), "Socket disconnect!");
						isRun = false;
					}
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					if (writer != null) {
						writer.close();
					}
					if (reader != null) {
						reader.close();
					}
					if (client != null) {
						client.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
