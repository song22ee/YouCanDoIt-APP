package com.goal.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.ConnectDB;

public class GodlifeGoalDao {
	ConnectDB connectDB = new ConnectDB();
	
	public int pedometerGoalSelect(String id) {
		int goal = 0;
		try(
			Connection conn = connectDB.getConnection();
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(String.format("select * from godlife_goal where mem_id='%s'", id));
		){
			if(rs.next()) {
				goal = rs.getInt("goal_pedometer");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return goal;
	}
	
	public void pedometerGoalUpdate(String id, int goal) {
		try(
				Connection conn = connectDB.getConnection();
				Statement stmt = conn.createStatement();
			){
				stmt.executeUpdate(String.format("update godlife_goal set goal_pedometer=%d where mem_id='%s'", goal, id));
			} catch(Exception e) {
				e.printStackTrace();
			}
	}
}
