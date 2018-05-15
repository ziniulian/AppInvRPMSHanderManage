package com.invengo.rpms.bean;

/**
 * 配件信息
 * Created by Administrator on 2018/4/10.
 */

public class Parts extends BaseBean {
	/// <summary>
	/// 配件编码
	/// </summary>
	public String PartCode;

	/// <summary>
	/// 配件状态
	/// </summary>
	public String Status;

	/// <summary>
	/// 配件位置
	/// </summary>
	public String Location;

	/// <summary>
	/// 最后操作时间
	/// </summary>
	public String LastOpTime;

	/// <summary>
	/// 原厂编码
	/// </summary>
	public String FactoryCode;

	/// <summary>
	/// 操作人（USER_ID）
	/// </summary>
	public String OpUser;

	@Override
	public String getAddSql() {
		StringBuilder r = new StringBuilder();
		r.append("insert into TbParts values(");
		if (PartCode == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(PartCode);
			r.append("'");
		}
		r.append(",");
		if (Status == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(Status);
			r.append("'");
		}
		r.append(",");
		if (LastOpTime == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(LastOpTime);
			r.append("'");
		}
		r.append(",");
		if (Location == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(Location);
			r.append("'");
		}
		r.append(",");
		if (FactoryCode == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(FactoryCode);
			r.append("'");
		}
		r.append(",");
		if (OpUser == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(OpUser);
			r.append("'");
		}
		r.append(")");
		return r.toString();
	}

	@Override
	public String getDelSql() {
		// TODO: 2018/5/10 删除语句
		return "";
	}

	@Override
	public String getDelAllSql() {
		return "delete from TbParts";
	}
}
