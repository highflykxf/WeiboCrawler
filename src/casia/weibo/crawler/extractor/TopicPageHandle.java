package casia.weibo.crawler.extractor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import casia.weibo.crawler.login.SinaLogin;
import casia.weibo.crawler.utils.CommonUtils;
import casia.weibo.crawler.utils.DOMUtil;
import casia.weibo.crawler.utils.PHTMLCleaner;
import casia.weibo.crawler.utils.TimeUtil;
import casia.weibo.entity.Status;
import casia.weibo.entity.User;

public class TopicPageHandle {

	public static final String xpath_statuslist = ".//DIV[@action-type='feed_list_item']";
	public static final String xpath_nextpage = ".//DIV[@class='WB_cardwrap S_bg2']/@action-data";
	public static final String xpath_nextpage_full = ".//A[@class='page next S_txt1 S_line1']/@href";

	public List<Status> handleTopicPage(HttpClient client, String url) {
		List<Status> statuslist = new ArrayList<Status>();
		if (client == null) {
			client = SinaLogin.login();
			SinaLogin
					.writeCookie("D:/sina_cookies", (DefaultHttpClient) client);
		}

		// CommonUtils.writeString2File("D:/test.html", html);
		// String html = CommonUtils.readStringFromFile("D:/test.html");

		String topicid = url.substring(url.indexOf("p/") + 2, url.indexOf("?"));
		System.out.println("Topic id = " + topicid);

		Set<Long> status_id_set = new HashSet<Long>();
		int current_page = 0;
		String last_since_id = null;
		String res_type = null;
		String next_since_id = null;
		int max_try = 5;
		int page_count = 1;
		boolean all_finished = false;
		while (!all_finished) {
			String html = null;
			System.out.println("First:" + page_count);
			boolean found = false;
			int try_count = 0;
			while (!found && try_count < max_try) {
				html = CommonUtils.getHtml(url, client);
				if (html.indexOf("暂时没有内容哦") < 0) {
					found = true;
				} else {
					System.out.println("暂时没有内容哦~~~");
					TimeUtil.sleep(10);
				}
				try_count++;
			}
			if (!found) {
				break;
			}
			String results_str = null;
			Pattern pattern = Pattern.compile("<script>(.*?)</script>");
			Matcher matcher = pattern.matcher(html);
			while (matcher.find()) {
				results_str = matcher.group();
				if (results_str.indexOf("\"domid\":\"Pl_Third_App__11\"") >= 0) {
					break;
				}
			}
			if (results_str == null
					|| results_str.indexOf("\"domid\":\"Pl_Third_App__11\"") < 0
					|| results_str.indexOf("html") < 0) {
				System.out.println("First Parser Error!");
				CommonUtils.writeString2File("D:/tmp.html", results_str);
				continue;
			}
			results_str = CommonUtils.UnicodeToUTF8(results_str);
			results_str = results_str.substring(
					results_str.indexOf("\"html\""),
					results_str.indexOf("</script>"));
			try {
				DOMUtil dom = new DOMUtil();
				Node domtree = dom.ini(results_str, "UTF-8");
				NodeList nodes = XPathAPI.selectNodeList(domtree,
						xpath_statuslist);
				if (nodes == null || nodes.getLength() == 0) {
					break;
				}
				for (int i = 0; i < nodes.getLength(); i++) {
					Status status = extractInf(nodes.item(i));
					if (!status_id_set.contains(status.getId())) {
						statuslist.add(status);
					}
					status_id_set.add(status.getId());
				}
				System.out.println("Current=" + nodes.getLength());
				System.out.println("Total=" + status_id_set.size());
				Node nextpage_node = XPathAPI.selectSingleNode(domtree,
						xpath_nextpage);
				if (nextpage_node != null) {
					System.out.println(nextpage_node.getTextContent().trim());
					String rawstr = nextpage_node.getTextContent().trim();
					NextPageInf next_page = new NextPageInf(rawstr);
					current_page = next_page.getCurrent_page();
					last_since_id = next_page.getLast_since_id();
					res_type = next_page.getRes_type();
					next_since_id = next_page.getNext_since_id();
					if (current_page == 0) {
						break;
					}
					url = "http://weibo.com/p/aj/v6/mblog/mbloglist?ajwvr=6&domain=100808&feed_sort=timeline&feed_filter=timeline"
							+ "&pagebar=0&tab=home&current_page="
							+ current_page
							+ "&since_id=%7B%22"
							+ "last_since_id%22%3A"
							+ last_since_id
							+ "%2C%22res_type%22%3A"
							+ res_type
							+ "%2C%22next_since_id%22%3A"
							+ next_since_id
							+ "%7D&pl_name=Pl_Third_App__11&id="
							+ topicid
							+ "&script_uri=/p/"
							+ topicid
							+ "&feed_type=1&page="
							+ page_count
							+ "&pre_page="
							+ page_count
							+ "&domain_op=100808&__rnd="
							+ new Date().getTime();
					System.out.println(url);
				}
			} catch (TransformerException e) {
				e.printStackTrace();
			}

			// Second part in a page!!!
			System.out.println("Second!");
			try_count = 0;
			while (true) {
				found = false;
				while (!found && try_count < max_try) {
					html = CommonUtils.getHtml(url, client);
					results_str = CommonUtils.UnicodeToUTF8(html);
					if (results_str.indexOf("暂时没有内容哦") < 0
							&& results_str.indexOf("\"data\":\"\"") < 0) {
						found = true;
					} else {
						System.out.println("暂时没有内容哦~~~\t..." + try_count);
						TimeUtil.sleep(10);
					}
					try_count++;
					CommonUtils.writeString2File("D:/tmp.html",
							PHTMLCleaner.cleanHtml(results_str));
				}
				if (!found) {
					all_finished = true;
					break;
				}
				try {
					DOMUtil dom = new DOMUtil();
					Node domtree = dom.ini(results_str, "UTF-8");
					NodeList nodes = XPathAPI.selectNodeList(domtree,
							xpath_statuslist);
					if (nodes == null || nodes.getLength() == 0) {
						continue;
					}
					for (int i = 0; i < nodes.getLength(); i++) {
						Status status = extractInf(nodes.item(i));
						if (!status_id_set.contains(status.getId())) {
							statuslist.add(status);
						}
						status_id_set.add(status.getId());
					}
					System.out.println("Current=" + nodes.getLength());
					System.out.println("Total=" + status_id_set.size());
					Node nextpage_node = XPathAPI.selectSingleNode(domtree,
							xpath_nextpage);
					if (nextpage_node != null) {
						System.out.println(nextpage_node.getTextContent()
								.trim());
						String rawstr = nextpage_node.getTextContent().trim();
						NextPageInf next_page = new NextPageInf(rawstr);
						current_page = next_page.getCurrent_page();
						last_since_id = next_page.getLast_since_id();
						res_type = next_page.getRes_type();
						next_since_id = next_page.getNext_since_id();

						url = "http://weibo.com/p/aj/v6/mblog/mbloglist?ajwvr=6&domain=100808&feed_sort=timeline&feed_filter=timeline"
								+ "&pagebar=1&tab=home&current_page="
								+ current_page
								+ "&since_id=%7B%22"
								+ "last_since_id%22%3A"
								+ last_since_id
								+ "%2C%22res_type%22%3A"
								+ res_type
								+ "%2C%22next_since_id%22%3A"
								+ next_since_id
								+ "%7D&pl_name=Pl_Third_App__11&id="
								+ topicid
								+ "&script_uri=/p/"
								+ topicid
								+ "&feed_type=1&page="
								+ page_count
								+ "&pre_page="
								+ page_count
								+ "&domain_op=100808&__rnd="
								+ new Date().getTime();
						System.out.println(url);
					}
				} catch (TransformerException e) {
					e.printStackTrace();
				}
				break;
			}

			if (all_finished)
				break;

			// Third part in a page!!!
			System.out.println("Third!");
			try_count = 0;
			while (true) {
				found = false;
				while (!found && try_count < max_try) {
					html = CommonUtils.getHtml(url, client);
					results_str = CommonUtils.UnicodeToUTF8(html);
					if (results_str.indexOf("暂时没有内容哦") < 0
							&& results_str.indexOf("\"data\":\"\"") < 0) {
						found = true;
					} else {
						System.out.println("暂时没有内容哦~~~");
						TimeUtil.sleep(10);
					}
					try_count++;
					CommonUtils.writeString2File("D:/tmp.html",
							PHTMLCleaner.cleanHtml(results_str));
				}
				if (!found) {
					all_finished = true;
					break;
				}
				try {
					DOMUtil dom = new DOMUtil();
					Node domtree = dom.ini(results_str, "UTF-8");
					NodeList nodes = XPathAPI.selectNodeList(domtree,
							xpath_statuslist);
					if (nodes == null || nodes.getLength() == 0) {
						continue;
					}
					for (int i = 0; i < nodes.getLength(); i++) {
						Status status = extractInf(nodes.item(i));
						if (!status_id_set.contains(status.getId())) {
							statuslist.add(status);
						}
						status_id_set.add(status.getId());
					}
					System.out.println("Current=" + nodes.getLength());
					System.out.println("Total=" + status_id_set.size());
					Node nextpage_node = XPathAPI.selectSingleNode(domtree,
							xpath_nextpage_full);
					if (nextpage_node != null) {
						url = "http://weibo.com"
								+ nextpage_node.getTextContent().trim();
						System.out.println(url);
					} else {
						all_finished = true;
					}
					page_count++;
				} catch (TransformerException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		return statuslist;
	}

	public static final String xpath_mid = "@mid";
	public static final String xpath_content = ".//DIV[@node-type='feed_list_content']";
	public static final String xpath_pubtime = ".//DIV[@class='WB_from S_txt2']/A[@node-type='feed_list_item_date']/@date";
	public static final String xpath_source = ".//DIV[@class='WB_from S_txt2']/A[@action-type='app_source']/text()";
	public static final String xpath_sourceurl = ".//DIV[@class='WB_from S_txt2']/A[@action-type='app_source']/@href";
	public static final String xpath_url = ".//DIV[@class='WB_from S_txt2']/A[@node-type='feed_list_item_date']/@href";
	public static final String xpath_pic = ".//DIV[@class='media_box']/UL/LI[1]/IMG/@src";
	public static final String xpath_cmtcount = ".//DIV[@class='WB_feed_handle']/DIV/UL/LI[3]/A//EM[2]/text()";
	public static final String xpath_rttcount = ".//DIV[@class='WB_feed_handle']/DIV/UL/LI[2]/A//EM[2]/text()";

	public static final String xpath_userid = ".//DIV[@class='WB_info']/A[1]/@usercard";
	public static final String xpath_userhref = ".//DIV[@class='WB_info']/A[1]/@href";
	public static final String xpath_username = ".//DIV[@class='WB_info']/A[1]/@nick-name";
	public static final String xpath_userimg = ".//DIV[@class='face']/A/IMG/@src";
	public static final String xpath_verified = ".//I[@class='W_icon icon_approve']/@title";
	public static final String xpath_verified_co = ".//I[@class='W_icon icon_approve_co']/@title";

	public Status extractInf(Node node) {
		if (node == null)
			return null;
		Status status = new Status();
		User user = new User();
		status.setUser(user);
		try {
			Node time_node = XPathAPI.selectSingleNode(node, xpath_pubtime);
			if (time_node != null) {
				status.setCreatedAt(new Date(Long.parseLong(time_node
						.getTextContent().trim())));
			} else {
				System.out.println("Time Node Null!");
			}
			Node mid_node = XPathAPI.selectSingleNode(node, xpath_mid);
			if (mid_node != null) {
				status.setId(Long.parseLong(mid_node.getTextContent().trim()));
			} else {
				System.out.println("Mid Node Null!");
			}
			Node source_node = XPathAPI.selectSingleNode(node, xpath_source);
			if (source_node != null) {
				status.setSource(source_node.getTextContent().trim());
			} else {
				// System.out.println("Source Node Null!");
			}
			Node sourceurl_node = XPathAPI.selectSingleNode(node,
					xpath_sourceurl);
			if (sourceurl_node != null) {
				status.setSourceUrl(sourceurl_node.getTextContent().trim());
			} else {
				// System.out.println("Source url Node Null!");
			}
			Node url_node = XPathAPI.selectSingleNode(node, xpath_url);
			if (url_node != null) {
				status.setUrl(url_node.getTextContent().trim());
			} else {
				System.out.println("Url Node Null!");
			}
			Node pic_node = XPathAPI.selectSingleNode(node, xpath_pic);
			if (pic_node != null) {
				status.setThumbnail_pic(pic_node.getTextContent().trim());
			} else {
				// System.out.println("Pic Node Null!");
			}
			Node cmt_node = XPathAPI.selectSingleNode(node, xpath_cmtcount);
			if (cmt_node != null) {
				String cmt_str = cmt_node.getTextContent().trim();
				if (cmt_str.matches("^[0-9]+$")) {
					status.setCommCount(Integer.parseInt(cmt_str));
				}
			} else {
				System.out.println("Cmt Node Null!");
			}
			Node rtt_node = XPathAPI.selectSingleNode(node, xpath_rttcount);
			if (rtt_node != null) {
				String rtt_str = rtt_node.getTextContent().trim();
				if (rtt_str.matches("^[0-9]+$")) {
					status.setRttCount(Integer.parseInt(rtt_str));
				}
			} else {
				System.out.println("Rtt Node Null!");
			}
			status.setText(getWeiboText(XPathAPI.selectSingleNode(node,
					xpath_content)));

			Node userid_node = XPathAPI.selectSingleNode(node, xpath_userid);
			if (userid_node != null) {
				String userid = userid_node.getTextContent().trim();
				userid = userid.substring(userid.indexOf("id=") + 3,
						userid.indexOf("&"));
				user.setId(Long.parseLong(userid));
			} else {
				System.out.println("User id Node Null!");
			}
			Node user_href_node = XPathAPI.selectSingleNode(node,
					xpath_userhref);
			if (user_href_node != null) {
				String userurl = user_href_node.getTextContent().trim();
				if (userurl.lastIndexOf("?") > 0)
					userurl = userurl.substring(0, userurl.lastIndexOf("?"));
				user.setUrl(userurl);
			} else {
				System.out.println("User url Node Null!");
			}
			Node username_node = XPathAPI
					.selectSingleNode(node, xpath_username);
			if (username_node != null) {
				user.setName(username_node.getTextContent().trim());
			} else {
				System.out.println("Username Node Null!");
			}
			Node userimg_node = XPathAPI.selectSingleNode(node, xpath_userimg);
			if (userimg_node != null) {
				user.setProfileImageUrl(userimg_node.getTextContent().trim());
			} else {
				System.out.println("Userimg Node Null!");
			}
			Node user_verified_node = XPathAPI.selectSingleNode(node,
					xpath_verified);
			if (user_verified_node == null) {
				user_verified_node = XPathAPI.selectSingleNode(node,
						xpath_verified_co);
			}
			if (user_verified_node != null) {
				user.setVerifed(true);
				user.setVerifedDescription(user_verified_node.getTextContent()
						.trim());
			} else {
				user.setVerifed(false);
			}

			// System.out.println(status.getId() + "\t" + status.getCommCount()
			// + "\t" + status.getRttCount() + "\t"
			// + status.getCreatedAt().getTime() + "\t"
			// + status.getSource() + "\t" + status.getSourceUrl() + "\t"
			// + status.getUrl() + "\t" + status.getThumbnail_pic() + "\t"
			// + status.getText());
			// System.out.println(user.getId() + "\t" + user.getName() + "\t"
			// + user.getUrl() + "\t" + user.getProfileImageUrl() + "\t"
			// + user.getVerifedDescription());
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return status;
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
		DefaultHttpClient client = SinaLogin
				.getHttpClientWithCookies("D:/sina_cookies");
		TopicPageHandle handle = new TopicPageHandle();
		handle.handleTopicPage(
				client,
				"http://weibo.com/p/10080811f78df754cd78be77554d8a39fe6793?feed_sort=timeline&feed_filter=timeline#Pl_Third_App__11");
	}

}
