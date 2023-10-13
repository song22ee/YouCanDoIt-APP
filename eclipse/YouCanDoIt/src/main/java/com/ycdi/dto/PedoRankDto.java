package com.ycdi.dto;

import java.util.Date;

public class PedoRankDto {

	private Date pedometer_date;
	private int group_number = 0;
	private String mem_id = "";
	private int pedometer_result = 0;
	private int pedometer_rank = 0;
	
	public PedoRankDto() {
		
	}

	public PedoRankDto(Date pedometer_date, String mem_id, int pedometer_result) {
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

	public int getGroup_number() {
		return group_number;
	}

	public void setGroup_number(int group_number) {
		this.group_number = group_number;
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

	public int getPedometer_rank() {
		return pedometer_rank;
	}

	public void setPedometer_rank(int pedometer_rank) {
		this.pedometer_rank = pedometer_rank;
	}

}
