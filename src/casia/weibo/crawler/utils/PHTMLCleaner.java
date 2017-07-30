package casia.weibo.crawler.utils;

import java.io.IOException;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;

public class PHTMLCleaner {
	public static String cleanHtml(String htmltext) {
		HtmlCleaner cleaner = new HtmlCleaner();
		try {
			CleanerProperties props = cleaner.getProperties();    
            props.setUseCdataForScriptAndStyle(false);    
            props.setRecognizeUnicodeChars(true);    
            props.setUseEmptyElementTags(true);    
            props.setAdvancedXmlEscape(true);    
            props.setTranslateSpecialEntities(true);    
            props.setBooleanAttributeValues("empty");
            
            
			TagNode node = cleaner.clean(htmltext);
			htmltext = new PrettyXmlSerializer(props).getAsString(node);
			
//			htmltext = htmltext.replace("node-type", "nodetype");
//			htmltext = htmltext.replace("action-type", "actiontype");
			
			return htmltext;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
