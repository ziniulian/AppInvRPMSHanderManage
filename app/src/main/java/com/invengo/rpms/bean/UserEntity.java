package com.invengo.rpms.bean;

/**
 * 用户信息
 * Created by Administrator on 2018/4/10.
 */

public class UserEntity extends BaseBean {
	public String userId;
	public String password;
	public String userName;
	public String deptCode;
	public String deptName;
	public String groupCode;
	public String groupName;
	public String postCode;
	public String postName;
	public String tel;
	public String isEnable;

	@Override
	public String getAddSql() {
		StringBuilder r = new StringBuilder();
		r.append("insert into TbUser values(");
		if (userId == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(userId);
			r.append("'");
		}
		r.append(",");
		if (userName == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(userName);
			r.append("'");
		}
		r.append(",");
		if (deptCode == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(deptCode);
			r.append("'");
		}
		r.append(",");
		if (groupCode == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(groupCode);
			r.append("'");
		}
		r.append(",");
		if (postCode == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(postCode);
			r.append("'");
		}
		r.append(",");
		if (password == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(password);
			r.append("'");
		}
		r.append(",");
		if (tel == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(tel);
			r.append("'");
		}
		r.append(",");
		if (isEnable == null) {
			r.append("null");
		} else {
			r.append("'");
			r.append(isEnable);
			r.append("'");
		}
		r.append(")");
		return r.toString();
	}

	@Override
	public String getDelAllSql() {
		return "delete from TbUser";
	}
}
