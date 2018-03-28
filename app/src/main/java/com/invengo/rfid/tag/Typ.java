package com.invengo.rfid.tag;

import java.util.HashMap;
import java.util.Map;

/**
 * 标签类型（未来计划使用配置文件）
 * Created by LZR on 2017/8/9.
 */

public class Typ {
	private static Map<String, Typ> m = new HashMap<String, Typ>();
	private String nam;
	private int ewl = 0;
	private int twl = 0;
	private int uwl = 0;
	private int bwl = 0;
	private int erl = 0;
	private int trl = 0;
	private int url = 0;
	private int brl = 0;

	private Typ () {}
	public static Typ get (String t) {
		if (m.containsKey(t)) {
			return m.get(t);
		} else {
			Typ r = new Typ();
			r.setNam(t);
			m.put(t, r);
			return r;
		}
	}

	public static Base getTag (String t) {
		String s = t.toUpperCase();
		if (s.equals("6C")) {
			return new T6C();
		} else if (s.equals("6Cnote")) {
			return new T6Cnote();
		} else if (s.equals("6Cblock")) {
			return new T6Cblock();
		} else {
			return null;
		}
	}

	private void setNam(String n) {
		nam = n.toUpperCase();
		if (nam.equals("6C")) {
			ewl = 30;
			uwl = 32;
		} else if (nam.equals("6C")) {
			ewl = 60;
			uwl = 32;
//			uwl = 64;	// 实际可写64个字节。但当指定tid进行写入时，只能将后32位写入；若写入字节长度不大于32，则能够写到前32位中。
		} else if (nam.equals("6C")) {
			ewl = 30;
			uwl = 32;
//			uwl = 64;	// 实际可写64个字节。但当指定tid进行写入时，只能将后32位写入；若写入字节长度不大于32，则能够写到前32位中。
		}
	}

	public String getNam() {
		return nam;
	}

	public int getBrl() {
		return brl;
	}

	public int getUrl() {
		return url;
	}

	public int getTrl() {
		return trl;
	}

	public int getErl() {
		return erl;
	}

	public int getBwl() {
		return bwl;
	}

	public int getUwl() {
		return uwl;
	}

	public int getEwl() {
		return ewl;
	}

	public int getTwl() {
		return twl;
	}
}
