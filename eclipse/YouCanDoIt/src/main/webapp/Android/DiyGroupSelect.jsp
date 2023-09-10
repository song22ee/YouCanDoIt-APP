<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.certify.db.*" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import= "org.json.simple.*" %>

<%

request.setCharacterEncoding("utf-8");

try {
	String id = request.getParameter("mem_id");
	
	ArrayList<GroupDto> dtoList = new CertifyDao().certifyGroupSelect(id);
	
	JSONArray jArray = new JSONArray();
	for(GroupDto dto : dtoList) {
		JSONObject jObject = new JSONObject();
		jObject.put("groupNumber", dto.getGroup_number());
		jObject.put("groupName", dto.getGroup_name());
		jObject.put("groupSubject", dto.getGroup_subject());
		jObject.put("groupImage", dto.getGroup_image());
		jArray.add(jObject);
	}
	
	System.out.println("DiyGroupSelect.jsp: 성공");
	System.out.println(jArray);
	out.println(jArray);
} catch(Exception e) {
	e.printStackTrace();
	System.out.println("DiyGroupSelect.jsp: 오류");
}
%>
