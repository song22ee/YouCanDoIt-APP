<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.goal.db.*" %>
<%@ page import="java.util.ArrayList" %>

<%

request.setCharacterEncoding("utf-8");

try {
	String id = request.getParameter("mem_id");
	
	int goal = new GodlifeGoalDao().pedometerGoalSelect(id);
	
	
	System.out.println("PedometerGoalSelect.jsp: 성공");
	out.println(goal);
} catch(Exception e) {
	e.printStackTrace();
	System.out.println("PedometerGoalSelect.jsp: 오류");
}
%>
