package com.ycdi.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.Part;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ycdi.dao.CertifyDao;
import com.ycdi.dao.GodlifeGoalDao;
import com.ycdi.dao.MemberDao;
import com.ycdi.dao.PedoRankDao;
import com.ycdi.dao.PedometerDao;
import com.ycdi.dao.ReminderDao;
import com.ycdi.dao.ScheduleDao;
import com.ycdi.dto.DiyCertifyDto;
import com.ycdi.dto.GroupDto;
import com.ycdi.dto.MemberDto;
import com.ycdi.dto.PedoRankDto;
import com.ycdi.dto.PedometerDto;
import com.ycdi.dto.ReminderDto;
import com.ycdi.dto.ScheduleDto;

public class Service {
	
	/** 로그인 */
	public String login(String id, String pw) {
		//해당 멤버 정보 가져오기.
		MemberDto dto = new MemberDao().selectOne(id, pw);
		System.out.println(dto);

		if(dto != null){
			System.out.println("로그인 성공!");
			System.out.println("아이디: "+dto.getMem_id());
			System.out.println("닉네임: "+dto.getNickname());
			return dto.getNickname();
		}else{
			System.out.println("아이디 또는 비번이 틀립니다.");
			return "로그인 실패";
		}
	}
	
	/** firebase 토큰 저장 */
	public String newToken(String id, String token) {
		new MemberDao().tokenUpdate(id, token);
		
		System.out.println("토큰 저장 완료.");
		return "newToken : 성공";
	}
	
	/** 만보기값 업데이트 */
	public String pedometerUpdate(String dateStr, String id, int pedometer, String isLast) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date = formatter.parse(dateStr);
			
			//dto 세팅.
			PedoRankDto pedoRankDto = new PedoRankDto(date, id, pedometer);

			//만보기 결과값 업데이트.
			new PedoRankDao().updateOne(pedoRankDto);
			
			if(isLast.equals("1")) {
				System.out.println("자정입니다.");
				
				PedometerDto pedometerDto = new PedometerDto(date, id, pedometer);
				new PedometerDao().pedometerInsert(pedometerDto);
			}
			
			System.out.println("만보기값 업데이트 완료.");
			// 안드로이드로 전송
			return "pedometerUpdate : 성공";
		} catch (ParseException e) {
			e.printStackTrace();
			return "pedometerUpdate : 실패";
		}
	}
	
	/** 만보기 목표 조회 */
	public int pedometerGoalSelect(String id) {
		int goal = new GodlifeGoalDao().pedometerGoalSelect(id);
		
		System.out.println("만보기 목표 조회 완료. : " + goal);
		return goal;
	}
	
	/** 만보기 목표 설정 */
	public String pedometerGoalUpdate(String id, int goal) {
		new GodlifeGoalDao().pedometerGoalUpdate(id, goal);
		
		System.out.println("만보기 목표 설정 완료. : " + goal);
		return "pedometerGoalUpdate : 성공";
	}
	
	/** 인증이 필요한 diy 챌린지 그룹 조회 */
	@SuppressWarnings("unchecked")
	public JSONArray diyGroupSelect(String id) {
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
		
		System.out.println("diy 챌린지 그룹 조회 완료.");
		System.out.println(jArray);
		return jArray;
	}

	/** diy 챌린지 인증 */
	public String diyCertify(Part idPart, Part numberPart, Part imagePart) {
		try {
			if(idPart != null && numberPart != null && imagePart != null) {
				String id = new BufferedReader(new InputStreamReader(idPart.getInputStream())).readLine();
				String number = new BufferedReader(new InputStreamReader(numberPart.getInputStream())).readLine();
				
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
			System.out.println("diy 챌린지 인증 완료");
			return "diyCertify : 성공";
		} catch (IOException e) {
			e.printStackTrace();
			return "diyCertify : 실패";
		}
	}
	
	/** 오늘의 일정 조회 */
	@SuppressWarnings("unchecked")
	public JSONArray todayScheduleSelect(String id) {
		ArrayList<ScheduleDto> dtoList = new ScheduleDao().todayScheduleSelect(id);
		
		JSONArray jArray = new JSONArray();
		for(ScheduleDto dto : dtoList) {
			JSONObject jObject = new JSONObject();
			jObject.put("scheduleNumber", dto.getSchedule_number());
			jObject.put("scheduleTitle", dto.getSchedule_title());
			jObject.put("scheduleStartDate", dto.getSchedule_startdate());
			jObject.put("scheduleEndDate", dto.getSchedule_enddate());
			jObject.put("scheduleSuccess", dto.getSchedule_success());
			jArray.add(jObject);
		}
		
		System.out.println("오늘의 일정 조회 완료.");
		System.out.println(jArray);
		return jArray;
	}
	
	/** 일정 체크 or 해제 */
	public String scheduleChecked(int number, String success) {
		new ScheduleDao().scheduleUpdate(number, success);
		
		System.out.println("일정 업데이트 완료.");
		return "scheduleChecked : 성공";
	}
	
	/** 리마인더 조회 */
	@SuppressWarnings("unchecked")
	public JSONArray reminderSelect(String id) {
		ArrayList<ReminderDto> dtoList = new ReminderDao().reminderSelect(id);
		
		JSONArray jArray = new JSONArray();
		for(ReminderDto dto : dtoList) {
			jArray.add(dto.getReminder_contents());
		}
		
		System.out.println("리마인더 조회 완료.");
		System.out.println(jArray);
		return jArray;
	}
}
