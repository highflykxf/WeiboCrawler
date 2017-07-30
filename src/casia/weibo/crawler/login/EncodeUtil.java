package casia.weibo.crawler.login;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncodeUtil {
	//base62
	static String[] str62key = {"0", "1", "2", "3", "4", "5", "6", "7", "8",
			"9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
			"m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y",
			"z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
			"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
			"Z" };

	//base64
	static private char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
			.toCharArray();
	static private byte[] codes = new byte[256];
	static {
		for (int i = 0; i < 256; i++)
			codes[i] = -1;
		for (int i = 'A'; i <= 'Z'; i++)
			codes[i] = (byte) (i - 'A');
		for (int i = 'a'; i <= 'z'; i++)
			codes[i] = (byte) (26 + i - 'a');
		for (int i = '0'; i <= '9'; i++)
			codes[i] = (byte) (52 + i - '0');
		codes['+'] = 62;
		codes['/'] = 63;
	}
	
	
	/**
	 * url杞寲鎴恗id鐨勫�
	 * 
	 * @param url
	 * @return
	 */
	public static String url2mid(String url) {
		String mid = "";
		String k = url.toString().substring(3, 4);// 鐢ㄤ簬绗洓浣嶄负0鏃剁殑杞崲
		if (!k.equals("0")) {
			for (int i = url.length() - 4; i > -4; i = i - 4) {// 鍒嗗埆浠ュ洓涓负涓�粍
				int offset1 = i < 0 ? 0 : i;
				int offset2 = i + 4;
				String str = url.toString().substring(offset1, offset2);
				str = str62to10(str);// String绫诲瀷鐨勮浆鍖栨垚鍗佽繘鍒剁殑鏁�
				// 鑻ヤ笉鏄涓�粍锛屽垯涓嶈冻7浣嶈ˉ0
				if (offset1 > 0) {
					while (str.length() < 7) {
						str = '0' + str;
					}
				}
				mid = str + mid;
			}
		} else {
			for (int i = url.length() - 4; i > -4; i = i - 4) {
				int offset1 = i < 0 ? 0 : i;
				int offset2 = i + 4;
				if (offset1 > -1 && offset1 < 1 || offset1 > 4) {
					String str = url.toString().substring(offset1, offset2);
					str = str62to10(str);
					// 鑻ヤ笉鏄涓�粍锛屽垯涓嶈冻7浣嶈ˉ0
					if (offset1 > 0) {
						while (str.length() < 7) {
							str = '0' + str;
						}
					}
					mid = str + mid;
				} else {
					String str = url.toString().substring(offset1 + 1, offset2);
					str = str62to10(str);
					// 鑻ヤ笉鏄涓�粍锛屽垯涓嶈冻7浣嶈ˉ0
					if (offset1 > 0) {
						while (str.length() < 7) {
							str = '0' + str;
						}
					}
					mid = str + mid;
				}
			}
		}
		return mid;
	}

	/**
	 * mid杞崲鎴恥rl缂栫爜浠ュ悗鐨勫�
	 * 
	 * @param mid
	 * @return
	 */
	public static String mid2url(String mid) {
		String url = "";
		for (int j = mid.length() - 7; j > -7; j = j - 7) {// 浠�涓暟瀛椾负涓�釜鍗曚綅杩涜杞崲
			int offset3 = j < 0 ? 0 : j;
			int offset4 = j + 7;
			// String l = mid.substring(mid.length() - 14, mid.length() - 13);
			if ((j > 0 && j < 6)
					&& (mid.substring(mid.length() - 14, mid.length() - 13)
							.equals("0") && mid.length() == 19)) {
				String num = mid.toString().substring(offset3 + 1, offset4);
				num = int10to62(Integer.valueOf(num));// 鍗佽繘鍒惰浆鎹㈡垚62杩涘埗
				url = 0 + num + url;
				if (url.length() == 9) {
					url = url.substring(1, url.length());
				}
			} else {
				String num = mid.toString().substring(offset3, offset4);
				num = int10to62(Integer.valueOf(num));
				url = num + url;
			}
		}

		return url;
	}

	/**
	 * 62杩涘埗杞崲鎴�0杩涘埗
	 * 
	 * @param str
	 * @return
	 */
	private static String str62to10(String str) {
		String i10 = "0";
		int c = 0;
		for (int i = 0; i < str.length(); i++) {
			int n = str.length() - i - 1;
			String s = str.substring(i, i + 1);
			for (int k = 0; k < str62key.length; k++) {
				if (s.equals(str62key[k])) {
					int h = k;
					c += (int) (h * Math.pow(62, n));
					break;
				}
			}
			i10 = String.valueOf(c);
		}
		return i10;
	}

	/**
	 * 10杩涘埗杞崲鎴�2杩涘埗
	 * 
	 * @param int10
	 * @return
	 */
	private static String int10to62(double int10) {
		String s62 = "";
		int w = (int) int10;
		int r = 0;
		int a = 0;
		while (w != 0) {
			r = (int) (w % 62);
			s62 = str62key[r] + s62;
			a = (int) (w / 62);
			w = (int) Math.floor(a);
		}
		return s62;
	}

	/**
	 * SHA1 鍔犲瘑
	 * 
	 * @param url
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String getSHA1Encoder(String url)
			throws NoSuchAlgorithmException {
		if (url == null || url.trim().equals("")) {
			return null;
		}
		MessageDigest digest = MessageDigest.getInstance("SHA1");
		byte[] bytes = digest.digest(url.getBytes());
		StringBuilder output = new StringBuilder(bytes.length);
		for (Byte entry : bytes) {
			output.append(String.format("%02x", entry));
		}
		digest.reset();
		return output.toString();
	}
	
	/**
	 * 灏嗗師濮嬫暟鎹紪鐮佷负base64缂栫爜
	 */
	public static  char[] getBase64Encode(byte[] data) {
		char[] out = new char[((data.length + 2) / 3) * 4];
		for (int i = 0, index = 0; i < data.length; i += 3, index += 4) {
			boolean quad = false;
			boolean trip = false;
			int val = (0xFF & (int) data[i]);
			val <<= 8;
			if ((i + 1) < data.length) {
				val |= (0xFF & (int) data[i + 1]);
				trip = true;
			}
			val <<= 8;
			if ((i + 2) < data.length) {
				val |= (0xFF & (int) data[i + 2]);
				quad = true;
			}
			out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 1] = alphabet[val & 0x3F];
			val >>= 6;
			out[index + 0] = alphabet[val & 0x3F];
		}
		return out;
	}

	/**
	 * 灏哹ase64缂栫爜鐨勬暟鎹В鐮佹垚鍘熷鏁版嵁
	 */
	public static  byte[] getBase64Decode(char[] data) {
		int len = ((data.length + 3) / 4) * 3;
		if (data.length > 0 && data[data.length - 1] == '=')
			--len;
		if (data.length > 1 && data[data.length - 2] == '=')
			--len;
		byte[] out = new byte[len];
		int shift = 0;
		int accum = 0;
		int index = 0;
		for (int ix = 0; ix < data.length; ix++) {
			int value = codes[data[ix] & 0xFF];
			if (value >= 0) {
				accum <<= 6;
				shift += 6;
				accum |= value;
				if (shift >= 8) {
					shift -= 8;
					out[index++] = (byte) ((accum >> shift) & 0xff);
				}
			}
		}
		if (index != out.length)
			throw new Error("miscalculated data length!");
		return out;
	}

	
	
	
	

	/**
	 * 鑾峰彇鏌愪釜瀛楃涓茬殑md5鍊�
	 * @param text
	 * @return
	 */
	public synchronized  static String getMD5(String text) {
		MessageDigest digest;
		String md5Value = null;
		try {
			digest = MessageDigest.getInstance("MD5");
			byte[] bytes = digest.digest(text.getBytes("utf-8"));
			StringBuilder output = new StringBuilder(bytes.length);
			for (Byte entry : bytes) {
				output.append(String.format("%02x", entry));
			}
			digest.reset();
			md5Value = output.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return md5Value;
	}
	
	
	
	
	/**
	 * 杞崲16杩涘埗鐨刄nicode<br>
	 * 姣斿灏嗏�\u5352鈥濊浆鎹负鈥滃崚鈥�
	 * @param code 杞崲鍓嶅瓧绗�
	 * @return  String 杞崲缁撴灉
	 */
	public static String convertFromHex(String code) {
		StringBuffer sb = new StringBuffer(code);
		int pos;
		while ((pos = sb.indexOf("\\u")) > -1) {
			
				String tmp = sb.substring(pos, pos + 6);
				sb.replace(pos, pos + 6, Character.toString((char) Integer
							.parseInt(tmp.substring(2), 16)));
		}
		return code = sb.toString();
//		return toUtf8(code);
	}
	/**
	 * 寰楀埌url鐨刪ot 濡俬ttp://www.baidu.com  host 涓�baidu.com
	 * 
	 * @param url
	 * @return
	 */
	public static String getHost(String url) {
		if (url == null) {
			return null;
		}
		url = url.replaceAll("^https?://", "").replaceAll("([^/]+).*", "$1");
		return url;
	}
	
	/**
	 * 鑾峰彇鏌愪釜瀛楃涓茬殑md5鍊�
	 * @param text
	 * @return
	 */
	public synchronized  static String MD5(String text) {
		MessageDigest digest;
		String md5Value = null;
		try {
			digest = MessageDigest.getInstance("MD5");
			byte[] bytes = digest.digest(text.getBytes("utf-8"));
			StringBuilder output = new StringBuilder(bytes.length);
			for (Byte entry : bytes) {
				output.append(String.format("%02x", entry));
			}
			digest.reset();
			md5Value = output.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return md5Value;
	}

	
	public static void main(String[] args) {

//		try {
//			System.out.println(getSHA1Encoder("a"));
//		} catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		String aa = url2mid("xtMaBAmVR");
//        System.out.println(aa);
//        String bb = mid2url("3370966818667963");
//        System.out.println(bb); 
//        
//        
        /*String strSrc = "isiteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥絠siteam鎴戠埍浣犱腑鍥�;
        for(int i = 0; i < 100; i++){
        	strSrc += strSrc+"\n";
        }
        System.out.println(strSrc.length());
        String strOut = new String(getBase64Encode(strSrc.getBytes()));
        char[] temp = strOut.toCharArray();
        String temp2 = "";
        
        System.out.println("1------"+new String(temp));
//        for(int i = 0 ; i < temp.length; i ++){
//        	boolean get = false;
//        	for(int j=0; j < str62key.length; j++){
//    			if(Character.toString(temp[i]).equals(str62key[j])){
//    				int point = (j + 3)%str62key.length;
//    				temp2 += str62key[point];
//    				get = true;
//    				break;
//    			}
//    		}
//        	if(!get){
//        		temp2 += temp[i];
//        	}
//		}
        System.out.println("2------"+new String(temp2));
		
		System.out.println(strOut);
//		String strOut2 = new String(getBase64Decode(strOut.toCharArray()));
		String strOut2 = new String(getBase64Decode(temp2.toCharArray()));
		System.out.println(strOut2);
//		
//		//select userid from tw_userinfo  where md5='b91500aa72666e0aada844db7a5933ca';
//		System.out.println(getMD5("http://weibo.com/2267093583"));
		System.out.println(url2mid("ywSiLaqLi"));
		System.out.println(mid2url("3478352352486156"));*/
//		System.out.println(MD5("http://weibo.com/1644489953"));
		
		
//		System.out.println(getBase64Encode("http://world.people.com.cn/n/2012/0726/c1002-18604130.html".getBytes()));
		
//		System.out.println(MD5("http://t.qq.com/wxnf-b-_c8"));
		String headImg = "background-image: url(\"http://s5.cr.itc.cn/mblog/icon/60/c7/78625388857247873.gif\");";
		System.out.println(headImg.substring(headImg.indexOf("http"), headImg.lastIndexOf("\"")));
		
	}
}
