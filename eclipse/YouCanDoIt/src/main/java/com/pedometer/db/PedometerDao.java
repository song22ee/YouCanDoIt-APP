package com.pedometer.db;

import java.sql.Connection;
import java.sql.Statement;

import com.ConnectDB;

public class PedometerDao {
	ConnectDB connectDB = new ConnectDB();
	
	public void pedometerInsert(PedometerDto dto) {
		try(
			Connection conn = connectDB.getConnection();
			Statement stmt = conn.createStatement();
		){
			stmt.executeUpdate(String.format("insert into pedometer (pedometer_date, mem_id, pedometer_result) "
					+ "values('%tF', '%s', '%d')", 
					dto.getPedometer_date(), dto.getMem_id(), dto.getPedometer_result()));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
