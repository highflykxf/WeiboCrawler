package casia.weibo.crawler.extractor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.http.client.HttpClient;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import casia.weibo.crawler.login.SinaLogin;
import casia.weibo.crawler.utils.CommonUtils;
import casia.weibo.crawler.utils.DOMUtil;
import casia.weibo.crawler.utils.TimeUtil;
import casia.weibo.entity.Comment;
import casia.weibo.entity.Status;
import casia.weibo.entity.User;

public class RetweetPageHandle {
	public int sum_pages = -1;
	public int LIMIT;
	public boolean fetchUserIdFromText;
	public int timeInteval;

	public RetweetPageHandle() {
		LIMIT = 100;
		fetchUserIdFromText = true;
		timeInteval = 60; //s
	}

	public static final String xpath_nodelist = "//DIV[@action-type='feed_list_item']";
	public static final String xpath_userid = "./DIV[@class='list_con']/DIV[@class='WB_text']/A/@usercard";
	public static final String xpath_username = "./DIV[@class='list_con']/DIV[@class='WB_text']/A[@usercard]";
	public static final String xpath_content = "./DIV[@class='list_con']/DIV[@class='WB_text']/SPAN[@node-type='text']";
	public static final String xpath_retweetid = "./@mid";
	public static final String xpath_totalpage = "//DIV[@class='W_pages']"
			+ "/A[@action-type='feed_list_page']/text()";

	public List<Comment> handleRetweetPage(HttpClient client, Status status) {
		if (status == null) {
			System.err.println("Status Info Not Complete!\n" + status);
			return null;
		}

		if (client == null) {
			client = SinaLogin.login();
		}

		List<Comment> commlist = new ArrayList<Comment>();
		Comment comm = null;

		int page = 1;
		String url = null;
		String html = null;
		DOMUtil dom = new DOMUtil();
		Node domtree = null;
		NodeList nodelist = null;
		Node node = null;
		while (true) {
			url = "http://weibo.com/aj/v6/mblog/info/big?_wv=5&ajwvr=6&id="
					+ status.getId() + "&__rnd=" + new Date().getTime();
			if (page > 1) {
				url += "&page=" + page;
			}
			html = CommonUtils.getHtml(url, client);
			html = CommonUtils.UnicodeToUTF8(html);
			html = CommonUtils.replaceString(html);
			html = "<html>" + html + "</html>";

			try {
				domtree = dom.ini(html, "UTF-8");
				nodelist = XPathAPI.selectNodeList(domtree, xpath_nodelist);
				for (int i = 0; i < nodelist.getLength(); i++) {
					node = nodelist.item(i);
					comm = parseRetweetNode(node,client);
					if (comm != null) {
						commlist.add(comm);
					}
				}

				if (sum_pages < 0) {
					nodelist = XPathAPI
							.selectNodeList(domtree, xpath_totalpage);
					if (nodelist.getLength() > 0) {
						sum_pages = Integer.parseInt(nodelist
								.item(nodelist.getLength() - 1)
								.getTextContent().trim());

//						System.out.println("Total Pages = " + sum_pages);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (page >= sum_pages || page >= LIMIT)
				break;
			page++;
			
			TimeUtil.sleep(timeInteval);

		}

		for (Comment retweet : commlist) {
			retweet.setStatus(status);
		}

		return commlist;
	}

	public Comment parseRetweetNode(Node node, HttpClient client) {
		Node tmpnode = null;
		Comment retweet = new Comment();
		User user = new User();
		try {
			// id
			tmpnode = XPathAPI.selectSingleNode(node, xpath_retweetid);
			retweet.setId(Long.parseLong(tmpnode.getTextContent()));

			// text
			tmpnode = XPathAPI.selectSingleNode(node, xpath_content);
			String content = getWeiboText(tmpnode);
			if(fetchUserIdFromText) {
				content = Username2Id.getUserIdFromText(content, client);
			}
			retweet.setText(content);
			if (content.length() == 0)
				return null;

			// userid
			tmpnode = XPathAPI.selectSingleNode(node, xpath_userid);
			String uid = tmpnode.getTextContent();
			uid = uid.replace("id=", "");
			user.setId(Long.parseLong(uid));
			user.setUrl("http://weibo.com/u/" + uid);

			// username
			tmpnode = XPathAPI.selectSingleNode(node, xpath_username);
			user.setName(tmpnode.getTextContent().trim());

			retweet.setUser(user);

			System.out.println("============================");
			System.out.println("Retweet Id = " + retweet.getId());
			System.out.println("Retweet Content = " + retweet.getText());
			System.out.println("Retweet Userid = " + user.getId());
			System.out.println("Retweet Username = " + user.getName());
			System.out.println("============================");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return retweet;
	}

	private String getWeiboText(Node textnode) {
		if (textnode == null)
			return "";
		String xpath_imgtitle = "./@title";
		NodeList nodelist = textnode.getChildNodes();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if ("A".equals(node.getNodeName())) {
				sb.append("{" + node.getTextContent().trim() + "}");
			} else if ("IMG".equals(node.getNodeName())) {
				try {
					Node imgnode = XPathAPI.selectSingleNode(node,
							xpath_imgtitle);
					if (imgnode != null) {
						sb.append(imgnode.getTextContent().trim());
					}
					imgnode = null;
				} catch (TransformerException e) {
					e.printStackTrace();
				}
			} else if ("#text".equals(node.getNodeName())) {
				sb.append(node.getTextContent().trim());
			}
			node = null;
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		Status status = new Status();
		status.setId(3836996470890649l);
		new RetweetPageHandle().handleRetweetPage(null, status);
		
		
	}
}
