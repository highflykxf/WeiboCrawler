package test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import casia.weibo.crawler.dao.DBConstants;
import casia.weibo.crawler.utils.JdbcUtil;

public class UserInfoTransferer {
	
	public static void transferUser() {
		String sql = "select u.userid, u.username, u.url"
				+ " from user_info u"
				+ " where u.lackinf=0 and u.userid NOT IN"
				+ " (select userid from user_analysis_info);";
		String inssql = "insert into user_analysis_info(userid, username, url)"
				+ " VALUES(?,?,?);";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement insps = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_DATBASE);
			insps = conn.prepareStatement(inssql);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()) {
				long userid = rs.getLong(1);
				String username = rs.getString(2);
				String url = rs.getString(3);
				
				insps.setLong(1, userid);
				insps.setString(2, username);
				insps.setString(3, url);
				insps.addBatch();
			}
			insps.executeBatch();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(rs, ps, null);
			JdbcUtil.close(null, insps, conn);
		}
	}
	
	public static void main(String[] args) {
		transferUser();
	}

}
