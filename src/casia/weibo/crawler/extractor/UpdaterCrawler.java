package casia.weibo.crawler.extractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;

import casia.weibo.crawler.dao.StatusDao;
import casia.weibo.crawler.dao.UserAnalysisDao;
import casia.weibo.crawler.login.SinaLogin;
import casia.weibo.crawler.utils.TimeUtil;
import casia.weibo.entity.Status;
import casia.weibo.entity.User;

public class UpdaterCrawler {
	
	public void update(long startid, long endid) {
		HomepageHandle statushandle = new HomepageHandle();
		StatusDao statusdao = new StatusDao();
		UserAnalysisDao uadao = new UserAnalysisDao();
		HttpClient client = SinaLogin.getDefaultHttpClient();
		
		List<Long> updatelist = new ArrayList<Long>();
		uadao.getAnalysisUserIds(updatelist, startid, endid);
		
		List<Status> statuslist = new ArrayList<Status>();
		List<Status> toInsert = new ArrayList<Status>();
		List<Status> toUpdate = new ArrayList<Status>();
		for(long userid : updatelist) {
			User u = new User();
			u.setId(userid);
			statuslist = statushandle.handleStatusPages(client, u, null);
			for(Status status : statuslist) {
				if(toUpdate.contains(status) || toInsert.contains(status))
					continue;
				if(statusdao.existStatus(status)) {
					toUpdate.add(status);
				} else {
					toInsert.add(status);
				}
			}
			statusdao.updateStatusBatch(toUpdate);
			statusdao.insertStatusBatch(toInsert);
			statuslist.clear();
			toUpdate.clear();
			toInsert.clear();
			System.out.println("User:"+userid+" Updated!!!");
			TimeUtil.sleep(60);
		}
	
	}

}
