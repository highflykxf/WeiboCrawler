package casia.weibo.crawler.login;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class SinaLogin {
	
	static String SINA_PK = "EB2A38568661887FA180BDDB5CABD5F21C7BFD59C090CB2D24"
			+ "5A87AC253062882729293E5506350508E7F9AA3BB77F4333231490F915F6D63C55FE2F08A49B353F444AD39"
			+ "93CACC02DB784ABBB8E42A9B1BBFFFB38BE18D78E87A0E41B9B8F73A928EE0CCEE"
			+ "1F6739884B9777E4FE9E88A1BBE495927AC4A799B3181D6442443";
	static String usernm = "jjlin001@126.com";
	static String passwd = "linjunjie123456";
	
	public static final String NULL = "null";
	
	private static DefaultHttpClient gclient = null;
	
	public static boolean ForceVerification = false;
	
	public static DefaultHttpClient login() {
		
		return login(usernm,passwd);
	}
	
	@SuppressWarnings("deprecation")
	public static DefaultHttpClient login(String username, String password) {

		try {
			DefaultHttpClient client = null;
			while (true) {
				client = new DefaultHttpClient();
				client.getCookieSpecs().register("easy",
						new MyCookieSpecFactory());
				client.getParams().setParameter(ClientPNames.COOKIE_POLICY,
						"easy");
				// client.getParams().setParameter("http.protocol.cookie-policy",
				// CookiePolicy.BROWSER_COMPATIBILITY);
				client.getParams().setParameter(
						HttpConnectionParams.CONNECTION_TIMEOUT, 5000);

				HttpPost post = new HttpPost(
						"http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.5)");

				PreLoginInfo info = getPreLoginBean(client, username);

				long servertime = info.servertime;
				String nonce = info.nonce;

				String pwdString = servertime + "\t" + nonce + "\n" + password;
				String sp = new BigIntegerRSA().rsaCrypt(SINA_PK, "10001",
						pwdString);

				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("entry", "weibo"));
				nvps.add(new BasicNameValuePair("gateway", "1"));
				if (ForceVerification || info.showpin != null && info.showpin.equals("1")
						&& info.picNO != null) {
					nvps.add(new BasicNameValuePair("door", info.picNO));
				}
				nvps.add(new BasicNameValuePair("from", ""));
				nvps.add(new BasicNameValuePair("savestate", "7"));
				nvps.add(new BasicNameValuePair("useticket", "1"));
				nvps.add(new BasicNameValuePair("ssosimplelogin", "1"));
				nvps.add(new BasicNameValuePair("vsnf", "1"));
				nvps.add(new BasicNameValuePair("su", encodeUserName(username)));
				nvps.add(new BasicNameValuePair("service", "miniblog"));
				nvps.add(new BasicNameValuePair("servertime", servertime + ""));
				nvps.add(new BasicNameValuePair("nonce", nonce));
				nvps.add(new BasicNameValuePair("pwencode", "rsa2"));
				nvps.add(new BasicNameValuePair("rsakv", info.rsakv));
				nvps.add(new BasicNameValuePair("sp", sp));
				nvps.add(new BasicNameValuePair("encoding", "UTF-8"));
				nvps.add(new BasicNameValuePair("prelt", "167"));
				nvps.add(new BasicNameValuePair("returntype", "META"));
				nvps.add(new BasicNameValuePair(
						"url",
						"http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack"));

				post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

				HttpResponse response = client.execute(post);
				String entity = EntityUtils.toString(response.getEntity());

				String url = null;
				try {
					url = entity.substring(
							entity.indexOf("http://weibo.com/ajaxlogin.php?"),
							entity.indexOf("code=0") + 6);
					// ��ȡ��ʵ��url��������
					HttpGet getMethod = new HttpGet(url);

					response = client.execute(getMethod);
					entity = EntityUtils.toString(response.getEntity());
					entity = entity.substring(
							entity.indexOf("userdomain") + 13,
							entity.lastIndexOf("\""));
				} catch (Exception e) {
					// e.printStackTrace();
//					System.out.println(entity);
					System.out.println("Login fail!"+new Date().toLocaleString());
					client = null;
				}
				
				if(client!=null) {
					ForceVerification = false;
					break;
				} else {
					ForceVerification = true;
				}
			}

			return client;

		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	private static PreLoginInfo getPreLoginBean(HttpClient client,String username)
			throws HttpException, IOException, JSONException {

		String serverTime = getPreLoginInfo(client,username);
		JSONObject jsonInfo = new JSONObject(serverTime);
		PreLoginInfo info = new PreLoginInfo();
		info.nonce = jsonInfo.getString("nonce");
		info.pcid = jsonInfo.getString("pcid");
		info.pubkey = jsonInfo.getString("pubkey");
		info.retcode = jsonInfo.getInt("retcode");
		info.rsakv = jsonInfo.getString("rsakv");
		info.servertime = jsonInfo.getLong("servertime");
		info.showpin = jsonInfo.getString("showpin");
		String door = null;
		if (ForceVerification || info.showpin != null && info.showpin.equals("1")) {
			String picUrl = "http://login.sina.com.cn/cgi/pin.php?s=0&p="
					+ info.pcid + "&r=" + (new Date().getTime() % 100000000);
			LoginUtils.downloadVPic(client,picUrl, "utf8", "images\\", "sinaImg");
			System.out.println("Enter verification code:");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			try {
				door = br.readLine();
			} catch (IOException e) {
			}
			try {
				br.close();
			} catch (IOException e) {
			}
//			door = getVerificationCode();
			info.picNO = door;
		}
		
		return info;
	}

	public static String getPreLoginInfo(HttpClient client,String username)
			throws ParseException, IOException {
		
		String usernameBase64 = username;
		if (usernameBase64.contains("@")) {
			usernameBase64 = usernameBase64.replaceFirst("@", "%40");// MzM3MjQwNTUyJTQwcXEuY29t
		}
		usernameBase64 = new String(EncodeUtil.getBase64Encode(usernameBase64
				.getBytes()));
		
		String preloginurl = "http://login.sina.com.cn/sso/prelogin.php?entry=weibo&"
				+ "callback=sinaSSOController.preloginCallBack&su="
				+ usernameBase64
				+ "&checkpin=1&rsakt=mod&client=ssologin.js(v1.4.5)"
				+ "&_=" + getCurrentTime();
		HttpGet get = new HttpGet(preloginurl);

		HttpConnectionParams.setConnectionTimeout(get.getParams(), 10000);
		HttpConnectionParams.setSoTimeout(get.getParams(), 10000);
		HttpResponse response = client.execute(get);

		String getResp = EntityUtils.toString(response.getEntity());
		
		int firstLeftBracket = getResp.indexOf("(");
		int lastRightBracket = getResp.lastIndexOf(")");

		String jsonBody = getResp.substring(firstLeftBracket + 1,
				lastRightBracket);
		return jsonBody;

	}

	private static String getCurrentTime() {
		long servertime = new Date().getTime() / 1000;
		return String.valueOf(servertime);
	}

	private static String encodeUserName(String email) {
		email = email.replaceFirst("@", "%40");// MzM3MjQwNTUyJTQwcXEuY29t
		email = Base64.encodeBase64String(email.getBytes());
		return email;
	}
	
	public static DefaultHttpClient getDefaultHttpClient() {
		if(gclient == null) {
			gclient = login();
		}
		return gclient;
	}
	
	public static HttpClient createHttpClient(String username,String password) {
		return login(username,password);
	}
	
	public static void writeCookie(String filepath,DefaultHttpClient client) {
		List<Cookie> cookies = client.getCookieStore().getCookies();
		try {
			FileWriter fw = new FileWriter(filepath);
			if (cookies.isEmpty()) {
	        } else {
	            for (int i = 0; i < cookies.size(); i++) {
	                fw.write(cookies.get(i).toString()+"\r\n");
	            }
	        }
	        fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	public static DefaultHttpClient getHttpClientWithCookies(String filepath) {
		DefaultHttpClient client = new DefaultHttpClient();
		client.getCookieSpecs().register("easy", new MyCookieSpecFactory());
		client.getParams().setParameter(ClientPNames.COOKIE_POLICY, "easy");
		client.getParams().setParameter(
				HttpConnectionParams.CONNECTION_TIMEOUT, 5000);
		try {
			Scanner sc = new Scanner(new File(filepath));
			while(sc.hasNextLine()) {
				
				String name = null;
				String value = null;
				String domain = null;
				String version = null;
				String path = null;
				String expiry = null;
				String line = sc.nextLine();
				line = line.substring(1, line.length()-1);
				String[] strs = line.split("]\\[");
				for(String pair : strs) {
					String namestr = pair.split(":")[0];
					String valuestr = pair.substring(pair.indexOf(":")+1);
					valuestr = valuestr.substring(1);
					if("name".equals(namestr)) {
						name = valuestr;
					} else if ("value".equals(namestr)) {
						value = valuestr;
					} else if ("domain".equals(namestr)) {
						domain = valuestr;
					} else if ("version".equals(namestr)) {
						version = valuestr;
					} else if ("path".equals(namestr)) {
						path = valuestr;
					} else if ("expiry".equals(namestr)) {
						expiry = valuestr;
					}
//					switch(namestr) {
//					case "name" : name = valuestr;break;
//					case "value" : value = valuestr;break;
//					case "domain" : domain = valuestr;break;
//					case "version" : version = valuestr;break;
//					case "path" : path = valuestr;break;
//					case "expiry" : expiry = valuestr;break;
//					}
				}
				BasicClientCookie cookie = new BasicClientCookie(name, value);
				if(!NULL.equals(domain) && domain!=null)
					cookie.setDomain(domain);
				if(!NULL.equals(path) && path!=null)
					cookie.setPath(path);
				if(!NULL.equals(version) && version!=null)
					cookie.setVersion(Integer.parseInt(version));
				if(!NULL.equals(expiry) && expiry!=null) {
					int begin = expiry.indexOf("GMT");
					if(begin>=0) {
						int end = expiry.indexOf(" ",begin);
						String del = expiry.substring(begin, end+1);
						expiry = expiry.replace(del, "");
					}
					cookie.setExpiryDate(new Date(expiry));
				}
				
				client.getCookieStore().addCookie(cookie);
			}
			sc.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return client;
	}
	
	public static void main(String[] args) {
		HttpClient client = login("jjlin001@126.com","linjunjie123456");
		if(client!=null) {
			DefaultHttpClient dclient = (DefaultHttpClient) client;
			System.out.println(dclient.getCookieStore().getCookies());
			writeCookie("D:\\sina_cookies",dclient);
		} else {
			System.out.println("Error Code!");
		}
//		String datestr = "Sat Jun 15 09:18:38 GMT+08:00 2013";
//		int begin = datestr.indexOf("GMT");
//		if(begin>=0) {
//			int end = datestr.indexOf(" ",begin);
//			String del = datestr.substring(begin, end+1);
//			datestr = datestr.replace(del, "");
//		}
//		System.out.println(datestr);
//		Date date = new Date(datestr);
//		writeCookie("D:\\cookie.txt",login());
//		writeCookie("D:\\cookie2.txt",login());
//		HttpClient client = getHttpClientWithCookies("D:\\cookie.txt");
//		writeCookie("D:\\cookie2.txt",(DefaultHttpClient) client);
//		System.out.println(new Date().getTime()%100000000);
	}

}
