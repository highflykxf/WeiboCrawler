package casia.weibo.crawler.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.log4j.Logger;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.InputSource;

/**
 * dom初始化类
 * 
 * @author Administrator
 * @since 2011�?7�?
 */
public class DOMUtil {
	private static Logger LOGGER = Logger.getLogger(DOMUtil.class);
	private static final String UTF8 = "utf-8";
	private byte[] byt = null;
	private InputSource source = null;
	private DOMFragmentParser parser = null;
	private DocumentFragment domtree = null;
	

	/**
	 * 对页面的结构化内容的dom初始�?
	 * 
	 * @param content
	 * @param charset
	 * @return
	 */
	public DocumentFragment init(String content, String charset) {
		if (content == null)
			return null;
		if (charset == null)
			charset = UTF8;
		// replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "").
		InputStreamReader isr = null;
		try {
			byt = content.getBytes();
			isr = new InputStreamReader(new ByteArrayInputStream(byt), charset);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("获得字节失败，检查编码是否存�?", e);
		}
		source = new InputSource();
		source.setEncoding(charset);
		source.setCharacterStream(isr);
		parser = new DOMFragmentParser();
		domtree = new HTMLDocumentImpl().createDocumentFragment();
		byt = null;
		try {
			// //是否允许增补缺失的标签�?�如果要以XML方式操作HTML文件，此值必须为�?
			// parser.setFeature("http://cyberneko.org/html/features/balance-tags",
			// true);
			// //是否剥掉<script>元素中的<!-- -->等注释符
			// parser.setFeature("http://cyberneko.org/html/features/scanner/script/strip-comment-delims",
			// true);
			parser.parse(source, domtree);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("字符编码" + charset + "，url"
					+ StringUtil.extrator(content, "^http://.+")
					+ "的Dom解析失败！非法字�?");
		}
		return domtree;
	}

	/**
	 * 为进行Xpath解析初始化数�?
	 * 
	 * @param content
	 * @param url
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public DocumentFragment ini(String content, String charset) {
		if (content == null)
			return null;
		if (charset == null)
			charset = UTF8;
		try {
			byt = content.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "")
					.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("获得字节失败，检查编码是否存�?", e);
			return null;
		}
		source = new InputSource(new ByteArrayInputStream(byt));
		source.setEncoding(charset);
		parser = new DOMFragmentParser();
		domtree = new HTMLDocumentImpl().createDocumentFragment();
		byt = null;
		try {
			// 是否允许增补缺失的标签�?�如果要以XML方式操作HTML文件，此值必须为�?
			parser.setFeature(
					"http://cyberneko.org/html/features/balance-tags", true);
			// 是否剥掉<script>元素中的<!-- -->等注释符
			parser.setFeature(
					"http://cyberneko.org/html/features/scanner/script/strip-comment-delims",
					false);
			parser.parse(source, domtree);
			return domtree;
		} catch (Exception e) {
			LOGGER.error(
					"字符编码" + charset + "内容" + content + "的Dom解析失败，网页数据有误！", e);
			return domtree;
		}
	}

}