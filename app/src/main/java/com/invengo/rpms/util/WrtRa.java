package com.invengo.rpms.util;

import android.os.Handler;
import android.util.Log;

import invengo.javaapi.core.IMessage;
import invengo.javaapi.core.MemoryBank;
import invengo.javaapi.protocol.IRP1.AccessPwdConfig_6C;
import invengo.javaapi.protocol.IRP1.LockMemoryBank_6C;
import invengo.javaapi.protocol.IRP1.Reader;
import invengo.javaapi.protocol.IRP1.WriteEpc;
import invengo.javaapi.protocol.IRP1.WriteUserData_6C;

/**
 * 写标签线程
 * Created by Administrator on 2018/3/27.
 */

public class WrtRa implements Runnable {
	public static final int WRT_OK = 200;	// 写入成功
	public static final int WRT_EPC_ERR = 201;	// EPC写入失败
	public static final int WRT_UD_ERR = 202;	// 用户区写入失败
	public static final int WRT_PWD_ERR = 203;	// 改密码失败
	public static final int WRT_LOCK_ERR = 204;	// 锁定密码失败
	private static final byte antenna = 0x01;
	private static byte[] defaulPwd = new byte[] {0, 0, 0, 0};
	private static byte[] pwd = new byte[] {0x20, 0x26, 0x31, 0x07};
	private Reader rd;
	private byte[] tid;
	private byte[] epc;
	private byte[] ud;
	private boolean isNew = false;
	private Handler hd;

	public WrtRa (Reader rd, byte[] tid, byte[] epc, byte[] ud, boolean n, Handler hd) {
		this.rd = rd;
		this.tid = tid;
		this.epc = epc;
		this.ud = ud;
		this.isNew = n;
		this.hd = hd;
	}

	@Override
	public void run() {
		IMessage msg;
		if (isNew) {
			// 新标签写入EPC
			msg = new WriteEpc(antenna, defaulPwd, epc, tid, MemoryBank.TIDMemory);
			if (rd.send(msg)) {
//Log.i("-----", "new EPC");
				// 修改密码
				msg = new AccessPwdConfig_6C(antenna, defaulPwd, pwd, tid, MemoryBank.TIDMemory);
				if (rd.send(msg)) {
//Log.i("-----", "new PWD");
					// 锁密码
					msg = new LockMemoryBank_6C(antenna, pwd, (byte)0, (byte)2, tid, MemoryBank.TIDMemory);
					if (rd.send(msg)) {
//Log.i("-----", "new LOCK");
						// 写入用户区
						msg = new WriteUserData_6C(antenna, pwd, (byte)0, ud, tid, MemoryBank.TIDMemory);
						if (rd.send(msg)) {
//Log.i("-----", "new UD");
							hd.sendMessage(hd.obtainMessage(WRT_OK));
						} else {
							hd.sendMessage(hd.obtainMessage(WRT_UD_ERR));
						}
					} else {
						hd.sendMessage(hd.obtainMessage(WRT_LOCK_ERR));
					}
				} else {
					hd.sendMessage(hd.obtainMessage(WRT_PWD_ERR));
				}
			} else {
				hd.sendMessage(hd.obtainMessage(WRT_EPC_ERR));
			}
		} else {
			// 旧标签写入EPC
			msg = new WriteEpc(antenna, pwd, epc, tid, MemoryBank.TIDMemory);
			if (rd.send(msg)) {
//Log.i("-----", "old EPC");
				// 写入用户区
				msg = new WriteUserData_6C(antenna, pwd, (byte)0, ud, tid, MemoryBank.TIDMemory);
				if (rd.send(msg)) {
//Log.i("-----", "old UD");
					hd.sendMessage(hd.obtainMessage(WRT_OK));
				} else {
					hd.sendMessage(hd.obtainMessage(WRT_UD_ERR));
				}
			} else {
				hd.sendMessage(hd.obtainMessage(WRT_EPC_ERR));
			}
		}
	}
}
