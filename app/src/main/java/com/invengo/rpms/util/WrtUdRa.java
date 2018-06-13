package com.invengo.rpms.util;

import android.os.Handler;

import invengo.javaapi.core.IMessage;
import invengo.javaapi.core.MemoryBank;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.Reader;
import invengo.javaapi.protocol.IRP1.WriteUserData_6C;

import static com.invengo.rpms.util.WrtRa.WRT_OK;
import static com.invengo.rpms.util.WrtRa.WRT_UD_ERR;

/**
 * Created by LZR on 2018/6/5.
 */

public class WrtUdRa implements Runnable {
	private static final byte antenna = 0x01;
	private static byte[] pwd = new byte[] {0x20, 0x26, 0x31, 0x07};
	private Reader rd;
	private String[] o;
	private byte[] tid;
	private byte[] ud;
	private int a1;
	private int a2;
	private Handler hd;

	public WrtUdRa (Reader rd, String[] obj, Handler hd, int a1, int a2) {
		this.rd = rd;
		this.o = obj;
		this.tid = Util.convertHexStringToByteArray(obj[0]);
		this.ud = Util.convertHexStringToByteArray(obj[2]);
		this.hd = hd;
		this.a1 = a1;
		this.a2 = a2;
	}

	@Override
	public void run() {
		// 写入用户区
		IMessage msg = new WriteUserData_6C(antenna, pwd, (byte) 0, ud, tid, MemoryBank.TIDMemory);
		if (rd.send(msg)) {
			hd.sendMessage(hd.obtainMessage(WRT_OK, a1, a2, o));
		} else {
			hd.sendMessage(hd.obtainMessage(WRT_UD_ERR, a1, a2, o));
		}
	}
}
