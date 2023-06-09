<%@page import="com.pedoRank.db.*"%>
<%@page import="java.util.Date" %>
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

	String id = request.getParameter("mem_id");

	int pedometer = Integer.parseInt(request.getParameter("pedometer_result"));
	

	//dto 세팅.
	pedoRankDto.setPedometer_date(date);
	pedoRankDto.setMem_id(id);
	pedoRankDto.setPedometer_result(pedometer);

	//만보기 결과값 업데이트.
	pedoRankDao.updateOne(pedoRankDto);
	
	System.out.println("PedoRankUpdate.jsp: 성공");
	
	// 안드로이드로 전송

	out.println("PedoRankUpdate.jsp: 성공");
}
catch(Exception e){
	e.printStackTrace();
	System.out.println("PedoRankUpdate.jsp: 실패");
	// 안드로이드로 전송
	out.println("PedoRankUpdate.jsp: 실패");
}





%>