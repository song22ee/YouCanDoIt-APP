package com.ycdi.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import com.ConnectDB;
import com.ycdi.dto.ScheduleDto;

public class ScheduleDao {
ConnectDB connectDB = new ConnectDB();
	
	public ArrayList<ScheduleDto> todayScheduleSelect(String id) {
		ArrayList<ScheduleDto> dtoList = new ArrayList<>();
		
		String today = LocalDate.now().toString();
		
		try(
			Connection conn = connectDB.getConnection();
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery("select * from schedule where mem_id='" + id 
					+ "' and date_format(schedule_startdate, '%Y-%m-%d')='" + today + "' order by schedule_startdate");
		){
			while(rs.next()) {
				ScheduleDto dto = new ScheduleDto(
						rs.getInt("schedule_number"), 
						rs.getString("schedule_title"), 
						rs.getString("schedule_startdate"), 
						rs.getString("schedule_enddate"),
						rs.getString("schedule_success"));
				dtoList.add(dto);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return dtoList;
	}
	
	public void scheduleUpdate(int schedule_number) {
		try(
			Connection conn = connectDB.getConnection();
			Statement stmt = conn.createStatement();
		){
			stmt.executeUpdate(String.format("update schedule set schedule_success='%s' where schedule_number=%d", 
					"1", schedule_number));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
