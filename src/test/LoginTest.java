package test;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import casia.weibo.crawler.utils.CommonUtils;

public class LoginTest {
	
	public static void main(String[] args) {
		HttpClient client = new DefaultHttpClient();
		String html = CommonUtils.getHtml("http://weibo.com", client);
		CommonUtils.writeString2File("E:\\NoLogin.html", html);
		html = CommonUtils.getHtml("http://weibo.com", null);
		CommonUtils.writeString2File("E:\\Login.html", html);
	}

}
