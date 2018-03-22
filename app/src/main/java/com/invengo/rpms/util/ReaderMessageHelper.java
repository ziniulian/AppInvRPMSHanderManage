package com.invengo.rpms.util;

import com.invengo.rpms.entity.OpType;

import invengo.javaapi.core.MemoryBank;
import invengo.javaapi.core.Util;
import invengo.javaapi.protocol.IRP1.AccessPwdConfig_6C;
import invengo.javaapi.protocol.IRP1.ReadUserData_6C;
import invengo.javaapi.protocol.IRP1.WriteEpc;
import invengo.javaapi.protocol.IRP1.WriteUserData_6C;

public class ReaderMessageHelper {

	private final static byte antenna = 0x01;

	// 读配件标签用户数据消息
	public static ReadUserData_6C GetReadUserData_6C(String epc) {

		byte ptr = 0;
		byte length = 2;
		byte[] bytesTagId = Util.convertHexStringToByteArray(epc);

		ReadUserData_6C msg = new ReadUserData_6C(antenna, ptr, length,
				bytesTagId, MemoryBank.EPCMemory);
		msg.setIsReturn(true);// 设定需要访问值
		return msg;
	}

	// 写配件标签用户数据消息
	public static WriteUserData_6C GetWriteUserData_6C(String epc, int opType) {

		byte[] psw = new byte[4];
		psw[0] = 0x20;
		psw[1] = 0x26;
		psw[2] = 0x31;
		psw[3] = 0x07;

		byte ptr = 0;
		byte[] bytesUserData = getUserData(opType);
		byte[] bytesTagId = Util.convertHexStringToByteArray(epc);

		WriteUserData_6C msg = new WriteUserData_6C(antenna, psw, ptr,
				bytesUserData, bytesTagId, MemoryBank.EPCMemory);
		msg.setIsReturn(true);// 设定需要访问值
		return msg;
	}

	// 写配件标签用户数据消息
	public static WriteUserData_6C GetWriteUserData_6CForDefaultPsw(String epc,
			int opType) {

		byte[] psw = new byte[4];
		psw[0] = 0x00;
		psw[1] = 0x00;
		psw[2] = 0x00;
		psw[3] = 0x00;

		byte ptr = 0;
		byte[] bytesUserData = getUserData(opType);
		byte[] bytesTagId = Util.convertHexStringToByteArray(epc);

		WriteUserData_6C msg = new WriteUserData_6C(antenna, psw, ptr,
				bytesUserData, bytesTagId, MemoryBank.EPCMemory);
		msg.setIsReturn(true);// 设定需要访问值
		return msg;
	}

	// 写配件标签epc消息
	public static WriteEpc GetWriteEpc_6C(String epc, String byEpc) {

		byte[] psw = new byte[4];
		psw[0] = 0x20;
		psw[1] = 0x26;
		psw[2] = 0x31;
		psw[3] = 0x07;

		byte ptr = 0;
		byte[] bytesEpc = Util.convertHexStringToByteArray(epc);
		byte[] bytesTagId = Util.convertHexStringToByteArray(byEpc);

		WriteEpc msg = new WriteEpc(antenna, psw, bytesEpc, bytesTagId,
				MemoryBank.EPCMemory);
		msg.setIsReturn(true);// 设定需要访问值
		return msg;
	}

	// 写配件标签epc消息
	public static WriteEpc GetWriteEpc_6CForDefaultPsw(String epc, String byEpc) {

		byte[] psw = new byte[4];
		psw[0] = 0x00;
		psw[1] = 0x00;
		psw[2] = 0x00;
		psw[3] = 0x00;

		byte ptr = 0;
		byte[] bytesEpc = Util.convertHexStringToByteArray(epc);
		byte[] bytesTagId = Util.convertHexStringToByteArray(byEpc);

		WriteEpc msg = new WriteEpc(antenna, psw, bytesEpc, bytesTagId,
				MemoryBank.EPCMemory);
		msg.setIsReturn(true);// 设定需要访问值
		return msg;
	}

	// 写配件标签配置密码消息
	public static AccessPwdConfig_6C GetAccessPwdConfig_6CForDefaultPsw(
			String byEpc) {

		byte[] pswOld = new byte[4];
		pswOld[0] = 0x00;
		pswOld[1] = 0x00;
		pswOld[2] = 0x00;
		pswOld[3] = 0x00;

		byte[] pswNew = new byte[4];
		pswOld[0] = 0x20;
		pswOld[1] = 0x26;
		pswOld[2] = 0x31;
		pswOld[3] = 0x07;

		byte[] bytesTagId = Util.convertHexStringToByteArray(byEpc);

		AccessPwdConfig_6C msg = new AccessPwdConfig_6C(antenna, pswOld,
				pswNew, bytesTagId, MemoryBank.EPCMemory);
		msg.setIsReturn(true);// 设定需要访问值
		return msg;
	}

	private static byte[] getUserData(int opType) {
		byte[] bytesUserData = new byte[2];
		switch (opType) {
		case OpType.StockIn:
			bytesUserData[0] = 0x02;
			break;
		case OpType.StockOut:
			bytesUserData[0] = 0x03;
			break;
		case OpType.SendOut:
			bytesUserData[0] = 0x04;
			break;
		case OpType.Use:
			bytesUserData[0] = 0x05;
			break;
		case OpType.Stop:
			bytesUserData[0] = 0x06;
			break;
		case OpType.Back:
			bytesUserData[0] = 0x07;
			break;
		case OpType.SendRepair:
			bytesUserData[0] = 0x08;
			break;
		case OpType.Repair_B:
			bytesUserData[0] = 0x09;
			break;
		case OpType.Repair_O:
			bytesUserData[0] = 0x0A;
			break;
		case OpType.Repair_S:
			bytesUserData[0] = 0x0B;
			break;
		case OpType.BackFactory:
			bytesUserData[0] = 0x0C;
			break;
		case OpType.Scrap: {
			bytesUserData[0] = 0x00;
			bytesUserData[1] = 0x01;
		}
			break;
		default:
			break;
		}

		return bytesUserData;
	}
}
