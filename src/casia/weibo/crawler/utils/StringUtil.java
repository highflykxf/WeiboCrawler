package casia.weibo.crawler.utils;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 字符串处理类
 * @author root
 * @since 2012.5
 */
public class StringUtil {

	/**
	 * 抽取匹配内容
	 * @param str
	 * @param pattern
	 * @return
	 */
	public static String extrator(String str, String pattern) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			sb.append(m.group().trim());
		}
		return sb.toString();
	}
	/**
	 * 匹配起始和结束位置之间的内容
	 * @param content
	 * @param start
	 * @param end
	 * @return
	 */
	public static String regMatcher(String content, String start, String end) {
		return regMatcher(content, start, end, true);
	}
	/**
	 *  匹配起始和结束位置之间的内容，是否贪婪匹配，默认为否
	 * @param content
	 * @param start
	 * @param end
	 * @param is
	 * @return
	 */
	public static String regMatcher(String content, String start, String end, boolean is) {
		String mat = null;
		if(is) {
			mat = start + "([\\s\\S]+?)(\\s)?" + end;
		} else {
			mat = start + "([\\s\\S]+)(\\s)?" + end;
		}
		Pattern p = Pattern.compile(mat);
		Matcher m = p.matcher(content);
		if(m.find()) {
			return m.group(1);
		}
		return null;
	}

	/**
	 * 格式化字符串
	 * @param s
	 * @return
	 */
	public static String format(String s) {
		String result = s;
		if (s != null && s.trim().length()>0){
			while (result.indexOf("\r")!=-1)
				result = result.replaceAll("\r", "");
			while (result.indexOf("\n")!=-1)
				result = result.replaceAll("\n", "");
			while (result.indexOf("\t")!=-1)
				result = result.replaceAll("\t", "");
			while (result.indexOf("  ")!=-1)
				result = result.replaceAll("  ", " ");
			while (result.indexOf("�?")!=-1)
				result = result.replaceAll("�?", "");
			return result.trim();
		}
		return "";
	}
	
	/**
	 * 判断是否有中文字�?
	 * @param str
	 * @return
	 */
	public static boolean haveChinese(String str) {
		boolean judge = false;
		if(str==null || str.equals("")) 
			return judge;
		char[] ch = str.toCharArray();
		for(int i = 0; i < ch.length;i++) {
			if(isChinese(ch[i])) {
				judge = true;
				break;
			}
		}
		return judge;
    }
	private static boolean isChinese(char c) {  
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);  
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A  
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {  
            return true;  
        }  
        return false;  
    }
	
	public static String getContent(String filename, String encode) {
		String s = "";
		StringBuffer sb = new StringBuffer();
		try {
			java.io.FileInputStream fis = new java.io.FileInputStream(filename);
			java.io.InputStreamReader in = new java.io.InputStreamReader(fis, encode);
			java.io.BufferedReader br = new java.io.BufferedReader(in);
			while((s=br.readLine())!=null) 
				sb.append(s).append("\r\n");
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static String getContent(String filename) {
		String s = "";
		StringBuffer sb = new StringBuffer();
		try {
			java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(filename)));
			while((s=br.readLine())!=null) 
				sb.append(s).append("\r\n");
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static List<String> contentList(String filename) {
		String s = "";
		List<String> sb = new ArrayList<String>();
		try {
			java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(filename)));
			while((s=br.readLine())!=null) 
				sb.add(s);
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb;
	}
	
	public static void writeFile(String filename, String content) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			fos.write(content.getBytes("utf-8"));
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
