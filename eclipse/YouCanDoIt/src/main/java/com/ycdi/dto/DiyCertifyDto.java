package com.ycdi.dto;

import java.sql.Date;

public class DiyCertifyDto {
	private Date certify_date;
	private int group_number;
	private String mem_id;
	private String certify_image;
	private int opposite_count;
	
	public DiyCertifyDto(int group_number, String mem_id, String certify_image) {
		this.group_number = group_number;
		this.mem_id = mem_id;
		this.certify_image = certify_image;
	}

	public Date getCertify_date() {
		return certify_date;
	}

	public void setCertify_date(Date certify_date) {
		this.certify_date = certify_date;
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

	public String getCertify_image() {
		return certify_image;
	}

	public void setCertify_image(String certify_image) {
		this.certify_image = certify_image;
	}

	public int getOpposite_count() {
		return opposite_count;
	}

	public void setOpposite_count(int opposite_count) {
		this.opposite_count = opposite_count;
	}

}
