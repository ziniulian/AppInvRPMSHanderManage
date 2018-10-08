package com.invengo.rpms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.invengo.rpms.entity.PartsEntity;
import com.invengo.rpms.entity.StationEntity;
import com.invengo.rpms.entity.TbCodeEntity;
import com.invengo.rpms.util.Btn001;
import com.invengo.rpms.util.SqliteHelper;
import com.invengo.rpms.util.UtilityHelper;

import java.util.HashMap;

/**
 * 多标签查询单一页面
 * Created by LZR on 2018/9/19.
 */

public class MultiQueryOneActivity extends Activity {
	private Context con = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multiqueryone);

		initView();
	}

	// 初始化控件
	private void initView() {
		Button btn;

		// 退出按钮
		btn = (Button) findViewById(R.id.btnBack);
		btn.setOnTouchListener(new Btn001());
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		// 显示内容
		TextView infv = (TextView)findViewById(R.id.info);
		Intent git = getIntent();
		String epc = git.getStringExtra("epc");
		int typ = git.getIntExtra("typ", 1);
		switch (typ) {
			case 0:		// 配件
				infv.setText(parseParts(epc, git.getByteExtra("stu", (byte) 0x01), git.getByteExtra("err", (byte) 0x01)).toString());
				break;
			case 1:		// 库位
				infv.setText(parseLocation(epc).toString());
				break;
			case 2:		// 站点
				infv.setText(parseStation(epc).toString());
				break;
		}
	}

	// 解析库位
	private StringBuilder parseLocation (String cod) {
		StringBuilder r = new StringBuilder();
		r.append("库位编码：");
		r.append(cod);
		r.append("\n\n");
		r.append(UtilityHelper.getStorageLocationInfo(cod));
		return r;
	}

	// 解析站点
	private StringBuilder parseStation (String cod) {
		StringBuilder r = new StringBuilder();
		StationEntity se = SqliteHelper.queryStationByCode(cod);
		r.append("站点编码：");
		r.append(cod);
		r.append("\n\n站点名称：");
		r.append(se.StationName);
		r.append("\n\n厂家：");
		r.append(se.FactoryName);
		return r;
	}

	// 解析配件数据
	private StringBuilder parseParts (String cod, byte stu, byte err) {
		StringBuilder r = new StringBuilder();
		PartsEntity entity = UtilityHelper.GetPairEntityByCode(cod);
		r.append("编码：");
		r.append(entity.PartsCode);
		r.append("\n\n厂家：");
		r.append(entity.FactoryName);
		r.append("\n\n型号：");
		r.append(entity.PartsType);
		r.append("\n\n类别：");
		r.append(entity.BoxType);
		r.append("\n\n名称：");
		r.append(entity.PartsName);
		r.append("\n\n序列号：");
		r.append(entity.SeqNo);

		r.append(qryPartPosition(cod, stu, err));
		return r;
	}

	// 查询配件位置
	private StringBuilder qryPartPosition (String cod, byte stu, byte err) {
		StringBuilder r = new StringBuilder();
		HashMap<String, String> m = SqliteHelper.queryOnePart(cod);

		if (m != null) {
			r.append("\n\n原厂编码：");
			r.append(m.get("FactoryCode"));
		}

		r.append("\n\n状态：");
		if (err == 0x01) {
			r.append("已报废");
		} else {
			// 状态
			switch (stu) {
				case 0x01:
					r.append("在所（待入库）");
					break;
				case 0x02:
					r.append("已入库");
					break;
				case 0x03:
					r.append("在所（已出库）");
					break;
				case 0x04:
					r.append("在段");
					break;
				case 0x06:
					r.append("在段（已停用）");
					break;
				case 0x07:
					r.append("在段（已恢复）");
					break;
				case 0x05:
					r.append("已启用");
					break;
				case 0x08:
					r.append("在所（已送修）");
					break;
				case 0x09:
					r.append("在所（待厂修）");
					break;
				case 0x0A:
					r.append("在所（待入库）");
//					r.append("在所（已修竣）");
					break;
				case 0x0B:
					r.append("在所（待报废）");
				case 0x0D:
					r.append("到组");
					break;
			}

			if (m != null) {
				r.append("\n\n位置：");
				TbCodeEntity ce;
				switch (stu) {
					case 0x01:
					case 0x03:
					case 0x08:
					case 0x09:
					case 0x0A:
					case 0x0B:
						r.append("检测所");
						break;
					case 0x02:
						r.append("库位_");
						r.append(m.get("Code"));
						break;
					case 0x04:
					case 0x06:
					case 0x07:
						ce = SqliteHelper.queryDbCodeByType("01", m.get("Code"));
						if (ce != null) {
							r.append("单位_");
							r.append(ce.dbName);
						}
						break;
					case 0x05:
						StationEntity entityStation = SqliteHelper.queryStationByCode(m.get("Code"));
						if (entityStation != null) {
							r.append("站点_");
							r.append(entityStation.StationName);
						}
						break;
					case 0x0D:
						ce = SqliteHelper.queryDbCodeByType("02", m.get("Code"));
						if (ce != null) {
							r.append("班组_");
							r.append(ce.dbName);
						}
						break;
				}
			}
		}
		return r;
	}

}
