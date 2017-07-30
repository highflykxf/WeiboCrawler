package casia.weibo.crawler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import casia.weibo.crawler.utils.JdbcUtil;

public class UserAnalysisDao {

	private static Logger logger = Logger.getLogger(UserAnalysisDao.class);

	public void getAnalysisUserIds(List<Long> idlist, long startid, long endid) {
		if (idlist == null)
			return;

		String sql = "select userid from user_analysis_info where userid>=? and userid<=?";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_DATBASE);
			ps = conn.prepareStatement(sql);
			ps.setLong(1, startid);
			ps.setLong(2, endid);
			rs = ps.executeQuery();
			while (rs.next()) {
				idlist.add(rs.getLong(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(rs, ps, conn);
		}
	}

	public void getExtraUserStatus(Map<String, String> urlmap) {
		if (urlmap == null)
			return;
		String sql = "select u.userid, m.id, m.picurl"
				+ " from user_analysis_info u, status_info m"
				+ " where u.extraversion=1 and u.userid=m.userid";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_DATBASE);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				long uid = rs.getLong(1);
				long mid = rs.getLong(2);
				String url = rs.getString(3);
				if (url == null || url.equals(""))
					continue;
				String[] tags = url.split("\\|\\|");
				for (String tag : tags) {
					if (tag.length() == 0)
						continue;
					String name = tag.substring(tag.lastIndexOf("/") + 1);
					urlmap.put(uid + "_" + mid + "_" + name, tag);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(rs, ps, conn);
		}
	}
}
