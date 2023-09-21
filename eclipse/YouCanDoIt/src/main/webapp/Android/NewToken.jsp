<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.member.db.*" %>

<%

request.setCharacterEncoding("utf-8");

try {
	String id = request.getParameter("mem_id");
	String token = request.getParameter("token");
	
	new MemberDao().tokenUpdate(id, token);
	
	System.out.println("NewToken.jsp: 성공");
	out.println("NewToken.jsp: 성공");
} catch(Exception e) {
	e.printStackTrace();
	System.out.println("NewToken.jsp: 오류");
}
%>
