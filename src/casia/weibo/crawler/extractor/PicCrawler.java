package casia.weibo.crawler.extractor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import casia.weibo.crawler.dao.UserAnalysisDao;
import casia.weibo.crawler.utils.CommonUtils;

public class PicCrawler {
	
	public void downloadExtraPictures(String folderpath) {
		if(folderpath == null)
			return;
		
		File folder = new File(folderpath);
		if(!folder.exists() || !folder.isDirectory())
			return;
		
		UserAnalysisDao uadao = new UserAnalysisDao();
		Map<String,String> urlmap = new HashMap<String,String>();
		uadao.getExtraUserStatus(urlmap);
		
		if(folderpath.charAt(folderpath.length()-1)!='\\'
				&&folderpath.charAt(folderpath.length()-1)!='/') {
			folderpath += "\\";
		}
		for(Entry<String,String> entry : urlmap.entrySet()) {
			String key = entry.getKey();
			File file = new File(folderpath+key);
			if(file.exists() && file.isFile())
				continue;
			String url = entry.getValue();
			CommonUtils.savePictureFromUrl(url, folderpath, key);
		}
	}

}
