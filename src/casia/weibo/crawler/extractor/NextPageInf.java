package casia.weibo.crawler.extractor;

public class NextPageInf {

	private int current_page = 0;
	private String last_since_id = null;
	private String res_type = null;
	private String next_since_id = null;

	public int getCurrent_page() {
		return current_page;
	}

	public void setCurrent_page(int current_page) {
		this.current_page = current_page;
	}

	public String getLast_since_id() {
		return last_since_id;
	}

	public void setLast_since_id(String last_since_id) {
		this.last_since_id = last_since_id;
	}

	public String getRes_type() {
		return res_type;
	}

	public void setRes_type(String res_type) {
		this.res_type = res_type;
	}

	public String getNext_since_id() {
		return next_since_id;
	}

	public void setNext_since_id(String next_since_id) {
		this.next_since_id = next_since_id;
	}

	public NextPageInf() {
	}

	public NextPageInf(String rawstr) {
		try {
			current_page = Integer.parseInt(rawstr.substring(
					rawstr.indexOf("current_page=") + "current_page=".length(),
					rawstr.indexOf("&since_id")));
			last_since_id = rawstr.substring(
					rawstr.indexOf("last_since_id%22%3A")
							+ "last_since_id%22%3A".length(),
					rawstr.indexOf("%2C%22res_type"));
			next_since_id = rawstr.substring(
					rawstr.indexOf("next_since_id%22%3A")
							+ "next_since_id%22%3A".length(),
					rawstr.indexOf("%7D"));
			res_type = rawstr.substring(rawstr.indexOf("res_type%22%3A")
					+ "res_type%22%3A".length(),
					rawstr.indexOf("%2C%22next_since_id"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
