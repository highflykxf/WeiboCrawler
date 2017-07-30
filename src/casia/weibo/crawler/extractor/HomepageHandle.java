package casia.weibo.crawler.extractor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.transform.TransformerException;

import org.apache.http.client.HttpClient;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import casia.weibo.crawler.login.SinaLogin;
import casia.weibo.crawler.utils.CommonUtils;
import casia.weibo.crawler.utils.DOMUtil;
import casia.weibo.entity.Status;
import casia.weibo.entity.User;

public class HomepageHandle {

	public int sum_pages;

	public int LIMIT;

	public static SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss");

	private static final String siteprefix = "http://weibo.com";

	public HomepageHandle() {
		sum_pages = -1;
		LIMIT = 10;
	}

	public ArrayList<Status> handleStatusPages(HttpClient client, User user,
			String keyword) {
		ArrayList<Status> wbloglist = new ArrayList<Status>();
		sum_pages = -1;

		int page = 1;
		int count = 15;
		String max_id = "";
		int pre_page = 1;
		String end_id = "";
		int pagebar = 0;
		String uid = "" + user.getId();
		String __rnd = "" + new Date().getTime();
		while (true) {
			output("开始分析用户" + user.getId() + "的微博页面:" + page + " ...");
			String url = "";
			String htmltext = "";

			pagebar = 0;
			__rnd = "" + new Date().getTime();
			if (page == 1) {
				url = "http://weibo.com/aj/mblog/mbloglist?_wv=5&page=" + page
						+ "&pagebar=" + pagebar + "&uid=" + uid + "&_t=0"
						+ "&__rnd=" + __rnd;
			} else {
				pre_page = page - 1;
				url = "http://weibo.com/aj/mblog/mbloglist?_wv=5&page=" + page
						+ "&pre_page=" + pre_page + "&end_id=" + end_id
						+ "&pagebar=" + pagebar + "&uid=" + uid + "&_t=0"
						+ "&__rnd=" + __rnd;
			}
			htmltext = CommonUtils.getHtml(url, client);
			htmltext = CommonUtils.UnicodeToUTF8(htmltext);
			CommonUtils.writeString2File("E:\\test.html", htmltext);
			ArrayList<Status> statuslist = handleAStatusPage(htmltext, user,
					keyword);
			if (statuslist.size() > 0) {
				if (page == 1) {
					end_id = "" + statuslist.get(0).getId();
				}
				max_id = "" + statuslist.get(statuslist.size() - 1).getId();
				wbloglist.addAll(statuslist);
			}
			

			pagebar = 0;
			__rnd = "" + new Date().getTime();
			pre_page = page;
			url = "http://weibo.com/aj/mblog/mbloglist?_wv=5&page=" + page
					+ "&count=" + count + "&max_id=" + max_id + "&pre_page="
					+ pre_page + "&end_id=" + end_id + "&pagebar=" + pagebar
					+ "&uid=" + uid + "&_t=0" + "&__rnd=" + __rnd;
			htmltext = CommonUtils.getHtml(url, client);
			htmltext = CommonUtils.UnicodeToUTF8(htmltext);
			statuslist = handleAStatusPage(htmltext, user, keyword);
			if (statuslist.size() > 0) {
				max_id = "" + statuslist.get(statuslist.size() - 1).getId();
				wbloglist.addAll(statuslist);
			}

			pagebar = 1;
			__rnd = "" + new Date().getTime();
			pre_page = page;
			url = "http://weibo.com/aj/mblog/mbloglist?_wv=5&page=" + page
					+ "&count=" + count + "&max_id=" + max_id + "&pre_page="
					+ pre_page + "&end_id=" + end_id + "&pagebar=" + pagebar
					+ "&uid=" + uid + "&_t=0" + "&__rnd=" + __rnd;
			htmltext = CommonUtils.getHtml(url, client);
			htmltext = CommonUtils.UnicodeToUTF8(htmltext);
			statuslist = handleAStatusPage(htmltext, user, keyword);
			if (statuslist.size() > 0) {
				max_id = "" + statuslist.get(statuslist.size() - 1).getId();
				wbloglist.addAll(statuslist);
			}

			output("用户" + user.getId() + "的微博页面:" + page + "/" + sum_pages
					+ " 分析完成...");
			if ((sum_pages >= 0 && page >= sum_pages) || page >= LIMIT
					|| sum_pages < 0)
				break;
			page++;

		}

		System.out.println(wbloglist.size());
		return wbloglist;
	}

	private static final String xpath_statuislist = "//DIV[@action-type='feed_list_item']";
	private static final String xpath_pagelist = "//DIV[@class='W_pages']/SPAN/DIV/UL/LI[1]/A/@href";

	public ArrayList<Status> handleAStatusPage(String htmltext, User user,
			String keyword) {
		// CommonUtils.writeString2File("E:\\test.html", htmltext);
		ArrayList<Status> wbloglist = new ArrayList<Status>();
		try {
			DOMUtil dom = new DOMUtil();
			Node domtree = dom.ini(htmltext, "UTF-8");
			NodeList nodes = XPathAPI
					.selectNodeList(domtree, xpath_statuislist);
			for (int i = 0; i < nodes.getLength(); i++) {
				Status status = extractStatusFromNode(nodes.item(i), user,
						keyword);
				if (status != null) {
					wbloglist.add(status);
					// System.out.println(status);
				}
			}

			if (sum_pages < 0) {
				Node pagenode = XPathAPI.selectSingleNode(domtree,
						xpath_pagelist);
				if (pagenode != null) {
					String pageStr = pagenode.getTextContent().trim();
					int startindex = pageStr.indexOf("page=");
					int endindex = pageStr.indexOf("&", startindex);
					if (startindex >= 0 && endindex >= 0) {
						sum_pages = Integer.parseInt(pageStr.substring(
								startindex + 5, endindex));
					}
				}
			}

		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return wbloglist;
	}

	@SuppressWarnings("unused")
	private void detectNode(Node node) {
		NodeList nlist = node.getChildNodes().item(3).getChildNodes();
		for (int i = 0; i < nlist.getLength(); i++) {
			if (nlist.item(i).getAttributes() == null) {
				System.out.println(i);
				continue;
			}
			System.out.println(nlist.item(i).getNodeName()
					+ nlist.item(i).getAttributes().getNamedItem("class"));
		}
	}

	private static final String xpath_text = "./DIV/DIV/DIV[@class='WB_text']";
	private static final String xpath_id = "./@mid";
	private static final String xpath_id2 = "./DIV/DIV[@class='WB_detail']/DIV/DIV[@class='WB_handle']/A[2]/@action-data";
	private static final String xpath_date = "./DIV/DIV/DIV/DIV[@class='WB_from']/A/@date";
	private static final String xpath_source = "./DIV/DIV/DIV/DIV[@class='WB_from']/A[2]/text()";
	private static final String xpath_sourceurl = "./DIV/DIV/DIV/DIV[@class='WB_from']/A[2]/@href";
	private static final String xpath_img = "./DIV/DIV[@class='WB_detail']/UL/LI/DIV/IMG[@class='bigcursor']/@src";
	private static final String xpath_imglist = "./DIV/DIV[@class='WB_detail']/UL/LI"
			+ "/DIV[@node-type='fl_pic_list']/UL/LI/IMG/@src";
	private static final String xpath_url = "./DIV/DIV/DIV/DIV[@class='WB_from']/A[1]/@href";
	private static final String xpath_rtt = "./DIV/DIV[@class='WB_detail']/DIV/DIV[@class='WB_handle']"
			+ "/A[2]/text()";
	private static final String xpath_comm = "./DIV/DIV[@class='WB_detail']/DIV/DIV[@class='WB_handle']"
			+ "/A[4]/text()";

	private static final String xpath_ret = "./@isforward";
	private static final String xpath_oid = "./@omid";
	private static final String xpath_oid2 = ".//DIV[@node-type='feed_list_forwardContent']/DIV"
			+ "/DIV/DIV[@class='WB_handle']/@mid";
	private static final String xpath_otext = ".//DIV[@node-type='feed_list_forwardContent']/DIV"
			+ "/DIV[@class='WB_text']";
	private static final String xpath_odate = ".//DIV[@node-type='feed_list_forwardContent']/DIV"
			+ "/DIV/DIV[@class='WB_from']/A/@date";
	private static final String xpath_osource = ".//DIV[@node-type='feed_list_forwardContent']/DIV"
			+ "/DIV/DIV[@class='WB_from']/A[@action-type='app_source']/text()";
	private static final String xpath_osourceurl = ".//DIV[@node-type='feed_list_forwardContent']/DIV"
			+ "/DIV/DIV[@class='WB_from']/A[@action-type='app_source']/@href";
	private static final String xpath_oimg = ".//DIV[@node-type='feed_list_forwardContent']/DIV"
			+ "/DIV/UL/LI/DIV/IMG[@class='bigcursor']/@src";
	private static final String xpath_oimglist = ".//DIV[@node-type='feed_list_forwardContent']/DIV"
			+ "/DIV/UL/LI/DIV[@node-type='fl_pic_list']/UL/LI/IMG/@src";
	private static final String xpath_ourl = ".//DIV[@node-type='feed_list_forwardContent']/DIV"
			+ "/DIV/DIV[@class='WB_handle']/A[3]/@href";
	private static final String xpath_ortt = ".//DIV[@node-type='feed_list_forwardContent']/DIV"
			+ "/DIV/DIV[@class='WB_handle']/A[2]/text()";
	private static final String xpath_ocomm = ".//DIV[@node-type='feed_list_forwardContent']/DIV"
			+ "/DIV/DIV[@class='WB_handle']/A[3]/text()";

	private static final String xpath_ouid = ".//DIV[@node-type='feed_list_forwardContent']/DIV/DIV[@class='WB_info']"
			+ "/A[@node-type='feed_list_originNick']/@usercard";
	private static final String xpath_ouname = ".//DIV[@node-type='feed_list_forwardContent']/DIV/DIV[@class='WB_info']"
			+ "/A[@node-type='feed_list_originNick']/@nick-name";
	private static final String xpath_ouurl = ".//DIV[@node-type='feed_list_forwardContent']/DIV/DIV[@class='WB_info']"
			+ "/A[@node-type='feed_list_originNick']/@href";
	private static final String xpath_ouverify = ".//DIV[@node-type='feed_list_forwardContent']/DIV/DIV[@class='WB_info']"
			+ "/A[@href='http://verified.weibo.com/verify']";

	public Status extractStatusFromNode(Node node, User user, String keyword) {
		Status status = new Status();
		status.setUser(user);

		try {
			Node idnode = XPathAPI.selectSingleNode(node, xpath_id);
			if (idnode != null) {
				String idstr = idnode.getTextContent().trim();
				if (idstr.length() == 0) {
					Node idnode2 = XPathAPI.selectSingleNode(node, xpath_id2);
					if (idnode2 != null) {
						idstr = idnode2.getTextContent().trim();
						int startindex = idstr.indexOf("mid=");
						int endindex = idstr.indexOf("&", startindex);
						if (startindex >= 0 && endindex >= 0) {
							idstr = idstr.substring(startindex + 4, endindex);
						}
					}
				}
				if (idstr.length() > 0) {
					status.setId(Long.parseLong(idnode.getTextContent().trim()));
					status.setMid(CommonUtils.Id2Mid("" + status.getId()));
				} else {
					return null;
				}
			}

			Node textnode = XPathAPI.selectSingleNode(node, xpath_text);
			if (textnode != null) {
				status.setText(getWeiboText(textnode));
			}

			Node datenode = XPathAPI.selectSingleNode(node, xpath_date);
			if (datenode != null) {
				status.setCreatedAt(new Date(Long.parseLong(datenode
						.getTextContent().trim())));
			} else {
				System.out.println("date null!");
			}

			Node sourcenode = XPathAPI.selectSingleNode(node, xpath_source);
			if (sourcenode != null) {
				status.setSource(sourcenode.getTextContent().trim());
			}

			Node sourceurlnode = XPathAPI.selectSingleNode(node,
					xpath_sourceurl);
			if (sourceurlnode != null) {
				status.setSourceUrl(sourceurlnode.getTextContent().trim());
			}

			Node singleimgnode = XPathAPI.selectSingleNode(node,
					xpath_img);
			if (singleimgnode != null) {
				status.setThumbnail_pic(singleimgnode.getTextContent().trim());
			}
			NodeList imgnodes = XPathAPI.selectNodeList(node, xpath_imglist);
			if (imgnodes != null) {
				String imgstr = "";
				if(status.getThumbnail_pic()!=null) {
					imgstr = status.getThumbnail_pic();
				}
				if(imgstr.length()>0&&imgnodes.getLength()>0) {
					imgstr += "||";
				}
				for(int i=0;i<imgnodes.getLength();i++) {
					Node imgnode = imgnodes.item(i);
					imgstr += imgnode.getTextContent().trim();
					if(i!=imgnodes.getLength()-1) {
						imgstr += "||";
					}
					imgnode = null;
				}
				status.setThumbnail_pic(imgstr);
			}
			if(status.getThumbnail_pic()!=null) {
				status.setBmiddle_pic(getBigImgUrl(status.getThumbnail_pic()));
			}

			Node urlnode = XPathAPI.selectSingleNode(node, xpath_url);
			if (urlnode != null) {
				String url = urlnode.getTextContent().trim();
				if (url.charAt(0) == '/') {
					url = siteprefix + url;
				}
				status.setUrl(url);
			}

			Node rttnode = XPathAPI.selectSingleNode(node, xpath_rtt);
			if (rttnode != null) {
				status.setRttCount(getNumberInBrakets(rttnode.getTextContent()
						.trim()));
			}

			Node commnode = XPathAPI.selectSingleNode(node, xpath_comm);
			if (commnode != null) {
				status.setCommCount(getNumberInBrakets(commnode
						.getTextContent().trim()));
			}

			Node retnode = XPathAPI.selectSingleNode(node, xpath_ret);
			if (retnode != null) {
				Status ostatus = new Status();

				Node oidnode = XPathAPI.selectSingleNode(node, xpath_oid);
				if (oidnode != null) {
					String idstr = oidnode.getTextContent().trim();
					if (idstr.length() == 0) {
						Node oidnode2 = XPathAPI.selectSingleNode(node,
								xpath_oid2);
						if (oidnode2 != null) {
							idstr = oidnode2.getTextContent().trim();
						}
					}
					if (idstr.length() > 0) {
						ostatus.setId(Long.parseLong(oidnode.getTextContent()
								.trim()));
						ostatus.setMid(CommonUtils.Id2Mid("" + ostatus.getId()));
					} else {
						System.err.println("OID NULL!");
					}
				}

				Node otextnode = XPathAPI.selectSingleNode(node, xpath_otext);
				if (otextnode != null) {
					ostatus.setText(getWeiboText(otextnode));
				}

				Node odatenode = XPathAPI.selectSingleNode(node, xpath_odate);
				if (odatenode != null) {
					long odatetime = 0;
					if (odatenode.getTextContent().trim().length() > 0) {
						odatetime = Long.parseLong(odatenode.getTextContent()
								.trim());
					}
					ostatus.setCreatedAt(new Date(odatetime));
				}

				Node osrcnode = XPathAPI.selectSingleNode(node, xpath_osource);
				if (osrcnode != null) {
					ostatus.setSource(osrcnode.getTextContent().trim());
				}

				Node osrcurlnode = XPathAPI.selectSingleNode(node,
						xpath_osourceurl);
				if (osrcurlnode != null) {
					ostatus.setSourceUrl(osrcnode.getTextContent().trim());
				}

				Node osingleimgnode = XPathAPI.selectSingleNode(node, xpath_oimg);
				if (osingleimgnode != null) {
					ostatus.setThumbnail_pic(osingleimgnode.getTextContent().trim());
				}
				NodeList oimgnodes = XPathAPI.selectNodeList(node, xpath_oimglist);
				if (oimgnodes != null) {
					String imgstr = "";
					if(ostatus.getThumbnail_pic()!=null) {
						imgstr = ostatus.getThumbnail_pic();
					}
					if(imgstr.length()>0&&oimgnodes.getLength()>0) {
						imgstr += "||";
					}
					for(int i=0;i<oimgnodes.getLength();i++) {
						Node oimgnode = oimgnodes.item(i);
						imgstr += oimgnode.getTextContent().trim();
						if(i!=oimgnodes.getLength()-1) {
							imgstr += "||";
						}
						oimgnode = null;
					}
					ostatus.setThumbnail_pic(imgstr);
				}
				if(ostatus.getThumbnail_pic()!=null) {
					ostatus.setBmiddle_pic(getBigImgUrl(ostatus.getThumbnail_pic()));
				}

				Node ourlnode = XPathAPI.selectSingleNode(node, xpath_ourl);
				if (ourlnode != null) {
					String url = ourlnode.getTextContent().trim();
					if (url.charAt(0) == '/') {
						url = siteprefix + url;
					}
					ostatus.setUrl(url);
				}

				Node orttnode = XPathAPI.selectSingleNode(node, xpath_ortt);
				if (orttnode != null) {
					ostatus.setRttCount(getNumberInBrakets(orttnode
							.getTextContent().trim()));
				}

				Node ocommnode = XPathAPI.selectSingleNode(node, xpath_ocomm);
				if (ocommnode != null) {
					ostatus.setCommCount(getNumberInBrakets(ocommnode
							.getTextContent().trim()));
				}

				User ouser = new User();
				Node uidnode = XPathAPI.selectSingleNode(node, xpath_ouid);
				if (uidnode != null) {
					String uid = uidnode.getTextContent().trim();
					uid = uid.replace("id=", "");
					ouser.setId(Long.parseLong(uid));
				}

				Node usernamenode = XPathAPI.selectSingleNode(node,
						xpath_ouname);
				if (usernamenode != null) {
					ouser.setName(usernamenode.getTextContent().trim());
				}

				Node userurlnode = XPathAPI.selectSingleNode(node, xpath_ouurl);
				if (userurlnode != null) {
					String userurl = userurlnode.getTextContent().trim();
					if (userurl.charAt(0) == '/') {
						userurl = siteprefix + userurl;
					}
					ouser.setUrl(userurl);
				}

				Node verifynode = XPathAPI.selectSingleNode(node,
						xpath_ouverify);
				if (verifynode != null) {
					ouser.setVerifed(true);
				} else {
					ouser.setVerifed(false);
				}

				ouser.setLackInf(true);
				ostatus.setUser(ouser);
				status.setRetweeted_status(ostatus);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return status;
	}
	
	private String getWeiboText(Node textnode) {
		if(textnode == null)
			return "";
		String xpath_imgtitle = "./@title";
		NodeList nodelist = textnode.getChildNodes();
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<nodelist.getLength();i++) {
			Node node = nodelist.item(i);
			if("A".equals(node.getNodeName())) {
				sb.append("{"+node.getTextContent().trim()+"}");
			} else if("IMG".equals(node.getNodeName())) {
				try {
					Node imgnode = XPathAPI.selectSingleNode(node, xpath_imgtitle);
					if(imgnode!=null) {
						sb.append(imgnode.getTextContent().trim());
					}
					imgnode = null;
				} catch (TransformerException e) {
					e.printStackTrace();
				}
			} else if("#text".equals(node.getNodeName())) {
				sb.append(node.getTextContent().trim());
			}
			node = null;
		}
		return sb.toString();
	}

	private int getNumberInBrakets(String str) {
		if (str == null)
			return 0;
		int number = 0;
		int startindex = str.indexOf('(');
		int endindex = str.indexOf(')', startindex);
		if (startindex >= 0 && endindex >= 0) {
			number = Integer.parseInt(str.substring(startindex + 1, endindex));
		}
		return number;
	}
	
	private String getBigImgUrl(String url) {
		if(url == null)
			return null;
		url = url.replace("square", "bmiddle");
		url = url.replace("thumbnail", "bmiddle");
		return url;
	}

	public void output(String message) {
		System.out.println(message);
	}

	public static void main(String[] args) {
		User user = new User();
		user.setId(Long.parseLong("3819085127"));
		ArrayList<Status> statuslist = new HomepageHandle().handleStatusPages(
				SinaLogin.getDefaultHttpClient(), user, null);
		System.out.println("All:" + statuslist.size());
		for(Status status : statuslist) {
//			if(status.getRetweeted_status()==null)
//				continue;
			System.out.println(status.getText()
					+"----"+status.getBmiddle_pic());
		}
		// System.out.println("start:");
		// ArrayList<Status> statuslist = new
		// HomepageHandle(null).fetchTopStatus(
		// cm.getMainClient(), user, 100);
		// System.out.println("Size:"+statuslist.size());
		//
		// StringBuffer wstr = new StringBuffer("Size:"+statuslist.size());
		// for(Status status : statuslist) {
		// wstr.append(""+status.getId()+":"+status.getText()+"\r\n-------------------------------\r\n");
		// }
		// CommonUtils.writeString2File("D:\\statusall.txt", new String(wstr));
	}

}
