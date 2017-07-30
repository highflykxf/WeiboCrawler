package casia.weibo.crawler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import casia.weibo.crawler.utils.JdbcUtil;
import casia.weibo.entity.Status;
import casia.weibo.entity.User;

public class TopicStatusDao {

	private static Logger logger = Logger.getLogger(TopicStatusDao.class);

	public Set<Long> getTopicStatusIdSet(int topicid) {
		Set<Long> idset = new HashSet<Long>();
		String sql = "select mid from status_info where topicid=?;";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
			int count = 1;
			ps.setInt(count++, topicid);
			rs = ps.executeQuery();
			while (rs.next()) {
				idset.add(rs.getLong(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(rs, ps, conn);
		}
		return idset;
	}

	public List<Long> getTopicStatusIdSet(int topicid, int min_cmt_count) {
		List<Long> idset = new ArrayList<Long>();
		String sql = "select mid from status_info where topicid=? and cmtcount>=? order by cmtcount desc;";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
			int count = 1;
			ps.setInt(count++, topicid);
			ps.setInt(count++, min_cmt_count);
			rs = ps.executeQuery();
			while (rs.next()) {
				idset.add(rs.getLong(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(rs, ps, conn);
		}
		return idset;
	}

	public void insertStatusBatch(List<Status> statuslist, int topic_id) {
		if (statuslist == null || statuslist.size() == 0)
			return;
		String sql = "insert into status_info(mid, content, pubtime, source,"
				+ " source_url, pic_url, retid, userid,"
				+ "url, rttcount, cmtcount,topicid)"
				+ " values(?,?,?,?,?,?,?,?,?,?,?,?);";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
			for (Status status : statuslist) {
				int count = 1;
				ps.setLong(count++, status.getId());
				String content = status.getText();
				content = content.replaceAll(
						"[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]",
						"[emoji]");
				ps.setString(count++, content);
				Timestamp time = new Timestamp(0);
				if (status.getCreatedAt() != null) {
					time.setTime(status.getCreatedAt().getTime());
				}
				ps.setTimestamp(count++, time);
				ps.setString(count++, status.getSource());
				ps.setString(count++, status.getSourceUrl());
				ps.setString(count++, status.getThumbnail_pic());
				long retid = 0;
				if (status.getRetweeted_status() != null)
					retid = status.getRetweeted_status().getId();
				ps.setLong(count++, retid);
				long userid = 0;
				if (status.getUser() != null)
					userid = status.getUser().getId();
				ps.setLong(count++, userid);
				ps.setString(count++, status.getUrl());
				ps.setInt(count++, status.getRttCount());
				ps.setInt(count++, status.getCommCount());
				ps.setInt(count++, topic_id);
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(null, ps, conn);
		}
	}

	public List<Status> getAnalysisStatus(Map<Long, Integer> status_topic_map) {
		List<Status> statuslist = new ArrayList<Status>();
		String sql = "select mid, content, topicid, userid from status_info;";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				int count = 1;
				Status status = new Status();
				status.setId(rs.getLong(count++));
				status.setText(rs.getString(count++));
				status_topic_map.put(status.getId(), rs.getInt(count++));
				User user = new User();
				user.setId(rs.getLong(count++));
				status.setUser(user);
				statuslist.add(status);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(rs, ps, conn);
		}
		return statuslist;
	}

	public void insertAnalysisStatus(List<Status> statuslist,
			Map<Long, Integer> status_topic_map) {
		String sql = "insert into content_analysis(mid, content, topicid, userid, type) values (?,?,?,?,?);";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
			for (Status status : statuslist) {
				int count = 1;
				ps.setLong(count++, status.getId());
				ps.setString(count++, status.getText());
				ps.setInt(count++, status_topic_map.get(status.getId()));
				long userid = 0;
				if (status.getUser() != null)
					userid = status.getUser().getId();
				ps.setLong(count++, userid);
				ps.setInt(count++, 1);
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(null, ps, conn);
		}
	}
}
