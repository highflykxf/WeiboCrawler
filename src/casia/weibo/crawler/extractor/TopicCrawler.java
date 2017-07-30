package casia.weibo.crawler.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.impl.client.DefaultHttpClient;

import casia.weibo.crawler.dao.TopicCommentDao;
import casia.weibo.crawler.dao.TopicDao;
import casia.weibo.crawler.dao.TopicStatusDao;
import casia.weibo.crawler.dao.TopicUserDao;
import casia.weibo.crawler.login.SinaLogin;
import casia.weibo.crawler.utils.TimeUtil;
import casia.weibo.entity.Comment;
import casia.weibo.entity.Status;
import casia.weibo.entity.User;

public class TopicCrawler {

	public void generateTopicData(String topic_name, String topic_mid) {
		TopicDao tpdao = new TopicDao();
		tpdao.insertTopic(topic_name, topic_mid);
	}

	public void crawlTopic(int topic_id) {
		TopicStatusDao tsdao = new TopicStatusDao();
		Set<Long> idset = tsdao.getTopicStatusIdSet(topic_id);
		TopicUserDao tudao = new TopicUserDao();
		Set<Long> useridset = tudao.getAllUsersIdSet();
		TopicDao tpdao = new TopicDao();
		String topic_tag = tpdao.getTopicTagById(topic_id);
		String url = "http://weibo.com/p/" + topic_tag
				+ "?feed_sort=timeline&feed_filter=timeline#Pl_Third_App__11";
		TopicPageHandle handle = new TopicPageHandle();
		DefaultHttpClient client = SinaLogin
				.getHttpClientWithCookies("D:/sina_cookies");
		List<Status> statuslist = handle.handleTopicPage(client, url);
		List<Status> statusToAdd = new ArrayList<Status>();
		List<User> usersToAdd = new ArrayList<User>();
		for (Status status : statuslist) {
			if (!idset.contains(status.getId())) {
				statusToAdd.add(status);
			}
			idset.add(status.getId());
			if (status.getUser() != null) {
				if (!useridset.contains(status.getUser().getId())) {
					usersToAdd.add(status.getUser());
				}
				useridset.add(status.getUser().getId());
			}
		}
		tsdao.insertStatusBatch(statusToAdd, topic_id);
		System.out.println("Status Added:\t" + statusToAdd.size());
		tudao.insertUsersBatch(usersToAdd);
		System.out.println("Users Added:\t" + usersToAdd.size());
	}

	public void crawlComments(int topic_id, int min_cmt_count,
			boolean force_crawl) {
		int interval_time = 10;
		TopicCommentDao tcdao = new TopicCommentDao();
		// get all the existed users
		TopicUserDao tudao = new TopicUserDao();
		Set<Long> useridset = tudao.getAllUsersIdSet();

		// get all the topic related comments
		Set<Long> cmtidset = tcdao.getTopicCommentIdSet(topic_id);

		// get statuses to crawl comments
		TopicStatusDao tsdao = new TopicStatusDao();
		List<Long> status_idset = tsdao.getTopicStatusIdSet(topic_id,
				min_cmt_count);
		List<Long> status2crawled = new ArrayList<Long>();
		for (long sid : status_idset) {
			if (!force_crawl && tcdao.existsComment(sid))
				continue;
			status2crawled.add(sid);
		}

		// crawl comments
		List<Comment> comment2Add = new ArrayList<Comment>();
		List<User> users2Add = new ArrayList<User>();
		CommentPageHandle cmt_handle = new CommentPageHandle();
		DefaultHttpClient client = SinaLogin
				.getHttpClientWithCookies("D:/sina_cookies");
		for (long sid : status2crawled) {
			Status status = new Status();
			status.setId(sid);
			List<Comment> cmtlist = cmt_handle
					.handleCommentPage(client, status);
			for (Comment comment : cmtlist) {
				if (!cmtidset.contains(comment.getId())) {
					cmtidset.add(comment.getId());
					comment2Add.add(comment);
				}
			}
			System.out.println("Status " + sid + " Finish Comments Crawling!\t"
					+ cmtlist.size());

			tcdao.insertCommentBatch(comment2Add, topic_id);
			for (Comment comment : comment2Add) {
				if (comment.getUser() == null)
					continue;
				if (!useridset.contains(comment.getUser().getId())) {
					useridset.add(comment.getUser().getId());
					users2Add.add(comment.getUser());
				}
			}
			tudao.insertUsersBatch(users2Add);
			comment2Add.clear();
			users2Add.clear();

			cmtlist.clear();
			cmtlist = null;
			TimeUtil.sleep(interval_time);
		}
	}

	public static void main(String[] args) {
		TopicCrawler crawler = new TopicCrawler();
		// crawler.generateTopicData("云南导游辱骂游客",
		// "100808befb0ddb56c7e54cb3882b3af8e1a7b3");
		// crawler.crawlTopic(30);
		crawler.crawlComments(30, 1, true);
	}

}
