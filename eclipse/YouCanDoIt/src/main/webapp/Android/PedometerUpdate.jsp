<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>
<%@ page import="com.pedoRank.db.*"%>
<%@ page import="com.pedometer.db.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%

PedoRankDao pedoRankDao = new PedoRankDao();

//한글 인코딩 부분
request.setCharacterEncoding("utf-8");

try{
	//앱에서 값 받아오기.
	String dateStr = request.getParameter("date");
	String id = request.getParameter("mem_id");
	int pedometer = Integer.parseInt(request.getParameter("pedometer_result"));
	String isLast = request.getParameter("is_last");
	
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	Date date = formatter.parse(dateStr);

	//dto 세팅.
	PedoRankDto pedoRankDto = new PedoRankDto(date, id, pedometer);

	//만보기 결과값 업데이트.
	pedoRankDao.updateOne(pedoRankDto);
	
	if(isLast.equals("1")) {
		System.out.println("자정입니다.");
		PedometerDao pedometerDao = new PedometerDao();
		PedometerDto pedometerDto = new PedometerDto(date, id, pedometer);
		
		pedometerDao.pedometerInsert(pedometerDto);
	}
	
	System.out.println("PedometerUpdate.jsp: 성공");
	// 안드로이드로 전송
	out.println("PedometerUpdate.jsp: 성공");
}
catch(Exception e){
	e.printStackTrace();
	System.out.println("PedometerUpdate.jsp: 실패");
	// 안드로이드로 전송
	out.println("PedometerUpdate.jsp: 실패");
}
%>