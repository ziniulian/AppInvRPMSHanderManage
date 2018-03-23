package com.invengo.rpms.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.invengo.rpms.entity.CheckDetailEntity;
import com.invengo.rpms.entity.CheckEntity;
import com.invengo.rpms.entity.OpType;
import com.invengo.rpms.entity.PartsStorageLocationEntity;
import com.invengo.rpms.entity.SendRepairEntity;
import com.invengo.rpms.entity.StationEntity;
import com.invengo.rpms.entity.StorageLocationEntity;
import com.invengo.rpms.entity.TbCodeEntity;
import com.invengo.rpms.entity.TbPartsOpEntity;
import com.invengo.rpms.entity.UserEntity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class SqliteHelper {

	// 从sd卡中数据库路径
	private static String filedirPath = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/RPMS";
	private static String filePath = filedirPath + "/RPMSHanderV1000";

	private static SimpleDateFormat f = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");

	// 初始化数据库
	public static void InintDatabase() {

		if (UtilityHelper.checkSDCard() == 0) {
			File dir = new File(filedirPath);
			Boolean isExsit = dir.exists();
			if (!isExsit) {
				dir.mkdirs();
			}

			File file = new File(filePath);
			isExsit = file.exists();
			if (!isExsit) {
				SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(file,
						null);// 读SD卡数据库必须如此--用静态方法打开数据库

				List<String> listSql = new ArrayList<String>();
				String sql_table1 = "Create TABLE TbTHDS(THDSCode varchar(20) PRIMARY KEY,THDSName nvarchar(50),FactoryName nvarchar(50),MoreInfo nvarchar(100))";
				listSql.add(sql_table1);
				String sql_table2 = "Create TABLE TbUser(UserId varchar(20) PRIMARY KEY,UserName nvarchar(20),DeptCode varchar(20),GroupCode varchar(20),PostCode varchar(20),Password varchar(50),Tel varchar(20),IsEnable char(1))";
				listSql.add(sql_table2);
				String sql_table3 = "Create TABLE TbCode(Dbtype varchar(2),DbCode varchar(20),DbName nvarchar(50),DbtypeBeyond varchar(2),DbCodeBeyond varchar(20))";
				listSql.add(sql_table3);
				String sql_table4 = "Create TABLE TbCheck(CheckCode varchar(20),CheckPartsType varchar(100),AddUser varchar(20),AddTime varchar(20),Remark varchar(50),IsFinish char(1))";
				listSql.add(sql_table4);
				String sql_table5 = "Create TABLE TbCheckDetail(CheckCode varchar(20),PartsCode varchar(20),StorageLocation varchar(20),IsFind char(1),CheckUser varchar(20),CheckTime varchar(20))";
				listSql.add(sql_table5);
				String sql_table6 = "Create TABLE TbPartsOp(PartsCode varchar(20),OpType char(1),Info varchar(500))";
				listSql.add(sql_table6);
				String sql_table7 = "Create TABLE TbSendRepair(ID varchar(40),PartsCode varchar(20),FaultCode varchar(50),FaultDes varchar(100),Remark varchar(100))";
				listSql.add(sql_table7);
				String sql_table8 = "Create TABLE StorageLocation(StorageLocationCode varchar(4) PRIMARY KEY,PartsAllow varchar(20),MaxVolume int,IsEnable char(1))";
				listSql.add(sql_table8);
				String sql_table9 = "Create TABLE PartsStorageLocation(PartsCode varchar(20) PRIMARY KEY,StorageLocationCode varchar(4),StockinTime datetime)";
				listSql.add(sql_table9);

				// TODO: 2018/3/23 加入配件信息表
				// TODO: 2018/3/23 新增查询：获取配件信息表中同类型的数量

				db.beginTransaction();
				for (String sql : listSql) {
					db.execSQL(sql);
				}
				db.setTransactionSuccessful();
				db.endTransaction(); // 处理完成
				db.close();
			}
		}
	}

	// 读取库存信息
	public static List<PartsStorageLocationEntity> queryPartsStorageLocation(
			String PartsSort, String PartsName, int num) {

		List<PartsStorageLocationEntity> listInfo = new ArrayList<PartsStorageLocationEntity>();
		try {

			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select * from PartsStorageLocation where PartsCode like '%"
					+ PartsSort
					+ "%"
					+ PartsName
					+ "%' order by StockinTime desc Limit " + num;

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				String PartsCode = cursor.getString(cursor
						.getColumnIndex("PartsCode"));
				String StorageLocationCode = cursor.getString(cursor
						.getColumnIndex("StorageLocationCode"));
				String StockinTime = cursor.getString(cursor
						.getColumnIndex("StockinTime"));

				PartsStorageLocationEntity entity = new PartsStorageLocationEntity();
				entity.PartsCode = PartsCode;
				entity.StorageLocationCode = StorageLocationCode;
				entity.StockinTime = f.parse(StockinTime);

				listInfo.add(entity);
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return listInfo;
	}
	
	// 读取库存信息
		public static List<PartsStorageLocationEntity> queryPartsStorageLocation(
				String key, int num) {

			List<PartsStorageLocationEntity> listInfo = new ArrayList<PartsStorageLocationEntity>();
			try {

				File name = new File(filePath);
				SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
				String sql = "select * from PartsStorageLocation where PartsCode like '%"
						+ key
						+ "%' order by StockinTime desc Limit " + num;

				Cursor cursor = db.rawQuery(sql, null);
				while (cursor.moveToNext()) {

					String PartsCode = cursor.getString(cursor
							.getColumnIndex("PartsCode"));
					String StorageLocationCode = cursor.getString(cursor
							.getColumnIndex("StorageLocationCode"));
					String StockinTime = cursor.getString(cursor
							.getColumnIndex("StockinTime"));

					PartsStorageLocationEntity entity = new PartsStorageLocationEntity();
					entity.PartsCode = PartsCode;
					entity.StorageLocationCode = StorageLocationCode;
					entity.StockinTime = f.parse(StockinTime);

					listInfo.add(entity);
				}

				cursor.close();
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return listInfo;
		}

	// 获得库位存放的配件数量
	public static int queryPairsNumByStorageLocationCode(String code) {

		int pairsNum = 0;
		try {
			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select count(*) pairsNum from PartsStorageLocation where StorageLocationCode='"
					+ code + "'";

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				pairsNum = cursor.getInt(cursor.getColumnIndex("pairsNum"));
				break;
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return pairsNum;
	}

	// 读取库位信息
	public static StorageLocationEntity queryStorageLocationByCode(String code) {

		StorageLocationEntity entity = null;
		try {

			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select * from StorageLocation where StorageLocationCode='"
					+ code + "'";

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				String PartsAllow = cursor.getString(cursor
						.getColumnIndex("PartsAllow"));
				int MaxVolume = cursor.getInt(cursor
						.getColumnIndex("MaxVolume"));
				String IsEnable = cursor.getString(cursor
						.getColumnIndex("IsEnable"));

				entity = new StorageLocationEntity();
				entity.StorageLocationCode = code;
				entity.PartsAllow = PartsAllow;
				entity.MaxVolume = MaxVolume;
				entity.IsEnable = IsEnable;
				break;
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return entity;
	}

	// 读取用户信息
	public static UserEntity queryUserById(String userId) {

		UserEntity entityUser = null;
		try {

			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select * from TbUser where UserId='" + userId + "'";

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				String UserName = cursor.getString(cursor
						.getColumnIndex("UserName"));
				String Password = cursor.getString(cursor
						.getColumnIndex("Password"));
				String DeptCode = cursor.getString(cursor
						.getColumnIndex("DeptCode"));
				String GroupCode = cursor.getString(cursor
						.getColumnIndex("GroupCode"));
				String PostCode = cursor.getString(cursor
						.getColumnIndex("PostCode"));
				String Tel = cursor.getString(cursor.getColumnIndex("Tel"));
				String IsEnable = cursor.getString(cursor
						.getColumnIndex("IsEnable"));
				// float temp = cursor.getFloat(cursor.getColumnIndex("Temp"));

				entityUser = new UserEntity();
				entityUser.userId = userId;
				entityUser.password = Password;
				entityUser.userName = UserName;
				entityUser.deptCode = DeptCode;
				entityUser.groupCode = GroupCode;
				entityUser.postCode = PostCode;
				entityUser.tel = Tel;
				entityUser.isEnable = IsEnable;
				break;
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return entityUser;
	}

	// 读取用户信息
	public static List<UserEntity> queryUser() {

		List<UserEntity> listUser = new ArrayList<UserEntity>();
		try {

			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select * from TbUser ";

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				String UserId = cursor.getString(cursor
						.getColumnIndex("UserId"));
				String UserName = cursor.getString(cursor
						.getColumnIndex("UserName"));
				String Password = cursor.getString(cursor
						.getColumnIndex("Password"));
				String DeptCode = cursor.getString(cursor
						.getColumnIndex("DeptCode"));
				String GroupCode = cursor.getString(cursor
						.getColumnIndex("GroupCode"));
				String PostCode = cursor.getString(cursor
						.getColumnIndex("PostCode"));
				String Tel = cursor.getString(cursor.getColumnIndex("Tel"));
				String IsEnable = cursor.getString(cursor
						.getColumnIndex("IsEnable"));
				// float temp = cursor.getFloat(cursor.getColumnIndex("Temp"));

				UserEntity entityUser = new UserEntity();
				entityUser.userId = UserId;
				entityUser.password = Password;
				entityUser.userName = UserName;
				entityUser.deptCode = DeptCode;
				entityUser.groupCode = GroupCode;
				entityUser.postCode = PostCode;
				entityUser.tel = Tel;
				entityUser.isEnable = IsEnable;
				listUser.add(entityUser);
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return listUser;
	}

	// 删除配件送修故障信息
	public static Boolean DeteleSendRepair(List<String> listPartsCode) {
		List<String> listSql = new ArrayList<String>();

		for (String partsCode : listPartsCode) {
			String sql = "delete from TbSendRepair where PartsCode='"
					+ partsCode + "'";
			listSql.add(sql);
		}

		if (listSql.size() > 0) {
			return ExceSql(listSql);
		}

		return false;
	}

	// 读取配件送修故障信息
	public static SendRepairEntity querySendRepaiByCode(String partsCode) {

		SendRepairEntity entity = null;
		try {

			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select * from TbSendRepair where PartsCode='"
					+ partsCode + "'";

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				String id = cursor.getString(cursor.getColumnIndex("ID"));
				String FaultCode = cursor.getString(cursor
						.getColumnIndex("FaultCode"));
				String FaultDes = cursor.getString(cursor
						.getColumnIndex("FaultDes"));
				String Remark = cursor.getString(cursor
						.getColumnIndex("Remark"));

				entity = new SendRepairEntity();
				entity.ID = id;
				entity.PartsCode = partsCode;
				entity.FaultCode = FaultCode;
				entity.FaultDes = FaultDes;
				entity.Remark = Remark;

				break;
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return entity;
	}

	// 读取站点信息
	public static StationEntity queryStationByCode(String stationCode) {

		StationEntity entity = null;
		try {

			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select * from TbTHDS where THDSCode='" + stationCode
					+ "'";

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				String THDSName = cursor.getString(cursor
						.getColumnIndex("THDSName"));
				String FactoryName = cursor.getString(cursor
						.getColumnIndex("FactoryName"));

				entity = new StationEntity();
				entity.StationCode = stationCode;
				entity.StationName = THDSName;
				entity.FactoryName = FactoryName;
				break;
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return entity;
	}

	// 读取数据字典
	public static List<TbCodeEntity> queryDbCodeByType(String dbType) {

		List<TbCodeEntity> listEntity = new ArrayList<TbCodeEntity>();
		try {

			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select * from TbCode where Dbtype='" + dbType + "'";

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				String dbCode = cursor.getString(cursor
						.getColumnIndex("DbCode"));
				String dbName = cursor.getString(cursor
						.getColumnIndex("DbName"));
				String dbTypeBeyond = cursor.getString(cursor
						.getColumnIndex("DbtypeBeyond"));
				String dbCodeBeyond = cursor.getString(cursor
						.getColumnIndex("DbCodeBeyond"));

				TbCodeEntity entity = new TbCodeEntity();
				entity.dbType = dbType;
				entity.dbCode = dbCode;
				entity.dbName = dbName;
				entity.dbTypeBeyond=dbTypeBeyond;
				entity.dbCodeBeyond=dbCodeBeyond;

				listEntity.add(entity);
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return listEntity;
	}

	// 读取数据字典
	public static TbCodeEntity queryDbCodeByType(String dbType, String dbCode) {

		try {

			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select * from TbCode where Dbtype='" + dbType
					+ "' and DbCode='" + dbCode + "'";

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				String dbName = cursor.getString(cursor
						.getColumnIndex("DbName"));
				String dbTypeBeyond = cursor.getString(cursor
						.getColumnIndex("DbtypeBeyond"));
				String dbCodeBeyond = cursor.getString(cursor
						.getColumnIndex("DbCodeBeyond"));

				TbCodeEntity entity = new TbCodeEntity();
				entity.dbType = dbType;
				entity.dbCode = dbCode;
				entity.dbName = dbName;
				entity.dbTypeBeyond=dbTypeBeyond;
				entity.dbCodeBeyond=dbCodeBeyond;

				return entity;
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// 删除盘点单
	public static boolean DeleteCheck(String checkCode) {
		List<String> listSql = new ArrayList<String>();

		String sql = "delete from TbCheck where CheckCode='" + checkCode + "'";
		listSql.add(sql);
		sql = "delete from TbCheckDetail where CheckCode='" + checkCode + "'";
		listSql.add(sql);

		if (listSql.size() > 0) {
			return ExceSql(listSql);
		}

		return false;
	}

	// 创建盘点单
	public static boolean CreateCheck(CheckEntity entity) {

		try {

			List<String> listSql = new ArrayList<String>();
			boolean hasParts = false;

			String sql = "insert into TbCheck values('" + entity.CheckCode
					+ "', '" + entity.CheckPartsType + "', '" + entity.AddUser
					+ "', '" + f.format(entity.AddTime) + "', '"
					+ entity.Remark + "','N')";
			listSql.add(sql);

			String[] checkPartsTypes = entity.CheckPartsType.split("-");
			if (checkPartsTypes.length == 5) {
				String t5 = checkPartsTypes[0];
				String factory = checkPartsTypes[1];
				String sort = checkPartsTypes[2];
				String host = checkPartsTypes[3];
				String name = checkPartsTypes[4];

				String quetyKey = "";
				if (!t5.equals("0")) {
					String keyt5 = "";
					while (t5.length() > 1) {
						String s = t5.substring(0, 2);
						t5 = t5.substring(2, t5.length());
						keyt5 += "'" + s + "',";
					}
					if (keyt5.length() > 0) {
						keyt5 = keyt5.substring(0, keyt5.length() - 1);
						String sqlKey = String.format(
								"substr(PartsCode,1,2) in (%s) and ", keyt5);
						quetyKey += sqlKey;
					}

				}
				if (!factory.equals("0")) {
					String keyfactory = "";
					while (factory.length() > 1) {
						String s = factory.substring(0, 2);
						factory = factory.substring(2, factory.length());
						keyfactory += "'" + s + "',";
					}
					if (keyfactory.length() > 0) {
						keyfactory = keyfactory.substring(0,
								keyfactory.length() - 1);
						String sqlKey = String.format(
								"substr(PartsCode,3,2) in (%s) and ",
								keyfactory);
						quetyKey += sqlKey;
					}

				}
				if (!sort.equals("0")) {
					String keysort = "";
					while (sort.length() > 0) {
						String s = sort.substring(0, 1);
						sort = sort.substring(1, sort.length());
						keysort += "'" + s + "',";
					}
					if (keysort.length() > 0) {
						keysort = keysort.substring(0, keysort.length() - 1);
						String sqlKey = String.format(
								"substr(PartsCode,5,1) in (%s) and ", keysort);
						quetyKey += sqlKey;
					}

				}
				if (!host.equals("0")) {
					String keyhost = "";
					while (host.length() > 1) {
						String s = host.substring(0, 2);
						host = host.substring(2, host.length());
						keyhost += "'" + s + "',";
					}
					if (keyhost.length() > 0) {
						keyhost = keyhost.substring(0, keyhost.length() - 1);
						String sqlKey = String.format(
								"substr(PartsCode,6,2) in (%s) and ", keyhost);
						quetyKey += sqlKey;
					}

				}
				if (!name.equals("0")) {
					String keyname = "";
					while (name.length() > 1) {
						String s = name.substring(0, 3);
						name = name.substring(3, name.length());
						keyname += "'" + s + "',";
					}
					if (keyname.length() > 0) {
						keyname = keyname.substring(0, keyname.length() - 1);
						String sqlKey = String.format(
								"substr(PartsCode,8,3) in (%s) and ", keyname);
						quetyKey += sqlKey;
					}

				}

				sql = "select * from PartsStorageLocation";
				if (quetyKey.length() > 0) {
					quetyKey += "1=1";
					sql += " where " + quetyKey;
				}

				File nameFile = new File(filePath);
				SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
						nameFile, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
				Cursor cursor = db.rawQuery(sql, null);
				while (cursor.moveToNext()) {

					String PartsCode = cursor.getString(cursor
							.getColumnIndex("PartsCode"));
					String StorageLocationCode = cursor.getString(cursor
							.getColumnIndex("StorageLocationCode"));

					sql = "insert into TbCheckDetail(CheckCode,PartsCode,StorageLocation,IsFind) values('"
							+ entity.CheckCode + "', '" + PartsCode + "', '"
							+ StorageLocationCode + "','N')";
					listSql.add(sql);
					hasParts = true;
				}

				cursor.close();
				db.close();
			}

			if (hasParts) {
				if (listSql.size() > 0) {
					return ExceSql(listSql);
				}
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	// 读取盘点单信息
	public static List<CheckEntity> queryCheck(boolean isFinish) {

		List<CheckEntity> listEntity = new ArrayList<CheckEntity>();
		try {

			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select * from TbCheck";
			if (isFinish) {
				sql += " where IsFinish='Y'";
			}

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				CheckEntity entity = new CheckEntity();
				entity.CheckCode = cursor.getString(cursor
						.getColumnIndex("CheckCode"));
				entity.CheckPartsType = cursor.getString(cursor
						.getColumnIndex("CheckPartsType"));
				entity.AddTime = f.parse(cursor.getString(cursor
						.getColumnIndex("AddTime")));
				entity.AddUser = cursor.getString(cursor
						.getColumnIndex("AddUser"));
				entity.IsFinish = cursor.getString(cursor
						.getColumnIndex("IsFinish"));
				entity.Remark = cursor.getString(cursor
						.getColumnIndex("Remark"));

				listEntity.add(entity);
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return listEntity;
	}

	// 读取盘点单信息
	public static CheckEntity queryCheckByCode(String checkCode) {

		try {

			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select * from TbCheck where CheckCode='" + checkCode
					+ "'";

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				CheckEntity entity = new CheckEntity();
				entity.CheckCode = cursor.getString(cursor
						.getColumnIndex("CheckCode"));
				entity.AddTime = f.parse(cursor.getString(cursor
						.getColumnIndex("AddTime")));
				entity.AddUser = cursor.getString(cursor
						.getColumnIndex("AddUser"));
				entity.IsFinish = cursor.getString(cursor
						.getColumnIndex("IsFinish"));
				entity.Remark = cursor.getString(cursor
						.getColumnIndex("Remark"));

				return entity;
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// 记录操作记录
	public static Boolean SaveOpRecord(List<String> listPartsCode, int opType,
			String info) {
		List<String> listSql = new ArrayList<String>();

		for (String partsCode : listPartsCode) {
			String sql = "insert into TbPartsOp values('" + partsCode + "', '"
					+ opType + "', '" + info + "')";
			listSql.add(sql);
		}

		if (listSql.size() > 0) {
			return ExceSql(listSql);
		}

		return false;
	}

	// 记录操作记录
	public static Boolean SaveOpRecord(List<String> listPartsCode, int opType,
			String info, String storageLocationStr) {
		
		List<String> listSql = new ArrayList<String>();
		List<String> listSql1 = new ArrayList<String>();

		for (String partsCode : listPartsCode) {
			String sql = "insert into TbPartsOp values('" + partsCode + "', '"
					+ opType + "', '" + info + "')";
			listSql.add(sql);

			if (opType == OpType.StockIn) {
				
				String sql1 = "delete from PartsStorageLocation where PartsCode='"
						+ partsCode + "'";
				listSql1.add(sql1);
				
				String sql2 = "insert into PartsStorageLocation values('"
						+ partsCode + "', '" + storageLocationStr + "', '"
						+ f.format(new Date()) + "')";
				listSql.add(sql2);
				
			}
		}
		
		//如果有库存，先删掉库存
		if (listSql.size() > 0) {
			return ExceSql(listSql);
		}

		if (listSql.size() > 0) {
			return ExceSql(listSql);
		}

		return false;
	}

	// 记录操作记录
	public static Boolean SaveOpRecord(List<String> listPartsCode, int opType,
			String info,
			List<PartsStorageLocationEntity> listPartsStorageLocation) {
		List<String> listSql = new ArrayList<String>();

		for (String partsCode : listPartsCode) {

			String storageLocationStr = "未知";
			for (PartsStorageLocationEntity partsEntity : listPartsStorageLocation) {
				if (partsEntity.PartsCode.equals(partsCode)) {
					storageLocationStr = partsEntity.StorageLocationCode;
					break;
				}
			}
			String storageLocationStrinfo = storageLocationStr + "," + info;

			String sql = "insert into TbPartsOp values('" + partsCode + "', '"
					+ opType + "', '" + storageLocationStrinfo + "')";
			listSql.add(sql);

			if (opType == OpType.StockOut) {
				String sql1 = "delete from PartsStorageLocation where PartsCode='"
						+ partsCode + "'";
				listSql.add(sql1);
			}
		}

		if (listSql.size() > 0) {
			return ExceSql(listSql);
		}

		return true;
	}

	// 读取操作记录信息
	public static List<TbPartsOpEntity> queryOpRecord() {

		List<TbPartsOpEntity> listEntity = new ArrayList<TbPartsOpEntity>();
		try {

			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select * from TbPartsOp";

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				TbPartsOpEntity entity = new TbPartsOpEntity();
				entity.PartsCode = cursor.getString(cursor
						.getColumnIndex("PartsCode"));
				entity.OpType = cursor.getString(cursor
						.getColumnIndex("OpType"));
				entity.Info = cursor.getString(cursor.getColumnIndex("Info"));
				listEntity.add(entity);
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return listEntity;
	}

	// 读取盘点明细信息
	public static List<CheckDetailEntity> queryCheckDetailByCheckCode(
			String CheckCode) {

		List<CheckDetailEntity> listEntity = new ArrayList<CheckDetailEntity>();
		try {

			File name = new File(filePath);
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。
			String sql = "select * from TbCheckDetail where CheckCode='"
					+ CheckCode + "'";

			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {

				CheckDetailEntity entity = new CheckDetailEntity();
				entity.CheckCode = cursor.getString(cursor
						.getColumnIndex("CheckCode"));
				entity.PartsCode = cursor.getString(cursor
						.getColumnIndex("PartsCode"));
				entity.CheckTime = cursor.getString(cursor
						.getColumnIndex("CheckTime"));
				entity.CheckUser = cursor.getString(cursor
						.getColumnIndex("CheckUser"));
				entity.IsFind = cursor.getString(cursor
						.getColumnIndex("IsFind"));
				entity.StorageLocation = cursor.getString(cursor
						.getColumnIndex("StorageLocation"));

				listEntity.add(entity);
			}

			cursor.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return listEntity;
	}

	// 记录盘点操作记录
	public static Boolean SaveCheckDetaiRecord(String checkCode,
			String partsCode, String checkUser) {
		List<String> listSql = new ArrayList<String>();

		String sql = "update TbCheckDetail set IsFind='Y'," + "CheckUser='"
				+ checkUser + "',CheckTime='" + f.format(new Date(System.currentTimeMillis()))
				+ "'  where CheckCode='" + checkCode + "' and partsCode='"
				+ partsCode + "'";
		listSql.add(sql);

		if (listSql.size() > 0) {
			return ExceSql(listSql);
		}

		return false;
	}

	// 记录盘点记录
	public static Boolean SaveCheckRecord(String checkCode) {
		List<String> listSql = new ArrayList<String>();

		String sql = "update TbCheck set IsFinish='Y' where CheckCode='"
				+ checkCode + "'";
		listSql.add(sql);

		if (listSql.size() > 0) {
			return ExceSql(listSql);
		}

		return false;
	}

	// 数据库执行sql
	public static Boolean ExceSql(List<String> listSql) {

		File name = new File(filePath);
		Boolean isExsit = name.exists();
		if (isExsit) {
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(name, null);// 读SD卡数据库必须如此--用静态方法打开数据库。

			db.beginTransaction();

			try {

				for (String sql : listSql) {
					db.execSQL(sql);
				}

				db.setTransactionSuccessful();
				return true;
			} catch (Exception e) {
				e.printStackTrace();

			} finally {
				db.endTransaction(); // 处理完成
			}
			db.close();
		}
		return false;
	}
}
