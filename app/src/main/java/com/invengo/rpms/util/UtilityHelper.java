package com.invengo.rpms.util;

import android.os.Environment;
import android.os.StatFs;
import invengo.javaapi.core.Util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.invengo.rpms.entity.OpType;
import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.entity.StationEntity;
import com.invengo.rpms.entity.StorageLocationEntity;
import com.invengo.rpms.entity.TbCodeEntity;

public class UtilityHelper {
	
	// 判断SD卡是否存在
	public static int checkSDCard() {
		boolean isSDCard = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);

		if (!isSDCard) {
			return 1; // sd卡不存在
		}

		String root = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		StatFs statFs = new StatFs(root);
		long blockSize = statFs.getBlockSize();
		long availableBlocks = statFs.getAvailableBlocks();
		long availableSize = blockSize * availableBlocks;
		if (availableSize < 1000000) {
			return 2; // sd卡空间不够
		}

		return 0;
	}

	// sha1加密
	public static String getSha1(String str) {
		if (null == str || 0 == str.length()) {
			return null;
		}
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
			mdTemp.update(str.getBytes("UTF-8"));

			byte[] md = mdTemp.digest();
			int j = md.length;
			char[] buf = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
				buf[k++] = hexDigits[byte0 & 0xf];
			}

			return new String(buf).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return "";
	}

	// 验证epc码是否有效并获得数据类型,0为配件，1为库位，2为站点
	public static int CheckEpc(String epc) {
		if (epc.length() > 1) {
			if (epc.substring(0, 2).equals("50"))
				return 0;
			if (epc.substring(0, 2).equals("4B"))
				return 1;
			if (epc.substring(0, 2).equals("5A"))
				return 2;
		}

		return -1;
	}

	// 根据epc获得相关编码
	public static String GetCodeByEpc(String epc) {
		if (epc.length() > 1) {
			try {
				String codeStr = "";
				if (epc.substring(0, 2).equals("50")) {
					byte[] dataBytes = Util.convertHexStringToByteArray(epc
							.substring(2, epc.length()));
					if (dataBytes.length >= 11) {
						if (dataBytes[0] == 0x01) {
							codeStr += "TH";
						}

						if (dataBytes[1] == 0x01) {
							codeStr += "GH";
						}
						if (dataBytes[1] == 0x02) {
							codeStr += "HK";
						}

						byte[] bs = new byte[6];
						System.arraycopy(dataBytes, 2, bs, 0, 6);
						codeStr += new String(bs);

						byte[] bs1 = new byte[3];
						System.arraycopy(dataBytes, 8, bs1, 0, 3);
						codeStr += Util.convertByteArrayToHexString(bs1)
								.substring(1, 6);
					}
				}
				if (epc.substring(0, 2).equals("4B")) {
					byte[] data = Util.convertHexStringToByteArray(epc
							.substring(2, epc.length()));
					codeStr += new String(data);
				}
				if (epc.substring(0, 2).equals("5A")) {
					byte[] data = Util.convertHexStringToByteArray(epc
							.substring(2, epc.length()));
					codeStr += new String(data);
				}

				return codeStr=codeStr.trim();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return "";
	}

	// 验证用户数据能否进行相关操作,
	public static boolean CheckUserData(String userData, int opType) {
		if (userData.length() > 3) {
			String status = userData.substring(0, 2);
			String isScrap = userData.substring(2, 4);

			if (opType == OpType.StockIn
					&& (status.equals("01") || status.equals("03") || status.equals("04")
							|| status.equals("0A") || (status.equals("00") && isScrap
							.equals("01")))) {
				return true;
			} else if (opType == OpType.StockOut && (status.equals("02"))) {
				return true;
			} else if (opType == OpType.SendOut && (status.equals("03"))) {
				return true;
			} else if (opType == OpType.Use
					&& (status.equals("04") || status.equals("07"))
					&& isScrap.equals("00")) {
				return true;
			} else if (opType == OpType.Stop && status.equals("05")
					&& isScrap.equals("00")) {
				return true;
			} else if (opType == OpType.Back && status.equals("06")
					&& isScrap.equals("00")) {
				return true;
			} else if (opType == OpType.SendRepair && status.equals("06")
					&& isScrap.equals("00")) {
				return true;
			} else if (opType == OpType.Repair
					&& (status.equals("08") || status.equals("0C"))
					&& isScrap.equals("00")) {
				return true;
			} else if (opType == OpType.BackFactory && (status.equals("09"))
					&& isScrap.equals("00")) {
				return true;
			} else if (opType == OpType.Scrap && (status.equals("0B"))
					&& isScrap.equals("00")) {
				return true;
			}
		}

		return false;
	}

	// 根据用户数据获得配件状态
	public static String GetPairStatus(String userData) {
		String statusStr = "";
		if (userData.length() >= 4) {
			if (userData.substring(0, 2).equals("01")) {
				statusStr = "待入库";
			} else if (userData.substring(0, 2).equals("02")) {
				statusStr = "已入库";
			} else if (userData.substring(0, 2).equals("03")) {
				statusStr = "已出库";
			} else if (userData.substring(0, 2).equals("04")) {
				statusStr = "已发料";
			} else if (userData.substring(0, 2).equals("05")) {
				statusStr = "已启用";
			} else if (userData.substring(0, 2).equals("06")) {
				statusStr = "已停用";
			} else if (userData.substring(0, 2).equals("07")) {
				statusStr = "已恢复";
			} else if (userData.substring(0, 2).equals("08")) {
				statusStr = "已送修";
			} else if (userData.substring(0, 2).equals("09")) {
				statusStr = "待厂修";
			} else if (userData.substring(0, 2).equals("0A")) {
				statusStr = "已修竣";
			} else if (userData.substring(0, 2).equals("0B")) {
				statusStr = "待报废";
			} else if (userData.substring(0, 2).equals("0C")) {
				statusStr = "送厂修";
			}

			if (userData.substring(2, 4).equals("01")) {
				statusStr += "   已报废";
			} else {
				statusStr += "";
			}
		}
		return statusStr;
	}

	// 根据配件编码获得配件信息
	public static PartsEntity GetPairEntityByCode(String pairsCode) {
		PartsEntity entiry = new PartsEntity();
		entiry.PartsCode = pairsCode;
		if (pairsCode.length() >= 15) {
			entiry.FactoryCode = pairsCode.substring(2, 4);
			entiry.FactoryName = GetFactoryNameByCode(entiry.FactoryCode);
			entiry.PartsType = GetPartTypeByCode(pairsCode.substring(4, 5));
			entiry.BoxType = GetBoxTypeByCode(pairsCode.substring(5, 7));
			entiry.PartsName = GetPartsNameByCode(pairsCode.substring(7, 10));
			entiry.SeqNo = pairsCode.substring(10, 15);
		}
		return entiry;
	}

	// 根据编码获得箱体型号
	public static String GetBoxTypeByCode(String code) {
		TbCodeEntity entity = SqliteHelper.queryDbCodeByType("08", code);
		if (entity != null) {
			return entity.dbName;
		}

		return "";
	}

	// 根据编码获得配件型号
	public static String GetPartTypeByCode(String code) {
		TbCodeEntity entity = SqliteHelper.queryDbCodeByType("07", code);
		if (entity != null) {
			return entity.dbName;
		}

		return "";
	}

	// 根据编码获得厂名
	public static String GetFactoryNameByCode(String code) {
		TbCodeEntity entity = SqliteHelper.queryDbCodeByType("06", code);
		if (entity != null) {
			return entity.dbName;
		}

		return "";
	}

	// 获得库位信息
	public static String getStorageLocationInfo(String storageLocationCode) {

		StorageLocationEntity entity = SqliteHelper
				.queryStorageLocationByCode(storageLocationCode);
		if (entity != null) {
			if (entity.IsEnable.equals("N")) {
				return "库位禁用";
			}

			String Info1 = "限制：";
			String condition = "";
			if (entity.PartsAllow.length() == 10) {
				String t5 = entity.PartsAllow.substring(0, 2);
				String factotyCode = entity.PartsAllow.substring(2, 4);
				String partsSort = entity.PartsAllow.substring(4, 5);
				String partsType = entity.PartsAllow.substring(5, 7);
				String partsName = entity.PartsAllow.substring(7, 10);

				if (!t5.equals("00")) {
					condition += t5 + " ";
				}

				if (!factotyCode.equals("00")) {
					List<TbCodeEntity> listCode = SqliteHelper
							.queryDbCodeByType("06");
					for (TbCodeEntity entityCode : listCode) {
						if (entityCode.dbCode.equals(factotyCode)) {
							condition += entityCode.dbName + " ";
							break;
						}
					}

				}

				if (!partsSort.equals("0")) {

					List<TbCodeEntity> listCode = SqliteHelper
							.queryDbCodeByType("07");
					for (TbCodeEntity entityCode : listCode) {
						if (entityCode.dbCode.equals(partsSort)) {
							condition += entityCode.dbName + " ";
							break;
						}
					}

				}

				if (!partsType.equals("00")) {

					List<TbCodeEntity> listCode = SqliteHelper
							.queryDbCodeByType("08");
					for (TbCodeEntity entityCode : listCode) {
						if (entityCode.dbCode.equals(partsType)) {
							condition += entityCode.dbName + " ";
							break;
						}
					}

				}

				if (!partsName.equals("000")) {

					List<TbCodeEntity> listCode = SqliteHelper
							.queryDbCodeByType("09");
					for (TbCodeEntity entityCode : listCode) {
						if (entityCode.dbCode.equals(partsName)) {
							condition += entityCode.dbName + " ";
							break;
						}
					}
				}
			}

			if (condition.length() == 0) {
				Info1 += "未限";
			} else {
				Info1 += condition;
			}

			int pairsNum = SqliteHelper
					.queryPairsNumByStorageLocationCode(storageLocationCode);
			String Info2 = "最大容量：";
			if (entity.MaxVolume > 0) {
				Info2 += String.format("%s，当前库存%s", entity.MaxVolume,
						pairsNum);
			} else {
				Info2 += String.format("未限，当前库存%s", pairsNum);
			}

			return String.format("库位%s\n%s", Info1, Info2);
		}

		return "";
	}

	// 根据编码获得配件名称
	public static String GetPartsNameByCode(String code) {
		TbCodeEntity entity = SqliteHelper.queryDbCodeByType("09", code);
		if (entity != null) {
			return entity.dbName;
		}

		return "";
	}

	@Deprecated
	public static String convertByteArrayToHexString(byte[] byte_array) {
		String s = "";

		if (byte_array == null)
			return s;

		for (int i = 0; i < byte_array.length; i++) {
			String hex = Integer.toHexString(byte_array[i] & 0xff);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			s = s + hex;
		}
		return s.toUpperCase();
	}
}
