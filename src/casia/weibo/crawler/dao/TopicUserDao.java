package casia.weibo.crawler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import casia.weibo.crawler.utils.JdbcUtil;
import casia.weibo.entity.User;

public class TopicUserDao {

	private static Logger logger = Logger.getLogger(TopicUserDao.class);

	public Set<Long> getAllUsersIdSet() {
		Set<Long> idset = new HashSet<Long>();
		String sql = "select userid from user_info;";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
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

	public void insertUsersBatch(List<User> userlist) {
		if (userlist == null || userlist.size() == 0)
			return;
		String sql = "insert into user_info(userid, username, location, description,"
				+ " url, imgurl, gender, follower_count, friend_count, status_count, verified, verified_note)"
				+ " values(?,?,?,?,?,?,?,?,?,?,?,?);";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_TOPIC_DATBASE);
			ps = conn.prepareStatement(sql);
			for (User user : userlist) {
				int count = 1;
				ps.setLong(count++, user.getId());
				ps.setString(count++, user.getName());
				ps.setString(count++, user.getLocation());
				ps.setString(count++, user.getDescription());
				ps.setString(count++, user.getUrl());
				ps.setString(count++, user.getProfileImageUrl());
				int gender = 0;
				if (user.getGender() != null
						&& !"".equals(user.getGender().trim())) {
					gender = Integer.parseInt(user.getGender().trim());
				}
				ps.setInt(count++, gender);
				ps.setInt(count++, user.getFollowersCount());
				ps.setInt(count++, user.getFriendsCount());
				ps.setInt(count++, user.getStatusesCount());
				ps.setInt(count++, user.isVerifed() ? 1 : 0);
				ps.setString(count++, user.getVerifedDescription());
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
