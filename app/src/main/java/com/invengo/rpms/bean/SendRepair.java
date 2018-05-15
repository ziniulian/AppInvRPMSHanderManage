package com.invengo.rpms.bean;

/**
 * 送修信息
 * Created by LZR on 2018/5/15.
 */

public class SendRepair extends BaseBean {
	public String ID;
	public String PartsCode;
	public String FaultCode;
	public String FaultDes;
	public String Remark;

	@Override
	public String getDelSql() {
		// TODO: 2018/5/15
		return "";
	}

	@Override
	public String getDelAllSql() {
		return "delete from TbSendRepair";
	}

	@Override
	public String getAddSql() {
		StringBuilder r = new StringBuilder();
		r.append("insert into TbSendRepair values(");
		if (ID == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(ID);
			r.append("'");
		}
		r.append(",");
		if (PartsCode == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(PartsCode);
			r.append("'");
		}
		r.append(",");
		if (FaultCode == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(FaultCode);
			r.append("'");
		}
		r.append(",");
		if (FaultDes == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(FaultDes);
			r.append("'");
		}
		r.append(",");
		if (Remark == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(Remark);
			r.append("'");
		}
		r.append(")");
		return r.toString();
	}
}
