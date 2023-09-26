package com.ycdi.dto;

public class ReminderDto {
	private int reminder_number;
	private String mem_id;
	private String reminder_contents;
	private String reminder_date;

	public int getReminder_number() {
		return reminder_number;
	}

	public void setReminder_number(int reminder_number) {
		this.reminder_number = reminder_number;
	}

	public String getMem_id() {
		return mem_id;
	}

	public void setMem_id(String mem_id) {
		this.mem_id = mem_id;
	}

	public String getReminder_contents() {
		return reminder_contents;
	}

	public void setReminder_contents(String reminder_contents) {
		this.reminder_contents = reminder_contents;
	}

	public String getReminder_date() {
		return reminder_date;
	}

	public void setReminder_date(String reminder_date) {
		this.reminder_date = reminder_date;
	}

}
