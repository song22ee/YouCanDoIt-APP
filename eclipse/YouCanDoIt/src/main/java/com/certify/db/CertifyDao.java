package com.certify.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import com.ConnectDB;

public class CertifyDao {
	ConnectDB connectDB = new ConnectDB();
	
	public ArrayList<GroupDto> certifyGroupSelect(String id) {
		ArrayList<GroupDto> dtoList = new ArrayList<>();
		
		try(
			Connection conn = connectDB.getConnection();
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(String.format("select * from challenge_group g "
					+ "join group_person p on p.group_number=g.group_number "
					+ "where g.group_class='2' and g.group_state='2' and mem_id='%s'", id));
		){
			while(rs.next()) {
				if(!isCertifySelect(rs.getInt("group_number"), id)) {
					GroupDto dto = new GroupDto(
							rs.getInt("group_number"), 
							rs.getString("group_name"), 
							rs.getString("group_subject"), 
							rs.getString("group_image"));
					dtoList.add(dto);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return dtoList;
	}
	
	private boolean isCertifySelect(int groupNumber, String id) {
		boolean isCertify = false;
		String today = LocalDate.now().toString();
		
		try(
			Connection conn = connectDB.getConnection();
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(String.format("select * from diy_certify "
					+ "where certify_date='%s' and group_number=%d and mem_id='%s'", today, groupNumber, id));
		){
			if(rs.next()) {
				isCertify = true;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return isCertify;
	}
	
	public void certifyInsert(DiyCertifyDto dto) {
		String today = LocalDate.now().toString();
		
		try(
			Connection conn = connectDB.getConnection();
			Statement stmt = conn.createStatement();
		){
			stmt.executeUpdate(String.format("insert into diy_certify (certify_date, group_number, mem_id, certify_image) "
					+ "values('%s', %d, '%s', '%s')", 
					today, dto.getGroup_number(), dto.getMem_id(), dto.getCertify_image()));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
