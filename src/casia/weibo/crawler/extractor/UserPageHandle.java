package casia.weibo.crawler.extractor;

import javax.xml.transform.TransformerException;

import org.apache.http.client.HttpClient;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import casia.weibo.crawler.login.SinaLogin;
import casia.weibo.crawler.utils.CommonUtils;
import casia.weibo.crawler.utils.DOMUtil;
import casia.weibo.entity.User;

public class UserPageHandle {

	public static final String XPATH_SCRIPTLIST = "//SCRIPT";
	public static final String XPATH_FOLLOWCOUNT = "//TABLE[@class='tb_counter']"
			+ "/TBODY/TR/TD[1]//STRONG";
	public static final String XPATH_FANSCOUNT = "//TABLE[@class='tb_counter']"
			+ "/TBODY/TR/TD[2]//STRONG";
	public static final String XPATH_WEIBOCOUNT = "//TABLE[@class='tb_counter']"
			+ "/TBODY/TR/TD[3]//STRONG";
	public static final String XPATH_VERIFY = "//P[@class='verify clearfix']"
			+ "/SPAN[1]/A/@href";

	public User handleUserPage(long userid, HttpClient client) {
		if (userid == 0)
			return null;

		User user = new User();
		user.setId(userid);
		user.setVerifedDescription("0");

		String url = "http://weibo.com/u/" + userid;
		String html = CommonUtils.getHtml(url, client);
		// CommonUtils.writeString2File("E:\\test.html", html);

		DOMUtil dom = new DOMUtil();
		NodeList nodelist = null;
		Node node = null;
		try {
			Node domtree = dom.ini(html, "UTF-8");
			nodelist = XPathAPI.selectNodeList(domtree, XPATH_SCRIPTLIST);
			for (int i = 0; i < nodelist.getLength(); i++) {
				if (nodelist.item(i).getTextContent().contains("PCD_counter")) {
					node = nodelist.item(i);
					break;
				}
			}
			if (node == null)
				return null;
			String counthtml = CommonUtils.replaceString(node.getTextContent()
					.trim());

			node = null;
			for (int i = 0; i < nodelist.getLength(); i++) {
				if (nodelist.item(i).getTextContent()
						.contains("verify clearfix")) {
					node = nodelist.item(i);
					break;
				}
			}
			String verifyhtml = null;
			if (node != null)
				verifyhtml = CommonUtils.replaceString(node.getTextContent()
						.trim());

			domtree = dom.ini(counthtml, "UTF-8");
			node = XPathAPI.selectSingleNode(domtree, XPATH_FOLLOWCOUNT);
			if (node != null) {
				int followcount = Integer
						.parseInt(node.getTextContent().trim());
				user.setFriendsCount(followcount);
			}

			node = XPathAPI.selectSingleNode(domtree, XPATH_FANSCOUNT);
			if (node != null) {
				int fanscount = Integer.parseInt(node.getTextContent().trim());
				user.setFollowersCount(fanscount);
			}

			node = XPathAPI.selectSingleNode(domtree, XPATH_WEIBOCOUNT);
			if (node != null) {
				int weibocount = Integer.parseInt(node.getTextContent().trim());
				user.setStatusesCount(weibocount);
			}

			if (verifyhtml != null) {
				domtree = dom.ini(verifyhtml, "UTF-8");
				node = XPathAPI.selectSingleNode(domtree, XPATH_VERIFY);
				if (node != null) {
					String vtext = node.getTextContent();
					if (vtext.contains("company")) {
						user.setVerifedDescription("3");
					} else if (vtext.contains("club")) {
						user.setVerifedDescription("1");
					} else if (vtext.contains("verified")) {
						user.setVerifedDescription("2");
					}
				}
			}

		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return user;
	}

	public static void main(String[] args) {
		HttpClient client = SinaLogin.getDefaultHttpClient();
		long userid = Username2Id.getUserIdByName("老罗的微博", client);
		System.out.println(userid);
		User user = new UserPageHandle().handleUserPage(userid, client);
		System.out.println(user.getFollowersCount());
		System.out.println(user.getFriendsCount());
		System.out.println(user.getStatusesCount());
		System.out.println(user.getVerifedDescription());
	}

}
