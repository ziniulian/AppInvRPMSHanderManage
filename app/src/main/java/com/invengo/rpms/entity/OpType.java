package com.invengo.rpms.entity;

public class OpType {

	public static final int StockIn = 1;// 入库
	public static final int StockOut = 2;// 出库
	public static final int SendOut = 3;// 发料
	public static final int Use = 4;// 启用
	public static final int Stop = 5;// 停用
	public static final int Back = 6;// 恢复
	public static final int SendRepair = 7;// 送修
	public static final int Repair = 8;// 维修检测待厂修
	public static final int Repair_B = 9;// 维修检测待厂修
	public static final int Repair_O = 10;// 维修检测修浚
	public static final int Repair_S = 11;// 维修检测待报废
	public static final int BackFactory = 12;// 返厂
	public static final int Scrap =13;// 报废
	public static final int Del =14;// 删除一个配件标签
	public static final int New =15;// 新增一个配件标签
}