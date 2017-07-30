package casia.weibo.crawler.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UrlUtils {

	public static String encodeUrlRequestLine(String url) {
		if (url == null | url.length() == 0)
			return null;

		String realurl = url;
		int index = realurl.indexOf("?");
		if (index >= 0) {
			String requestline = realurl.substring(index + 1);
			String[] params = requestline.split("&");

			realurl = realurl.substring(0, index + 1);
			for (String param : params) {
				String[] pair = param.split("=");
				if (pair.length <= 0)
					continue;
				realurl += pair[0] + "=";
				if (pair.length <= 1) {
					realurl += "&";
					continue;
				}
				try {
					realurl += URLEncoder.encode(pair[1],"utf-8");
					realurl += "&";
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			realurl = realurl.substring(0,realurl.length()-1);
		}

		return realurl;
	}
	
	public static void detectEncode(String content) {
		try {
			String str = new String(content.getBytes("windows-1252"));
			System.out.println(content.getBytes("windows-1252").length);
			System.out.println(str);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		detectEncode("nick=å«å¦_æå®å¨æ¯å¤ªCJäº");
	}

}
