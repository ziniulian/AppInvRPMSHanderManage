package com.invengo.rpms.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.invengo.rpms.bean.BaseBean;
import com.invengo.rpms.bean.Parts;
import com.invengo.rpms.bean.THDSEntity;
import com.invengo.rpms.bean.TableVersion;
import com.invengo.rpms.bean.TbCodeEntity;
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
	private static SynchroDbRa self = null;
	private boolean redo = true;
	private Thread t = null;
	private Gson gson=new Gson();

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

	private WebSrv ws = new WebSrv(
		"http://192.168.1.131:8000/Service.svc",
		"http://tempuri.org/"
	);

	private Type clsCode = new TypeToken<List<TbCodeEntity>>(){}.getType();
	private Type clsPart = new TypeToken<List<Parts>>(){}.getType();
	private Type clsThds = new TypeToken<List<THDSEntity>>(){}.getType();
	private Type clsUser = new TypeToken<List<UserEntity>>(){}.getType();
	private Type clsSl = new TypeToken<List<TbStorageLocationEntity>>(){}.getType();
	private Type clsTbv = new TypeToken<List<TableVersion>>(){}.getType();

	private String[] GetTableVersionOpKeys = new String[] {"tableName", "versionsFrom", "versionsTo"};
	private String[] SavePartsTagByJsonKeys = new String[] {"strJson"};
	private String[] SavaPartsOpKeys = new String[] {"opInfo"};
	private String[] CoverKeys = new String[] {"page", "pageSize"};

	public void start() {
		if (t == null) {
			redo = true;
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
//Log.i("---", "同步开始");
					pushSynDat();
					matchTabVer();
//Log.i("---", "同步完成");
				}
				Thread.sleep(120000);	// 休眠两分钟
			} catch (Exception e) {
				redo = false;
			}
		}
	}

	// 获取服务端数据表版本
	private HashMap<String, int[]> getSrvTabVers() {
		HashMap<String, int[]> r = new HashMap<String, int[]>();
		String res = ws.qry("GetTableVersion");
		if (res != null) {
//Log.i("----", res);
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
				}
			}
		}

		SqliteHelper.updLocalTabVer(dat);
		return dat;
	}

	// 推送本地数据库的更新内容
	private void pushSynDat () {
		// 获取服务器的时间（减两分钟）
		String [] ts = ws.qry("GetSysTime").split(":");
		int m = Integer.parseInt(ts[1]) - 2;
		StringBuilder tim = new StringBuilder(ts[0]);
		if (m < 10) {
			tim.append(":0");
		} else {
			tim.append(":");
		}
		tim.append(m);
		tim.append(":");
		tim.append(ts[2]);

		// 获取同步时间
		String min = SqliteHelper.getSynTim();
//Log.i("---", min);

		// 设置同步时间
		String max = SqliteHelper.setSynTim(tim.toString());
//Log.i("---", max);

		// 获取未同步的已删除配件信息
		String[] ds = SqliteHelper.getSynDel(min, max);

		if (ds.length > 0) {
//			List<TbPartsOpEntity> ds = new ArrayList<TbPartsOpEntity>();
//			for (int i = 0; i < cds.length; i ++) {
//				TbPartsOpEntity tpo = new TbPartsOpEntity();
//				tpo.PartsCode = cds[i];
//				tpo.OpType = "14";
//				ds.add(tpo);
//			}
//			String json = gson.toJson(ds);
//Log.i("---", json);

//			StringBuilder sb = new StringBuilder();
//			sb.append("[{\"OpType\":\"14\",\"PartsCode\":\"");
//			sb.append(ds[0]);
//			sb.append("\"}");
//			for (int i = 1; i < ds.length; i ++) {
//				sb.append(",{\"OpType\":\"14\",\"PartsCode\":\"");
//				sb.append(ds[i]);
//				sb.append("\"}");
//			}
//			sb.append("]");
//Log.i("---", sb.toString());

			StringBuilder sb = new StringBuilder();
			sb.append(ds[0]);
			sb.append(",14,");
			for (int i = 1; i < ds.length; i ++) {
				sb.append("-");
				sb.append(ds[i]);
				sb.append(",14,");
			}
//Log.i("---", sb.toString());

			// 上传服务器
			if (ws.qry("SavaPartsOp", SavaPartsOpKeys, new Object[] {sb.toString()}).equals("0")) {
				// TODO: 2018/4/11 删除本地的冗余数据
//				Log.i("---", "SavaPartsOp OK!");
			}
		}

		// 获取未同步的最后操作配件信息
		List<Parts> ps = SqliteHelper.getSynAdd(min, max);
		if (ps.size() > 0) {
			String json = gson.toJson(ps);
//Log.i("---", json);

			// 上传服务器
			if (ws.qry("SavePartsTagByJson", SavePartsTagByJsonKeys, new Object[] {json}).equals("true")) {
				// TODO: 2018/4/11 删除本地的冗余数据
//				Log.i("---", "SavePartsTagByJson OK!");
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
//Log.i("---S---", srvNam);
//for (i = 0; i < sql.size(); i ++) {
//	Log.i("\t---", sql.get(i));
//}
//Log.i("---E---", srvNam);
			SqliteHelper.ExceSql(sql);
		}
	}

}
