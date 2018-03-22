package com.invengo.rpms.entity;

public class SimpleAdapterParams {
	private int resource;
	private String[] from;
	private int[] to;

	public SimpleAdapterParams() {
	}

	public int getResource() {
		return resource;
	}

	public void setResource(int resource) {
		this.resource = resource;
	}

	public String[] getFrom() {
		return from;
	}

	public void setFrom(String[] from) {
		this.from = from;
	}

	public int[] getTo() {
		return to;
	}

	public void setTo(int[] to) {
		this.to = to;
	}

}
