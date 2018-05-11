package com.invengo.rpms.bean;

/**
 * 库位信息
 * Created by Administrator on 2018/4/10.
 */

public class TbStorageLocationEntity extends BaseBean {
	public String LocationCode;
	public String PartAllow;
	public int MaxVolumn = 0;
	public String IsEnable;

	@Override
	public String getAddSql() {
		StringBuilder r = new StringBuilder();
		r.append("insert into StorageLocation values(");
		if (LocationCode == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(LocationCode);
			r.append("'");
		}
		r.append(",");
		if (PartAllow == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(PartAllow);
			r.append("'");
		}
		r.append(",");
		r.append(MaxVolumn);
		r.append(",");
		if (IsEnable == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(IsEnable);
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
		return "delete from StorageLocation";
	}
}
