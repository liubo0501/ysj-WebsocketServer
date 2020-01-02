package com.test;

import com.dao.UserDao;

public class Tesyt {

	public static void main(String[] args) {
		UserDao dao = new UserDao();
System.out.println(dao.isUser(1));
	}

}
