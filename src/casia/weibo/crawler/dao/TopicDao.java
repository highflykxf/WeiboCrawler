package casia.weibo.crawler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import casia.weibo.crawler.utils.JdbcUtil;

public class TopicDao {

	private static Logger logger = Logger.getLogger(TopicDao.class);

	public void insertTopic(String topic_name, String topic_mid) {
		String sql = "insert into topic_info(topic_tag, topic_name) values(?,?);";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
			int count = 1;
			ps.setString(count++, topic_mid);
			ps.setString(count++, topic_name);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(null, ps, conn);
		}
	}

	public String getTopicTagById(int topic_id) {
		String topic_tag = null;
		String sql = "select topic_tag from topic_info where topicid=?;";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
			int count = 1;
			ps.setInt(count++, topic_id);
			rs = ps.executeQuery();
			if (rs.next()) {
				topic_tag = rs.getString(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(rs, ps, conn);
		}
		return topic_tag;
	}
}
