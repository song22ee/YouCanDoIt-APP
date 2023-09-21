package com.member.db;
import com.ConnectDB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;



public class MemberDao {
	ConnectDB connectDB = new ConnectDB();
	
	public MemberDto selectOne(String id, String pwd) {
		MemberDto dto = new MemberDto();
		
		try(
				Connection conn = connectDB.getConnection();
				Statement stmt = conn.createStatement();
				
				ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM member "
						+ "WHERE mem_id = '%s' and password = '%s'"
						,id,pwd));
					){
					if(rs.next()) {
						System.out.print(rs);
						dto.setMem_id(rs.getString("mem_id"));
						dto.setPassword(rs.getString("password"));
						dto.setNickname(rs.getString("nickname"));
						dto.setPhone_number(rs.getString("phone_number"));
						dto.setProfile_picture(rs.getString("profile_picture"));
						dto.setJoin_date(rs.getDate("join_date"));
						dto.setMem_class(rs.getString("mem_class"));
					}else //아이디 비번 잘못입력함.
						return null;
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			return dto;
	}
	
	public void tokenUpdate(String id, String token) {
		try(
				Connection conn = connectDB.getConnection();
				Statement stmt = conn.createStatement();
			){
				stmt.executeUpdate(String.format("update member set mobile_token='%s' where mem_id='%s'", token, id));
			} catch(Exception e) {
				e.printStackTrace();
			}
	}
	
}
