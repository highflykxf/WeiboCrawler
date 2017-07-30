package test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import casia.weibo.crawler.dao.DBConstants;
import casia.weibo.crawler.utils.JdbcUtil;

public class StatusInfoTransferer {
	
	public static void transferStatus() {
		String sql = "select m.id, m.userid, m.content"
				+ " from status_info m, user_analysis_info u"
				+ " where u.`read`=1 and m.userid=u.userid and m.id NOT IN"
				+ " (select id from status_analysis_info);";
		String inssql = "insert into status_analysis_info(id, userid, content)"
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
				long id = rs.getLong(1);
				long userid = rs.getLong(2);
				String content = rs.getString(3);
				
				insps.setLong(1, id);
				insps.setLong(2, userid);
				insps.setString(3, content);
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
		transferStatus();
	}

}
