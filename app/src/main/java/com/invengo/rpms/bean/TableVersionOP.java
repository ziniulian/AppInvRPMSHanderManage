package com.invengo.rpms.bean;

/**
 * 表版本操作信息
 * Created by Administrator on 2018/4/10.
 */

public class TableVersionOP {
	/// <summary>
	/// 表名称
	/// </summary>
	public String TableName;

	/// <summary>
	/// 表版本
	/// </summary>
	public int TableVersion;

	/// <summary>
	/// 操作类型
	/// </summary>
	public String OpType;

	/// <summary>
	/// 操作表主键
	/// </summary>
	public String OpTablePK;

	/// <summary>
	/// 操作时间
	/// </summary>
	public String OpTime;

	/// <summary>
	/// 对应的修改表信息，json格式字符
	/// </summary>
	public String Info;
}
