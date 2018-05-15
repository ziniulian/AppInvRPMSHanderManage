package com.invengo.rpms.util;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.invengo.rpms.bean.BaseBean;
import com.invengo.rpms.bean.CheckDetailEntity;
import com.invengo.rpms.bean.CheckEntity;
import com.invengo.rpms.bean.Parts;
import com.invengo.rpms.bean.SendRepair;
import com.invengo.rpms.bean.THDSEntity;
import com.invengo.rpms.bean.TableVersion;
import com.invengo.rpms.bean.TbCodeEntity;
import com.invengo.rpms.bean.TbPartsOpEntity;
import com.invengo.rpms.bean.TbStorageLocationEntity;
import com.invengo.rpms.bean.UserEntity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 数据库同步
 * Created by Administrator on 2018/4/4.
 */

public class SynchroDbRa implements Runnable {
	public static final int SYN_OK = 501;		// 同步成功
	public static final int SYN_ERR = 502;		// 同步失败

	private static SynchroDbRa self = null;
	private boolean redo = true;
	private Thread t = null;
	private Handler hd = null;
	private Gson gson = new Gson();

	private WebSrv ws = null;
	private String ip = "192.168.1.131";
	private String port = "8000";
	private String srvNam = "Service.svc";
	private String npc = "http://tempuri.org/";

	private Type clsCode = new TypeToken<List<TbCodeEntity>>(){}.getType();
	private Type clsPart = new TypeToken<List<Parts>>(){}.getType();
	private Type clsThds = new TypeToken<List<THDSEntity>>(){}.getType();
	private Type clsUser = new TypeToken<List<UserEntity>>(){}.getType();
	private Type clsSl = new TypeToken<List<TbStorageLocationEntity>>(){}.getType();
	private Type clsPnr = new TypeToken<List<SendRepair>>(){}.getType();
	private Type clsTbv = new TypeToken<List<TableVersion>>(){}.getType();

	private String[] GetTableVersionOpKeys = new String[] {"tableName", "versionsFrom", "versionsTo"};
	private String[] SavaPartsOpKeys = new String[] {"opInfo"};
	private String[] SavaCheckOpKeys = new String[] {"checkInfo", "checkDetailInfo"};
	private String[] CoverKeys = new String[] {"page", "pageSize"};

	private SynchroDbRa () {}

	public static void oneStart() {
		if (self == null) {
			self = new SynchroDbRa();
			self.start();
		}
	}

	public static void oneStop() {
		if (self != null) {
			self.stop();
			self = null;
		}
	}

	// 立即同步数据，并设置URL，返回初次同步的结果
	public static void reset(String ip, String port, Handler hd) {
		oneStop();
		self = new SynchroDbRa();
		if (ip != null) {
			self.ip = ip;
		}
		if (port != null) {
			self.port = port;
		}
		if (hd != null) {
			self.hd = hd;
		}
		self.start();
	}

	public static void reset(Handler hd) {
		if (self == null) {
			reset(null, null, hd);
		} else {
			reset(self.ip, self.port, hd);
		}
	}

	public static void reset() {
		reset(null);
	}

	public void start() {
		if (t == null) {
			redo = true;
			if (ws == null) {
				ws = new WebSrv("http://" + ip + ":" + port + "/" + srvNam, npc);
			}
			t = new Thread(this);
			t.start();
		}
	}

	public void stop() {
		if (t != null) {
			redo = false;
			try {
				t.interrupt();
				t.join();
				t = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		String res;
		while (redo) {
			try {
				res = ws.qry("HelloWorld");
				if (res.equals("HelloWorld!")) {
					pushSynDat();
					matchTabVer();
					sendMsg(SYN_OK);
				} else {
					sendMsg(SYN_ERR);
				}
				Thread.sleep(120000);	// 休眠两分钟
			} catch (Exception e) {
				redo = false;
				sendMsg(SYN_ERR);
			}
		}
	}

	// 发送消息
	private void sendMsg (int sta) {
		if (hd != null) {
			hd.sendMessage(hd.obtainMessage(sta));
			hd = null;
		}
	}

	// 获取服务端数据表版本
	private HashMap<String, int[]> getSrvTabVers() {
		HashMap<String, int[]> r = new HashMap<String, int[]>();
		String res = ws.qry("GetTableVersion");
		if (res != null) {
			List<TableVersion> ls = gson.fromJson(res, clsTbv);
			for (int i = 0; i < ls.size(); i ++) {
				TableVersion tv = ls.get(i);
				r.put(tv.TableName, new int[]{0, tv.Version});
			}
		}
		return r;
	}

	// 比对数据表版本变化，并同步
	private HashMap<String, int[]> matchTabVer() {
		HashMap<String, int[]> dat = getSrvTabVers();
		SqliteHelper.getLocalTabVers(dat);
		int[] v;

		for (String k: dat.keySet()) {
			v = dat.get(k);
			if (v[0] == 0) {
				if (k.equals("CODE")) {
					cover(clsCode, "GetCodeByPage");
				} else if (k.equals("PARTS")) {
					cover(clsPart, "GetPartsByPage");
				} else if (k.equals("THDS")) {
					cover(clsThds, "GetTHDSByPage");
				} else if (k.equals("USER")) {
					cover(clsUser, "GetUserByPage");
				} else if (k.equals("STORAGELOCATION")) {
					cover(clsSl, "GetStorageLocationByPage");
				} else if (k.equals("PARTSNOREPAIR")) {
					cover(clsPnr, "GetPartsNoRepair");
				}
			} else if (v[0] < v[1]) {
				String r = ws.qry("GetTableVersionOP", GetTableVersionOpKeys, new Object[] {k, v[0], v[1]});
				Log.i("---", r);

				// 暂时用覆盖的方式解决版本问题，待 GetTableVersionOP 功能完善后再重新调整。
				if (k.equals("CODE")) {
					cover(clsCode, "GetCodeByPage");
				} else if (k.equals("PARTS")) {
					cover(clsPart, "GetPartsByPage");
				} else if (k.equals("THDS")) {
					cover(clsThds, "GetTHDSByPage");
				} else if (k.equals("USER")) {
					cover(clsUser, "GetUserByPage");
				} else if (k.equals("STORAGELOCATION")) {
					cover(clsSl, "GetStorageLocationByPage");
				} else if (k.equals("PARTSNOREPAIR")) {
					cover(clsPnr, "GetPartsNoRepair");
				}
			}
		}

		SqliteHelper.updLocalTabVer(dat);
		return dat;
	}

	// 推送本地数据库的更新内容
	private void pushSynDat () {
		String tim = ws.qry("GetSysTime");	// 获取服务器的时间
		String min = SqliteHelper.getSynTim();	// 获取同步时间
		String max = SqliteHelper.setSynTim(tim);	// 设置同步时间
		pushSynOp ();	// 上传操作记录
		pushSynCheck ();	// 上传盘点记录
	}

	// 上传盘点记录
	private void pushSynCheck () {
		List<CheckEntity> cs = SqliteHelper.queryCheck(true);
		List<CheckDetailEntity> cds = new ArrayList<CheckDetailEntity>();
		if (cs.size() > 0) {
			List<String> sql = new ArrayList<String>();
			for (CheckEntity c : cs) {
				List<CheckDetailEntity> t = SqliteHelper.queryCheckDetailByCheckCode(c.CheckCode);
				for (CheckDetailEntity cd : t) {
					cds.add(cd);
				}
				sql.add("delete from TbCheck where CheckCode='" + c.CheckCode + "'");
				sql.add("delete from TbCheckDetail where CheckCode='" + c.CheckCode + "'");
			}

			// 数据上传服务器
			if (ws.qry("SavaCheckOp", SavaCheckOpKeys, new Object[] {gson.toJson(cs), gson.toJson(cds)}).equals("0")) {
				SqliteHelper.ExceSql(sql);	// 删除本地数据
			}
		}
	}

	// 上传操作记录
	private void pushSynOp () {
		List<TbPartsOpEntity> ops = SqliteHelper.queryOpRecord();
		if (ops.size() > 0) {
			StringBuilder sb = new StringBuilder();
			List<String> sql = new ArrayList<String>();
			for (TbPartsOpEntity op : ops) {
				sb.append(op.getPushStr());
				sb.append("-");
				sql.add(op.getDelSql());
			}
			sb.deleteCharAt(sb.length() - 1);

			// 数据上传服务器
			if (ws.qry("SavaPartsOp", SavaPartsOpKeys, new Object[] {sb.toString()}).equals("0")) {
				SqliteHelper.ExceSql(sql);	// 删除本地数据
			}
		}
	}

	// 覆盖数据字典信息
	private void cover (Type typ, String srvNam) {
		boolean b = true;
		String res;
		int i = 1;
		int j;
		int n = 100;
		List<String> sql = null;
		while (b) {
			res = ws.qry(srvNam, CoverKeys, new Object[] {i, n});
			List<BaseBean> ls = gson.fromJson(res, typ);
			if (sql == null && ls.size() > 0) {
				sql = new ArrayList<String>();
				sql.add(ls.get(0).getDelAllSql());
			}
			for (j = 0; j < ls.size(); j ++) {
				sql.add(ls.get(j).getAddSql());
			}
			if (j < n) {
				b = false;
			} else {
				i ++;
			}
		}
		if (sql != null) {
			SqliteHelper.ExceSql(sql);
		}
	}

}
