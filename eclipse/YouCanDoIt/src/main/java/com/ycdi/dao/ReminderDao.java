package com.ycdi.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.ConnectDB;
import com.ycdi.dto.ReminderDto;

public class ReminderDao {
ConnectDB connectDB = new ConnectDB();
	
	public ArrayList<ReminderDto> reminderSelect(String id) {
		ArrayList<ReminderDto> dtoList = new ArrayList<>();
		
		try(
			Connection conn = connectDB.getConnection();
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(String.format("select * from reminder where mem_id='%s' order by reminder_date desc", id));
		){
			while(rs.next()) {
				ReminderDto dto = new ReminderDto();
				dto.setReminder_contents(rs.getString("reminder_contents"));
				dtoList.add(dto);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return dtoList;
	}

}