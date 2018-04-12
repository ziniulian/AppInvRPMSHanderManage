package com.invengo.rpms.bean;

/**
 * 站点信息
 * Created by Administrator on 2018/4/10.
 */

public class THDSEntity extends BaseBean {
	public String THDSCode;
	public String THDSName;
	public String FactoryName;

	@Override
	public String getAddSql() {
		StringBuilder r = new StringBuilder();
		r.append("insert into TbTHDS values(");
		if (THDSCode == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(THDSCode);
			r.append("'");
		}
		r.append(",");
		if (THDSName == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(THDSName);
			r.append("'");
		}
		r.append(",");
		if (FactoryName == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(FactoryName);
			r.append("'");
		}
		r.append(",null)");
		return r.toString();
	}

	@Override
	public String getDelAllSql() {
		return "delete from TbTHDS";
	}
}
