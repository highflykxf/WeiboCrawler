package test;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import casia.weibo.crawler.dao.UserDao;
import casia.weibo.crawler.extractor.CommentPageHandle;
import casia.weibo.crawler.extractor.MainCrawler;
import casia.weibo.crawler.extractor.PicCrawler;
import casia.weibo.crawler.extractor.RetweetPageHandle;
import casia.weibo.crawler.extractor.UpdaterCrawler;
import casia.weibo.crawler.utils.CommonUtils;
import casia.weibo.entity.Comment;
import casia.weibo.entity.Status;
import casia.weibo.entity.User;

public class Test {

	public static void main(String[] args) {

		System.out.println("Start: " + new Date().toLocaleString());

		// UserDao userdao = new UserDao();
		// User user = new User();
		// user.setId(11);
		// user.setName("abc");
		// userdao.insertUser(user);

		// 1642635773
		// 1886477075
		// 2219088342
		// 2430259303
		// 1932383433
		// MainCrawler crawler = new MainCrawler();
		// crawler.process(1932383433l);

		// Status status = new Status();
		// status.setId(3779277016806575l);
		// new RetweetPageHandle().handleRetweetPage(null, status);;

		// UpdaterCrawler uc = new UpdaterCrawler();
		// uc.update(1771596430, 1861274540);

		// new PicCrawler().downloadExtraPictures("E:\\test\\");

		// Status status = new Status();
		// status.setId(3918938461133424l);
		// CommentPageHandle handle = new CommentPageHandle();
		// List<Comment> commlist = handle.handleCommentPage(null, status);
		// for (Comment comm : commlist) {
		// System.out.println(comm);
		// }
		// System.out.println("Total comments size = " + commlist.size());

		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet getMethod = new HttpGet(
					"https://www.kickstarter.com/projects/1743305220/moments-of-insight/description");
			HttpConnectionParams.setConnectionTimeout(getMethod.getParams(),
					10000);
			HttpConnectionParams.setSoTimeout(getMethod.getParams(), 10000);
			
//			getMethod.setHeader("Host", "www.kickstarter.com");
//			getMethod.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0");
//			getMethod.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//			getMethod.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//			getMethod.setHeader("Accept-Encoding", "gzip, deflate, br");
			
//			BasicClientCookie cookie = new BasicClientCookie("vis", "ae29e30fc9eefd6a-45236519dfcf90be-cbd80f03d1bd48a0v1");
//			client.getCookieStore().addCookie(cookie);
//			cookie = new BasicClientCookie("last_page", "https%3A%2F%2Fwww.kickstarter.com%2Fprojects%2F1743305220%2Fmoments-of-insight%2Fdescription");
//			client.getCookieStore().addCookie(cookie);
//			cookie = new BasicClientCookie("lang", "en");
//			client.getCookieStore().addCookie(cookie);
//			cookie = new BasicClientCookie("_ksr_session", "M0lubFF5VjJ2UzRGMmJldElPYnR6bHFIWjRrSkgyOVBnWlQ4eGwxV3VrcUpIY0cwZHFsQWg1M05HVmlYL0h0YmlsYU9HZWF1Y0RZZXNHUXRNS2p5UFp3YkpKTjNBdHlNckI2d09PWmJvTHYycDk1L3l2SnZHU1B3YnZYZnBONkltcno3ZE5qcElJejRxaWRVczc0dTFOQzZhSCtaejdDRTB6WTJYUE5zU1hBPS0tdmZtWWp1bFNDRWNvQ0tSV2k3U3o4Zz09--e205df323f1bbe1449b494116571a6bcd5000b3f");
//			client.getCookieStore().addCookie(cookie);
//			cookie = new BasicClientCookie("request_time", "Mon%2C+12+Sep+2016+13%3A21%3A04+-0000");
//			client.getCookieStore().addCookie(cookie);
//			cookie = new BasicClientCookie("lux_uid", "147368646229265277");
//			client.getCookieStore().addCookie(cookie);
//			cookie = new BasicClientCookie("local_offset", "-6180");
//			client.getCookieStore().addCookie(cookie);
//			cookie = new BasicClientCookie("_ga", "GA1.2.546180990.1473686481");
//			client.getCookieStore().addCookie(cookie);
//			cookie = new BasicClientCookie("_gat", "1");
//			client.getCookieStore().addCookie(cookie);
			
			HttpResponse response = client.execute(getMethod);
			String entity = EntityUtils.toString(response.getEntity(), "UTF-8");
			System.out.println(entity);
			CommonUtils.writeString2File("D:/test.html", entity);
		} catch (NoHttpResponseException e) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (e instanceof java.net.SocketTimeoutException) {
				System.out.println("�������Ƶ�����ڵȴ�������");
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println(StringEscapeUtils.unescapeHtml("&quot;Pl_Core_T5MultiText__31&quot;,&quot;css&quot;:"));

		System.out.println("Finish: " + new Date().toLocaleString());
	}

}
