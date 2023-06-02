package com.pedoRank.db;
import com.ConnectDB;

import java.sql.Connection;
import java.util.Date;
import java.sql.ResultSet;
import java.sql.Statement;

public class PedoRankDao {
	ConnectDB connectDB = new ConnectDB();
	
	public PedoRankDto selectOne(Date date,int group, String id) {
		PedoRankDto dto = new PedoRankDto();
		
		try(
			Connection conn = connectDB.getConnection();
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(String.format("select * from pedometer_ranking "
					+ "where pedometer_date='%tF' and group_number=%d and mem_id='%s'"
					,date,group,id));
				){
				if(rs.next()) {
					dto.setPedometer_date(rs.getDate("pedometer_date"));
					dto.setGroup_number(rs.getInt("group_number"));
					dto.setMem_id(rs.getString("mem_id"));
					dto.setPedometer_result(rs.getInt("pedometer_result"));
					dto.setPedometer_rank(rs.getInt("pedometer_rank"));
					System.out.println("PedoRankDao.java : selectOne if문에 들어옴 dto= "+dto);
				}else {
					dto = null;
					System.out.println("PedoRankDao.java : selectOne else문에 들어옴 dto= "+dto);
				}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return dto;
	}

	public void insertOne(PedoRankDto dto) {
		try(
		 		Connection conn = connectDB.getConnection();
				Statement stmt = conn.createStatement();
			){
				System.out.println(String.format("insert into pedometer_ranking (pedometer_date,group_number,mem_id,pedometer_result,pedometer_rank) "
		 				+ "values('%tF',%d,'%s',0,null)", 
		 				dto.getPedometer_date(),dto.getGroup_number(),dto.getMem_id(),0));
				stmt.executeUpdate(String.format("insert into pedometer_ranking (pedometer_date,group_number,mem_id,pedometer_result,pedometer_rank) "
		 				+ "values('%tF',%d,'%s',0,null)", 
		 				dto.getPedometer_date(),dto.getGroup_number(),dto.getMem_id(),0));
		 	}
		 	catch(Exception e){
		 		e.printStackTrace();
		 	}
	}
	
	public void updateOne(PedoRankDto dto)
	{
		try(
		 		Connection conn = connectDB.getConnection();
				Statement stmt = conn.createStatement();
			){
				System.out.println(String.format("update pedometer_ranking set pedometer_result = '%d'"
		 				+ "where pedometer_date = '%tF' and group_number= %d and mem_id='%s'" , 
		 				dto.getPedometer_result(),dto.getPedometer_date(),dto.getGroup_number(),dto.getMem_id()));
				
		 		stmt.executeUpdate(String.format("update pedometer_ranking set pedometer_result = '%d'"
		 				+ "where pedometer_date = '%tF' and group_number= %d and mem_id='%s'" , 
		 				dto.getPedometer_result(),dto.getPedometer_date(),dto.getGroup_number(),dto.getMem_id()));
		 	}
		 	catch(Exception e){
		 		e.printStackTrace();
		 	}
	}


}
