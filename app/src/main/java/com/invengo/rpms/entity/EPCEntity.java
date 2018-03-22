package com.invengo.rpms.entity;

import java.io.Serializable;

public class EPCEntity implements Serializable {

	private int number;
	private String epcData;
	
	public EPCEntity() {
		
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getEpcData() {
		return epcData;
	}

	public void setEpcData(String epcData) {
		this.epcData = epcData;
	}
}
