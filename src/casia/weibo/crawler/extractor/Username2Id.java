package casia.weibo.crawler.extractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.apache.http.client.HttpClient;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import casia.weibo.crawler.login.SinaLogin;
import casia.weibo.crawler.utils.CommonUtils;
import casia.weibo.crawler.utils.DOMUtil;

public class Username2Id {

	public static final Pattern idPattern = Pattern
			.compile("\\$CONFIG\\['oid'\\]='[0-9]+?';");
	public static final Pattern atPattern = Pattern.compile("\\{@(.*?)\\}");

	public static long getUserIdByName(String username, HttpClient client) {
		if (username == null || username.trim().length() == 0)
			return 0;

		if (client == null) {
			client = SinaLogin.getDefaultHttpClient();
		}
		String url = "http://weibo.com/n/" + username + "?from=feed&amp;loc=at";
		String html = CommonUtils.getHtml_NonAutoRedirect(url, client);

		CommonUtils.writeString2File("E:\\testerror.html", html);
		long userid = 0;
		int endindex = html.indexOf("<!-- / $CONFIG -->");
		if (endindex < 0) {
			// CommonUtils.writeString2File("E:\\testerror.html", html);
			return getUserIDFromHTML(html);
		}
		html = html.substring(0, endindex);

		Matcher matcher = idPattern.matcher(html);
		if (matcher.find()) {
			String idstr = matcher.group();
			idstr = idstr.replaceAll("[^0-9]", "");
			userid = Long.parseLong(idstr);
		}

		return userid;
	}

	public static final String XPATH_SCRIPTLIST = "//SCRIPT";
	public static final String XPATH_USERURL = "//DIV[@class='person_detail']/P/A/@uid";

	private static long getUserIDFromHTML(String html) {
		DOMUtil dom = new DOMUtil();
		NodeList nodelist = null;
		Node node = null;
		try {
			Node domtree = dom.ini(html, "UTF-8");
			nodelist = XPathAPI.selectNodeList(domtree, XPATH_SCRIPTLIST);
			for (int i = 0; i < nodelist.getLength(); i++) {
				if (nodelist.item(i).getTextContent()
						.contains("\"pid\":\"pl_user_feedList\"")) {
					node = nodelist.item(i);
					break;
				}
			}
			if (node == null)
				return 0;

			String idhtml = node.getTextContent().trim();
			idhtml = CommonUtils.replaceString(idhtml);
			domtree = dom.ini(idhtml, "UTF-8");
			node = XPathAPI.selectSingleNode(domtree, XPATH_USERURL);
			if (node == null)
				return 0;
			String uid = node.getTextContent().trim();
			if (!uid.matches("^[0-9]+$"))
				return 0;
			return Long.parseLong(uid);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static String getUserIdFromText(String text, HttpClient client) {
		if (text == null || text.trim().length() == 0)
			return text;

		String newtext = new String(text);
		Matcher matcher = null;
		while (true) {
			matcher = atPattern.matcher(newtext);
			if (matcher.find()) {
				String username = matcher.group();
				long userid = getUserIdByName(
						username.substring(2, username.length() - 1), client);
				String tag = "{At:"
						+ username.substring(2, username.length() - 1) + "-"
						+ userid + "}";
				newtext = newtext.replace(username, tag);
			} else {
				break;
			}
		}

		return newtext;
	}

	public static void main(String[] args) {
		// V新传媒V
		HttpClient client = SinaLogin.getDefaultHttpClient();
		long userid = getUserIdByName("千雪SAMA", client);
		System.out.println(userid);

	}

}
