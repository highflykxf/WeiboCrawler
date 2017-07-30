package casia.weibo.crawler.login;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.params.HttpParams;

public class MyCookieSpecFactory implements CookieSpecFactory {
	public CookieSpec newInstance(HttpParams params) {
		return new BrowserCompatSpec() {
			@Override
			public void validate(Cookie cookie, CookieOrigin origin)
					throws MalformedCookieException {
				// Oh, I am easy
			}
		};
	}
	
	public static void main(String[] args) {
		System.out.println(ClientPNames.COOKIE_POLICY);
	}
}
