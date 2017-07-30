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
import casia.weibo.entity.Comment;
import casia.weibo.entity.User;

public class TopicCommentDao {

	private static Logger logger = Logger.getLogger(TopicCommentDao.class);

	public boolean existsComment(long statusid) {
		boolean hasComments = false;
		String sql = "select mid from comment_info where statusid=?;";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
			int count = 1;
			ps.setLong(count++, statusid);
			rs = ps.executeQuery();
			if (rs.next()) {
				hasComments = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(rs, ps, conn);
		}
		return hasComments;
	}

	public Set<Long> getTopicCommentIdSet(int topicid) {
		Set<Long> idset = new HashSet<Long>();
		String sql = "select mid from comment_info where topicid=?;";
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

	public void insertCommentBatch(List<Comment> commentlist, int topic_id) {
		if (commentlist == null || commentlist.size() == 0)
			return;
		String sql = "insert into comment_info(mid, content, pubtime, source,"
				+ " userid, statusid, topicid)" + " values(?,?,?,?,?,?,?);";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
			for (Comment comment : commentlist) {
				int count = 1;
				ps.setLong(count++, comment.getId());
				String content = comment.getText();
				content = content.replaceAll(
						"[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]",
						"[emoji]");
				ps.setString(count++, content);
				Timestamp time = new Timestamp(0);
				if (comment.getCreatedAt() != null) {
					time.setTime(comment.getCreatedAt().getTime());
				}
				ps.setTimestamp(count++, time);
				ps.setString(count++, comment.getSource());
				long userid = 0;
				if (comment.getUser() != null)
					userid = comment.getUser().getId();
				ps.setLong(count++, userid);
				long statusid = 0;
				if (comment.getStatus() != null)
					statusid = comment.getStatus().getId();
				ps.setLong(count++, statusid);
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

	public List<Comment> getAnalysisComments(
			Map<Long, Integer> comment_topic_map) {
		List<Comment> commentlist = new ArrayList<Comment>();
		String sql = "select mid, content, topicid, userid from comment_info;";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				int count = 1;
				Comment comment = new Comment();
				comment.setId(rs.getLong(count++));
				comment.setText(rs.getString(count++));
				comment_topic_map.put(comment.getId(), rs.getInt(count++));
				User user = new User();
				user.setId(rs.getLong(count++));
				comment.setUser(user);
				commentlist.add(comment);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(rs, ps, conn);
		}
		return commentlist;
	}

	public void insertAnalysisComments(List<Comment> commentlist,
			Map<Long, Integer> comment_topic_map) {
		String sql = "insert into content_analysis(mid, content, topicid, userid, type) values (?,?,?,?,?);";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
			for (Comment comment : commentlist) {
				int count = 1;
				ps.setLong(count++, comment.getId());
				ps.setString(count++, comment.getText());
				ps.setInt(count++, comment_topic_map.get(comment.getId()));
				long userid = 0;
				if (comment.getUser() != null)
					userid = comment.getUser().getId();
				ps.setLong(count++, userid);
				ps.setInt(count++, 2);
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
