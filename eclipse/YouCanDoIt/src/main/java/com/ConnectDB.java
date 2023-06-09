package com;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectDB {
	private static ConnectDB instance = new ConnectDB();
	
	public ConnectDB() {}
	
	public static ConnectDB getInstance() {
		return instance;
	}
	
	
	public Connection getConnection() throws Exception{
		Class.forName("org.mariadb.jdbc.Driver");
		 
		String dbUrl = "jdbc:mariadb://ycdi.cafe24.com:3306/ycdi";
	 	String dbUser = "ycdi";
	 	String dbPw = "ycdi2023!";
	 	
	 	return DriverManager.getConnection(dbUrl,dbUser,dbPw);
	 	
	}
	
	

}
