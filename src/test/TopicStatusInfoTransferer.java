package test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import casia.weibo.crawler.dao.TopicCommentDao;
import casia.weibo.crawler.dao.TopicStatusDao;
import casia.weibo.entity.Comment;
import casia.weibo.entity.Status;

public class TopicStatusInfoTransferer {
	
	public static void copyData2Analysis() {
		TopicStatusDao tsdao = new TopicStatusDao();
		TopicCommentDao tcdao = new TopicCommentDao();
		Map<Long, Integer> status_topic_map = new HashMap<Long, Integer>();
		Map<Long, Integer> comment_topic_map = new HashMap<Long, Integer>();
		List<Status> statuslist = tsdao.getAnalysisStatus(status_topic_map);
		List<Comment> commentlist = tcdao.getAnalysisComments(comment_topic_map);
		tsdao.insertAnalysisStatus(statuslist, status_topic_map);
		tcdao.insertAnalysisComments(commentlist, comment_topic_map);
	}
	
	public static void main(String[] args) {
		copyData2Analysis();
	}

}
