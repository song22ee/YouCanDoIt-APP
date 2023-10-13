package com.ycdi.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ycdi.service.Service;

@WebServlet("/")
public class Controller extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public Controller() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//한글 인코딩 부분
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		
		String uri = request.getRequestURI();
		String conPath = request.getContextPath();
		String com = uri.substring(conPath.length());
		
		Service service = new Service();
		
		Object result = null;
		
		switch(com) {
		case "/login":
			result = service.login(request.getParameter("id"), request.getParameter("pw"));
			break;
			
		case "/newToken":
			result = service.newToken(request.getParameter("id"), request.getParameter("token"));
			break;
			
		case "/pedometerUpdate":
			String dateStr = request.getParameter("date");
			String id = request.getParameter("id");
			int pedometer = Integer.parseInt(request.getParameter("pedometer_result"));
			String isLast = request.getParameter("last");
			result = service.pedometerUpdate(dateStr, id, pedometer, isLast);
			break;
			
		case "/pedometerGoalSelect":
			result = service.pedometerGoalSelect(request.getParameter("id"));
			break;
			
		case "/pedometerGoalUpdate":
			result = service.pedometerGoalUpdate(request.getParameter("id"), Integer.parseInt(request.getParameter("goal")));
			break;
			
		case "/diyGroupSelect":
			result = service.diyGroupSelect(request.getParameter("id"));
			break;
			
		case "/diyCertify":
			result = service.diyCertify(request.getPart("id"), request.getPart("groupNumber"), request.getPart("certifyImage"));
			break;
			
		case "/todayScheduleSelect":
			result = service.todayScheduleSelect(request.getParameter("id"));
			break;
			
		case "/scheduleChecked":
			result = service.scheduleChecked(Integer.parseInt(request.getParameter("schedule_number")), request.getParameter("success"));
			break;
			
		case "/reminderSelect":
			result = service.reminderSelect(request.getParameter("id"));
			break;
			
		default:
			result = "Invalid URL";
		}
		
		out.print(result);	
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
