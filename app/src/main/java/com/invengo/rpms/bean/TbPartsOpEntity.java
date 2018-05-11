package com.invengo.rpms.bean;

/**
 * 配件操作信息
 * Created by Administrator on 2018/4/11.
 */

public class TbPartsOpEntity extends BaseBean {
	public String PartsCode;
	public String OpType;
	public String Info;

	@Override
	public String getAddSql() {
		StringBuilder r = new StringBuilder();
		r.append("insert into TbPartsOp values(");
		if (PartsCode == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(PartsCode);
			r.append("'");
		}
		r.append(",");
		if (OpType == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(OpType);
			r.append("'");
		}
		r.append(",");
		if (Info == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(Info);
			r.append("'");
		}
		r.append(")");
		return r.toString();
	}

	@Override
	public String getDelSql() {
		StringBuilder r = new StringBuilder();
		r.append("delete from TbParts where PartsCode=");
		if (PartsCode == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(PartsCode);
			r.append("'");
		}
		r.append(" and OpType=");
		if (OpType == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(OpType);
			r.append("'");
		}
		r.append(" and Info=");
		if (Info == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(Info);
			r.append("'");
		}
		r.append(")");
		return r.toString();
	}

	@Override
	public String getDelAllSql() {
		return "delete from TbPartsOp";
	}

	// 获取向服务器上传时的数据格式
	public String getPushStr() {
		StringBuilder r = new StringBuilder();
		r.append(PartsCode);
		r.append(",");
		r.append(OpType);
		r.append(",");
		r.append(Info);
		return r.toString().replace("-", "===");
	}
}
