package casia.weibo.crawler.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import casia.weibo.crawler.login.SinaLogin;

public class CommonUtils {

	public static String readStringFromFile(String filepath) {
		if (filepath == null)
			return "";

		String str = "";
		File file = new File(filepath);
		try {
			FileInputStream in = new FileInputStream(file);
			// size 为字串的长度 ，这里一次性读完
			int size = in.available();
			byte[] buffer = new byte[size];
			in.read(buffer);
			in.close();
			str = new String(buffer, "utf8");

		} catch (IOException e) {
			e.printStackTrace();
		}

		return str;
	}

	public static String replaceString(String str) {
		str = str.replace("&lt;", "<");
		str = str.replace("&gt;", ">");
		str = str.replace("&quot;", "\"");
		str = str.replace("&amp;", "&");

		str = str.replace("\\n", "\n");
		str = str.replace("\\t", "\t");
		str = str.replace("\\r", "\r");
		str = str.replace("\\\\", "\\");
		str = str.replace("\\/", "/");
		str = str.replace("\\\"", "\"");

		return str;
	}

	public static String clearString(String str) {
		String ans = new String(str);
		ans = ans.replace("\t", "");
		ans = ans.replace("\n", "");
		return ans;
	}

	private static String str62keys = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static String IntToEnode62(long int10) {
		String s62 = "";
		int r = 0;
		while (int10 != 0) {
			r = (int) (int10 % 62);
			s62 = str62keys.charAt(r) + s62;
			int10 = (int10 / 62);
		}
		return s62;
	}

	public static long Encode62ToInt(String str62) {
		long i10 = 0;
		for (int i = 0; i < str62.length(); i++) {
			double n = str62.length() - i - 1;
			i10 += (str62keys.indexOf(str62.charAt(i)) * Math.pow(62, n));
		}
		return i10;
	}

	public static String Mid2Id(String str62) // yvr29p8dG
	{
		String id = "";
		for (int i = str62.length() - 4; i > -4; i = i - 4) // �������ǰ��4�ֽ�Ϊһ���ȡ�ַ�
		{
			int offset = i < 0 ? 0 : i;
			int len = i < 0 ? str62.length() % 4 : 4;
			String str = ""
					+ Encode62ToInt(str62.substring(offset, offset + len));

			if (offset > 0) { // �����ǵ�һ�飬����7λ��0
				while (str.length() < 7) {
					str = "0" + str;
				}
			}

			id = str + id;
		}
		return id;
	}

	public static String Id2Mid(String str10) // 3474920895989800
	{
		String mid = "";
		for (int i = str10.length() - 7; i > -7; i = i - 7) // �������ǰ��7�ֽ�Ϊһ���ȡ�ַ�
		{
			int offset = i < 0 ? 0 : i;
			int len = i < 0 ? str10.length() % 7 : 7;
			String str = IntToEnode62(Long.parseLong(str10.substring(offset,
					offset + len)));
			mid = str + mid;
		}
		return mid;
	}

	public static String getHtml(String url, HttpClient client) {

		if (client == null) {
			client = SinaLogin.login();
		}

		boolean success = false;

		while (!success) {
			try {
				HttpGet getMethod = new HttpGet(url);
				HttpConnectionParams.setConnectionTimeout(
						getMethod.getParams(), 10000);
				HttpConnectionParams.setSoTimeout(getMethod.getParams(), 10000);
				HttpResponse response = client.execute(getMethod);
				String entity = EntityUtils.toString(response.getEntity(),
						"UTF-8");
				return entity;
			} catch (NoHttpResponseException e) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				success = false;
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				success = false;
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
				success = false;
			}
		}

		return null;
	}

	public static String getHtml_NonAutoRedirect(String url, HttpClient client) {

		if (client == null) {
			client = SinaLogin.login();
		}

		boolean success = false;
		while (!success) {
			try {
				if (client instanceof DefaultHttpClient) {
					((DefaultHttpClient) client)
							.setRedirectHandler(new DefaultRedirectHandler() {
								@Override
								public URI getLocationURI(
										HttpResponse response, HttpContext arg1)
										throws ProtocolException {
									String realurl = response.getFirstHeader(
											"Location").getValue();
									int index = realurl.indexOf("?");
									if (index >= 0) {
										realurl = realurl.substring(0, index);
									}
									response.setHeader("Location", realurl);
									return super.getLocationURI(response, arg1);
								}
							});
				}
				HttpGet getMethod = new HttpGet(url);
				HttpConnectionParams.setConnectionTimeout(
						getMethod.getParams(), 10000);
				HttpConnectionParams.setSoTimeout(getMethod.getParams(), 10000);
				HttpResponse response = client.execute(getMethod);
				String entity = EntityUtils.toString(response.getEntity(),
						"UTF-8");
				return entity;
			} catch (NoHttpResponseException e) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				success = false;
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				success = false;
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
				success = false;
			}
		}

		return null;
	}

	public static void writeString2File(String filepath, String entity) {
		if (entity == null)
			return;
		try {
			File file = new File(filepath);
			OutputStreamWriter output = new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8");
			output.write(entity);
			output.flush();
			output.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String UnicodeToUTF8(String str) {
		str = loadConvert(str.toCharArray(), 0, str.length(), new char[0]);
		str = StringEscapeUtils.unescapeHtml(str);
		return str;
	}

	private static String loadConvert(char[] in, int off, int len,
			char[] convtBuf) {
		if (convtBuf.length < len) {
			int newLen = len * 2;
			if (newLen < 0) {
				newLen = Integer.MAX_VALUE;
			}
			convtBuf = new char[newLen];
		}
		char aChar;
		char[] out = convtBuf;
		int outLen = 0;
		int end = off + len;

		while (off < end) {
			aChar = in[off++];
			if (aChar == '\\') {
				aChar = in[off++];
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = in[off++];
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed \\uxxxx encoding.");
						}
					}
					out[outLen++] = (char) value;
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					out[outLen++] = aChar;
				}
			} else {
				out[outLen++] = (char) aChar;
			}
		}

		char[] realout = new char[outLen];
		for (int i = 0; i < outLen; i++) {
			realout[i] = out[i];
		}

		byte[] bytes = getBytes(realout);

		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private static byte[] getBytes(char[] chars) {
		Charset cs = Charset.forName("UTF-8");
		CharBuffer cb = CharBuffer.allocate(chars.length);
		cb.put(chars);
		cb.flip();
		ByteBuffer bb = cs.encode(cb);

		return bb.array();
	}

	public static Date parseStringToDate(String date) throws ParseException {
		Date result = null;
		String parse = date;
		parse = parse.replaceFirst("^[0-9]{4}([^0-9]?)", "yyyy$1");
		parse = parse.replaceFirst("^[0-9]{2}([^0-9]?)", "yy$1");
		parse = parse.replaceFirst("([^0-9]?)[0-9]{1,2}([^0-9]?)", "$1MM$2");
		parse = parse.replaceFirst("([^0-9]?)[0-9]{1,2}( ?)", "$1dd$2");
		parse = parse.replaceFirst("( )[0-9]{1,2}([^0-9]?)", "$1HH$2");
		parse = parse.replaceFirst("([^0-9]?)[0-9]{1,2}([^0-9]?)", "$1mm$2");
		parse = parse.replaceFirst("([^0-9]?)[0-9]{1,2}([^0-9]?)", "$1ss$2");

		DateFormat format = new SimpleDateFormat(parse);

		result = format.parse(date);

		return result;
	}

	public static String cleanHtml(String htmltext) {
		return PHTMLCleaner.cleanHtml(htmltext);
	}

	public static void savePictureFromUrl(String url, String saveFolder) {
		if (url == null || saveFolder == null)
			return;
		if (saveFolder.charAt(saveFolder.length() - 1) == '\\'
				|| saveFolder.charAt(saveFolder.length() - 1) == '/') {
			saveFolder = saveFolder.substring(0, saveFolder.length() - 1);
		}
		File folder = new File(saveFolder);
		if (!folder.exists() || !folder.isDirectory()) {
			return;
		}
		String filename = url.substring(url.lastIndexOf("/") + 1);
		savePictureFromUrl(url, saveFolder, filename);
	}

	public static void savePictureFromUrl(String url, String saveFolder,
			String filename) {
		if (url == null || saveFolder == null || filename == null)
			return;
		if (saveFolder.charAt(saveFolder.length() - 1) == '\\'
				|| saveFolder.charAt(saveFolder.length() - 1) == '/') {
			saveFolder = saveFolder.substring(0, saveFolder.length() - 1);
		}
		File folder = new File(saveFolder);
		if (!folder.exists() || !folder.isDirectory()) {
			return;
		}
		String filepath = saveFolder + "\\" + filename;
		File file = new File(filepath);

		try {
			org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
			GetMethod get = new GetMethod(url);
			client.executeMethod(get);
			FileOutputStream output = new FileOutputStream(file);
			// 得到网络资源的字节数组,并写入文件
			output.write(get.getResponseBody());
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean checkHttpClient(HttpClient client) {
		if (client == null)
			return false;
		String html = getHtml("http://weibo.com", client);
		if (html.indexOf("我的首页") < 0) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		// System.out.println(Id2Mid("3557847067541134"));
		// System.out.println(Mid2Id("yvr29p8dG"));
		// System.out.println(new Date().getTime());
		// Calendar c = Calendar.getInstance();
		// System.out.println(c.getTimeInMillis());
		// int page = 1;
		// int count = 15;
		// String max_id = "3559383496048786";
		// int pre_page = 1;
		// String end_id = "3559662874165031";
		// int pagebar = 1;
		// String _k = "136417413184033";
		// String uid = "1197161814";
		// String __rnd = ""+new Date().getTime();
		// String url = "http://weibo.com/aj/mblog/mbloglist?_wv=5&page="+ page
		// +
		// "&count=" + count +
		// "&max_id=" + max_id +
		// "&pre_page=" + pre_page +
		// "&end_id=" + end_id +
		// "&pagebar=" + pagebar +
		// "&_k=" + _k +
		// "&uid=" + uid +
		// "&_t=0" +
		// "&__rnd="+__rnd;
		// String entity = getHtml(url);
		// entity = UnicodeToUTF8_File(entity);
		// entity = PHTMLCleaner.cleanHtml(entity);
		// writeString2File("D:\\test1.xml", entity);

		// savePictureFromUrl("http://ww4.sinaimg.cn/bmiddle/67893f49jw1efo4ppmnqaj20dw07t3yv.jpg"
		// ,"E:\\test");

		// HttpClient client = new DefaultHttpClient();
		// System.out.println(checkHttpClient(client));
		// client = SinaLogin.getDefaultHttpClient();
		// System.out.println(checkHttpClient(client));
	}
}
