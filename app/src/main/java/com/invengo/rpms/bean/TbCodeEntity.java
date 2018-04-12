package com.invengo.rpms.bean;

/**
 * 数据字典信息
 * Created by Administrator on 2018/4/10.
 */

public class TbCodeEntity extends BaseBean {
	public String dbType;
	public String dbCode;
	public String dbName;
	public String dbTypeBeyond;
	public String dbCodeBeyond;

	@Override
	public String getAddSql() {
		StringBuilder r = new StringBuilder();
		r.append("insert into TbCode values(");
		if (dbType == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(dbType);
			r.append("'");
		}
		r.append(",");
		if (dbCode == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(dbCode);
			r.append("'");
		}
		r.append(",");
		if (dbName == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(dbName);
			r.append("'");
		}
		r.append(",");
		if (dbTypeBeyond == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(dbTypeBeyond);
			r.append("'");
		}
		r.append(",");
		if (dbCodeBeyond == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(dbCodeBeyond);
			r.append("'");
		}
		r.append(")");
		return r.toString();
	}

	@Override
	public String getDelAllSql() {
		return "delete from TbCode";
	}
}
