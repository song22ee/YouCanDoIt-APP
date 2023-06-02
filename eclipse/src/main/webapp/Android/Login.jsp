<%@page import="com.member.db.*"%>

<%@ page language="java" contentType="text/html; charset=utf-8"

pageEncoding="utf-8"%>

<%
Boolean canLogin = false;

MemberDao memberDao = new MemberDao();


//한글 인코딩 부분
request.setCharacterEncoding("utf-8"); 

//앱에서 입력한 id,pw 받아오기

String id = request.getParameter("id");

String pw = request.getParameter("pw");

//해당 멤버 정보 가져오기.

MemberDto dto = memberDao.selectOne(id, pw);
System.out.println(dto);

if(dto != null){
	canLogin = true;
	System.out.println("로그인 성공!");
	System.out.println("아이디: "+dto.getMem_id());
	System.out.println("닉네임: "+dto.getNickname());
	 //안드로이드로 전송
	out.println(dto.getNickname());
}else{
	System.out.println("아이디 또는 비번이 틀립니다.");
	 //안드로이드로 전송
	out.println("로그인 실패");
}





%>