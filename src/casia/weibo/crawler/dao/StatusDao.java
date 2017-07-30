package casia.weibo.crawler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;

import casia.weibo.crawler.utils.JdbcUtil;
import casia.weibo.entity.Status;

public class StatusDao {
	private static Logger logger = Logger.getLogger(StatusDao.class);

	public void insertStatus(Status status) {
		if (status == null)
			return;
		String sql = "insert into status_info(id, content, pubtime, source, sourceurl, picurl, retid, userid,"
				+ "url, rttcount, commcount)"
				+ " values(?,?,?,?,?,?,?,?,?,?,?);";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_DATBASE);
			ps = conn.prepareStatement(sql);
			int count = 1;
			ps.setLong(count++, status.getId());
			ps.setString(count++, status.getText());
			ps.setTimestamp(count++, new Timestamp(status.getCreatedAt()
					.getTime()));
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

			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(e.getMessage());
			logger.error(Thread.currentThread().getName() + "\tSQLException");
		} finally {
			JdbcUtil.close(null, ps, conn);
		}
	}

	public void insertStatusBatch(List<Status> statuslist) {
		if (statuslist == null || statuslist.size() == 0)
			return;
		String sql = "insert into status_info(id, content, pubtime, source,"
				+ " sourceurl, picurl, retid, userid,"
				+ "url, rttcount, commcount)"
				+ " values(?,?,?,?,?,?,?,?,?,?,?);";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_DATBASE);
			ps = conn.prepareStatement(sql);
			for (Status status : statuslist) {
				int count = 1;
				ps.setLong(count++, status.getId());
				ps.setString(count++, status.getText());
				Timestamp time = new Timestamp(0);
				if(status.getCreatedAt()!=null) {
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
	
	public void updateStatusBatch(List<Status> statuslist) {
		if (statuslist == null || statuslist.size() == 0)
			return;
		String sql = "update status_info set content=?, picurl=? where id=?";
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_DATBASE);
			ps = conn.prepareStatement(sql);
			for (Status status : statuslist) {
				int count = 1;
				ps.setString(count++, status.getText());
				ps.setString(count++, status.getBmiddle_pic());
				ps.setLong(count++, status.getId());
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
	
	public boolean existStatus(Status status) {
		if(status == null)
			return false;
		
		boolean existed = false;
		String sql = "select id from status_info where id=?";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = JdbcUtil.getConn(DBConstants.MBLOG_INFO_DATBASE);
			ps = conn.prepareStatement(sql);
			ps.setLong(1, status.getId());
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
