package casia.weibo.crawler.login;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

@SuppressWarnings("restriction")
public class LoginUtils {
	
	public static HttpGet getMethod;
	
	/**
	 * ����ͼƬ
	 * @param client
	 * @param url
	 * @param charset
	 * @param picDir  void
	 */
	public static synchronized boolean downloadVPic(HttpClient client,
			String url, String charset, String picDir,String fileName) {
		try {
			url = encodeURL(url, charset);
			getMethod = new HttpGet(url);
			getMethod.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
			fileName = picDir + File.separator + fileName + ".jpg";
			File picFile = new File(fileName);
			HttpResponse response = client.execute(getMethod);
			HttpEntity entity = response.getEntity();
			if(entity != null){
				// �õ�������Դ���ֽ�����,��д���ļ�
				InputStream in = entity.getContent();
				BufferedImage img = ImageIO.read(in);
				in.close();

				if (img == null ) {
					return false;
				}

				if(!picFile.getParentFile().exists()){
					picFile.getParentFile().mkdirs();
				}

				AffineTransformOp op = new AffineTransformOp(AffineTransform
						.getScaleInstance(1, 1), null);

				BufferedImage new_img = op.filter(img, null);
				FileOutputStream out = new FileOutputStream(picFile);
				JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
				encoder.encode(new_img);
				out.close();
				
				return true;
				
			}
		} catch (Exception e) {
			
		} catch(OutOfMemoryError error){
			
		} finally{
			try{
				getMethod.abort();
			}catch(Exception e){
				
			}		
		}
		
		return false;
	}
	
	/**
	 * ��url�е����Ľ��б���
	 * @param url
	 * @param charset
	 * @return
	 */
	private static String encodeURL(String url, String charset){
		StringBuffer encodeURL = new StringBuffer();
		
		try {
			Pattern pattern = Pattern.compile("[��\\[\\]{}\\s\u4e00-\u9fa5]");
			Matcher matcher = pattern.matcher(url);
			while(matcher.find()){
				matcher.appendReplacement(encodeURL, URLEncoder.encode(matcher.group(), charset));
			}
			matcher.appendTail(encodeURL);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return encodeURL.toString();
	}

}
