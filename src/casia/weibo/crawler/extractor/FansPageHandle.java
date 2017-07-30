package casia.weibo.crawler.extractor;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import casia.weibo.crawler.login.SinaLogin;
import casia.weibo.crawler.utils.CommonUtils;
import casia.weibo.crawler.utils.DOMUtil;
import casia.weibo.entity.User;

public class FansPageHandle {
	
	public int sum_pages_fans;
	
	public int LIMIT;
	
	private static final String siteprefix = "http://weibo.com";
	private static final String malestr = "W_icon icon_male";
	private static final String femalestr = "W_icon icon_female";
	private static final String verifiedurl = "http://verified.weibo.com/verify";
	
	private static final String JSTAG = "Pl_Official_HisRelation";
	private static final String FOLLOWITEMTAG = "follow_item S_line2";
	
	private static final String xpath_followitem = "//LI[@class='"+FOLLOWITEMTAG+"']";
	private static final String xpath_scriptlist = "//SCRIPT/text()";
	private static final String xpath_userid = "./DL/DD/DIV/A/@usercard";
	private static final String xpath_username = "./DL/DD/DIV/A[@usercard]/text()";
	private static final String xpath_location = "./DL/DD/DIV[@class='info_add']/SPAN/text()";
	private static final String xpath_description = "./DL/DD/DIV[@class='info_intro']/SPAN/text()";
	private static final String xpath_url = "./DL/DD/DIV/A[@usercard]/@href";
	private static final String xpath_pimgurl = "./DL/DT[@class='mod_pic']/A/IMG/@src";
	private static final String xpath_gender = "./DL/DD[1]/DIV[1]/A[last()]/I/@class";
	private static final String xpath_followcount = "./DL/DD/DIV[@class='info_connect']/SPAN[1]/EM/A/text()";
	private static final String xpath_fanscount = "./DL/DD/DIV[@class='info_connect']/SPAN[2]/EM/A/text()";
	private static final String xpath_statuscount = "./DL/DD/DIV[@class='info_connect']/SPAN[3]/EM/A/text()";
	private static final String xpath_verified = "./DL/DD/DIV/A[@href='"+verifiedurl+"']";
	private static final String xpath_pagelimit = "//DIV[@class='W_pages']/A[last()-1]/text()";
	
	public FansPageHandle() {
		sum_pages_fans = -1;
		LIMIT = 5;
	}
	
	public ArrayList<User> handleFansPage(HttpClient client,String userid) {
		ArrayList<User> fanslist = new ArrayList<User>();
		sum_pages_fans = -1;
		
		try {
			HttpGet getMethod = new HttpGet("http://weibo.com/"+userid+"/fans");
			HttpConnectionParams.setConnectionTimeout(getMethod.getParams(), 10000);
			HttpConnectionParams.setSoTimeout(getMethod.getParams(), 10000);
			HttpResponse response = client.execute(getMethod);
			String entity = EntityUtils.toString(response.getEntity(), "utf-8");
	        
			output("开始解析用户："+userid +"的粉丝页！");
	        ArrayList<User> flist = parserFanspage(client,entity);
	        if(flist!=null)
	        	fanslist.addAll(flist);

	        if(sum_pages_fans>1) {
				for (int i = 2; i <= sum_pages_fans && i<=LIMIT; i++) {
					entity = CommonUtils.getHtml("http://weibo.com/"+userid+"/fans?page="+i,null);
			        flist = parserFanspage(client,entity);
			        if(flist!=null)
			        	fanslist.addAll(flist);
				}
	        }
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fanslist;
	}
	
	public ArrayList<User> parserFanspage(HttpClient client,String htmltext) {
		ArrayList<User> fanslist = null;
		try {
			DOMUtil dom = new DOMUtil();
			Node domtree = dom.ini(htmltext, "UTF-8");
			NodeList nodes = XPathAPI.selectNodeList(domtree, xpath_scriptlist);

			String fanstext = "";
			for (int i = 0; i < nodes.getLength(); i++) {
				String str = nodes.item(i).getTextContent();
				String substr = str.substring(0,
						100 > str.length() ? str.length() : 100);
				if (substr.indexOf(JSTAG) != -1) {
					str = str.replace("\\/", "/");

					String[] strs = str.split("\"html\":");
					str = strs[1];
					strs = str.split("</script>");
					str = strs[0];

					str = str.replace("\\n", "\n");
					str = str.replace("\\t", "\t");
					str = str.replace("\\r", "\r");
					str = str.replace("\\\"", "\"");

					str = "<html>" + str + "</html>";

					fanstext = str;
					
				}
			}

			CommonUtils.writeString2File("E:\\test.html", fanstext);
			domtree = dom.ini(fanstext, "UTF-8");
			nodes = XPathAPI.selectNodeList(domtree, xpath_followitem);
			fanslist = new ArrayList<User>();
			for(int i = 0;i<nodes.getLength();i++) {
				Node node = nodes.item(i);
				User fans = new User();
				
				Node idnode = XPathAPI.selectSingleNode(node, xpath_userid);
				if(idnode != null) {
					String userid = idnode.getTextContent().trim();
					userid = userid.substring(3);
					fans.setId(Long.parseLong(userid));
				}
				
				Node namenode = XPathAPI.selectSingleNode(node, xpath_username);
				if(namenode != null) {
					fans.setName(namenode.getTextContent().trim());
					fans.setScreenName(fans.getName());
				}
				
				Node locnode = XPathAPI.selectSingleNode(node, xpath_location);
				if(locnode!=null) {
					fans.setLocation(locnode.getTextContent().trim());
				}
				
				Node descnode = XPathAPI.selectSingleNode(node, xpath_description);
				if(descnode != null) {
					fans.setDescription(descnode.getTextContent().trim());
				}
				
				Node urlnode = XPathAPI.selectSingleNode(node, xpath_url);
				if(urlnode != null) {
					fans.setUrl(urlnode.getTextContent().trim());
					if(fans.getUrl().charAt(0) == '/') {
						fans.setUrl(siteprefix+fans.getUrl());
					}
				}
				
				Node pimgurlnode = XPathAPI.selectSingleNode(node, xpath_pimgurl);
				if(pimgurlnode != null) {
					fans.setProfileImageUrl(pimgurlnode.getTextContent().trim());
				}
				
				Node gendernode = XPathAPI.selectSingleNode(node, xpath_gender);
				if(gendernode != null) {
					String gender = gendernode.getTextContent().trim();
					if(malestr.equals(gender)) {
						fans.setGender("m");
					} else if(femalestr.equals(gender)) {
						fans.setGender("f");
					} else {
						fans.setGender("n");
					}
				}
				
				Node follownode = XPathAPI.selectSingleNode(node, xpath_followcount);
				if(follownode != null) {
					fans.setFriendsCount(Integer.parseInt(follownode.getTextContent().trim()));
				}
				
				Node fansnode = XPathAPI.selectSingleNode(node, xpath_fanscount);
				if(fansnode != null) {
					fans.setFollowersCount(Integer.parseInt(fansnode.getTextContent().trim()));
				}
				
				Node statusnode = XPathAPI.selectSingleNode(node, xpath_statuscount);
				if(statusnode!=null) {
					fans.setStatusesCount(Integer.parseInt(statusnode.getTextContent().trim()));
				}
				
				Node verifynode = XPathAPI.selectSingleNode(node, xpath_verified);
				if(verifynode != null) {
					fans.setVerifed(true);
				} else {
					fans.setVerifed(false);
				}
			
				fanslist.add(fans);
			}
			
			// 下一页
			if(sum_pages_fans == -1) {
				Node pagelimitnode = XPathAPI.selectSingleNode(domtree, xpath_pagelimit);
				if(pagelimitnode!=null) {
					sum_pages_fans = Integer.parseInt(pagelimitnode.getTextContent().trim());
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fanslist;
	}
	
	public void output(String message) {
		System.out.println(message);
	}
	
	public static void main(String[] args) {
		ArrayList<User> fans = new FansPageHandle().handleFansPage(SinaLogin.getDefaultHttpClient()
				,"1642635773");
		System.out.println("Total: "+fans.size());
		for(User user : fans) {
			System.out.println(user);
		}
		
//		try {
//			String wstr = "";
//			for(User user : fans) {
//				wstr += (user.toString()+"\r\n------------------------------------------\r\n");
//			}
//			File file = new File("tfile/fansall.txt");
//			OutputStreamWriter output = new OutputStreamWriter(
//					new FileOutputStream(file), "UTF-8");
//			output.write(wstr);
//			output.close();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
