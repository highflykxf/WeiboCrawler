package casia.weibo.crawler.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 格式化的策略,先找出满足格式的，如果没有满足的格式的，先找出时间，然后再日期，最后拼接
 *
 * @author xiaoming
 * @version 1.0 2010-09-12
 * @since jdk1.6
 * 
 */

public class TimeDateUtil {
	
	//标准时间格式
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	

	
	//时间正则
	private static final String timeRE = "((?:[01]?[0-9]|2[0-4])[\\:时](?:[0-5]?[0-9])(?:[\\:分][0-5]?[0-9]秒?)?(\\s?[aApP][mM])?)";
	private static Pattern timePatternALL = Pattern.compile(timeRE);
	
	//日期规则
	private static final String dateRE1 = "([0-9]{4})[-|年](0[1-9]|[1-9]|1[0-2])[-|月](0[1-9]|[1-2][0-9]|3[0-1]|[1-9])";//策略一，获取2010-1-1形式
	private static final String dateRE2 = "(0[1-9]|[1-2][0-9]|3[0-1]|[1-9])(\\s?)[a-zA-Z]{3}(\\s?)[0-9]{4}";//日期规则2，“23 Dec 2009”
	private static final String dateRE3 = "(.)*(\\s?)(0[1-9]|[1-2][0-9]|3[0-1]|[1-9])(,?)(\\s?)[0-9]{4}";//Dec 17, 2009
	private static final String dateRE4 = "(0[1-9]|[1-9]|1[0-2])[-|月](0[1-9]|[1-2][0-9]|3[0-1]|[1-9])[-|日][0-9]{4}";//策略一，获取02-08-2007形式
	private static final String dateRE5 = "([0-9]{1,2})[-|月]([0-9]{1,2})(日?)";//9-1,9月1日
	private static final String dateRE6 = "[a-zA-Z]{3,4}\\s[0-9]{1,2}[a-zA-Z]{2},?\\s?[0-9]{4}";//10:58 AM Oct 25th, 2009
	private static final String dateRE7 = "[a-zA-Z]{3,4}\\s[0-9]{1,2}(.)*\\s[0-9]{4}";//{time:'Wed Jan 20 08:59:20 +0000 2010'}
	
	
	
	
	
	/**
	 *  获得时间
	 * @param input 含有时间的字符串
	 * @return 返回时间字符串
	 */
	public static String getTime(String input){
		String time = null;
		if(input == null || "".equals(input.replaceAll(" ", "").trim())){//不符合的不处理
			return null;
		}
		Matcher matcher = timePatternALL.matcher(input);
		if (matcher.find()) {
			time = matcher.group(1);//获得时间字符窜
		}
		if (time != null) { //找到后对pm，am进行处理
			Matcher pm = Pattern.compile("[pP][mM]$").matcher(time);//如果是下午，进行处理
			if(pm.find()){
				String[] tiemTemp = time.split("\\:");
				int hourTemp = Integer.parseInt(tiemTemp[0]);
				if(hourTemp < 12){
					time = (Integer.parseInt(tiemTemp[0])+ 12) + "";
				}else{
					time = tiemTemp[0];
				}
				for(int i = 0; i < tiemTemp.length-1; i++){
					time += ":"+tiemTemp[i+1];
				}
			}
			time = time.replaceAll("[aApP][mM]$", "");//去掉后面的字母
			time = time.replaceAll(" ", "");
		}
		return time;
	}
	
	/**
	 *  获得日期
	 *  
	 * @param input 
	 * 				含有时间的字符串
	 * @return 
	 * 				返回日期字符串
	 */
	public static String getDate(String input){
		String date = null;
		if(input == null || "".equals(input.replaceAll(" ", "").trim())){//不符合的不处理
			return null;
		}
		Matcher matcher = Pattern.compile(dateRE1).matcher(input);//第一中策略2009-1-1或2009年1月1日
		if(matcher.find()){ //如果存在就进行转换，日期规则1，2009-1-1
			date = matcher.group();
			if(date != null){
				date = date.replaceAll("[\u4e00-\u9fa5]", "-");
			}
			if(date != null){
				return date;
			}
		}
		
		matcher = Pattern.compile("[0-1][0-9]-(0[1-9]|1[0-2])-([0-2][1-9]|[1-9]|3[0-1])").matcher(input);
		if(matcher.find()){ //如果存在就进行转换,日期规则2，“09-10-10”
			date = matcher.group();		
			if(date != null){ //对日期进行转换拼接	
				//TODO 
				return  "20" + date;
			}
		}
		
		matcher = Pattern.compile(dateRE2).matcher(input);
		if(matcher.find()){ //如果存在就进行转换,日期规则2，“23 Dec 2009”
			date = matcher.group();
			if(date != null){ //对日期进行转换拼接
				matcher = Pattern.compile("[a-zA-Z]{3}").matcher(date);
				String month = "";
				if(matcher.find()){
					month = matcher.group();
					date = date.split(month)[1] + "-" + getMonth(month)+ "-" + date.split(month)[0];
					return date;
				}
			}
		}
		matcher = Pattern.compile(dateRE3).matcher(input);
		if(matcher.find()){ //如果存在就进行转换,日期规则3，“Dec 23, 2009”
			date = matcher.group();
			date = date.replaceAll(",", "");
			if(date != null){ //对日期进行转换拼接
				String[] dateSplit = date.split(" ");
				date = date.substring(date.length()-4, date.length()) + "-" + getMonth(date) +"-" + dateSplit[dateSplit.length-2];
				return date;
			}
		}
		matcher = Pattern.compile(dateRE4).matcher(input);
		if(matcher.find()){ //如果存在就进行转换,日期规则4，“02-28-2009”
			date = matcher.group();
			date = date.replaceAll(",", "");
			if(date != null){ //对日期进行转换拼接
				date = date.substring(date.length()-4, date.length()) + "-" + date.split("-")[0] +"-" + date.split("-")[1];
				return date;
			}
		}
		matcher = Pattern.compile(dateRE5).matcher(input);
		if(matcher.find()){ //如果存在就进行转换,日期规则5，“02-28”
			date = matcher.group();
			date = date.replaceAll(",", "");
			date = date.replace("日", "");
			date = date.replace("月", "-");
			if(date != null){ //对日期进行转换拼接
				date = Calendar.getInstance().get(Calendar.YEAR)+"-"+date;
				return date;
			}
		}
		matcher = Pattern.compile(dateRE6).matcher(input);
		if(matcher.find()){ //如果存在就进行转换,日期规则6，“02-28-2009”
			date = matcher.group();
			date = date.replaceAll(",", "");
			if(date != null){ //对日期进行转换拼接
				String[] dateSplit = date.split(" "); 
				date = dateSplit[2]+ "-" + getMonth(dateSplit[0])+ "-" + getNumber(dateSplit[1]);
				return date;
			}
		}
		matcher = Pattern.compile(dateRE7).matcher(input);
		if(matcher.find()){ //如果存在就进行转换,日期规则6，“02-28-2009”
			date = matcher.group();
			if(date != null){ //对日期进行转换拼接
				String[] dateSplit = date.split(" "); 
				date = dateSplit[4]+ "-" + getMonth(dateSplit[0])+ "-" + dateSplit[1];
				return date;
			}
		}
		return date;
	}
	/**
	 *  英文月份转换为数字月份
	 * 
	 * @param month 英文月份
	 * @return 数字月份 0-11
	 */
	public static int getMonth(String month){
		String[] mon = new String[12];
		mon[0] = "Jan";
		mon[1] = "Feb";
		mon[2] = "Mar";
		mon[3] = "Apr";
		mon[4] = "May";
		mon[5] = "Jun";
		mon[6] = "Jul";
		mon[7] = "Aug";
		mon[8] = "Sep";
		mon[9] = "Oct";
		mon[10] = "Nov";
		mon[11] = "Dec";
		
		for(int i = 0; i < mon.length; i++){
			if(month.contains(mon[i])){
				return i+1;
			}
		}
		return -1;
	}
	
	/**
	 *  得到字符串中的数字
	 *  
	 * @param input 含有字符串的数字
	 * @return 返回字符串里的数字
	 */
	private static String getNumber(String input){
		String result = null;
		if(input == null){
			return result;
		}
		StringBuffer sb = new StringBuffer();
		char[] c = input.toCharArray();
		boolean start = false;
		for (int i = 0; i < c.length; i++) {
			if (c[i] >= '0' && c[i] <= '9') {
				start = true;
				sb.append(c[i]);
			} else {
				if(start){
					result = sb.toString();
					break;
				}
			}
		}
		if(result == null || result.equals("")){
			String[] number = new String[10];// {"一","二","三","四","五","六","七","八","九","两"};
			number[0] = "一";
			number[1] = "二";
			number[2] = "三";
			number[3] = "四";
			number[4] = "五";
			number[5] = "六";
			number[6] = "七";
			number[7] = "八";
			number[8] = "九";
			number[9] = "两";
			for(int i = 0; i < number.length; i ++){
				if(input.contains(number[i])){
					if(i == 9){
						result = 2 +"";
					}else{
						result = "" + (i+1);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 *  得到数字与单位的键值对
	 * 
	 * @param input 含有数字和单位的字符串，如1天前等
	 * @param map 用来存储时间单位和数字的键值对
	 * @return 返回包含解析后的时间单位和数字的键值对
	 */
	private static Map<String,Integer> getNumber(String input, Map<String,Integer> map){
		Set<String> key = map.keySet(); 
		char[] c = input.toCharArray();
		StringBuffer groupStr = new StringBuffer();//存单位
		StringBuffer groupNum = new StringBuffer();//存数字
		//如果到下一个数字则从新开始
		boolean number = false;
		boolean letter = false;
		for (int i = 0; i < c.length; i++) {
			if (c[i] >= '0' && c[i] <= '9') {
				//数字开始，字母结束
				if(letter){ //有单位
					groupNum.delete(0, groupNum.length());
				}
				groupNum.append(c[i]);
				letter = false;
				number = true;
			}else{
				//字母开始，数字结束
				if(number){//有数字后再开始存单位
					groupStr.delete(0, groupStr.length());
				}
				groupStr.append(c[i]);
				letter = true;
				number = false;	
			}
			for(String unit : key){
				if(groupStr.toString().contains(unit)){
					String num = groupNum.toString();//得到数字
//					if(num != null && !"".equals(num.trim().replaceAll(" ", ""))){
//						num = getNumber(input);
//					}
					if(num != null){
						map.put(unit, Integer.parseInt(num));
					}
					groupNum.delete(0, groupNum.length());
					groupStr.delete(0, groupStr.length());
					break;
				}
			}
		}
		return map;
	}
	
	/**
	 *  对“1天前”这样的时间形式进行转换,只支持中文
	 *  
	 * @param input 还有”1天前“ 这样的时间形式的中文字符串
	 * @return 返回日期格式的字符串
	 */
	public static   String  getDateForCN(String input){
		String result = null;
		Map<String,Integer> times = new HashMap<String,Integer>();
		times.put("年", 0);
		times.put("月", 0);
		times.put("星期", 0);
		times.put("天", 0);
		times.put("时", 0);
		times.put("分", 0);
		times.put("秒", 0);
		if(getNumber(input) != null && input.contains("半")){
			Calendar calendar = Calendar.getInstance();
			if(input.contains("年")){
				calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-6);
			}
			if(input.contains("月")){
				calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR)-15);
			}
			if(input.contains("天")){
				calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-12);
			}
			if(input.contains("时")){
				calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE)-30);
			}
			if(input.contains("分")){
				calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND)-30);
			}
			times = getNumber(input,times);
			if(times.get("年") > 0){
				calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)-times.get("年"));
			}
			if(times.get("月") > 0){
				calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)-times.get("月"));
			}
			if(times.get("星期") > 0){
				calendar.set(Calendar.DAY_OF_YEAR,calendar.get(Calendar.DAY_OF_YEAR)-times.get("星期")*7);
			}
			if(times.get("天") > 0){
				calendar.set(Calendar.DAY_OF_YEAR,calendar.get(Calendar.DAY_OF_YEAR)-times.get("天"));
			}
			if(times.get("时") > 0){
				calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY)-times.get("时"));
			}
			if(times.get("分") > 0){
				calendar.set(Calendar.MINUTE,calendar.get(Calendar.MINUTE)-times.get("分"));
			}
			return simpleDateFormat.format(calendar.getTime());
		}else if(getNumber(input) != null && containsCNUnit(input,times)){ //有数字,并且有中文日期单位
			times = getNumber(input,times);
			Calendar calendar = Calendar.getInstance();
			if(times.get("年") > 0){
				calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)-times.get("年"));
			}
			if(times.get("月") > 0){
				calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)-times.get("月"));
			}
			if(times.get("星期") > 0){
				calendar.set(Calendar.DAY_OF_YEAR,calendar.get(Calendar.DAY_OF_YEAR)-times.get("星期")*7);
			}
			if(times.get("天") > 0){
				calendar.set(Calendar.DAY_OF_YEAR,calendar.get(Calendar.DAY_OF_YEAR)-times.get("天"));
			}
			if(times.get("时") > 0){
				calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY)-times.get("时"));
			}
			if(times.get("分") > 0){
				calendar.set(Calendar.MINUTE,calendar.get(Calendar.MINUTE)-times.get("分"));
			}
			return simpleDateFormat.format(calendar.getTime());
			
		}else{
			if(input.contains("昨天")){
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_YEAR,calendar.get(Calendar.DAY_OF_YEAR)-1);
				result = simpleDateFormat.format(calendar.getTime());
				return result;
			}else if(input.contains("前天")){
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.DAY_OF_YEAR,calendar.get(Calendar.DAY_OF_YEAR)-2);
				result = simpleDateFormat.format(calendar.getTime());
				return result;
			}else if(input.contains("今天")){
				Calendar calendar = Calendar.getInstance();
				result = simpleDateFormat.format(calendar.getTime());
				return result;
			}else if(input.contains("刚刚")){
				result = simpleDateFormat.format(new Date());
				return result;
			}else if(input.contains("秒")){
				Calendar calendar = Calendar.getInstance();
				result = simpleDateFormat.format(calendar.getTime());
				return result;
			}
			
		}
		return result;
	}
	
	/**
	 * 查看是否有
	 * @param input 含有时间的字符串
	 * @param times 包含时间单位和数量的键值对
	 * @return 如果包含返回ture，否则返回false
	 */
	private static boolean containsCNUnit(String input, Map<String,Integer> times){
		boolean result = false;
		Set<String> keys = times.keySet();
		for(String key : keys){
			if(input.contains(key)){
				result = true;
				break;
			}
		}
		return result;
	}
	
	
	/**
	 *  对“1 day ago”这样的时间形式进行转换\
	 * 
	 * @param input 包含‘1 day ago’的日期字符串
	 * @return 经过解析后的标准日期时间的字符串
	 */
	public static String  getDateForEN(String input){
		String result = null;
		if(input.contains("half")){
			Calendar calendar = Calendar.getInstance();
			if(input.contains("year")){
				calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-6);
			}
			if(input.contains("month")){
				calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR)-15);
			}
			if(input.contains("day")){
				calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY)-12);
			}
			if(input.contains("hour")){
				calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE)-30);
			}
			if(input.contains("minute")){
				calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND)-30);
			}
			return simpleDateFormat.format(calendar.getTime());
		}
		String RE = "(a|[1-9])(.)*ago";
		Pattern pattern = Pattern.compile(RE);
		Matcher matcher = pattern.matcher(input);
		while(matcher.find()){
			String dateTimeStr = matcher.group();  //关键字符串
			Map<String,Integer> times = new HashMap<String,Integer>();
			times.put("minute", 0);
			times.put("hour", 0);
			times.put("day", 0);
			times.put("week", 0);
			times.put("month", 0);
			times.put("year", 0);
			times = getNumber(dateTimeStr,times);
			if(dateTimeStr.contains("moment")){
				return simpleDateFormat.format(Calendar.getInstance().getTime());
			}else{
				Calendar calendar = Calendar.getInstance();
				if(times.get("year") > 0){
					calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)-times.get("year"));
				}
				if(times.get("month") > 0){
					calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)-times.get("month"));
				}
				if(times.get("week") > 0){
					calendar.set(Calendar.DAY_OF_YEAR,calendar.get(Calendar.DAY_OF_YEAR)-times.get("week")*7);
				}
				if(times.get("day") > 0){
					calendar.set(Calendar.DAY_OF_YEAR,calendar.get(Calendar.DAY_OF_YEAR)-times.get("day"));
				}
				if(times.get("hour") > 0){
					calendar.set(Calendar.HOUR_OF_DAY,calendar.get(Calendar.HOUR_OF_DAY)-times.get("hour"));
				}
				if(times.get("minute") > 0){
					calendar.set(Calendar.MINUTE,calendar.get(Calendar.MINUTE)-times.get("minute"));
				}
				return simpleDateFormat.format(calendar.getTime());
			}
		}
		return result;
	}
	
	/**
	 * 得到具体时间日期
	 * 
	 * @param input 包含日期时间的字符串
	 * @return 返回标准格式的日期时间的字符串
	 */
	public static String getDateTime(String input){
		String result = null;
		String time = getTime(input);//获取时间
		String date = getDate(input);//获取日期
		if(time != null && date != null && !"".equals(time.trim().replaceAll(" ", "")) && !"".equals(date.trim().replaceAll(" ", ""))){
			 result = date +" " + time; 
			 return toChineseTime(result,input);//返回时间和日期
		}
		if(time != null && date == null){ //如果只有时间，添加当前日期
//			Calendar calendar = Calendar.getInstance();
//			result = calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" " + time;
//			return toChineseTime(result,input);
			result = TimeDateUtil_bak.getDate(input);
			if(result == null){
				if(input.contains("昨天")){
					Calendar calendar = Calendar.getInstance();
					calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) -1);
					result = calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" " + time;
					return toChineseTime(result,input);
				}
			}
			if(result == null){
				Calendar calendar = Calendar.getInstance();
				result = calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+" " + time;
				return toChineseTime(result,input);
			}
			return result;
		}
		if(date != null && time == null){ //如果只有时间，添加当前日期
			return toChineseTime(date + " 00:00",input);
		}
		result =  getDateForCN(input);//返日期，格式“1年前”等
		if(result != null){
			return toChineseTime(result,input);
		}else{
			result =  getDateForEN(input);//返回英文的日期，格式“1 year ago”等
		}
		return toChineseTime(result,input);
	}
	
	
	/**
	 * 得到具体时间日期
	 * 
	 * @param input 包含日期时间的字符串
	 * @return 返回标准格式的日期时间的字符串
	 */
	public static  Date getDateByFomat(String input,String formatString){
		SimpleDateFormat format = new SimpleDateFormat(formatString);
		Date result = null;
		try {
			result = format.parse(getDateTime(input));
		} catch (ParseException e) {
//			e.printStackTrace();
		}
		return result;
	}
	
	
	/**
	 *  把不是中国时区的时间转换成中国时区的时间
	 * @param result 处理过后的标准日期时间格式的字符串，该时间的时区不是中国的
	 * @param input 原来抓取的字符串，从中得到GMT的信息，然后进行转换
	 * @return 返回中国时间的时间
	 */
	public static String toChineseTime(String result,String input){
		try {
			Date date = simpleDateFormat.parse(result);
			if(input.contains("GMT")){
				date.setTime(date.getTime() + 8 * 60 * 60 *1000);
				result = simpleDateFormat.format(date);
			}
			Matcher matcher = Pattern.compile("T(.)*\\+[0-9]{2}:").matcher(input);//2010-03-03T05:07:20+00:00
			if(matcher.find()){
				String str = matcher.group();
				String time = str.substring(str.indexOf("+")+1,str.lastIndexOf(":"));
				date.setTime(date.getTime() + (8 - new Integer(time)) * 60 * 60 *1000);
				result = simpleDateFormat.format(date);
			}
		} catch (ParseException e) {
			//System.out.println(e.getMessage());
		}
		
		return result;
	}
	
	/**
	 *  得到具体时间日期和年数差, 先得到具体日期，再运算
	 * 
	 * @param input 标准的时间格式
	 * @param yearinput 需要在input减去的年数
	 * @return 返回运算过后的日期时间
	 */
	public String getDateTime(String input,int yearinput){
		String  date = getDateTime(input); 
		if(date != null){
			//取出年数运算
			String yearStr = date.split("-")[0];
			if(yearStr != null){			
				yearStr = (Integer.parseInt(yearStr)-yearinput) + "";	
			}
			date = yearStr + "-"+date.split("-")[1]+"-"+ date.split("-")[2];
		}
		return date;
	}
	
	/**
	 *  闰年2月份才有29号 
	 * 
	 * @param dateTime  标准日期时间
 	 * @return 返回处理的日期时间
	 */
	public String validate(String dateTime){
		String year = dateTime.split("-")[0];
		String month = dateTime.split("-")[1];
		String day = dateTime.split("-")[2].split(" ")[0];
		String time = dateTime.split(" ")[1];
		try{
			if(new Integer(year) == Calendar.getInstance().get(Calendar.YEAR)){//与当前年份一样
				//比较月份
				if(new Integer(month) > Calendar.getInstance().get(Calendar.MONTH)+1){//年份一样，月份大于当前的
					year = (new Integer(year) -1) + "";
				}else if(new Integer(month) == Calendar.getInstance().get(Calendar.MONTH)) { //如果年份，月份相等，比较日期
					if(new Integer(day) > Calendar.getInstance().get(Calendar.DAY_OF_MONTH)){
						year = (new Integer(year) -1) + "";
					}
				}
			}
		}catch(Exception e){
			return dateTime;
		}
		return year + "-" + month + "-" + day + " " + time;
	}
	
	
	public static Date getNow() {
		Date date = new Date();
		return date;
	}
	

	
	public static void main(String[] args){
//		TimeDateUtil dateUtil = new TimeDateUtil();
//		String date = " adadfasdfas a moment agoadfasdfasdfasdf";
//		String date1 = " By msoft 1year  10month, 13 day,9 hour, 0 minutes ago";
//		String date2 = "13:20:30";
//		String date3 = "02-08-2007 ";
//		String date4 = "2010-01-14T02:44:21+00:00";
//		String date5= "  几秒前   from web   ";
//		String date6= "3 hours ago / Thu, Jan 14, 2010 - 10:46 AM / 0 comment";
//		String date7="By happy24n7  15 hours, 13 minutes ago in Petaling Jaya Add Comment";
//		String date8 ="10 hours ago / Thu, Jan 14, 2010 - 12:04 PM / 0 comment";
//		String date9 = "January 11, 2010 at 21:35";
//		String date10 = "09-01-23 12:03:03 GMT";
//		String test1= " (11:22)";
//		String test2 = "昨天 23:59";
		System.out.println(TimeDateUtil.getDateTime("	20秒前"));
		//System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateFormat(date)));	
		
//		System.out.println(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)-1);
//		System.out.println(dateUtil.getDateFrom("1月前"));
		//System.out.println(dateUtil.getDateTime(date));
		//System.out.println(dateUtil.getDateForCN(date2));
		
		//System.out.println(dateUtil.getNumber(date));
//		System.out.println();
//		String dateTime = " 8:31:11 PM";
//		String date11 = "Thu, 01 Apr 2010 04:48:00 GMT";
//		String date12= "01-07 07:58   通过客户端  ";
//		String date13="{time:'Wed Feb 20 08:59:20 +0000 2000'}";
//		String date14 = "04-30 22:05   通过MSN  ";
//		String date15 = "{time:'Wed Feb 24 23:10:57 +0000 2010'}";
//		System.out.println(dateUtil.getDateByFomat("今天09:24","yyyy-MM-dd HH:mm").toString());
//		System.out.println(TimeDateUtil.getDateTime("昨天 10:28"));
//		System.out.println(new Date(1324368412000l));
		//System.out.println(dateUtil.getDateTime("2010-01-31T09:13:00+00:00"));
		//System.out.println(dateUtil.getDateTime("Wed Mar 03 06:51:43 +0000 2010"));
		//System.out.println(dateUtil.toChineseTime("2010-03-03 04:06:57 GMT"));
		
	//	System.out.println(dateUtil.getDateTime("2010-03-03T14:27:19+08:00"));
//		System.out.println(dateUtil.validate("2010-1-2 12:30"));
//		System.out.println(dateUtil.getDateForEN(date1));	
	}
	
	public static Date getRealTime(String timestr) {
		timestr = getDateTime(timestr);
		if(timestr.length()<19) {
			timestr += ":00";
		}
		Date pubtime = new Date(0);
		try {
			pubtime = simpleDateFormat.parse(timestr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return pubtime;
	}
}
