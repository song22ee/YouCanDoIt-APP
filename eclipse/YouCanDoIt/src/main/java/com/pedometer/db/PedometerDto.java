package com.pedometer.db;

import java.util.Date;

public class PedometerDto {
	private Date pedometer_date;
	private String mem_id;
	private int pedometer_result;
	
	public PedometerDto() {
		
	}
	
	public PedometerDto(Date pedometer_date, String mem_id, int pedometer_result) {
		this.pedometer_date = pedometer_date;
		this.mem_id = mem_id;
		this.pedometer_result = pedometer_result;
	}

	public Date getPedometer_date() {
		return pedometer_date;
	}

	public void setPedometer_date(Date pedometer_date) {
		this.pedometer_date = pedometer_date;
	}

	public String getMem_id() {
		return mem_id;
	}

	public void setMem_id(String mem_id) {
		this.mem_id = mem_id;
	}

	public int getPedometer_result() {
		return pedometer_result;
	}

	public void setPedometer_result(int pedometer_result) {
		this.pedometer_result = pedometer_result;
	}

}
