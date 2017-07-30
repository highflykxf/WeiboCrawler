package casia.weibo.crawler.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import casia.weibo.entity.Status;

public class WeiboPreprocesser {
	
	private static Set<String> replaceSet;
	private static Set<String> uselessSet;
	
	static {
		replaceSet = new HashSet<String>();
		replaceSet.add("分享图片");
		replaceSet.add("转发微博");
		replaceSet.add("#(.*)#");
		replaceSet.add("<(.*)>");
		replaceSet.add("《(.*)》");
		replaceSet.add("【(.*)】");
		replaceSet.add("「(.*)」");
		replaceSet.add("（分享自(.*)）");
		replaceSet.add("\\(分享自(.*)\\)");
		
		uselessSet = new HashSet<String>();
		uselessSet.add("我参加了");
		uselessSet.add("发表了一篇");
	}
	
	public static String filterContent(String content) {
		if(content == null)
			return "";
		if(content.trim().length() == 0)
			return "";
		
		content = content.trim();
		String[] tags = content.split("//");
		if(tags == null || tags.length == 0)
			return "";
		
		content = tags[0].trim();
		if(content.length() == 0)
			return "";
		
		for(String term : uselessSet) {
			if(content.contains(term))
				return "";
		}
		
		for(String term : replaceSet) {
			content = content.replaceAll(term, "");
		}
		
		return content.trim();
	}
	
	public static void filterWeiboList(List<Status> statuslist) {
		if(statuslist == null || statuslist.size() == 0)
			return;
		
		List<Status> toDelete = new ArrayList<Status>();
		for(Status status : statuslist) {
			String content = filterContent(status.getText());
			if(content.length() == 0) {
				toDelete.add(status);
			}
		}
		
		for(Status status : toDelete) {
			statuslist.remove(status);
		}
		
		toDelete.clear();
		toDelete = null;
	}

}
