package casia.weibo.crawler.extractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;

import casia.weibo.crawler.dao.StatusDao;
import casia.weibo.crawler.dao.UserDao;
import casia.weibo.crawler.login.SinaLogin;
import casia.weibo.crawler.utils.TimeUtil;
import casia.weibo.entity.Status;
import casia.weibo.entity.User;

public class MainCrawler {
	
	public static final int MAXDEPTH = 3;
	public static final int MINSTATUS = 100;
	
	public void process(long userid) {
		FansPageHandle fanshandle = new FansPageHandle();
		HomepageHandle statushandle = new HomepageHandle();
		StatusDao statusdao = new StatusDao();
		UserDao userdao = new UserDao();
		HttpClient client = SinaLogin.getDefaultHttpClient();
		
		User seeduser = new User();
		seeduser.setId(userid);
		
		List<User> crawlerlist = new ArrayList<User>();
		List<User> newlist = new ArrayList<User>();
		crawlerlist.add(seeduser);
		
		List<Status> statuslist = new ArrayList<Status>();
		List<Status> tmplist = null;
		
		int depth = 0;
		while(depth<MAXDEPTH) {
			for(User u : crawlerlist) {
				if(u.getId() == 0)
					continue;
				newlist.addAll(fanshandle.handleFansPage(client, ""+u.getId()));
				TimeUtil.sleep(60);
			}
			for(User u : newlist) {
				if(u.getId() == 0)
					continue;
				tmplist = statushandle.handleStatusPages(client, u, null);
				if(tmplist == null || tmplist.size()<MINSTATUS) {
					tmplist.clear();
					continue;
				}
				if(!userdao.existUser(u)) {
					userdao.insertUser(u);
				}
				for(Status status : tmplist) {
					if(!statusdao.existStatus(status) && status.getText()!=null 
							&& !statuslist.contains(status)) {
						statuslist.add(status);
					}
					
					if(status.getRetweeted_status()!=null && !statusdao.existStatus(status.getRetweeted_status())
							 && status.getRetweeted_status().getText()!=null
							 && !statuslist.contains(status.getRetweeted_status())) {
						if(!userdao.existUser(status.getRetweeted_status().getUser()) 
								&& status.getRetweeted_status().getUser().getId()!=0) {
							userdao.insertUser(status.getRetweeted_status().getUser());
						}
						statuslist.add(status.getRetweeted_status());
					}
				}
				tmplist.clear();
				statusdao.insertStatusBatch(statuslist);
				statuslist.clear();
				TimeUtil.sleep(60);
			}
			crawlerlist.clear();
			crawlerlist.addAll(newlist);
			newlist.clear();
			depth++;
		}
		
	}

}
