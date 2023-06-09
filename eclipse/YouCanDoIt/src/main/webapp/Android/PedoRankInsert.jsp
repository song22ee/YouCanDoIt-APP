<%@page import="com.pedoRank.db.*"%>
<%@page import="java.util.Date" %>
<%@page import="java.util.Objects" %>
<%@page import="java.text.SimpleDateFormat" %>
<%@page import="java.text.ParseException" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"

pageEncoding="UTF-8"%>

<%

PedoRankDao pedoRankDao = new PedoRankDao();
PedoRankDto pedoRankDto = new PedoRankDto();

//한글 인코딩 부분

request.setCharacterEncoding("utf-8");

try{
	
	//앱에서 값 받아오기.

	String dateStr = request.getParameter("pedometer_date");

	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	Date date = formatter.parse(dateStr);

	int group = Integer.parseInt(request.getParameter("group_number"));

	String id = request.getParameter("mem_id");

	//만약 레코드가 이미 있다면 insert 하지 않기. 
	PedoRankDto result = new PedoRankDao().selectOne(date, group, id);
	System.out.println("PedoRankInsert.jsp: date = "+date);
	System.out.println("PedoRankInsert.jsp: selectOne(date, group, id) = "+result);

	if(Objects.isNull(result)){
		//dto 세팅.
		pedoRankDto.setPedometer_date(date);
		pedoRankDto.setGroup_number(group);
		pedoRankDto.setMem_id(id);
		
		//만보기 레코드 추가.
		pedoRankDao.insertOne(pedoRankDto);
		
		System.out.println("PedoRankInsert.jsp: 성공");
		
		// 안드로이드로 전송

		out.println("PedoRankInsert.jsp: 성공");
	}else{
		System.out.println("PedoRankInsert.jsp: 레코드가 이미 있음.");
		
		// 안드로이드로 전송

		out.println("PedoRankInsert.jsp: 레코드가 이미 있음.");
	}

	
}
catch(Exception e){
	e.printStackTrace();
	System.out.println("PedoRankInsert.jsp: 실패");
	// 안드로이드로 전송
	out.println("PedoRankInsert.jsp: 실패");
}





%>