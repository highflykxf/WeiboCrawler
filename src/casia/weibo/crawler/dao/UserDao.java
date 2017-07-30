package casia.weibo.crawler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import casia.weibo.crawler.utils.JdbcUtil;
import casia.weibo.entity.User;

public class UserDao {
	
	private static Logger logger = Logger.getLogger(UserDao.class);
	
	public void insertUser(User user) {
		if(user == null)
			return;
		
		String sql = "insert into user_info(userid, username, location, description, url, imgurl, "
				+ "gender, followercount, friendcount, statuscount, verified, lackinf) "
				+ "values (?,?,?,?,?,?,?,?,?,?,?,?);";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_DATBASE);
			ps = conn.prepareStatement(sql);
			int count = 1;
			ps.setLong(count++, user.getId());
			ps.setString(count++, user.getName());
			ps.setString(count++, user.getLocation());
			ps.setString(count++, user.getDescription());
			ps.setString(count++, user.getUrl());
			ps.setString(count++, user.getProfileImageUrl());
			ps.setString(count++, user.getGender());
			ps.setInt(count++, user.getFollowersCount());
			ps.setInt(count++, user.getFriendsCount());
			ps.setInt(count++, user.getStatusesCount());
			ps.setInt(count++, user.isVerifed()?1:0);
			ps.setInt(count++, user.isLackInf()?1:0);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(null, ps, conn);
		}
	}
	
	public boolean existUser(User user) {
		if(user == null) {
			return true;
		}
		
		boolean existed = false;
		String sql = "select userid from user_info where userid=?";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_DATBASE);
			ps = conn.prepareStatement(sql);
			ps.setLong(1, user.getId());
			rs = ps.executeQuery();
			if(rs.next()) {
				existed = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(rs, ps, conn);
		}
		return existed;
	}

}
