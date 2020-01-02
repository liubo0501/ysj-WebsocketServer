package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.util.DruidUtil;

public class UserDao {
	public boolean isUser(int userId) {
	Connection conn = null;
	PreparedStatement ps=null;
	ResultSet rs=null;
	try {
		conn = DruidUtil.getConnection();
		String sql = "select count(*) count from T_USER_INFO where id = ?";
		ps = conn.prepareStatement(sql);
		ps.setInt(1,userId);
		rs = ps.executeQuery();
		while(rs.next()) {
			return rs.getInt("count")>0;
		}
	}catch(Exception e) {
		
	}finally {
		DruidUtil.close(conn, ps, rs);
	}
	return false;
	
}
	
	public boolean isPad(int padId) {
	Connection conn = null;
	PreparedStatement ps=null;
	ResultSet rs=null;
	try {
		conn = DruidUtil.getConnection();
		String sql = "select count(*) count from T_LIVE_INFO where id = ?";
		ps = conn.prepareStatement(sql);
		ps.setInt(1,padId);
		rs = ps.executeQuery();
		while(rs.next()) {
			return rs.getInt("count")>0;
		}
	}catch(Exception e) {
		
	}finally {
		DruidUtil.close(conn, ps, rs);
	}
	return false;
	
}
}
