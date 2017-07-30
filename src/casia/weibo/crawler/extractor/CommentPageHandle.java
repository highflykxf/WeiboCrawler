package casia.weibo.crawler.extractor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import casia.weibo.crawler.login.SinaLogin;
import casia.weibo.crawler.utils.CommonUtils;
import casia.weibo.crawler.utils.DOMUtil;
import casia.weibo.crawler.utils.TimeDateUtil;
import casia.weibo.crawler.utils.TimeUtil;
import casia.weibo.entity.Comment;
import casia.weibo.entity.Status;
import casia.weibo.entity.User;

public class CommentPageHandle {
	public int sum_pages;
	public int LIMIT;
	public int SleepTime = 10;

	SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public CommentPageHandle() {
		sum_pages = -1;
		LIMIT = 100;
	}

	public static final String xpath_itemlist = ".//DIV[@node-type='comment_list']/DIV[@comment_id]";
	public static final String xpath_totalpage = ".//DIV[@class='W_pages']"
			+ "/A[@action-type='feed_list_page']/text()";
	public static final String xpath_toofast = ".//HEAD/TITLE/text()";

	public List<Comment> handleCommentPage(HttpClient client, Status status) {
		sum_pages = -1;
		if (status == null) {
			System.err.println("Status Info Not Complete!\n" + status);
			return null;
		}

		if (client == null) {
			client = SinaLogin.login();
		}

		List<Comment> commlist = new ArrayList<Comment>();
		Comment comm = null;

		int max_try = 10;
		int page = 1;
		String url = null;
		String html = null;
		DOMUtil dom = new DOMUtil();
		Node domtree = null;
		NodeList nodelist = null;
		Node node = null;
		int try_count = 0;
		while (try_count < max_try) {
			url = "http://weibo.com/aj/v6/comment/big?ajwvr=6&id="
					+ status.getId() + "&__rnd=" + new Date().getTime();
			if (page > 1) {
				url += "&page=" + page;
			}

			html = CommonUtils.getHtml(url, client);
			html = CommonUtils.UnicodeToUTF8(html);
			html = CommonUtils.replaceString(html);
			html = "<html>" + html + "</html>";
			// CommonUtils.writeString2File("D:/test.html", html);

			try {
				domtree = dom.ini(html, "UTF-8");
				Node node_toofast = XPathAPI.selectSingleNode(domtree,
						xpath_toofast);
				if (node_toofast != null
						&& node_toofast.getTextContent().trim()
								.equals("你们太快了!慢点!")) {
					System.out.println();
					TimeUtil.sleep(600);
					System.out.println("你们太快了!慢点!休息10分钟："
							+ myFmt.format(new Date()));
					continue;
				}

				nodelist = XPathAPI.selectNodeList(domtree, xpath_itemlist);
				if (nodelist == null || nodelist.getLength() == 0) {
					try_count++;
					continue;
				} else {
					try_count = 0;
				}
				for (int i = 0; i < nodelist.getLength(); i++) {
					node = nodelist.item(i);
					comm = parseCommentNode(node);
					if (comm != null) {
						comm.setStatus(status);
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
					}
					System.out.println("Sum pages = " + sum_pages);
				}

				System.out.println("Comments num = " + commlist.size());
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (page >= sum_pages)
				break;
			page++;
			TimeUtil.sleep(SleepTime);
		}

		return commlist;

		// html = CommonUtils.UnicodeToUTF8_File(html);
		// html = CommonUtils.cleanHtml(html);
		// CommonUtils.writeString2File("E:\\test.html", html);
	}

	public static final String xpath_commid = "@comment_id";
	public static final String xpath_content = ".//DIV[@class='WB_text']";
	public static final String xpath_pubtime = ".//DIV[@class='WB_from S_txt2']/text()";
	public static final String xpath_userid = "./DIV/DIV/A/@usercard";
	public static final String xpath_username = "./DIV/DIV/A[@usercard]/text()";
	public static final String xpath_imgurl = "./DIV/A/IMG[@usercard]/@src";
	public static final String xpath_verified = ".//DIV[@class='WB_text']"
			+ "//I[@class='W_icon icon_approve']/@title";
	public static final String xpath_verified_co = ".//DIV[@class='WB_text']"
			+ "//I[@class='W_icon icon_approve_co']/@title";

	public Comment parseCommentNode(Node node) {
		Node tmpnode = null;
		Comment comment = new Comment();
		User user = new User();
		try {
			// id
			tmpnode = XPathAPI.selectSingleNode(node, xpath_commid);
			comment.setId(Long.parseLong(tmpnode.getTextContent()));

			// pubtime
			tmpnode = XPathAPI.selectSingleNode(node, xpath_pubtime);
			comment.setCreatedAt(TimeDateUtil.getRealTime(tmpnode
					.getTextContent()));

			// text
			tmpnode = XPathAPI.selectSingleNode(node, xpath_content);
			String content = tmpnode.getTextContent().trim();
			if (content.indexOf('：') >= 0) {
				content = content.substring(content.indexOf('：') + 1);
			}
			comment.setText(content);
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

			// imgurl
			tmpnode = XPathAPI.selectSingleNode(node, xpath_imgurl);
			user.setProfileImageUrl(tmpnode.getTextContent());

			// verified
			tmpnode = XPathAPI.selectSingleNode(node, xpath_verified);
			if (tmpnode == null) {
				tmpnode = XPathAPI.selectSingleNode(node, xpath_verified_co);
			}
			user.setVerifed(tmpnode != null);
			if (tmpnode != null) {
				user.setVerifedDescription(tmpnode.getTextContent().trim());
			}

			comment.setUser(user);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return comment;
	}

}
