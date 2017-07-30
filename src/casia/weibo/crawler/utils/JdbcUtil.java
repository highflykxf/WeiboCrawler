package casia.weibo.crawler.utils;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;

/**
 * 数据库链接辅助类
 * 
 * @author xiaoming
 * @version 1.0 2010-09-12
 * @since jdk1.6
 */
public class JdbcUtil {


	private static Logger logger = Logger.getLogger(JdbcUtil.class);
	private static boolean inited = false;
	private static void init() {
		try {
			JAXPConfigurator.configure("conf/proxool.xml", false);
		} catch (ProxoolException e1) {
			e1.printStackTrace();
		}
		inited = true;
	}
	
	/**
	 * 得到�?��数据库连接对象，在使用前，请保证数据库连接配置文件存在，并正确配�?
	 * 
	 * @return 返回数据库连接对象Connection
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 *             抛出类无法找到异�?
	 * @throws SQLException
	 *             抛出SQL异常
	 */
	
	public static synchronized Connection getConn(String dbName)
			throws SQLException {
		if(!inited) {
			init();
		}
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("proxool." + dbName);
		} catch (Exception e) {
			logger.info("get connection fail, will be get again！\t" + e.getMessage());
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return getConn(dbName);
		}
		return conn;
	}
	
	/**
	 * 关闭数据库连接的相关对象
	 * 
	 * @param res
	 *            要关闭的ResultSet 对象
	 * @param stmt
	 *            要关闭的Statement 对象
	 * @param conn
	 *            要关闭的Connection 对象
	 */
	public static void close(ResultSet rs, Statement pstmt, Connection conn) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				logger.error("Cloes ResulstSet Error!");
			}
		}
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				logger.error("Cloes Statement Error!");
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("Cloes Statement Error!");
			}
		}

	}
	
	

	public static void main(String[] args) {
		try {
			JAXPConfigurator.configure("conf/proxool.xml", false);
		} catch (ProxoolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
