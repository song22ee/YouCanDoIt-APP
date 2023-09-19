<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.goal.db.*" %>
<%@ page import="java.util.ArrayList" %>

<%

request.setCharacterEncoding("utf-8");

try {
	int goal = Integer.parseInt(request.getParameter("goal"));
	String id = request.getParameter("mem_id");
	
	new GodlifeGoalDao().pedometerGoalUpdate(id, goal);
	
	
	System.out.println("PedometerGoalUpdate.jsp: 성공");
	out.println("PedometerGoalUpdate.jsp: 성공");
} catch(Exception e) {
	e.printStackTrace();
	System.out.println("PedometerGoalUpdate.jsp: 오류");
}
%>
