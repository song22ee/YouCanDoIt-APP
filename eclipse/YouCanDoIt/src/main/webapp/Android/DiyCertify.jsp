<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.certify.db.*" %>
<%@ page import="java.io.*" %>

<%

request.setCharacterEncoding("utf-8");

try {
	String id, number;
	Part idPart = request.getPart("id");
	Part numberPart = request.getPart("groupNumber");
	Part imagePart = request.getPart("certifyImage");
	if(idPart != null && numberPart != null && imagePart != null) {
		id = new BufferedReader(new InputStreamReader(idPart.getInputStream())).readLine();
		number = new BufferedReader(new InputStreamReader(numberPart.getInputStream())).readLine();
		
		// 실제 저장 경로 /home/yun/ycdi/build/certifyImage/파일명
		// db 저장 경로 /certifyImage/파일명
		String fileName = number + "_" + id + "_" + imagePart.getSubmittedFileName(); // 파일명 지정
		String path = "/home/yun/ycdi/build/certifyImage/"; // 저장 경로
		String backupPath = "/home/yun/ycdi/backup/certifyImage/"; // 백업본 저장 경로
		String dbName = "/certifyImage/" + fileName; //db 저장 이름
		
		imagePart.write(path + fileName); // 파일 저장
		imagePart.write(backupPath + fileName); // 백업본 저장
		
		DiyCertifyDto dto = new DiyCertifyDto(Integer.parseInt(number), id, dbName);
		
		new CertifyDao().certifyInsert(dto);
	}
	
	
	
	System.out.println("DiyCertify.jsp: 성공");
	out.println("DiyCertify.jsp: 성공");
} catch(Exception e) {
	e.printStackTrace();
	System.out.println("DiyCertify.jsp: 실패");
}
%>
